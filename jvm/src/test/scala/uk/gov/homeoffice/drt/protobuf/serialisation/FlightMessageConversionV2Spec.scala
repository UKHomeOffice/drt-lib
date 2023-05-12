package uk.gov.homeoffice.drt.protobuf.serialisation

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.prediction.arrival.OffScheduleModelAndFeatures
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage.FlightMessage
import uk.gov.homeoffice.drt.protobuf.messages.Prediction.{PredictionIntMessage, PredictionsMessage}
import uk.gov.homeoffice.drt.time.SDate

class FlightMessageConversionV2Spec extends Specification {

  //FlightMessage version contain apiPaxOld, actPaxOld and transPaxOld
  // but totalPax does not exist
  /** *
   * FlightMessage( ..... ,
   * apiPaxOLD = None,
   * actPaxOLD = 95,
   * transPaxOld = 10
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
    "and there is actPaxOld and/or transPaxOld present in FlightMessage " +
    "then arrival should have actual and transit passengers for ForecastFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource, AclFeedSource),
      PassengerSources = Map(ForecastFeedSource -> Passengers(Option(95), None),
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString, AclFeedSource.toString),
      actPaxOLD = apiFlight.PassengerSources.get(ForecastFeedSource).flatMap(_.actual),
      tranPaxOLD = apiFlight.PassengerSources.get(ForecastFeedSource).flatMap(_.transit)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flight message is deserialize " +
    "and there is actPaxOld , transPaxOld and apiPaxOld present in FlightMessage " +
    "then arrival should have actual & transit for ForecastFeedSource and actual & transit for ApiFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource, ApiFeedSource),
      PassengerSources = Map(ApiFeedSource -> Passengers(Option(100), Some(10)), ForecastFeedSource -> Passengers(Some(95), Some(10))),
    )

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString, ApiFeedSource.toString),
      actPaxOLD = Option(95),
      tranPaxOLD = Option(10),
      apiPaxOLD = Option(100)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flight message is deserialize " +
    "and there is actPaxOld , transPaxOld and apiPaxOld present in FlightMessage " +
    "then arrival should have actual & transit for LiveFeedSource and actual & no transit for ApiFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(LiveFeedSource, AclFeedSource),
      PassengerSources = Map(ApiFeedSource -> Passengers(Option(100), None), LiveFeedSource -> Passengers(Some(95), Some(10))),
    )

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(LiveFeedSource.toString, AclFeedSource.toString),
      actPaxOLD = Option(95),
      tranPaxOLD = Option(10),
      apiPaxOLD = Option(100)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flight message is deserialize " +
    "and if there is actPaxOld , transPaxOld and apiPaxOld present in flightMessage " +
    "then arrival should have actual & transit for LiveFeedSource and actual & no transit for ApiFeedSource in passengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource, AclFeedSource, LiveFeedSource),
      PassengerSources = Map(ApiFeedSource -> Passengers(Option(100), None), LiveFeedSource -> Passengers(Option(95), Option(10))
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString, AclFeedSource.toString, LiveFeedSource.toString),
      actPaxOLD = apiFlight.PassengerSources.get(LiveFeedSource).flatMap(_.actual),
      tranPaxOLD = apiFlight.PassengerSources.get(LiveFeedSource).flatMap(_.transit),
      apiPaxOLD = Option(100)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }


  "when flight message is deserialize " +
    "and if there is actPaxOld and transPaxOld present " +
    "then arrival should have actual and transit for AclFeedSource in passengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(AclFeedSource),
      PassengerSources = Map(AclFeedSource -> Passengers(Option(95), None),
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(AclFeedSource.toString),
      actPaxOLD = apiFlight.PassengerSources.get(AclFeedSource).flatMap(_.actual),
      tranPaxOLD = Option(10)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }


}
