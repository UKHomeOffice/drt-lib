package uk.gov.homeoffice.drt.protobuf.serialisation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports.{LiveFeedSource, PortCode}

class LiveFlightMessageConversionTest extends AnyWordSpec with Matchers {
  "LiveFlightMessageConversion" should {
    "serialise and deserialise a LiveArrival without loss" in {
      val liveArrival = LiveArrival(
        operator = Option(Operator("BA")),
        carrierCode = CarrierCode("BA"),
        voyageNumber = VoyageNumber(1),
        maybeFlightCodeSuffix = Option(FlightCodeSuffix("A")),
        status = ArrivalStatus("Scheduled"),
        estimated = Option(1L),
        actual = Option(2L),
        estimatedChox = Option(3L),
        actualChox = Option(4L),
        gate = Option("G"),
        stand = Option("S"),
        totalPax = Option(100),
        transPax = Option(50),
        maxPax = Option(200),
        runwayID = Option("Runway A"),
        baggageReclaimId = Option("Belt 5"),
        terminal = Terminal("T1"),
        origin = PortCode("JFK"),
        scheduled = 5L,
        scheduledDeparture = Option(6L),
        feedSource = LiveFeedSource
      )

      val msg = LiveFlightMessageConversion.liveFlightToFlightMessage(liveArrival)
      val deserialised = LiveFlightMessageConversion.liveFlightFromMessage(msg)

      deserialised should ===(liveArrival)
    }
  }
}
