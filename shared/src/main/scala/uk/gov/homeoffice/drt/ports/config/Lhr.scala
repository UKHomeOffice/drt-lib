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
  val nonEgateQueueRatios: Map[PaxType, Seq[(Queue, Double)]] = Map(
    GBRNationalBelowEgateAge -> List(Queues.EeaDesk -> 1.0),
    EeaBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
    EeaNonMachineReadable -> List(Queues.EeaDesk -> 1.0),
    Transit -> List(Queues.Transfer -> 1.0),
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
        b5jsskToDesk -> (68d / 60),
        b5jsskChildToDesk -> (68d / 60),
        eeaMachineReadableToDesk -> 52d / 60,
        eeaNonMachineReadableToDesk -> 52d / 60,
        eeaChildToDesk -> 52d / 60,
        gbrNationalToDesk -> 43d / 60,
        gbrNationalChildToDesk -> 43d / 60,
        b5jsskToEGate -> (40d / 60),
        eeaMachineReadableToEGate -> 39d / 60,
        gbrNationalToEgate -> 39d / 60,
        visaNationalToDesk -> 120d / 60,
        nonVisaNationalToDesk -> 107d / 60,
        visaNationalToFastTrack -> 120d / 60,
        nonVisaNationalToFastTrack -> 107d / 60,
        visaNationalToEGate -> 47d / 60,
        nonVisaNationalToEGate -> 47d / 60,
        transitToTransfer -> 0d,
      ),
      T3 -> Map(
        b5jsskToDesk -> (68d / 60),
        b5jsskChildToDesk -> (68d / 60),
        eeaMachineReadableToDesk -> 49d / 60,
        eeaNonMachineReadableToDesk -> 49d / 60,
        eeaChildToDesk -> 49d / 60,
        gbrNationalToDesk -> 40d / 60,
        gbrNationalChildToDesk -> 40d / 60,
        b5jsskToEGate -> (43d / 60),
        eeaMachineReadableToEGate -> 42d / 60,
        gbrNationalToEgate -> 41d / 60,
        visaNationalToDesk -> 121d / 60,
        nonVisaNationalToDesk -> 107d / 60,
        visaNationalToFastTrack -> 121d / 60,
        nonVisaNationalToFastTrack -> 107d / 60,
        visaNationalToEGate -> 47d / 60,
        nonVisaNationalToEGate -> 47d / 60,
        transitToTransfer -> 0d,
      ),
      T4 -> Map(
        b5jsskToDesk -> (71d / 60),
        b5jsskChildToDesk -> (71d / 60),
        eeaMachineReadableToDesk -> 52d / 60,
        eeaNonMachineReadableToDesk -> 52d / 60,
        eeaChildToDesk -> 52d / 60,
        gbrNationalToDesk -> 41d / 60,
        gbrNationalChildToDesk -> 41d / 60,
        b5jsskToEGate -> (40d / 60),
        eeaMachineReadableToEGate -> 41d / 60,
        gbrNationalToEgate -> 41d / 60,
        visaNationalToDesk -> 123d / 60,
        nonVisaNationalToDesk -> 107 / 60,
        visaNationalToFastTrack -> 123d / 60,
        nonVisaNationalToFastTrack -> 107 / 60,
        visaNationalToEGate -> 48d / 60,
        nonVisaNationalToEGate -> 47d / 60,
        transitToTransfer -> 0d,
      ),
      T5 -> Map(
        b5jsskToDesk -> (70d / 60),
        b5jsskChildToDesk -> (70d / 60),
        eeaMachineReadableToDesk -> 51d / 60,
        eeaNonMachineReadableToDesk -> 51d / 60,
        eeaChildToDesk -> 51d / 60,
        gbrNationalToDesk -> 41d / 60,
        gbrNationalChildToDesk -> 41d / 60,
        b5jsskToEGate -> (41d / 60),
        eeaMachineReadableToEGate -> 41d / 60,
        gbrNationalToEgate -> 40d / 60,
        visaNationalToDesk -> 130d / 60,
        nonVisaNationalToDesk -> 109d / 60,
        visaNationalToFastTrack -> 130d / 60,
        nonVisaNationalToFastTrack -> 109d / 60,
        visaNationalToEGate -> 49d / 60,
        nonVisaNationalToEGate -> 49d / 60,
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
      Map(
        T2 -> (nonEgateQueueRatios + (
          GBRNational -> List(Queues.EGate -> 0.71, Queues.EeaDesk -> 0.29),
          EeaMachineReadable -> List(Queues.EGate -> 0.74, Queues.EeaDesk -> 0.26),
          B5JPlusNational -> List(Queues.EGate -> 0.72, Queues.EeaDesk -> 0.28),
          NonVisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
          VisaNational -> List(Queues.EGate -> 0.01, Queues.NonEeaDesk -> 0.99),
          )),
        T3 -> (nonEgateQueueRatios + (
          GBRNational -> List(Queues.EGate -> 0.73, Queues.EeaDesk -> 0.27),
          EeaMachineReadable -> List(Queues.EGate -> 0.78, Queues.EeaDesk -> 0.22),
          B5JPlusNational -> List(Queues.EGate -> 0.80, Queues.EeaDesk -> 0.20),
          NonVisaNational -> List(Queues.EGate -> 0.01, Queues.NonEeaDesk -> 0.99),
          VisaNational -> List(Queues.EGate -> 0.01, Queues.NonEeaDesk -> 0.99),
        )),
        T4 -> (nonEgateQueueRatios + (
          GBRNational -> List(Queues.EGate -> 0.69, Queues.EeaDesk -> 0.31),
          EeaMachineReadable -> List(Queues.EGate -> 0.75, Queues.EeaDesk -> 0.25),
          B5JPlusNational -> List(Queues.EGate -> 0.77, Queues.EeaDesk -> 0.23),
          NonVisaNational -> List(Queues.EGate -> 0.01, Queues.NonEeaDesk -> 0.99),
          VisaNational -> List(Queues.EGate -> 0.00, Queues.NonEeaDesk -> 1.00),
        )),
        T5 -> (nonEgateQueueRatios + (
          GBRNational -> List(Queues.EGate -> 0.77, Queues.EeaDesk -> 0.23),
          EeaMachineReadable -> List(Queues.EGate -> 0.80, Queues.EeaDesk -> 0.20),
          B5JPlusNational -> List(Queues.EGate -> 0.78, Queues.EeaDesk -> 0.22),
          NonVisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
          VisaNational -> List(Queues.EGate -> 0.01, Queues.NonEeaDesk -> 0.99),
        ))
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
