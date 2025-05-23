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
import uk.gov.homeoffice.drt.protobuf.messages.FlightsMessage.{FlightMessage, FlightsDiffMessage}
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
    PreviousPort = Option(PortCode("JFK")),
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
      )))))
    "When I convert it to a protobuf message and then back to an FlightsWithSplitsDiff" >> {
      val diffMessage = FlightMessageConversion.flightWithSplitsDiffToMessage(diff, 100L)
      val restoredDiff = FlightMessageConversion.flightWithSplitsDiffFromMessage(diffMessage)
      "Then the converted FlightsWithSplitsDiff should match the original" >> {
        restoredDiff === diff
      }
    }
  }

  "Given a flight message with a zero voyage number and one with a non-zero voyage number" >> {
    "When I ask for an arrivals diff I should only see the non-zero flight" >> {
      val validArrival = arrival.copy(VoyageNumber = VoyageNumber(0))
      val invalidArrival = arrival.copy(VoyageNumber = VoyageNumber(1))
      val flightMessage1 = FlightMessageConversion.apiFlightToFlightMessage(validArrival)
      val flightMessage2 = FlightMessageConversion.apiFlightToFlightMessage(invalidArrival)
      val diff = FlightMessageConversion.arrivalsDiffFromMessage(FlightsDiffMessage(Option(1L), Seq(), Seq(flightMessage1, flightMessage2)))
      diff.toUpdate.keySet === Set(invalidArrival.unique)
    }
  }

}
