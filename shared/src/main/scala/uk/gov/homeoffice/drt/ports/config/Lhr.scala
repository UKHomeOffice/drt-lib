package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LHR
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{ SplitRatio, SplitRatios, SplitSources }
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap

object Lhr extends AirportConfigLike {
  import AirportConfigDefaults._

  private val lhrDefaultQueueRatios: Map[PaxType, Seq[(Queue, Double)]] = Map(
    GBRNational -> List(Queues.EGate -> 0.80, Queues.EeaDesk -> 0.20),
    GBRNationalBelowEgateAge -> List(Queues.EeaDesk -> 1.0),
    EeaMachineReadable -> List(Queues.EGate -> 0.80, Queues.EeaDesk -> 0.20),
    EeaBelowEGateAge -> List(Queues.EeaDesk -> 1.0),
    EeaNonMachineReadable -> List(Queues.EeaDesk -> 1.0),
    Transit -> List(Queues.Transfer -> 1.0),
    NonVisaNational -> List(Queues.NonEeaDesk -> 1.0),
    VisaNational -> List(Queues.NonEeaDesk -> 1.0),
    B5JPlusNational -> List(Queues.EGate -> 0.70, Queues.EeaDesk -> 0.30),
    B5JPlusNationalBelowEGateAge -> List(Queues.EeaDesk -> 1)
  )

  private val t2ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 35.0,
    eea = 48.0,
    b5jssk = 61.0,
    nvn = 87.0,
    vn = 88.0,
    egates = 44.0
  )

  private val t3ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 32.0,
    eea = 42.0,
    b5jssk = 53.0,
    nvn = 79.0,
    vn = 85.0,
    egates = 44.0
  )

  private val t4ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 35.0,
    eea = 46.0,
    b5jssk = 58.0,
    nvn = 75.0,
    vn = 86.0,
    egates = 44.0
  )

  private val t5ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 32.0,
    eea = 42.0,
    b5jssk = 56.0,
    nvn = 84.0,
    vn = 100.0,
    egates = 47.0
  )

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LHR"),
    portName = "London Heathrow",
    queuesByTerminal = SortedMap(LocalDate(2020, 8, 28) -> SortedMap(
      T2 -> Seq(EeaDesk, EGate, NonEeaDesk, Transfer),
      T3 -> Seq(EeaDesk, EGate, NonEeaDesk, Transfer),
      T4 -> Seq(EeaDesk, EGate, NonEeaDesk, Transfer),
      T5 -> Seq(EeaDesk, EGate, NonEeaDesk, Transfer)
    )),
    slaByQueue = Map(EeaDesk -> 25, EGate -> 15, NonEeaDesk -> 45),
    crunchOffsetMinutes = 120,
    defaultWalkTimeMillis = Map(T2 -> 900000L, T3 -> 660000L, T4 -> 900000L, T5 -> 660000L),
    terminalPaxSplits = List(T2, T3, T4, T5).map(t =>
      (
        t,
        SplitRatios(
          SplitSources.TerminalAverage,
          SplitRatio(eeaMachineReadableToDesk, 0.64 * 0.2),
          SplitRatio(eeaMachineReadableToEGate, 0.64 * 0.8),
          SplitRatio(eeaNonMachineReadableToDesk, 0),
          SplitRatio(visaNationalToDesk, 0.08),
          SplitRatio(nonVisaNationalToDesk, 0.28)
        )
      )
    ).toMap,
    terminalProcessingTimes = Map(
      T2 -> (standardProcessingTimes(t2ProcessingTimesInSeconds) + (transitToTransfer -> 50d / 60)),
      T3 -> (standardProcessingTimes(t3ProcessingTimesInSeconds) + (transitToTransfer -> 50d / 60)),
      T4 -> (standardProcessingTimes(t4ProcessingTimesInSeconds) + (transitToTransfer -> 50d / 60)),
      T5 -> (standardProcessingTimes(t5ProcessingTimesInSeconds) + (transitToTransfer -> 50d / 60))
    ),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T2 -> Map(
        Queues.EGate ->
          (
            List(0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            List(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
          ),
        Queues.EeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9)
          ),
        Queues.NonEeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20)
          )
      ),
      T3 -> Map(
        Queues.EGate ->
          (
            List(0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            List(1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
          ),
        Queues.EeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16)
          ),
        Queues.NonEeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23)
          )
      ),
      T4 -> Map(
        Queues.EGate ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        Queues.EeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8)
          ),
        Queues.NonEeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27)
          )
      ),
      T5 -> Map(
        Queues.EGate ->
          (
            List(0, 0, 0, 0, 0, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
            List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
          ),
        Queues.EeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)
          ),
        Queues.NonEeaDesk ->
          (
            List(0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20)
          )
      )
    ),
    eGateBankSizes = Map(
      T2 -> Iterable(10, 5),
      T3 -> Iterable(10, 5),
      T4 -> Iterable(10),
      T5 -> Iterable(10, 9, 5)
    ),
    role = LHR,
    terminalPaxTypeQueueAllocation = {
      val egateSplitT2 = 0.8102
      val egateSplitT3 = 0.8075
      val egateSplitT4 = 0.7687
      val egateSplitT5 = 0.8466
      Map(
        T2 ->
          (lhrDefaultQueueRatios +
            (EeaMachineReadable -> List(
              EGate -> egateSplitT2,
              EeaDesk -> (1.0 - egateSplitT2)
            ))),
        T3 ->
          (lhrDefaultQueueRatios +
            (EeaMachineReadable -> List(
              EGate -> egateSplitT3,
              EeaDesk -> (1.0 - egateSplitT3)
            ))),
        T4 ->
          (lhrDefaultQueueRatios +
            (EeaMachineReadable -> List(
              EGate -> egateSplitT4,
              EeaDesk -> (1.0 - egateSplitT4)
            ))),
        T5 ->
          (lhrDefaultQueueRatios +
            (EeaMachineReadable -> List(
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
      T2 -> 29,
      T3 -> 28,
      T4 -> 39,
      T5 -> 27
    )
  )
}
