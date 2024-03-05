package uk.gov.homeoffice.drt.protobuf.serialisation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports.{LiveFeedSource, PortCode}

class ForecastFlightMessageConversionTest extends AnyWordSpec with Matchers {
  "ForecastFlightMessageConversion" should {
    "serialise and deserialise a ForecastArrival without loss" in {
      val forecastArrival = ForecastArrival(
        carrierCode = CarrierCode("BA"),
        voyageNumber = VoyageNumber(1),
        maybeFlightCodeSuffix = Option(FlightCodeSuffix("A")),
        totalPax = Option(100),
        transPax = Option(50),
        maxPax = Option(200),
        terminal = Terminal("T1"),
        origin = PortCode("JFK"),
        scheduled = 5L,
      )

      val msg = ForecastFlightMessageConversion.forecastFlightToFlightMessage(forecastArrival)
      val deserialised = ForecastFlightMessageConversion.forecastFlightFromMessage(msg)

      deserialised should ===(forecastArrival)
    }
  }
}
