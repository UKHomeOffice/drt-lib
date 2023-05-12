package uk.gov.homeoffice.drt.protobuf.serialisation

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.prediction.arrival.OffScheduleModelAndFeatures
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage.{FlightMessage, PassengersMessage, TotalPaxSourceMessage}
import uk.gov.homeoffice.drt.protobuf.messages.Prediction.{PredictionIntMessage, PredictionsMessage}
import uk.gov.homeoffice.drt.time.SDate

class FlightMessageConversionV4Spec extends Specification {

  //FlightMessage version does not contain apiPaxOld, actPaxOld and transPaxOld values
  //but totalPax has TotalPaxSourceMessage with PassengersMessage(actual & transit)

  /** *
   * FlightMessage( ..... ,
   * totalPax = [TotalPaxSourceMessage(FeedSource = LiveFeedSource, PassengersMessage(actual = 95, transit = 10) ,
   * TotalPaxSourceMessage(FeedSource = ACLFeedSource, PassengersMessage(actual = 95, transit = 10))]
   * )
   */

  val arrival: Arrival = Arrival(
    Operator = Option(Operator("British Airways")),
    CarrierCode = CarrierCode("BA"),
    VoyageNumber = VoyageNumber(1),
    FlightCodeSuffix = Option(FlightCodeSuffix("G")),
    Status = ArrivalStatus("Delayed"),
    Estimated = Option(1L),
    Predictions = Predictions(SDate.now().millisSinceEpoch, Map(OffScheduleModelAndFeatures.targetName -> 5)),
    Actual = Option(2L),
    EstimatedChox = Option(3L),
    ActualChox = Option(4L),
    Gate = Option("A"),
    Stand = Option("A1"),
    MaxPax = Option(101),
    RunwayID = Option("1"),
    BaggageReclaimId = Option("abc"),
    AirportID = PortCode("LHR"),
    Terminal = T1,
    Origin = PortCode("CDG"),
    Scheduled = 5L,
    PcpTime = Option(6L),
    FeedSources = Set.empty,
    CarrierScheduled = Option(7L),
    ScheduledDeparture = Option(8L),
    RedListPax = Option(26),
    PassengerSources = Map.empty
  )

  def getFlightMessageWithoutPax(apiFlight: Arrival): FlightMessage = {
    FlightMessage(
      operator = apiFlight.Operator.map(_.code),
      gate = apiFlight.Gate,
      stand = apiFlight.Stand,
      status = Option(apiFlight.Status.description),
      maxPax = apiFlight.MaxPax,
      runwayID = apiFlight.RunwayID,
      baggageReclaimId = apiFlight.BaggageReclaimId,
      airportID = Option(apiFlight.AirportID.iata),
      terminal = Option(apiFlight.Terminal.toString),
      iCAO = Option(apiFlight.flightCodeString),
      iATA = Option(apiFlight.flightCodeString),
      origin = Option(apiFlight.Origin.toString),
      pcpTime = apiFlight.PcpTime,
      feedSources = Seq.empty,
      scheduled = Option(apiFlight.Scheduled),
      estimated = apiFlight.Estimated,
      predictions = Option(PredictionsMessage(Option(apiFlight.Predictions.lastChecked),
        apiFlight.Predictions.predictions.map { case (k, v) => PredictionIntMessage(Option(k), Option(v)) }.toSeq)),
      touchdown = apiFlight.Actual,
      estimatedChox = apiFlight.EstimatedChox,
      actualChox = apiFlight.ActualChox,
      carrierScheduled = apiFlight.CarrierScheduled,
      redListPax = apiFlight.RedListPax,
      scheduledDeparture = apiFlight.ScheduledDeparture,
      totalPax = Seq.empty,
      apiPaxOLD = None,
      actPaxOLD = None,
      tranPaxOLD = None
    )
  }

  "when flight message is deserialize " +
    "and totalPax is present with feedSource and passengersMessage in FlightMessage" +
    "then arrival should have actual & transit passengers for LiveFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(LiveFeedSource),
      PassengerSources = Map(LiveFeedSource -> Passengers(Option(95), Option(10)),
      ))

    val flightMessage = FlightMessageConversion.apiFlightToFlightMessage(apiFlight)

    val flightMessageExpected = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(LiveFeedSource.toString),
      totalPax = Seq(TotalPaxSourceMessage(feedSource = Option(LiveFeedSource.toString), passengers = Option(PassengersMessage(Option(95), Option(10)))))
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    flightMessageExpected === flightMessage
    arrivalResult === apiFlight

  }

  "when flight message is deserialize " +
    "and totalPax is present with feedSource and passengersMessage in FlightMessage" +
    "then arrival should have actual & transit passengers for AclFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(AclFeedSource),
      PassengerSources = Map(AclFeedSource -> Passengers(Option(95), None),
      ))

    val flightMessageExpected = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(AclFeedSource.toString),
      totalPax = Seq(TotalPaxSourceMessage(feedSource = Option(AclFeedSource.toString), passengers = Option(PassengersMessage(Option(95), None))))
    )

    val flightMessage = FlightMessageConversion.apiFlightToFlightMessage(apiFlight)

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    flightMessageExpected === flightMessage
    arrivalResult === apiFlight

  }

  "when flight message is deserialize " +
    "and totalPax is present with feedSource and passengersMessage in FlightMessage" +
    "then arrival should have actual & transit passengers for ForecastFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource),
      PassengerSources = Map(ForecastFeedSource -> Passengers(Option(95), None),
      ))

    val flightMessageExpected = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString),
      totalPax = Seq(TotalPaxSourceMessage(feedSource = Option(ForecastFeedSource.toString), passengers = Option(PassengersMessage(Option(95), None))))

    )
    val flightMessage = FlightMessageConversion.apiFlightToFlightMessage(apiFlight)

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    flightMessageExpected === flightMessage
    arrivalResult === apiFlight
  }

}
