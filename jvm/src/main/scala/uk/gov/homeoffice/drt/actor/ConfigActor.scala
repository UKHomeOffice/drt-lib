package uk.gov.homeoffice.drt.actor

import org.apache.pekko.actor.ActorRef
import org.apache.pekko.persistence._
import org.apache.pekko.util.Timeout
import org.slf4j.{Logger, LoggerFactory}
import scalapb.GeneratedMessage
import uk.gov.homeoffice.drt.actor.ConfigActor._
import uk.gov.homeoffice.drt.actor.acking.AckingReceiver.StreamCompleted
import uk.gov.homeoffice.drt.actor.commands.Commands.{AddUpdatesSubscriber, GetState}
import uk.gov.homeoffice.drt.actor.commands.TerminalUpdateRequest
import uk.gov.homeoffice.drt.actor.serialisation.{ConfigDeserialiser, ConfigSerialiser, EmptyConfig}
import uk.gov.homeoffice.drt.ports.config.updates.{ConfigUpdate, Configs}
import uk.gov.homeoffice.drt.protobuf.messages.config.Configs.RemoveConfigMessage
import uk.gov.homeoffice.drt.time.MilliDate.MillisSinceEpoch
import uk.gov.homeoffice.drt.time.{LocalDate, SDate, SDateLike}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object ConfigActor {
  sealed trait Command

  case class SetUpdate[A](update: ConfigUpdate[A]) extends Command {
    lazy val firstMinuteAffected: Long = update.maybeOriginalEffectiveFrom match {
      case None => update.effectiveFrom
      case Some(originalEffectiveFrom) =>
        if (update.effectiveFrom < originalEffectiveFrom)
          update.effectiveFrom
        else originalEffectiveFrom
    }
  }

  case class RemoveConfig(effectiveFrom: MillisSinceEpoch) extends Command
}

class ConfigActor[A, B <: Configs[A]](val persistenceId: String,
                                      val now: () => SDateLike,
                                      updateRequests: LocalDate => Iterable[TerminalUpdateRequest],
                                      maxForecastDays: Int,
                                     )
                                     (implicit
                                      emptyProvider: EmptyConfig[A, B],
                                      serialiser: ConfigSerialiser[A, B],
                                      deserialiser: ConfigDeserialiser[A, B],
                                     ) extends RecoveryActorLike with PersistentDrtActor[B] {
  override val log: Logger = LoggerFactory.getLogger(getClass)

  override val maybeSnapshotInterval: Option[Int] = None

  override def processRecoveryMessage: PartialFunction[Any, Unit] = {
    case msg: RemoveConfigMessage =>
      state = state.remove(deserialiser.removeUpdate(msg).effectiveFrom).asInstanceOf[B]

    case msg: GeneratedMessage =>
      state = deserialiser.deserialiseCommand(msg) match {
        case update: SetUpdate[A] => stateUpdate(update.update)
      }
  }

  override def processSnapshotMessage: PartialFunction[Any, Unit] = {
    case msg: GeneratedMessage =>
      state = deserialiser.deserialiseState(msg)
  }

  override def stateToMessage: GeneratedMessage =
    serialiser.updatesWithHistory(state)

  var state: B = emptyProvider.empty

  private var maybeCrunchRequestQueueActor: Option[ActorRef] = None

  implicit val ec: ExecutionContextExecutor = context.dispatcher
  implicit val timeout: Timeout = new Timeout(60.seconds)

  override def initialState: B = emptyProvider.empty

  override def receiveCommand: Receive = {
    case AddUpdatesSubscriber(crunchRequestQueue) =>
      log.info("Received crunch request actor")
      maybeCrunchRequestQueueActor = Option(crunchRequestQueue)

    case update: SetUpdate[A] =>
      state = stateUpdate(update.update)
      persistAndMaybeSnapshot(serialiser.setUpdate(update, now().millisSinceEpoch))
      sendCrunchRequests(SDate(update.firstMinuteAffected).toLocalDate)

    case GetState =>
      sender() ! state

    case remove: RemoveConfig =>
      state = state.remove(remove.effectiveFrom).asInstanceOf[B]
      persistAndMaybeSnapshot(serialiser.removeUpdate(remove, now().millisSinceEpoch))
      sendCrunchRequests(SDate(remove.effectiveFrom).toLocalDate)

    case SaveSnapshotSuccess(md) =>
      log.debug(s"Save snapshot success: $md")

    case SaveSnapshotFailure(md, cause) =>
      log.error(s"Save snapshot failure: $md", cause)

    case StreamCompleted => log.warn("Received shutdown")

    case unexpected => log.error(s"Received unexpected message ${unexpected.getClass}")
  }

  private def sendCrunchRequests(firstDay: LocalDate): Unit =
    maybeCrunchRequestQueueActor.foreach { requestActor =>
      val today = now().toLocalDate
      val firstNonHistoricDate = if (firstDay < today) today else firstDay
      val end = SDate(today).addDays(maxForecastDays).toLocalDate
      val range = firstNonHistoricDate.to(end)(SDate(_))
      range.foreach(ld => updateRequests(ld).foreach(requestActor ! _))
    }

  private def stateUpdate(update: ConfigUpdate[A]): B = state.update(update).asInstanceOf[B]
}
