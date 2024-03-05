package uk.gov.homeoffice.drt.protobuf.serialisation

import org.slf4j.{Logger, LoggerFactory}
import uk.gov.homeoffice.drt.Nationality
import uk.gov.homeoffice.drt.actor.state.ArrivalsState
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.feeds.{FeedSourceStatuses, FeedStatus, FeedStatusFailure, FeedStatusSuccess, FeedStatuses}
import uk.gov.homeoffice.drt.ports.Queues.Queue
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitSource, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.protobuf.messages.CrunchState._
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage._
import uk.gov.homeoffice.drt.protobuf.messages.ForecastFlightsMessage.{ForecastFlightMessage, ForecastFlightStateSnapshotMessage, ForecastFlightsDiffMessage}
import uk.gov.homeoffice.drt.protobuf.messages.Prediction.{PredictionIntMessage, PredictionLongMessage, PredictionsMessage}
import uk.gov.homeoffice.drt.protobuf.serialisation.FlightMessageConversion.{feedStatusesFromFeedStatusesMessage, feedStatusesToMessage, uniqueArrivalFromMessage, uniqueArrivalToMessage}
import uk.gov.homeoffice.drt.time.SDate

import scala.collection.immutable.SortedMap

object ForecastFlightMessageConversion {
  val log: Logger = LoggerFactory.getLogger(getClass.toString)

  def arrivalsDiffToMessage(arrivalsDiff: ArrivalsDiff, nowMillis: Long): ForecastFlightsDiffMessage = {
    val updateMessages = arrivalsDiff.toUpdate.values.map(f => forecastFlightToFlightMessage(ForecastArrival(f))).toSeq
    val removalMessages = arrivalsDiff.toRemove.map(a => uniqueArrivalToMessage(a)).toSeq
    ForecastFlightsDiffMessage(
      createdAt = Option(nowMillis),
      removals = removalMessages,
      updates = updateMessages
    )
  }

  def arrivalsDiffFromMessage(flightsDiffMessage: ForecastFlightsDiffMessage): ArrivalsDiff =
    ArrivalsDiff(
      toUpdate = flightsDiffMessage.updates.map(forecastFlightFromMessage),
      toRemove = flightsDiffMessage.removals.map(uniqueArrivalFromMessage)
    )

  def arrivalsStateToSnapshotMessage(state: ArrivalsState): ForecastFlightStateSnapshotMessage = {
    val maybeStatusMessages: Option[FeedStatusesMessage] = state.maybeSourceStatuses.flatMap(feedStatuses => feedStatusesToMessage(feedStatuses.feedStatuses))

    ForecastFlightStateSnapshotMessage(
      state.arrivals.values.map(f => forecastFlightToFlightMessage(ForecastArrival(f))).toSeq,
      maybeStatusMessages
    )
  }

  def arrivalsStateFromSnapshotMessage(snapshotMessage: ForecastFlightStateSnapshotMessage, feedSource: FeedSource): ArrivalsState = {
    val maybeFeedStatuses = snapshotMessage.statuses.map(s => FeedSourceStatuses(feedSource, feedStatusesFromFeedStatusesMessage(s)))
    val arrivals = SortedMap.empty[UniqueArrival, ForecastArrival] ++ snapshotMessage.flightMessages.map(forecastFlightFromMessage).map(f => f.unique -> f)
    ArrivalsState(arrivals, feedSource, maybeFeedStatuses)
  }

  def forecastFlightToFlightMessage(arrival: ForecastArrival): ForecastFlightMessage = {
    ForecastFlightMessage(
      carrierCode = Option(arrival.carrierCode.code),
      voyageNumber = Option(arrival.voyageNumber.numeric),
      flightCodeSuffix = arrival.maybeFlightCodeSuffix.map(_.suffix),
      origin = Option(arrival.origin.iata),
      terminal = Option(arrival.terminal.toString),
      scheduled = Option(arrival.scheduled),
      totalPax = arrival.totalPax,
      transitPax = arrival.transPax,
      maxPax = arrival.maxPax
    )
  }

  def forecastFlightFromMessage(flightMessage: ForecastFlightMessage): ForecastArrival = ForecastArrival(
    carrierCode = flightMessage.carrierCode.map(CarrierCode(_)).getOrElse(throw new Exception("Missing carrier code")),
    voyageNumber = flightMessage.voyageNumber.map(VoyageNumber(_)).getOrElse(throw new Exception("Missing voyage number")),
    maybeFlightCodeSuffix = flightMessage.flightCodeSuffix.map(FlightCodeSuffix),
    origin = PortCode(flightMessage.origin.getOrElse(throw new Exception("Missing origin"))),
    terminal = Terminal(flightMessage.terminal.getOrElse(throw new Exception("Missing terminal"))),
    scheduled = flightMessage.scheduled.getOrElse(throw new Exception("Missing scheduled")),
    totalPax = flightMessage.totalPax,
    transPax = flightMessage.transitPax,
    maxPax = flightMessage.maxPax,
  )
}
