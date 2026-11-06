package uk.gov.homeoffice.drt.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.Nationality
import uk.gov.homeoffice.drt.arrivals.EventTypes.DC
import uk.gov.homeoffice.drt.arrivals.{ CarrierCode, VoyageNumber }
import uk.gov.homeoffice.drt.ports.PaxTypes.{ B5JPlusNational, GBRNational, GBRNationalBelowEgateAge, Transit }
import uk.gov.homeoffice.drt.ports.{ PaxAge, PortCode }

class PassengerInfoJsonTest extends AnyWordSpec with Matchers {
  private val arrivalPort: PortCode = PortCode("BHX")
  private val passenger: PassengerInfoJson = PassengerInfoJson(
    DocumentType = Some(DocumentType.Passport),
    DocumentIssuingCountryCode = Nationality("GBR"),
    EEAFlag = EeaFlag("N"),
    Age = Some(PaxAge(30)),
    DisembarkationPortCode = Some(PortCode("")),
    InTransitFlag = InTransit("N"),
    NationalityCountryCode = Some(Nationality("GBR")),
    PassengerIdentifier = Some("passenger-1")
  )

  private def manifest(passengers: List[PassengerInfoJson]): VoyageManifest = VoyageManifest(
    DC,
    arrivalPort,
    PortCode("ALC"),
    VoyageNumber("1222"),
    CarrierCode("LS"),
    ManifestDateOfArrival("2026-10-13"),
    ManifestTimeOfArrival("12:00:00"),
    passengers
  )

  "isInTransit" should {
    val cases = List(
      ("N with no port", "N", None, false),
      ("N with an empty port", "N", Some(PortCode("")), false),
      ("N with a whitespace-only port", "N", Some(PortCode(" \t\r\n ")), false),
      ("N with a matching port", "N", Some(arrivalPort), false),
      ("N with a different port", "N", Some(PortCode("LHR")), true),
      ("Y with no port", "Y", None, true),
      ("Y with an empty port", "Y", Some(PortCode("")), true),
      ("Y with a whitespace-only port", "Y", Some(PortCode(" \t\r\n ")), true),
      ("Y with a matching port", "Y", Some(arrivalPort), true),
      ("Y with a different port", "Y", Some(PortCode("LHR")), true)
    )

    cases.foreach {
      case (description, flag, port, expected) =>
        s"classify $description correctly" in {
          passenger.copy(
            DisembarkationPortCode = port,
            InTransitFlag = InTransit(flag)
          ).isInTransit(arrivalPort) shouldBe expected
        }
    }
  }

  "manifest summaries" should {
    "classify persisted-style empty ports by nationality rather than Transit" in {
      val passengers = (1 to 214).map { index =>
        val nationality = Nationality(if (index <= 203) "GBR" else "USA")
        passenger.copy(
          DocumentIssuingCountryCode = nationality,
          NationalityCountryCode = Some(nationality),
          PassengerIdentifier = Some(s"passenger-$index")
        )
      }.toList

      val summary = PassengerInfo.manifestToFlightManifestSummary(manifest(passengers)).get

      summary.paxTypes shouldBe Map(GBRNational -> 203, B5JPlusNational -> 11)
      summary.nationalities shouldBe Map(Nationality("GBR") -> 203, Nationality("USA") -> 11)
      summary.passengerCount shouldBe 214
    }

    "preserve real transit and identifier-based deduplication with blank ports" in {
      val child = passenger.copy(
        Age = Some(PaxAge(7)),
        DisembarkationPortCode = Some(PortCode(" \t ")),
        PassengerIdentifier = Some("child")
      )
      val flaggedTransit = passenger.copy(InTransitFlag = InTransit("Y"), PassengerIdentifier = Some("flagged"))
      val differentPortTransit = passenger.copy(
        DisembarkationPortCode = Some(PortCode("LHR")),
        PassengerIdentifier = Some("different-port")
      )
      val voyageManifest = manifest(List(
        passenger,
        passenger,
        child,
        flaggedTransit,
        differentPortTransit,
        passenger.copy(PassengerIdentifier = None),
        passenger.copy(PassengerIdentifier = Some(""))
      ))

      val summary = PassengerInfo.manifestToFlightManifestSummary(voyageManifest).get

      voyageManifest.uniquePassengers.size shouldBe 4
      summary.paxTypes shouldBe Map(GBRNational -> 1, GBRNationalBelowEgateAge -> 1, Transit -> 2)
      summary.nationalities shouldBe Map(Nationality("GBR") -> 4)
      summary.passengerCount shouldBe 4
    }

    "retain every passenger when no non-empty identifiers are available" in {
      val unidentified = passenger.copy(PassengerIdentifier = None)
      val voyageManifest = manifest(List(
        unidentified,
        unidentified.copy(PassengerIdentifier = Some("")),
        unidentified.copy(InTransitFlag = InTransit("Y"))
      ))

      val summary = PassengerInfo.manifestToFlightManifestSummary(voyageManifest).get

      voyageManifest.uniquePassengers.size shouldBe 3
      summary.paxTypes shouldBe Map(GBRNational -> 2, Transit -> 1)
      summary.passengerCount shouldBe 3
    }
  }
}
