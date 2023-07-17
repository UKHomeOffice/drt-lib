package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.MAN
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Man extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("MAN"),
    queuesByTerminal = SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk),
      T2 -> Seq(EeaDesk, EGate, NonEeaDesk),
      T3 -> Seq(EeaDesk, EGate, NonEeaDesk)
    ),
    slaByQueue = Map(EeaDesk -> 25, EGate -> 10, NonEeaDesk -> 45),
    defaultWalkTimeMillis = Map(T1 -> 180000L, T2 -> 600000L, T3 -> 180000L),
    terminalPaxSplits = List(T1, T2, T3).map(t => (t, SplitRatios(
      SplitSources.TerminalAverage,
      SplitRatio(eeaMachineReadableToDesk, 0.08335),
      SplitRatio(eeaMachineReadableToEGate, 0.7333),
      SplitRatio(eeaNonMachineReadableToDesk, 0.08335),
      SplitRatio(visaNationalToDesk, 0.05),
      SplitRatio(nonVisaNationalToDesk, 0.05)
    ))).toMap,
    terminalProcessingTimes = Map(
      T1 -> Map(
        b5jsskToDesk -> 65d / 60,
        b5jsskChildToDesk -> 65d / 60,
        eeaMachineReadableToDesk -> 45d / 60,
        eeaNonMachineReadableToDesk -> 45d / 60,
        eeaChildToDesk -> 45d / 60,
        gbrNationalToDesk -> 34d / 60,
        gbrNationalChildToDesk -> 34d / 60,
        b5jsskToEGate -> 39d / 60,
        eeaMachineReadableToEGate -> 38d / 60,
        gbrNationalToEgate -> 38d / 60,
        visaNationalToDesk -> 116d / 60,
        nonVisaNationalToDesk -> 111d / 60,
        visaNationalToEGate -> 48d / 60,
        nonVisaNationalToEGate -> 49d / 60,
      ),
      T2 -> Map(
        b5jsskToDesk -> 72d / 60,
        b5jsskChildToDesk -> 72d / 60,
        eeaMachineReadableToDesk -> 48d / 60,
        eeaNonMachineReadableToDesk -> 48d / 60,
        eeaChildToDesk -> 48d / 60,
        gbrNationalToDesk -> 32d / 60,
        gbrNationalChildToDesk -> 32d / 60,
        b5jsskToEGate -> 46d / 60,
        eeaMachineReadableToEGate -> 45d / 60,
        gbrNationalToEgate -> 43d / 60,
        visaNationalToDesk -> 119d / 60,
        nonVisaNationalToDesk -> 120d / 60,
        visaNationalToEGate -> 52d / 60,
        nonVisaNationalToEGate -> 50d / 60,
      ),
      T3 -> Map(
        b5jsskToDesk -> 68d / 60,
        b5jsskChildToDesk -> 68d / 60,
        eeaMachineReadableToDesk -> 43d / 60,
        eeaNonMachineReadableToDesk -> 43d / 60,
        eeaChildToDesk -> 43d / 60,
        gbrNationalToDesk -> 35d / 60,
        gbrNationalChildToDesk -> 35d / 60,
        b5jsskToEGate -> 39d / 60,
        eeaMachineReadableToEGate -> 40d / 60,
        gbrNationalToEgate -> 39d / 60,
        visaNationalToDesk -> 108d / 60,
        nonVisaNationalToDesk -> 105d / 60,
        visaNationalToEGate -> 45d / 60,
        nonVisaNationalToEGate -> 45d / 60,
      )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.EGate -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)),
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(5, 5, 5, 5, 5, 5, 7, 7, 7, 7, 5, 6, 6, 6, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5))
      ),
      T2 -> Map(
        Queues.EGate -> (List(1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 5, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(3, 3, 3, 3, 3, 8, 8, 8, 8, 8, 8, 3, 3, 3, 3, 3, 6, 6, 6, 6, 3, 3, 3, 3))
      ),
      T3 -> Map(
        Queues.EGate -> (List(1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
        Queues.EeaDesk -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3))
      )
    ),
    eGateBankSizes = Map(
      T1 -> Iterable(10),
      T2 -> Iterable(10),
      T3 -> Iterable(10),
    ),
    role = MAN,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.66, Queues.EeaDesk -> 0.34),
        EeaMachineReadable -> List(Queues.EGate -> 0.75, Queues.EeaDesk -> 0.25),
        B5JPlusNational -> List(Queues.EGate -> 0.70, Queues.EeaDesk -> 0.30),
        NonVisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
        VisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
      )),
      T2 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.62, Queues.EeaDesk -> 0.32),
        EeaMachineReadable -> List(Queues.EGate -> 0.73, Queues.EeaDesk -> 0.23),
        B5JPlusNational -> List(Queues.EGate -> 0.67, Queues.EeaDesk -> 0.33),
        NonVisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
        VisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
      )),
      T3 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.74, Queues.EeaDesk -> 0.26),
        EeaMachineReadable -> List(Queues.EGate -> 0.73, Queues.EeaDesk -> 0.23),
        B5JPlusNational -> List(Queues.EGate -> 0.77, Queues.EeaDesk -> 0.23),
        NonVisaNational -> List(Queues.EGate -> 0.03, Queues.NonEeaDesk -> 0.97),
        VisaNational -> List(Queues.EGate -> 0.03, Queues.NonEeaDesk -> 0.97),
      ))),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](
      T1 -> 14,
      T2 -> 11,
      T3 -> 9
    ),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource)
  )
}
