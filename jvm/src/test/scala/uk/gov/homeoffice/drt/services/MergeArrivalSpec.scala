package uk.gov.homeoffice.drt.services

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.arrivals.{Arrival, ArrivalStatus, CarrierCode, FlightCodeSuffix, ForecastArrival, Operator, Passengers, Predictions, VoyageNumber}
import uk.gov.homeoffice.drt.ports.{FeedSource, PortCode}
import uk.gov.homeoffice.drt.ports.Terminals.{T3, Terminal}
import uk.gov.homeoffice.drt.time.SDate


class MergeArrivalSpec extends Specification {
  "MergeArrival" should {
    "Take a forecast arrival and produce an arrival" in {
      val forecastArrival = ForecastArrival(
        carrierCode = CarrierCode("BA"),
        flightNumber = VoyageNumber(58),
        maybeFlightCodeSuffix = Option(FlightCodeSuffix("A")),
        origin = PortCode("CPT"),
        terminal = T3,
        scheduled = SDate("2024-05-01T10:30").millisSinceEpoch,
        totalPax = Option(200),
        transPax = Option(10),
        maxPax = Option(250)
      )
      val arrival = Arrival(
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
        MaxPax = Option(250),
        RunwayID = None,
        BaggageReclaimId = None,
        AirportID = PortCode("CPT"),
        Terminal = T3,
        Origin = PortCode("CPT"),
        Scheduled = SDate("2024-05-01T10:30").millisSinceEpoch,
        PcpTime = None,
        FeedSources = Set(ForecastFeedSource),
        CarrierScheduled = None,
        ScheduledDeparture = None,
        RedListPax = None,
        PassengerSources = Map(ForecastFeedSource -> Passengers(Option(200), Option(10)))
      )

    }
  }
}
