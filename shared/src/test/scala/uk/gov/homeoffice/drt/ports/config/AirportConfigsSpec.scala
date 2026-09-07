package uk.gov.homeoffice.drt.ports.config

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._

class AirportConfigsSpec extends Specification {

  "AirportConfigs" should {

    "build the standard processing-time matrix from seconds" in {
      val processingTimes = AirportConfigDefaults.standardProcessingTimes(
        AirportConfigDefaults.ProcessingTimesInSeconds(
          gbr = 60,
          eea = 120,
          b5jssk = 90,
          nvn = 150,
          vn = 75,
          egates = 45
        )
      )

      processingTimes mustEqual Map(
        b5jsskToDesk -> 1.5,
        b5jsskChildToDesk -> 1.5,
        eeaChildToDesk -> 2.0,
        eeaMachineReadableToDesk -> 2.0,
        eeaNonMachineReadableToDesk -> 2.0,
        gbrNationalToDesk -> 1.0,
        gbrNationalChildToDesk -> 1.0,
        b5jsskToEGate -> 0.75,
        eeaMachineReadableToEGate -> 0.75,
        gbrNationalToEgate -> 0.75,
        visaNationalToDesk -> 1.25,
        nonVisaNationalToDesk -> 2.5
      )
    }

    "have a list size of 24 of min and max desks by terminal and queue for all ports" in {
      for {
        port <- AirportConfigs.allPortConfigs
        terminalName <- port.minMaxDesksByTerminalQueue24Hrs.keySet
        queueName <- port.minMaxDesksByTerminalQueue24Hrs(terminalName).keySet
        (minDesks, maxDesks) = port.minMaxDesksByTerminalQueue24Hrs(terminalName)(queueName)
      } yield {
        minDesks.size.aka(s"minDesk-> ${port.portCode} -> $terminalName -> $queueName") mustEqual 24
        maxDesks.size.aka(s"maxDesk-> ${port.portCode} -> $terminalName -> $queueName") mustEqual 24
      }
    }

    "Queue names in min max desks by terminal and queues should be defined in Queues" in {
      for {
        port <- AirportConfigs.allPortConfigs
        terminalName <- port.minMaxDesksByTerminalQueue24Hrs.keySet
        queueName <- port.minMaxDesksByTerminalQueue24Hrs(terminalName).keySet
      } yield {
        Queues.displayName(queueName).aka(s"$queueName not found in Queues") mustNotEqual None
      }
    }

    "All Airport config queues must be defined in Queues" in {
      for {
        port <- AirportConfigs.allPortConfigs
        (_, queues) <- port.queuesByTerminal.values.flatten
        queueName <- queues
      } yield {
        Queues.displayName(queueName).aka(s"$queueName not found in Queues") mustNotEqual None
      }
    }

    "All configurations should be valid with no missing queues or terminals" in {
      AirportConfigs.allPortConfigs.foreach(_.assertValid())

      success
    }
  }

}
