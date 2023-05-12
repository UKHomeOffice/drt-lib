package uk.gov.homeoffice.drt.protobuf.serialisation

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.Nationality
import uk.gov.homeoffice.drt.arrivals.SplitStyle.PaxNumbers
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.SplitSources
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.SplitSources.Historical
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.prediction.arrival.OffScheduleModelAndFeatures
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage.{FlightMessage, PassengersMessage, TotalPaxSourceMessage}
import uk.gov.homeoffice.drt.protobuf.messages.Prediction.{PredictionIntMessage, PredictionsMessage}
import uk.gov.homeoffice.drt.time.SDate

class FlightMessageConversionSpec extends Specification {

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

  "Given an Arrival with no suffix" >> {
    "When I convert it to a protobuf message and then back to an Arrival" >> {
      val arrivalMessage = FlightMessageConversion.apiFlightToFlightMessage(arrival)
      val restoredArrival = FlightMessageConversion.flightMessageToApiFlight(arrivalMessage)
      "Then the converted Arrival should match the original" >> {
        restoredArrival === arrival
      }
    }
  }

  "Given an Arrival with a suffix" >> {
    val arrivalWithSuffix = arrival.copy(FlightCodeSuffix = Option(FlightCodeSuffix("P")))
    "When I convert it to a protobuf message and then back to an Arrival" >> {
      val arrivalMessage: FlightMessage = FlightMessageConversion.apiFlightToFlightMessage(arrivalWithSuffix)
      val restoredArrival = FlightMessageConversion.flightMessageToApiFlight(arrivalMessage)
      "Then the converted Arrival should match the original" >> {
        restoredArrival === arrivalWithSuffix
      }
    }
  }

  "Given an arrival with 0 Passengers" >> {
    val arrivalWith0Pax = arrival.copy(MaxPax = Option(0))
    "When I convert it to a protobuf message and then back to an Arrival" >> {
      val arrivalMessage = FlightMessageConversion.apiFlightToFlightMessage(arrivalWith0Pax)
      val restoredArrival = FlightMessageConversion.flightMessageToApiFlight(arrivalMessage)
      "Then the converted Arrival should match the original" >> {
        restoredArrival === arrivalWith0Pax
      }
    }
  }

  "Given a flight with splits containing API Splits" >> {
    val paxTypeAndQueueCount = ApiPaxTypeAndQueueCount(
      PaxTypes.EeaMachineReadable,
      Queues.EeaDesk,
      10,
      Option(Map(
        Nationality("GBR") -> 8,
        Nationality("ITA") -> 2
      )),
      Option(Map(
        PaxAge(5) -> 5,
        PaxAge(32) -> 5
      ))
    )

    val paxTypeAndQueueCountWithoutApi = ApiPaxTypeAndQueueCount(
      PaxTypes.EeaMachineReadable,
      Queues.EeaDesk,
      10,
      None,
      None
    )

    val splits = Set(
      Splits(
        Set(
          paxTypeAndQueueCount
        ),
        SplitSources.ApiSplitsWithHistoricalEGateAndFTPercentages,
        Option(EventType("DC")
        )
      )
    )
    val splitsWithoutApi = Set(
      Splits(
        Set(
          paxTypeAndQueueCountWithoutApi
        ),
        SplitSources.ApiSplitsWithHistoricalEGateAndFTPercentages,
        Option(EventType("DC")
        )
      )
    )

    val fws = ApiFlightWithSplits(
      arrival,
      splits
    )
    "When I convert it to a protobuf message and then back to an Arrival" >> {
      val fwsMessage = FlightMessageConversion.flightWithSplitsToMessage(fws)
      val restoredFWS = FlightMessageConversion.flightWithSplitsFromMessage(fwsMessage)
      val expectedWithoutApiData = fws.copy(splits = splitsWithoutApi)
      "Then the converted Arrival should match the original without API Data" >> {
        restoredFWS === expectedWithoutApiData
      }
    }
  }

  "Given a FlightsWithSplitsDiff" >> {
    val diff = FlightsWithSplitsDiff(
      List(ApiFlightWithSplits(arrival, Set(Splits(
        Set(
          ApiPaxTypeAndQueueCount(PaxTypes.EeaBelowEGateAge, Queues.EeaDesk, 1, None, None),
          ApiPaxTypeAndQueueCount(PaxTypes.EeaBelowEGateAge, Queues.EeaDesk, 1, None, None),
          ApiPaxTypeAndQueueCount(PaxTypes.EeaMachineReadable, Queues.EeaDesk, 3, None, None),
          ApiPaxTypeAndQueueCount(PaxTypes.EeaMachineReadable, Queues.EGate, 1, None, None)
        ),
        Historical,
        None,
        PaxNumbers
      )))), List(arrival.unique))
    "When I convert it to a protobuf message and then back to an FlightsWithSplitsDiff" >> {
      val diffMessage = FlightMessageConversion.flightWithSplitsDiffToMessage(diff)
      val restoredDiff = FlightMessageConversion.flightWithSplitsDiffFromMessage(diffMessage)
      "Then the converted FlightsWithSplitsDiff should match the original" >> {
        restoredDiff === diff
      }
    }
  }

  "when flight message is deserialize and if there is apiPax present then arrival should have apiPax in totalPax" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ApiFeedSource),
      PassengerSources = Map(ApiFeedSource -> Passengers(Option(95), None),
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ApiFeedSource.toString),
      apiPaxOLD = apiFlight.PassengerSources.get(ApiFeedSource).flatMap(_.getPcpPax)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flight message is deserialize and if there is actPax and/or transPax present then arrival should have actPax and transPax with LiveFeedSource in totalPax" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(LiveFeedSource),
      PassengerSources = Map(LiveFeedSource -> Passengers(Option(95), Option(10)),
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(LiveFeedSource.toString),
      actPaxOLD = apiFlight.PassengerSources.get(LiveFeedSource).flatMap(_.actual),
      tranPaxOLD = apiFlight.PassengerSources.get(LiveFeedSource).flatMap(_.transit)
    )

    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flight message is deserialize and if there is actPax and/or transPax present then arrival should have actPax and transPax with ForecastFeedSource in totalPax" >> {
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

  "when flight message is deserialize and if there is actPax and/or transPax present then arrival should have actPax , transPax with ForecastFeedSource and apiPax with ApiFeedSource in totalPax" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource, ApiFeedSource),
      PassengerSources = Map(ApiFeedSource -> Passengers(Option(100), Some(10)), ForecastFeedSource -> Passengers(Some(95), None)),
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

  "when flight message is deserialize and if there is actPax , transPax and apiPax present then arrival should have actPax , transPax with LiveFeedSource and apiPax with ApiFeedSource in totalPax" >> {
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

  "when flight message is deserialize and if there is actPax and/or transPax present then arrival should have actPax , transPax with LiveFeedSource and apiPax with ApiFeedSource in totalPax" >> {
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


  "when flight message is deserialize and if there is actPax and/or transPax present then arrival should have actPax and transPax with AclFeedSource in totalPax" >> {
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

  "when arrival object has PassengerSources then serialise and deserialize should have actPax and transPax with LiveFeedSource in totalPax" >> {
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

  "when arrival object has PassengerSources then serialise and deserialize should have actPax and transPax with AclFeedSource in totalPax" >> {
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

  "when arrival object has PassengerSources then serialise and deserialize should have actPax and transPax with ForecastFeedSource in totalPax" >> {
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


  "when flightMessage totalPax has old TotalPaxSourceMessage with paxOld then arrival object after deserialize should have actual  and transit with ForecastFeedSource in totalPax" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource),
      PassengerSources = Map(ForecastFeedSource -> Passengers(Option(95), None),
      ))

    val flightMessage = getFlightMessageWithoutPax(apiFlight).copy(
      feedSources = Seq(ForecastFeedSource.toString),
      tranPaxOLD = Option(10),
      totalPax = Seq(TotalPaxSourceMessage(feedSource = Option(ForecastFeedSource.toString), paxOLD = Option(95))))


    val arrivalResult = FlightMessageConversion.flightMessageToApiFlight(flightMessage)

    arrivalResult === apiFlight
  }

  "when flightMessage totalPax has TotalPaxSourceMessage with paxOld then arrival object after deserialize should have actual and transit with ForecastFeedSource and LiveFeedSource in totalPax" >> {
    val apiFlight = arrival.copy(
      FeedSources = Set(ForecastFeedSource, LiveFeedSource),
      PassengerSources = Map(ForecastFeedSource -> Passengers(Option(95), None),
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
