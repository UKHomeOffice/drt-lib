package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.{EXT, MME}
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Mme extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("MME"),
    queuesByTerminal = SortedMap(
      T1 -> Seq(Queues.QueueDesk)
    ),
    slaByQueue = Map(
      Queues.QueueDesk -> 20
    ),
    defaultWalkTimeMillis = Map(T1 -> 300000L),
    terminalPaxSplits = Map(T1 -> SplitRatios(
      SplitSources.TerminalAverage,
      SplitRatio(eeaMachineReadableToDesk, 0.98),
      SplitRatio(eeaNonMachineReadableToDesk, 0),
      SplitRatio(visaNationalToDesk, 0.01),
      SplitRatio(nonVisaNationalToDesk, 0.01)
    )),
    terminalProcessingTimes = Map(T1 -> Map(
      eeaMachineReadableToDesk -> 20d / 60,
      eeaNonMachineReadableToDesk -> 50d / 60,
      visaNationalToDesk -> 90d / 60,
      nonVisaNationalToDesk -> 78d / 60
    )),
    minMaxDesksByTerminalQueue24Hrs = Map(T1 -> Map(
      Queues.QueueDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)),
    )),
    eGateBankSizes = Map(),
    role = MME,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (defaultQueueRatios + (
        EeaMachineReadable -> List(Queues.QueueDesk -> 1.0),
        EeaBelowEGateAge -> List(Queues.QueueDesk -> 1.0),
        EeaNonMachineReadable -> List(Queues.QueueDesk -> 1.0),
        NonVisaNational -> List(Queues.QueueDesk -> 1.0),
        VisaNational -> List(Queues.QueueDesk -> 1.0),
        B5JPlusNational -> List(Queues.QueueDesk -> 1.0),
        B5JPlusNationalBelowEGateAge -> List(Queues.QueueDesk -> 1.0)
      ))),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, AclFeedSource),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 2)
  )
}