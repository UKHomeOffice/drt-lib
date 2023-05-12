package uk.gov.homeoffice.drt.protobuf.serialisation

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.prediction.arrival.OffScheduleModelAndFeatures
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage.{FlightMessage, TotalPaxSourceMessage}
import uk.gov.homeoffice.drt.protobuf.messages.Prediction.{PredictionIntMessage, PredictionsMessage}
import uk.gov.homeoffice.drt.time.SDate

class FlightMessageConversionV3Spec extends Specification {

  //FlightMessage version contain apiPaxOld, actPaxOld and transPaxOld
  //but totalPax has TotalPaxSourceMessage with paxOld and feedSource

  /** *
   * FlightMessage( ..... ,
   * apiPaxOLD = None,
   * actPaxOLD = 95,
   * transPaxOld = 10,
   * totalPax = [TotalPaxSourceMessage(paxOLD = 95, FeedSource = LiveFeedSource, passenger = None) ,
   * TotalPaxSourceMessage(paxOLD= 105 , FeedSource = ACLFeedSource, passenger = None)]
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
    FeedSources = Set(LiveFeedSource, AclFeedSource, ForecastFeedSource, LiveBaseFeedSource, ApiFeedSource),
    CarrierScheduled = Option(7L),
    ScheduledDeparture = Option(8L),
    RedListPax = Option(26),
    PassengerSources = Map(
      HistoricApiFeedSource -> Passengers(Option(95), None),
      ForecastFeedSource -> Passengers(Option(0), None),
      LiveFeedSource -> Passengers(Option(95), None),
      ApiFeedSource -> Passengers(Option(95), None),
      AclFeedSource -> Passengers(Option(95), None),
      LiveBaseFeedSource -> Passengers(Option(95), None),
      ScenarioSimulationSource -> Passengers(Option(95), None),
    )
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
    "and totalPax is also present with paxOld in FlightMessage" +
    "then arrival should have actual & transit passengers for ForecastFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource),
      PassengerSources = Map(ForecastFeedSource -> Passengers(Option(95), Option(10)),
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString),
      tranPaxOLD = Option(10),
      totalPax = Seq(TotalPaxSourceMessage(feedSource = Option(ForecastFeedSource.toString), paxOLD = Option(95))))


    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flight message is deserialize " +
    "and there is actPaxOld and/or transPaxOld present in FlightMessage " +
    "and totalPax is also present with paxOld in FlightMessage" +
    "then arrival should have actual & transit passengers for ForecastFeedSource " +
    "and actual & transit passengers for LiveFeedSource in PassengerSources" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource, LiveFeedSource),
      PassengerSources = Map(ForecastFeedSource -> Passengers(Option(95), Option(10)),
        LiveFeedSource -> Passengers(Option(90), Option(10))),
    )

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString, LiveFeedSource.toString),
      tranPaxOLD = Option(10),
      totalPax = Seq(TotalPaxSourceMessage(feedSource = Option(ForecastFeedSource.toString), paxOLD = Option(95)),
        TotalPaxSourceMessage(feedSource = Option(LiveFeedSource.toString), paxOLD = Option(90))))

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

}
