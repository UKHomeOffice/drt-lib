package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues.{EGate, EeaDesk, NonEeaDesk, Queue}
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.{PaxTypes, _}

object AirportConfigDefaults {
  val defaultSlas: Map[Queue, Int] = Map(
    EeaDesk -> 20,
    EGate -> 25,
    NonEeaDesk -> 45
  )

  val defaultPaxSplits: SplitRatios = SplitRatios(
    SplitSources.TerminalAverage,
    SplitRatio(eeaMachineReadableToDesk, 0.175),
    SplitRatio(eeaMachineReadableToEGate, 0.55),
    SplitRatio(eeaNonMachineReadableToDesk, 0.175),
    SplitRatio(visaNationalToDesk, 0.05),
    SplitRatio(nonVisaNationalToDesk, 0.05)
  )

  val defaultPaxSplitsWithoutEgates: SplitRatios = SplitRatios(
    SplitSources.TerminalAverage,
    SplitRatio(eeaMachineReadableToDesk, 0.725),
    SplitRatio(eeaNonMachineReadableToDesk, 0.175),
    SplitRatio(visaNationalToDesk, 0.05),
    SplitRatio(nonVisaNationalToDesk, 0.05)
  )

  val defaultQueueRatios: Map[PaxType, Seq[(Queue, Double)]] = Map(
    GBRNational -> List(Queues.EGate -> 0.8, Queues.EeaDesk -> 0.2),
    EeaMachineReadable -> List(Queues.EGate -> 0.8, Queues.EeaDesk -> 0.2),
    B5JPlusNational -> List(Queues.EGate -> 0.7, Queues.EeaDesk -> 0.3),
    NonVisaNational -> List(Queues.NonEeaDesk -> 0.98, Queues.EGate -> 0.02),
    VisaNational -> List(Queues.NonEeaDesk -> 0.98, Queues.EGate -> 0.02),
    GBRNationalBelowEgateAge -> List(Queues.EeaDesk -> 1.0),
    EeaBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
    EeaNonMachineReadable -> List(Queues.EeaDesk -> 1.0),
    B5JPlusNationalBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
    PaxTypes.Transit -> List(),
  )

  val defaultQueueRatiosWithoutEgates: Map[PaxType, Seq[(Queue, Double)]] = defaultQueueRatios ++ Map(
    GBRNational -> List(EeaDesk -> 1.0),
    EeaMachineReadable -> List(EeaDesk -> 1.0),
    B5JPlusNational -> List(EeaDesk -> 1.0),
    NonVisaNational -> List(Queues.NonEeaDesk -> 1.0),
    VisaNational -> List(Queues.NonEeaDesk -> 1.0),
    GBRNationalBelowEgateAge -> List(Queues.EeaDesk -> 1.0),
    EeaBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
    EeaNonMachineReadable -> List(Queues.EeaDesk -> 1.0),
    B5JPlusNationalBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
  )

  val defaultProcessingTimes: Map[PaxTypeAndQueue, Double] = Map(
    b5jsskToDesk -> (62d / 60),
    b5jsskChildToDesk -> (62d / 60),
    eeaChildToDesk -> 35d / 60,
    eeaMachineReadableToDesk -> 35d / 60,
    eeaNonMachineReadableToDesk -> 35d / 60,
    gbrNationalToDesk -> 25d / 60,
    gbrNationalChildToDesk -> 25d / 60,
    visaNationalToDesk -> 122d / 60,
    nonVisaNationalToDesk -> 109d / 60,
    gbrNationalToEgate -> 46d / 60,
    eeaMachineReadableToEGate -> 46d / 60,
    b5jsskToEGate -> (46d / 60),
    nonVisaNationalToEGate -> (44d / 60),
    visaNationalToEGate -> (43d / 60),
  )

  val fallbackProcessingTime: Double = defaultProcessingTimes.values.sum / defaultProcessingTimes.size
}
