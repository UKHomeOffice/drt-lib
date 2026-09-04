package uk.gov.homeoffice.drt.ports.config

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports.Queues.{ EeaDesk, NonEeaDesk, QueueDesk }
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.service.QueueConfig
import uk.gov.homeoffice.drt.time.LocalDate

class HuySpec extends Specification {
  "HUY" should {
    "use the historic separate queues before the single queue change" in {
      QueueConfig.queuesForDateAndTerminal(Huy.config.queuesByTerminal)(LocalDate(2026, 9, 3), T1) mustEqual
        Seq(EeaDesk, NonEeaDesk)
    }

    "use only QueueDesk from the single queue change" in {
      QueueConfig.queuesForDateAndTerminal(Huy.config.queuesByTerminal)(LocalDate(2026, 9, 4), T1) mustEqual
        Seq(QueueDesk)
    }

    "route both legacy desk queues through QueueDesk" in {
      Huy.config.divertedQueues mustEqual Map(EeaDesk -> QueueDesk, NonEeaDesk -> QueueDesk)
    }

    "provide a 25 minute SLA and two desks for QueueDesk" in {
      val (minDesks, maxDesks) = Huy.config.minMaxDesksByTerminalQueue24Hrs(T1)(QueueDesk)

      Huy.config.slaByQueue(QueueDesk) mustEqual 25
      minDesks mustEqual List.fill(24)(0)
      maxDesks mustEqual List.fill(24)(2)
      Huy.config.desksByTerminal(T1) mustEqual 2
    }
  }
}
