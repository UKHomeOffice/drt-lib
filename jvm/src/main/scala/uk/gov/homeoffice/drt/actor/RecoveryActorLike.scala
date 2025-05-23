package uk.gov.homeoffice.drt.actor

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.persistence._
import org.slf4j.Logger
import scalapb.GeneratedMessage
import uk.gov.homeoffice.drt.time.MilliDate.MillisSinceEpoch
import uk.gov.homeoffice.drt.time.SDate

import scala.util.{Failure, Try}

object Sizes {
  val oneMegaByte: Int = 1024 * 1024
}

trait RecoveryActorLike extends PersistentActor with RecoveryLogging {
  protected val log: Logger

  val recoveryStartMillis: MillisSinceEpoch = SDate.now().millisSinceEpoch
  var messageRecoveryStartMillis: Option[MillisSinceEpoch] = None
  val maybePointInTime: Option[Long] = None
  val snapshotBytesThreshold: Int = Sizes.oneMegaByte
  val maybeSnapshotInterval: Option[Int]
  var messagesPersistedSinceSnapshotCounter = 0
  var bytesSinceSnapshotCounter = 0
  var maybeAckAfterSnapshot: List[(ActorRef, Any)] = List()

  override def recovery: Recovery = maybePointInTime match {
    case None =>
      Recovery(SnapshotSelectionCriteria(Long.MaxValue, maxTimestamp = Long.MaxValue, 0L, 0L))
    case Some(pointInTime) =>
      val replayMax = maybeSnapshotInterval.map(_.toLong).getOrElse(Long.MaxValue)
      val criteria = SnapshotSelectionCriteria(maxTimestamp = pointInTime)
      Recovery(fromSnapshot = criteria, replayMax = replayMax)
  }

  def ackIfRequired(): Unit = {
    maybeAckAfterSnapshot.foreach {
      case (replyTo, msg) => replyTo ! msg
    }
    maybeAckAfterSnapshot = List()
  }

  def unknownMessage: PartialFunction[Any, Unit] = {
    case unknown => logUnknown(unknown)
  }

  def processRecoveryMessage: PartialFunction[GeneratedMessage, Unit]

  def processSnapshotMessage: PartialFunction[Any, Unit]

  def playRecoveryMessage: PartialFunction[GeneratedMessage, Unit] = processRecoveryMessage orElse unknownMessage

  def playSnapshotMessage: PartialFunction[Any, Unit] = processSnapshotMessage orElse unknownMessage

  def postRecoveryComplete(): Unit = {}

  def postSaveSnapshot(): Unit = {}

  def stateToMessage: GeneratedMessage

  def persistAndMaybeSnapshot(message: GeneratedMessage): Unit = persistAndMaybeSnapshotWithAck(message, List())

  def persistAndMaybeSnapshotWithAck(messageToPersist: GeneratedMessage, acks: List[(ActorRef, Any)]): Unit = {
    persist(messageToPersist) { message =>
      val messageBytes = message.serializedSize
      log.debug(s"Persisting $messageBytes bytes of ${message.getClass}")

      context.system.eventStream.publish(message)

      bytesSinceSnapshotCounter += messageBytes
      messagesPersistedSinceSnapshotCounter += 1
      logCounters(bytesSinceSnapshotCounter, messagesPersistedSinceSnapshotCounter, snapshotBytesThreshold, maybeSnapshotInterval)

      if (shouldTakeSnapshot) {
        takeSnapshot(stateToMessage)
        maybeAckAfterSnapshot = acks
      } else {
        acks.foreach {
          case (replyTo, ackMsg) =>
            replyTo ! ackMsg
        }
      }
    }
  }

  def takeSnapshot(stateToSnapshot: GeneratedMessage): Unit = {
    log.debug(s"Snapshotting ${stateToSnapshot.serializedSize} bytes of ${stateToSnapshot.getClass}. Resetting counters to zero")
    saveSnapshot(stateToSnapshot)

    bytesSinceSnapshotCounter = 0
    messagesPersistedSinceSnapshotCounter = 0
    postSaveSnapshot()
  }

  def shouldTakeSnapshot: Boolean = {
    val shouldSnapshotByCount = maybeSnapshotInterval.isDefined && messagesPersistedSinceSnapshotCounter >= maybeSnapshotInterval.get
    val shouldSnapshotByBytes = bytesSinceSnapshotCounter > snapshotBytesThreshold

    if (shouldSnapshotByCount) log.debug(f"Snapshot interval reached (${maybeSnapshotInterval.getOrElse(0)})")
    if (shouldSnapshotByBytes) log.debug(f"Snapshot bytes threshold reached (${snapshotBytesThreshold.toDouble / Sizes.oneMegaByte}%.2fMB)")

    shouldSnapshotByBytes || shouldSnapshotByCount
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(md, ss) =>
      logSnapshotOffer(md)
      playSnapshotMessage(ss)

    case RecoveryCompleted =>
      logRecoveryTime()
      postRecoveryComplete()

    case event: GeneratedMessage =>
      Try {
        bytesSinceSnapshotCounter += event.serializedSize
        messagesPersistedSinceSnapshotCounter += 1
        playRecoveryMessage(event)
      } match {
        case Failure(exception) =>
          log.error(s"Failed to replay recovery message $event", exception)
        case _ =>
      }
  }

  private def logRecoveryTime(): Unit = {
    val tookMs: MillisSinceEpoch = SDate.now().millisSinceEpoch - recoveryStartMillis
    val message = s"Recovery complete. $messagesPersistedSinceSnapshotCounter messages replayed. Took ${tookMs}ms."

    if (250L <= tookMs && tookMs < 5000L)
      log.warn(s"$message (slow)")
    else if (tookMs >= 5000L)
      log.error(s"$message (very slow)")
  }
}

