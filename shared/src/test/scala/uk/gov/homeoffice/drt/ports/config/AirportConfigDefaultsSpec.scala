package uk.gov.homeoffice.drt.ports.config

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports.PaxTypeAndQueue
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues.{ NonEeaDesk, QueueDesk }
import uk.gov.homeoffice.drt.ports.config.AirportConfigDefaults._

class AirportConfigDefaultsSpec extends Specification {
  "withCombinedDeskProcessingTimes" should {
    "add all nine default manual-desk times in minutes and preserve every original entry" in {
      withCombinedDeskProcessingTimes(defaultProcessingTimes) mustEqual (
        defaultProcessingTimes ++ Map(
          PaxTypeAndQueue(GBRNational, QueueDesk) -> (22d / 60),
          PaxTypeAndQueue(GBRNationalBelowEgateAge, QueueDesk) -> (22d / 60),
          PaxTypeAndQueue(EeaMachineReadable, QueueDesk) -> (26d / 60),
          PaxTypeAndQueue(EeaNonMachineReadable, QueueDesk) -> (26d / 60),
          PaxTypeAndQueue(EeaBelowEGateAge, QueueDesk) -> (26d / 60),
          PaxTypeAndQueue(B5JPlusNational, QueueDesk) -> (44d / 60),
          PaxTypeAndQueue(B5JPlusNationalBelowEGateAge, QueueDesk) -> (44d / 60),
          PaxTypeAndQueue(NonVisaNational, QueueDesk) -> (91d / 60),
          PaxTypeAndQueue(VisaNational, QueueDesk) -> (89d / 60)
        )
      )
    }

    "not derive combined-desk times from eGate, fast-track or transfer entries" in {
      val processingTimes = Map(
        gbrNationalToEgate -> 0.6,
        visaNationalToFastTrack -> 1.0,
        transitToTransfer -> 0.5
      )

      withCombinedDeskProcessingTimes(processingTimes) mustEqual processingTimes
    }

    "preserve an explicit combined-desk override instead of copying the manual-desk time" in {
      val combinedKey = PaxTypeAndQueue(GBRNational, QueueDesk)
      val processingTimes = Map(
        gbrNationalToDesk -> 0.5,
        combinedKey -> 1.0
      )

      withCombinedDeskProcessingTimes(processingTimes) mustEqual processingTimes
    }

    "accept matching times for the same passenger type in both manual queues" in {
      val processingTimes = Map(
        gbrNationalToDesk -> 0.5,
        PaxTypeAndQueue(GBRNational, NonEeaDesk) -> 0.5
      )

      withCombinedDeskProcessingTimes(processingTimes) mustEqual (
        processingTimes + (PaxTypeAndQueue(GBRNational, QueueDesk) -> 0.5)
      )
    }

    "reject conflicting manual-desk times with an actionable error" in {
      val processingTimes = Map(
        gbrNationalToDesk -> 0.5,
        PaxTypeAndQueue(GBRNational, NonEeaDesk) -> 1.0
      )

      withCombinedDeskProcessingTimes(processingTimes) must throwA[IllegalArgumentException](
        "requirement failed: Ambiguous manual-desk processing times for GBRNational; configure QueueDesk explicitly"
      )
    }

    "allow conflicting manual-desk times when a combined-desk override is explicit" in {
      val processingTimes = Map(
        gbrNationalToDesk -> 0.5,
        PaxTypeAndQueue(GBRNational, NonEeaDesk) -> 1.0,
        PaxTypeAndQueue(GBRNational, QueueDesk) -> 0.75
      )

      withCombinedDeskProcessingTimes(processingTimes) mustEqual processingTimes
    }

    "preserve a combined-desk entry without a corresponding manual-desk entry" in {
      val processingTimes = Map(PaxTypeAndQueue(GBRNational, QueueDesk) -> 0.75)

      withCombinedDeskProcessingTimes(processingTimes) mustEqual processingTimes
    }

    "return an empty map for empty processing times" in {
      withCombinedDeskProcessingTimes(Map.empty) mustEqual Map.empty[PaxTypeAndQueue, Double]
    }

    "leave an already completed processing-time map unchanged" in {
      val processingTimes = withCombinedDeskProcessingTimes(defaultProcessingTimes)

      withCombinedDeskProcessingTimes(processingTimes) mustEqual processingTimes
    }
  }
}
