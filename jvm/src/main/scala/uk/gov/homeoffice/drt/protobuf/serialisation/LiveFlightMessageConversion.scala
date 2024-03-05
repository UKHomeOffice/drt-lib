package uk.gov.homeoffice.drt.protobuf.serialisation

import org.slf4j.{Logger, LoggerFactory}
import uk.gov.homeoffice.drt.actor.state.ArrivalsState
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.feeds.FeedSourceStatuses
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage._
import uk.gov.homeoffice.drt.protobuf.messages.ForecastFlightsMessage.{ForecastFlightMessage, ForecastFlightStateSnapshotMessage, ForecastFlightsDiffMessage}
import uk.gov.homeoffice.drt.protobuf.messages.LiveFlightsMessage.{LiveFlightMessage, LiveFlightStateSnapshotMessage, LiveFlightsDiffMessage}
import uk.gov.homeoffice.drt.protobuf.serialisation.FlightMessageConversion.{feedStatusesFromFeedStatusesMessage, feedStatusesToMessage, uniqueArrivalFromMessage, uniqueArrivalToMessage}

import scala.collection.immutable.SortedMap

object LiveFlightMessageConversion {
  val log: Logger = LoggerFactory.getLogger(getClass.toString)

  def arrivalsDiffToMessage(arrivalsDiff: ArrivalsDiff, nowMillis: Long): LiveFlightsDiffMessage = {
    val updateMessages = arrivalsDiff.toUpdate.values.map(f => liveFlightToFlightMessage(LiveArrival(f))).toSeq
    val removalMessages = arrivalsDiff.toRemove.map(a => uniqueArrivalToMessage(a)).toSeq
    LiveFlightsDiffMessage(
      createdAt = Option(nowMillis),
      removals = removalMessages,
      updates = updateMessages
    )
  }

  def arrivalsDiffFromMessage(flightsDiffMessage: LiveFlightsDiffMessage): ArrivalsDiff =
    ArrivalsDiff(
      toUpdate = flightsDiffMessage.updates.map(liveFlightFromMessage),
      toRemove = flightsDiffMessage.removals.map(uniqueArrivalFromMessage)
    )

  def arrivalsStateToSnapshotMessage(state: ArrivalsState): LiveFlightStateSnapshotMessage = {
    val maybeStatusMessages: Option[FeedStatusesMessage] = state.maybeSourceStatuses.flatMap(feedStatuses => feedStatusesToMessage(feedStatuses.feedStatuses))

    LiveFlightStateSnapshotMessage(
      state.arrivals.values.map(f => liveFlightToFlightMessage(LiveArrival(f))).toSeq,
      maybeStatusMessages
    )
  }

  def arrivalsStateFromSnapshotMessage(snapshotMessage: LiveFlightStateSnapshotMessage, feedSource: FeedSource): ArrivalsState = {
    val maybeFeedStatuses = snapshotMessage.statuses.map(s => FeedSourceStatuses(feedSource, feedStatusesFromFeedStatusesMessage(s)))
    val arrivals = SortedMap.empty[UniqueArrival, LiveArrival] ++ snapshotMessage.flightMessages.map(liveFlightFromMessage).map(f => f.unique -> f)
    ArrivalsState(arrivals, feedSource, maybeFeedStatuses)
  }

  def liveFlightToFlightMessage(arrival: LiveArrival): LiveFlightMessage = {
    LiveFlightMessage(
      operator = arrival.operator.map(_.code),
      carrierCode = Option(arrival.carrierCode.code),
      voyageNumber = Option(arrival.voyageNumber.numeric),
      flightCodeSuffix = arrival.maybeFlightCodeSuffix.map(_.suffix),
      status = Option(arrival.status.description),
      scheduled = Option(arrival.scheduled),
      estimated = arrival.estimated,
      touchdown = arrival.actual,
      estimatedChox = arrival.estimatedChox,
      actualChox = arrival.actualChox,
      gate = arrival.gate,
      stand = arrival.stand,
      totalPax = arrival.totalPax,
      transitPax = arrival.transPax,
      maxPax = arrival.maxPax,
      runwayID = arrival.runwayID,
      baggageReclaimId = arrival.baggageReclaimId,
      terminal = Option(arrival.terminal.toString),
      origin = Option(arrival.origin.iata),
      scheduledDeparture = arrival.scheduledDeparture,
      feedSource = Option(arrival.feedSource.name),
    )
  }

  def liveFlightFromMessage(flightMessage: LiveFlightMessage): LiveArrival = LiveArrival(
    operator = flightMessage.operator.map(Operator),
    carrierCode = flightMessage.carrierCode.map(CarrierCode(_)).getOrElse(throw new Exception("Missing carrier code")),
    voyageNumber = flightMessage.voyageNumber.map(VoyageNumber(_)).getOrElse(throw new Exception("Missing voyage number")),
    maybeFlightCodeSuffix = flightMessage.flightCodeSuffix.map(FlightCodeSuffix),
    status = ArrivalStatus(flightMessage.status.getOrElse(throw new Exception("Missing status"))),
    scheduled = flightMessage.scheduled.getOrElse(throw new Exception("Missing scheduled")),
    estimated = flightMessage.estimated,
    actual = flightMessage.touchdown,
    estimatedChox = flightMessage.estimatedChox,
    actualChox = flightMessage.actualChox,
    gate = flightMessage.gate,
    stand = flightMessage.stand,
    totalPax = flightMessage.totalPax,
    transPax = flightMessage.transitPax,
    maxPax = flightMessage.maxPax,
    runwayID = flightMessage.runwayID,
    baggageReclaimId = flightMessage.baggageReclaimId,
    terminal = Terminal(flightMessage.terminal.getOrElse(throw new Exception("Missing terminal"))),
    origin = PortCode(flightMessage.origin.getOrElse(throw new Exception("Missing origin"))),
    scheduledDeparture = flightMessage.scheduledDeparture,
    feedSource = FeedSource(flightMessage.feedSource.getOrElse(throw new Exception("Missing feed source"))).getOrElse(throw new Exception("Missing feed source")),
  )
}
