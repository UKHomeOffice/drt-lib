package uk.gov.homeoffice.drt.actor

import org.apache.pekko.persistence.SnapshotMetadata
import org.slf4j.Logger
import uk.gov.homeoffice.drt.time.{SDate, SDateLike}

trait RecoveryLogging {
  protected val log: Logger

  val prefix = "Recovery"
  def persistenceId: String

  def snapshotOfferLogMessage(md: SnapshotMetadata): String = s"$persistenceId $prefix received SnapshotOffer from ${SDate(md.timestamp).toISOString}, sequence number ${md.sequenceNr}"

  def logSnapshotOffer(md: SnapshotMetadata): Unit = log.debug(snapshotOfferLogMessage(md))

  def logSnapshotOffer(md: SnapshotMetadata,
                       additionalInfo: String): Unit = log.debug(s"${snapshotOfferLogMessage(md)} - $additionalInfo")

  def logRecoveryMessage(message: String): Unit = log.info(s"$prefix - $message")

  def logPointInTimeCompleted(pit: SDateLike): Unit = log.info(s"$prefix completed to point-in-time ${pit.toISOString}")

  def logUnknown(unknown: Any): Unit = log.warn(s"$prefix received unknown message ${unknown.getClass}")

  def logCounters(bytes: Int, messages: Int, bytesThreshold: Int, maybeMessageThreshold: Option[Int]): Unit = {
    val megaBytes = bytes.toDouble / (1024 * 1024)
    val megaBytesThreshold = bytesThreshold.toDouble / (1024 * 1024)
    log.debug(f"$megaBytes%.2fMB persisted in $messages messages since last snapshot. Thresholds: $megaBytesThreshold%.2fMB, $maybeMessageThreshold messages")
  }
}
