package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.{NQY, PIK}
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues.{EeaDesk, NonEeaDesk}
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Nqy extends AirportConfigLike {

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("NQY"),
    queuesByTerminal = SortedMap(
      T1 -> Seq(Queues.QueueDesk)
    ),
    divertedQueues = Map(
      Queues.NonEeaDesk -> Queues.QueueDesk,
      Queues.EeaDesk -> Queues.QueueDesk
    ),
    slaByQueue = Map(
      Queues.QueueDesk -> 20,
    ),
    defaultWalkTimeMillis = Map(T1 -> 30000L),
    terminalPaxSplits = Map(T1 -> SplitRatios(
      SplitSources.TerminalAverage,
      SplitRatio(eeaMachineReadableToDesk, 0.99 * 0.2),
      SplitRatio(eeaNonMachineReadableToDesk, 0),
      SplitRatio(visaNationalToDesk, 0.0),
      SplitRatio(nonVisaNationalToDesk, 0.01)
    )),
    terminalProcessingTimes = Map(T1 -> Map(
      eeaMachineReadableToDesk -> 20d / 60,
      eeaNonMachineReadableToDesk -> 50d / 60,
      visaNationalToDesk -> 100d / 60,
      nonVisaNationalToDesk -> 80d / 60
    )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.QueueDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2))
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable()),
    role = NQY,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> Map(
        EeaMachineReadable -> List(Queues.QueueDesk -> 1.0),
        EeaBelowEGateAge -> List(Queues.QueueDesk -> 1.0),
        EeaNonMachineReadable -> List(Queues.QueueDesk -> 1.0),
        NonVisaNational -> List(Queues.QueueDesk -> 1.0),
        VisaNational -> List(Queues.QueueDesk -> 1.0),
        B5JPlusNational -> List(Queues.QueueDesk -> 1.0),
        B5JPlusNationalBelowEGateAge -> List(Queues.QueueDesk -> 1.0)
      )),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 2),
    feedSources = Seq(ApiFeedSource, LiveFeedSource)
  )
}
