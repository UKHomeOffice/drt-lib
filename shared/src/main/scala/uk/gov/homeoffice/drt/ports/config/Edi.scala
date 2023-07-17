package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.EDI
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Edi extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("EDI"),
    queuesByTerminal = SortedMap(
      A1 -> Seq(EeaDesk, EGate, NonEeaDesk),
      A2 -> Seq(EeaDesk, EGate, NonEeaDesk)
    ),
    slaByQueue = defaultSlas,
    defaultWalkTimeMillis = Map(A1 -> 180000L, A2 -> 120000L),
    terminalPaxSplits = List(A1, A2).map(t => (t, defaultPaxSplits)).toMap,
    terminalProcessingTimes = Map(
      A1 -> Map(
        b5jsskToDesk -> 38d / 60,
        b5jsskChildToDesk -> 38d / 60,
        eeaMachineReadableToDesk -> 32d / 60,
        eeaNonMachineReadableToDesk -> 32d / 60,
        eeaChildToDesk -> 32d / 60,
        gbrNationalToDesk -> 25d / 60,
        gbrNationalChildToDesk -> 25d / 60,
        b5jsskToEGate -> 41d / 60,
        eeaMachineReadableToEGate -> 41d / 60,
        gbrNationalToEgate -> 40d / 60,
        visaNationalToDesk -> 101d / 60,
        nonVisaNationalToDesk -> 70d / 60,
        visaNationalToEGate -> 48d / 60,
        nonVisaNationalToEGate -> 49d / 60,
      ),
      A2 -> Map(
        b5jsskToDesk -> 51d / 60,
        b5jsskChildToDesk -> 51d / 60,
        eeaMachineReadableToDesk -> 38d / 60,
        eeaNonMachineReadableToDesk -> 38d / 60,
        eeaChildToDesk -> 38d / 60,
        gbrNationalToDesk -> 31d / 60,
        gbrNationalChildToDesk -> 31d / 60,
        b5jsskToEGate -> 41d / 60,
        eeaMachineReadableToEGate -> 41d / 60,
        gbrNationalToEgate -> 40d / 60,
        visaNationalToDesk -> 115d / 60,
        nonVisaNationalToDesk -> 88d / 60,
        visaNationalToEGate -> 48d / 60,
        nonVisaNationalToEGate -> 49d / 60,
      )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      A1 -> Map(
        Queues.EGate -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9)),
        Queues.NonEeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(2, 2, 2, 2, 2, 2, 6, 6, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3))
      ),
      A2 -> Map(
        Queues.EGate -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)),
        Queues.NonEeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3))
      )
    ),
    eGateBankSizes = Map(A1 -> Iterable(5), A2 -> Iterable(10)),
    role = EDI,
    terminalPaxTypeQueueAllocation = Map(
      A1 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.58, Queues.EeaDesk -> 0.42),
        EeaMachineReadable -> List(Queues.EGate -> 0.64, Queues.EeaDesk -> 0.36),
        B5JPlusNational -> List(Queues.EGate -> 0.62, Queues.EeaDesk -> 0.38),
        NonVisaNational -> List(Queues.EGate -> 0.04, Queues.NonEeaDesk -> 0.96),
        VisaNational -> List(Queues.EGate -> 0.04, Queues.NonEeaDesk -> 0.96),
      )),
      A2 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.58, Queues.EeaDesk -> 0.42),
        EeaMachineReadable -> List(Queues.EGate -> 0.64, Queues.EeaDesk -> 0.36),
        B5JPlusNational -> List(Queues.EGate -> 0.62, Queues.EeaDesk -> 0.38),
        NonVisaNational -> List(Queues.EGate -> 0.04, Queues.NonEeaDesk -> 0.96),
        VisaNational -> List(Queues.EGate -> 0.04, Queues.NonEeaDesk -> 0.96),
      ))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](A1 -> 11, A2 -> 9),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource),
    feedSourceMonitorExemptions = Seq(LiveFeedSource),
    hasEstChox = false
  )
}
