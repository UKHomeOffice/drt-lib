package uk.gov.homeoffice.drt.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.models.PassengerInfo.ageRangesForDate
import uk.gov.homeoffice.drt.time.SDate
import uk.gov.homeoffice.drt.ports.PaxAge
import uk.gov.homeoffice.drt.Nationality
import uk.gov.homeoffice.drt.arrivals.EventTypes.DC
import uk.gov.homeoffice.drt.arrivals.CarrierCode
import uk.gov.homeoffice.drt.arrivals.VoyageNumber
import uk.gov.homeoffice.drt.ports.PortCode

class PassengerInfoTest extends AnyWordSpec with Matchers {
  "Deserializing age ranges" should {
    "get back the correct age range " in {
      val ageRangeStrings = PassengerInfo.ageRangesForDate(Some(SDate(System.currentTimeMillis()))).map(_.title)

      val result = ageRangeStrings.map(PaxAgeRange.parse)

      result should ===(PassengerInfo.ageRangesForDate(Some(SDate(System.currentTimeMillis()))))
    }
  }

  "age range helpers" should {
    "return expected ranges before and after egate eligibility change" in {
      val before = SDate("2026-07-07T09:00:00")
      val after = SDate("2026-07-09T11:00:00")

      ageRangesForDate(Some(before)).take(2) should ===(List(AgeRange(0, 9), AgeRange(10, 17)))
      ageRangesForDate(Some(after)).take(2) should ===(List(AgeRange(0, 7), AgeRange(8, 17)))
    }

    "isBeforeEgateAgeEligibilityDateChange behaves correctly" in {
      val before = SDate("2026-07-07T09:00:00")
      val after = SDate("2026-07-09T11:00:00")

      PassengerInfo.isBeforeEgateAgeEligibilityDateChange(Some(before)) shouldBe true
      PassengerInfo.isBeforeEgateAgeEligibilityDateChange(Some(after)) shouldBe false
      PassengerInfo.isBeforeEgateAgeEligibilityDateChange(None) shouldBe false
    }

    "treat the exact UTC cutover instant as post-change" in {
      val atCutover = SDate("2026-07-08T09:00:00Z")

      PassengerInfo.isBeforeEgateAgeEligibilityDateChange(Some(atCutover)) shouldBe false
      ageRangesForDate(Some(atCutover)).take(2) should ===(List(AgeRange(0, 7), AgeRange(8, 17)))
    }
  }

  "manifest aggregations" should {
    "map passengers to age ranges and include Unknown when age missing" in {
      val p1 = PassengerInfoJson(None, Nationality("USA"), EeaFlag("N"), Some(PaxAge(8)), None, InTransit(false), None, Some(Nationality("USA")), Some("id1"))
      val p2 = PassengerInfoJson(None, Nationality("GBR"), EeaFlag("N"), Some(PaxAge(10)), None, InTransit(false), None, Some(Nationality("GBR")), Some("id2"))
      val p3 = PassengerInfoJson(None, Nationality(""), EeaFlag("N"), None, None, InTransit(false), None, None, Some("id3"))

      val vm = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("2026-07-07"), ManifestTimeOfArrival("09:00:00"), List(p1, p2, p3))

      val counts = PassengerInfo.manifestToAgeRangeCount(vm)

      counts(AgeRange(0, 9)) should ===(1)
      counts(AgeRange(10, 17)) should ===(1)
      counts(UnknownAge) should ===(1)
    }

    "respect age boundary change for age 8" in {
      val p = PassengerInfoJson(None, Nationality("USA"), EeaFlag("N"), Some(PaxAge(8)), None, InTransit(false), None, Some(Nationality("USA")), Some("id"))

      val vmBefore = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("2026-07-07"), ManifestTimeOfArrival("09:00:00"), List(p))
      val vmAfter = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("2026-07-09"), ManifestTimeOfArrival("11:00:00"), List(p))

      PassengerInfo.manifestToAgeRangeCount(vmBefore)(AgeRange(0, 9)) should ===(1)
      PassengerInfo.manifestToAgeRangeCount(vmAfter)(AgeRange(8, 17)) should ===(1)
    }

    "map empty nationality to Unknown" in {
      val p = PassengerInfoJson(None, Nationality(""), EeaFlag("N"), None, None, InTransit(false), None, None, Some("id"))
      val vm = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("2026-07-07"), ManifestTimeOfArrival("09:00:00"), List(p))

      val natCounts = PassengerInfo.manifestToNationalityCount(vm)

      natCounts(Nationality("Unknown")) should ===(1)
    }

    "produce pax type counts using allocator" in {
      val p1 = PassengerInfoJson(Some(DocumentType.Passport), Nationality("USA"), EeaFlag("N"), Some(PaxAge(5)), None, InTransit(false), None, Some(Nationality("USA")), Some("id1"))
      val p2 = PassengerInfoJson(Some(DocumentType.Passport), Nationality("AUT"), EeaFlag("N"), Some(PaxAge(30)), None, InTransit(false), None, Some(Nationality("AUT")), Some("id2"))

      val vm = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("2026-07-07"), ManifestTimeOfArrival("09:00:00"), List(p1, p2))

      val paxTypes = PassengerInfo.manifestToPaxTypes(vm)

      paxTypes(uk.gov.homeoffice.drt.ports.PaxTypes.B5JPlusNationalBelowEGateAge) should ===(1)
      paxTypes(uk.gov.homeoffice.drt.ports.PaxTypes.EeaMachineReadable) should ===(1)
    }

    "default to post-change pax type allocation when schedule parsing fails" in {
      val p = PassengerInfoJson(Some(DocumentType.Passport), Nationality("JPN"), EeaFlag("N"), Some(PaxAge(8)), None, InTransit(false), None, Some(Nationality("JPN")), Some("id"))
      val vm = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("not-a-date"), ManifestTimeOfArrival("also-invalid"), List(p))

      val paxTypes = PassengerInfo.manifestToPaxTypes(vm)

      paxTypes(uk.gov.homeoffice.drt.ports.PaxTypes.B5JPlusNational) should ===(1)
    }

    "produce a FlightManifestSummary from a manifest" in {
      val p1 = PassengerInfoJson(None, Nationality("USA"), EeaFlag("N"), Some(PaxAge(8)), None, InTransit(false), None, Some(Nationality("USA")), Some("id1"))
      val p2 = PassengerInfoJson(None, Nationality("GBR"), EeaFlag("N"), Some(PaxAge(10)), None, InTransit(false), None, Some(Nationality("GBR")), Some("id2"))

      val vm = VoyageManifest(DC, PortCode("AAA"), PortCode("BBB"), VoyageNumber("1"), CarrierCode("BA"), ManifestDateOfArrival("2026-07-07"), ManifestTimeOfArrival("09:00:00"), List(p1, p2))

      val maybeSummary = PassengerInfo.manifestToFlightManifestSummary(vm)

      maybeSummary shouldBe defined
      val summary = maybeSummary.get

      summary.arrivalKey should ===(vm.maybeKey.get)
      summary.ageRanges should ===(PassengerInfo.manifestToAgeRangeCount(vm))
      summary.nationalities should ===(PassengerInfo.manifestToNationalityCount(vm))
      summary.paxTypes should ===(PassengerInfo.manifestToPaxTypes(vm))
    }
  }
}
