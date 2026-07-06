package uk.gov.homeoffice.drt.models

import uk.gov.homeoffice.drt.Nationality
import uk.gov.homeoffice.drt.ports.PaxType
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.services.PassengerTypeCalculator.{ isB5JPlus, isEea, isVisaNational }
import uk.gov.homeoffice.drt.time.SDateLike

trait PaxTypeAllocator {
  val transit: PartialFunction[ManifestPassengerProfile, PaxType] = {
    case ManifestPassengerProfile(_, _, _, isTransit, _) if isTransit => Transit
  }

  def apply(manifestPassengerProfile: ManifestPassengerProfile): PaxType
}

@scala.annotation.nowarn("msg=Declaration is never used")
case object DefaultPaxTypeAllocator extends PaxTypeAllocator {
  override def apply(manifestPassengerProfile: ManifestPassengerProfile): PaxType =
    manifestPassengerProfile match {
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, Some(age), _, _)
          if age.isUnder(8) => GBRNationalBelowEgateAge
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, _, _, _) =>
        GBRNational
      case ManifestPassengerProfile(country, Some(docType), Some(age), _, _)
          if isEea(country) && docType == DocumentType.Passport && age.isUnder(8) => EeaBelowEGateAge
      case ManifestPassengerProfile(country, Some(docType), _, _, _)
          if isEea(country) && docType == DocumentType.Passport => EeaMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if isEea(country) => EeaNonMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) && isVisaNational(country) => VisaNational
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) => NonVisaNational
    }
}

@scala.annotation.nowarn("msg=Declaration is never used")
case object DefaultWithTransitPaxTypeAllocator extends PaxTypeAllocator {
  override def apply(manifestPassengerProfile: ManifestPassengerProfile): PaxType =
    manifestPassengerProfile match {
      case ManifestPassengerProfile(_, _, _, isTransit, _) if isTransit => Transit
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, Some(age), _, _)
          if age.isUnder(8) => GBRNationalBelowEgateAge
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, _, _, _) =>
        GBRNational
      case ManifestPassengerProfile(country, Some(docType), Some(age), _, _)
          if isEea(country) && docType == DocumentType.Passport && age.isUnder(8) => EeaBelowEGateAge
      case ManifestPassengerProfile(country, Some(docType), _, _, _)
          if isEea(country) && docType == DocumentType.Passport => EeaMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if isEea(country) => EeaNonMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) && isVisaNational(country) => VisaNational
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) => NonVisaNational
    }
}

@scala.annotation.nowarn("msg=Declaration is never used")
case object B5JPlusTypeAllocator extends PaxTypeAllocator {
  override def apply(manifestPassengerProfile: ManifestPassengerProfile): PaxType =
    manifestPassengerProfile match {
      case ManifestPassengerProfile(country, _, Some(age), _, _)
          if isB5JPlus(country) && age.isUnder(8) => B5JPlusNationalBelowEGateAge
      case ManifestPassengerProfile(country, _, _, _, _)
          if isB5JPlus(country) => B5JPlusNational
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, Some(age), _, _)
          if age.isUnder(8) => GBRNationalBelowEgateAge
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, _, _, _) =>
        GBRNational
      case ManifestPassengerProfile(country, Some(docType), Some(age), _, _)
          if isEea(country) && docType == DocumentType.Passport && age.isUnder(8) => EeaBelowEGateAge
      case ManifestPassengerProfile(country, Some(docType), _, _, _)
          if isEea(country) && docType == DocumentType.Passport => EeaMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if isEea(country) => EeaNonMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) && isVisaNational(country) => VisaNational
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) => NonVisaNational
    }
}

@scala.annotation.nowarn("msg=Declaration is never used")
case object B5JPlusWithTransitTypeAllocator extends PaxTypeAllocator {
  override def apply(manifestPassengerProfile: ManifestPassengerProfile): PaxType =
    apply(manifestPassengerProfile, None)

  def apply(manifestPassengerProfile: ManifestPassengerProfile, scheduled: Option[SDateLike]): PaxType =
    manifestPassengerProfile match {
      case ManifestPassengerProfile(_, _, _, isTransit, _) if isTransit => Transit
      case ManifestPassengerProfile(country, _, Some(age), _, _)
          if isB5JPlus(country) && age.isUnder(if (PassengerInfo.isBeforeEgateAgeEligibilityDateChange(scheduled)) 10 else 8) =>
        B5JPlusNationalBelowEGateAge
      case ManifestPassengerProfile(country, _, _, _, _)
          if isB5JPlus(country) => B5JPlusNational
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, Some(age), _, _)
          if age.isUnder(if (PassengerInfo.isBeforeEgateAgeEligibilityDateChange(scheduled)) 10 else 8) =>
        GBRNationalBelowEgateAge
      case ManifestPassengerProfile(Nationality(CountryCodes.UK), _, _, _, _) =>
        GBRNational
      case ManifestPassengerProfile(country, Some(docType), Some(age), _, _)
          if isEea(country) && docType == DocumentType.Passport && age.isUnder(if (PassengerInfo.isBeforeEgateAgeEligibilityDateChange(scheduled)) 10 else 8) =>
        EeaBelowEGateAge
      case ManifestPassengerProfile(country, Some(docType), _, _, _)
          if isEea(country) && docType == DocumentType.Passport => EeaMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if isEea(country) => EeaNonMachineReadable
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) && isVisaNational(country) => VisaNational
      case ManifestPassengerProfile(country, _, _, _, _)
          if !isEea(country) => NonVisaNational
    }
}
