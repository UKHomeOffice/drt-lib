package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LHR
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Lhr extends AirportConfigLike {
  val lhrDefaultQueueRatios: Map[PaxType, Seq[(Queue, Double)]] = Map(
    GBRNational -> List(Queues.EGate -> 0.8, Queues.EeaDesk -> 0.2),
    GBRNationalBelowEgateAge -> List(Queues.EeaDesk -> 1.0),
    EeaMachineReadable -> List(Queues.EGate -> 0.8, Queues.EeaDesk -> 0.2),
    EeaBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
    EeaNonMachineReadable -> List(Queues.EeaDesk -> 1.0),
    Transit -> List(Queues.Transfer -> 1.0),
    NonVisaNational -> List(Queues.NonEeaDesk -> 1.0),
    VisaNational -> List(Queues.NonEeaDesk -> 1.0),
    B5JPlusNational -> List(Queues.EGate -> 0.6, Queues.EeaDesk -> 0.4),
    B5JPlusNationalBelowEGateAge -> List(Queues.EeaDesk -> 1)
  )

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LHR"),
    queuesByTerminal = SortedMap(
      T2 -> Seq(EeaDesk, EGate, NonEeaDesk, FastTrack, Transfer),
      T3 -> Seq(EeaDesk, EGate, NonEeaDesk, FastTrack, Transfer),
      T4 -> Seq(EeaDesk, EGate, NonEeaDesk, FastTrack, Transfer),
      T5 -> Seq(EeaDesk, EGate, NonEeaDesk, FastTrack, Transfer)
    ),
    slaByQueue = Map(EeaDesk -> 25, EGate -> 15, NonEeaDesk -> 45, FastTrack -> 15),
    crunchOffsetMinutes = 120,
    defaultWalkTimeMillis = Map(T2 -> 900000L, T3 -> 660000L, T4 -> 900000L, T5 -> 660000L),
    terminalPaxSplits = List(T2, T3, T4, T5).map(t => (t, SplitRatios(
      SplitSources.TerminalAverage,
      SplitRatio(eeaMachineReadableToDesk, 0.64 * 0.2),
      SplitRatio(eeaMachineReadableToEGate, 0.64 * 0.8),
      SplitRatio(eeaNonMachineReadableToDesk, 0),
      SplitRatio(visaNationalToDesk, 0.08),
      SplitRatio(visaNationalToFastTrack, 0),
      SplitRatio(nonVisaNationalToDesk, 0.28),
      SplitRatio(nonVisaNationalToFastTrack, 0)
    ))).toMap,
    terminalProcessingTimes = Map(
      T2 -> Map(
        b5jsskToDesk -> 55d / 60,
        b5jsskChildToDesk -> 55d / 60,
        eeaMachineReadableToDesk -> 43d / 60,
        eeaNonMachineReadableToDesk -> 43d / 60,
        eeaChildToDesk -> 43d / 60,
        gbrNationalToDesk -> 35d / 60,
        gbrNationalChildToDesk -> 35d / 60,
        b5jsskToEGate -> 44d / 60,
        eeaMachineReadableToEGate -> 44d / 60,
        gbrNationalToEgate -> 44d / 60,
        visaNationalToDesk -> 111d / 60,
        nonVisaNationalToDesk -> 85d / 60,
        visaNationalToFastTrack -> 111d / 60,
        nonVisaNationalToFastTrack -> 85d / 60,
        transitToTransfer -> 0d,
      ),
      T3 -> Map(
        b5jsskToDesk -> 52d / 60,
        b5jsskChildToDesk -> 52d / 60,
        eeaMachineReadableToDesk -> 40d / 60,
        eeaNonMachineReadableToDesk -> 40d / 60,
        eeaChildToDesk -> 40d / 60,
        gbrNationalToDesk -> 33d / 60,
        gbrNationalChildToDesk -> 33d / 60,
        b5jsskToEGate -> 44d / 60,
        eeaMachineReadableToEGate -> 44d / 60,
        gbrNationalToEgate -> 44d / 60,
        visaNationalToDesk -> 108d / 60,
        nonVisaNationalToDesk -> 94d / 60,
        visaNationalToFastTrack -> 108d / 60,
        nonVisaNationalToFastTrack -> 94d / 60,
        transitToTransfer -> 0d,
      ),
      T4 -> Map(
        b5jsskToDesk -> 55d / 60,
        b5jsskChildToDesk -> 55d / 60,
        eeaMachineReadableToDesk -> 43d / 60,
        eeaNonMachineReadableToDesk -> 43d / 60,
        eeaChildToDesk -> 43d / 60,
        gbrNationalToDesk -> 35d / 60,
        gbrNationalChildToDesk -> 35d / 60,
        b5jsskToEGate -> 44d / 60,
        eeaMachineReadableToEGate -> 44d / 60,
        gbrNationalToEgate -> 44d / 60,
        visaNationalToDesk -> 111d / 60,
        nonVisaNationalToDesk -> 85d / 60,
        visaNationalToFastTrack -> 111d / 60,
        nonVisaNationalToFastTrack -> 85d / 60,
        transitToTransfer -> 0d,
      ),
      T5 -> Map(
        b5jsskToDesk -> 55d / 60,
        b5jsskChildToDesk -> 55d / 60,
        eeaMachineReadableToDesk -> 42d / 60,
        eeaNonMachineReadableToDesk -> 42d / 60,
        eeaChildToDesk -> 42d / 60,
        gbrNationalToDesk -> 34d / 60,
        gbrNationalChildToDesk -> 34d / 60,
        b5jsskToEGate -> 47d / 60,
        eeaMachineReadableToEGate -> 47d / 60,
        gbrNationalToEgate -> 47d / 60,
        visaNationalToDesk -> 115d / 60,
        nonVisaNationalToDesk -> 98d / 60,
        visaNationalToFastTrack -> 115d / 60,
        nonVisaNationalToFastTrack -> 98d / 60,
        transitToTransfer -> 0d,
      )
    ),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T2 -> Map(
        Queues.EGate -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)),
        Queues.EeaDesk -> (List(0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2), List(9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9)),
        Queues.FastTrack -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20))
      ),
      T3 -> Map(
        Queues.EGate -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)),
        Queues.EeaDesk -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16)),
        Queues.FastTrack -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23))
      ),
      T4 -> Map(
        Queues.EGate -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
        Queues.EeaDesk -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8)),
        Queues.FastTrack -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27))
      ),
      T5 -> Map(
        Queues.EGate -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)),
        Queues.EeaDesk -> (List(0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2), List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)),
        Queues.FastTrack -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), List(0, 0, 0, 0, 0, 2, 2, 2, 1, 1, 1, 1, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 0)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20))
      )
    ),
    eGateBankSizes = Map(
      T2 -> Iterable(10, 5),
      T3 -> Iterable(10, 5),
      T4 -> Iterable(10),
      T5 -> Iterable(10, 9, 5),
    ),
    hasActualDeskStats = true,
    forecastExportQueueOrder = Queues.forecastExportQueueOrderWithFastTrack,
    desksExportQueueOrder = Queues.deskExportQueueOrderWithFastTrack,
    role = LHR,
    terminalPaxTypeQueueAllocation = {
      val egateSplitT2 = 0.8102
      val egateSplitT3 = 0.8075
      val egateSplitT4 = 0.7687
      val egateSplitT5 = 0.8466
      Map(
        T2 -> (lhrDefaultQueueRatios + (EeaMachineReadable -> List(
          EGate -> egateSplitT2,
          EeaDesk -> (1.0 - egateSplitT2)
        ))),
        T3 -> (lhrDefaultQueueRatios + (EeaMachineReadable -> List(
          EGate -> egateSplitT3,
          EeaDesk -> (1.0 - egateSplitT3)
        ))),
        T4 -> (lhrDefaultQueueRatios + (EeaMachineReadable -> List(
          EGate -> egateSplitT4,
          EeaDesk -> (1.0 - egateSplitT4)
        ))),
        T5 -> (lhrDefaultQueueRatios + (EeaMachineReadable -> List(
          EGate -> egateSplitT5,
          EeaDesk -> (1.0 - egateSplitT5)
        )))
      )
    },
    hasTransfer = true,
    maybeCiriumEstThresholdHours = Option(6),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, ForecastFeedSource, AclFeedSource),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](
      T2 -> 36,
      T3 -> 28,
      T4 -> 39,
      T5 -> 34
    )
  )
}
