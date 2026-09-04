package uk.gov.homeoffice.drt.ports.config

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports.Queues.{ EeaDesk, NonEeaDesk, QueueDesk }
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.service.QueueConfig
import uk.gov.homeoffice.drt.time.LocalDate

class LbaSpec extends Specification {
  "LBA" should {
    "use the historic separate queues before the single queue change" in {
      QueueConfig.queuesForDateAndTerminal(Lba.config.queuesByTerminal)(LocalDate(2026, 9, 3), T1) mustEqual
        Seq(EeaDesk, NonEeaDesk)
    }

    "use only QueueDesk from the single queue change" in {
      QueueConfig.queuesForDateAndTerminal(Lba.config.queuesByTerminal)(LocalDate(2026, 9, 4), T1) mustEqual
        Seq(QueueDesk)
    }

    "route both legacy desk queues through QueueDesk" in {
      Lba.config.divertedQueues mustEqual Map(EeaDesk -> QueueDesk, NonEeaDesk -> QueueDesk)
    }

    "provide a 25 minute SLA and eight desks for QueueDesk" in {
      val (minDesks, maxDesks) = Lba.config.minMaxDesksByTerminalQueue24Hrs(T1)(QueueDesk)

      Lba.config.slaByQueue(QueueDesk) mustEqual 25
      minDesks mustEqual List.fill(24)(0)
      maxDesks mustEqual List.fill(24)(8)
    }
  }
}
