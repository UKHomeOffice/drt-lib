package uk.gov.homeoffice.drt.services

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.Terminals.T3
import uk.gov.homeoffice.drt.ports.{ForecastFeedSource, LiveBaseFeedSource, LiveFeedSource, PortCode}
import uk.gov.homeoffice.drt.time.SDate


class MergeArrivalSpec extends Specification {
  "MergeArrival" should {
    "merge a forecast arrival with a live arrival" in {
      val forecastArrival = ForecastArrival(
        carrierCode = CarrierCode("BA"),
        voyageNumber = VoyageNumber(58),
        maybeFlightCodeSuffix = Option(FlightCodeSuffix("A")),
        origin = PortCode("CPT"),
        terminal = T3,
        scheduled = SDate("2024-05-01T10:30").millisSinceEpoch,
        totalPax = Option(200),
        transPax = Option(10),
        maxPax = Option(250)
      )
      val liveArrival = LiveArrival(
        operator = None,
        carrierCode = CarrierCode("BA"),
        voyageNumber = VoyageNumber(58),
        maybeFlightCodeSuffix = Option(FlightCodeSuffix("A")),
        status = ArrivalStatus("Scheduled"),
        estimated = None,
        actual = None,
        estimatedChox = None,
        actualChox = None,
        gate = None,
        stand = None,
        maxPax = Option(225),
        runwayID = None,
        baggageReclaimId = None,
        terminal = T3,
        origin = PortCode("CPT"),
        scheduled = SDate("2024-05-01T10:30").millisSinceEpoch,
        feedSource = LiveBaseFeedSource,
        totalPax = Option(195),
        transPax = Option(15),
        scheduledDeparture = Option(2L),
      )
      ArrivalMerger.merge(Seq(forecastArrival, liveArrival)) === liveArrival.toMergedArrival.copy(
        FeedSources = forecastArrival.FeedSources ++ liveArrival.FeedSources,
        PassengerSources = forecastArrival.PassengerSources ++ liveArrival.PassengerSources
      )
    }
    "merge the relevant parts of a merged arrival with a forecast arrival" in {
      val forecastArrival = ForecastArrival(
        carrierCode = CarrierCode("BA"),
        voyageNumber = VoyageNumber(58),
        maybeFlightCodeSuffix = Option(FlightCodeSuffix("A")),
        origin = PortCode("CPT"),
        terminal = T3,
        scheduled = SDate("2024-05-01T10:30").millisSinceEpoch,
        totalPax = Option(200),
        transPax = Option(10),
        maxPax = Option(250)
      )
      val mergedArrival = MergedArrival(
        Operator = None,
        CarrierCode = CarrierCode("BA"),
        VoyageNumber = VoyageNumber(58),
        FlightCodeSuffix = Option(FlightCodeSuffix("A")),
        Status = ArrivalStatus("Scheduled"),
        Estimated = None,
        Actual = None,
        Predictions = Predictions(0L, Map()),
        EstimatedChox = None,
        ActualChox = None,
        Gate = None,
        Stand = None,
        MaxPax = Option(225),
        RunwayID = None,
        BaggageReclaimId = None,
        Terminal = T3,
        Origin = PortCode("CPT"),
        Scheduled = SDate("2024-05-01T10:30").millisSinceEpoch,
        PcpTime = None,
        FeedSources = Set(ForecastFeedSource),
        CarrierScheduled = None,
        ScheduledDeparture = None,
        RedListPax = None,
        PassengerSources = Map(LiveFeedSource -> Passengers(Option(195), Option(15)))
      )
      ArrivalMerger.merge(Seq(forecastArrival, mergedArrival)) === mergedArrival.copy(
        PassengerSources = forecastArrival.PassengerSources ++ mergedArrival.PassengerSources
      )
    }
  }
}
