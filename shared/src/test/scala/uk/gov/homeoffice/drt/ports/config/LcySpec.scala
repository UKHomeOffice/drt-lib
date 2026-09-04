package uk.gov.homeoffice.drt.ports.config

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.service.QueueConfig
import uk.gov.homeoffice.drt.time.LocalDate

import AirportConfigDefaults._

class LcySpec extends Specification {
  "LCY desk configuration" should {
    "have a minimum of one deployed desk for every queue in every hour" in {
      Lcy.config.minMaxDesksByTerminalQueue24Hrs(T1).map {
        case (queue, (minimum, _)) => queue -> minimum
      } mustEqual Map(
        EGate -> List.fill(24)(1),
        EeaDesk -> List.fill(24)(1),
        NonEeaDesk -> List.fill(24)(1)
      )
    }

    "have the expected maximum deployed desks for every queue in every hour" in {
      Lcy.config.minMaxDesksByTerminalQueue24Hrs(T1).map {
        case (queue, (_, maximum)) => queue -> maximum
      } mustEqual Map(
        EGate -> List.fill(24)(1),
        EeaDesk -> List.fill(24)(5),
        NonEeaDesk -> List.fill(24)(4)
      )
    }
  }
}
