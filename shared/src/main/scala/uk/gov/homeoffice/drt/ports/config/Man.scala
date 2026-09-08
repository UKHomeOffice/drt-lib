package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.MAN
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{ SplitRatio, SplitRatios, SplitSources }
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap

object Man extends AirportConfigLike {

  import AirportConfigDefaults._

  private val t1ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 27.0,
    eea = 39.0,
    b5jssk = 50.0,
    nvn = 77.0,
    vn = 83.0,
    egates = 44.0
  )

  private val t2ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 28.0,
    eea = 41.0,
    b5jssk = 58.0,
    nvn = 89.0,
    vn = 86.0,
    egates = 51.0
  )

  private val t3ProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 29.0,
    eea = 38.0,
    b5jssk = 56.0,
    nvn = 87.0,
    vn = 82.0,
    egates = 44.0
  )

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("MAN"),
    portName = "Manchester",
    queuesByTerminal = SortedMap(LocalDate(2014, 1, 1) -> SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk),
      T2 -> Seq(EeaDesk, EGate, NonEeaDesk),
      T3 -> Seq(EeaDesk, EGate, NonEeaDesk)
    )),
    slaByQueue = Map(EeaDesk -> 25, EGate -> 10, NonEeaDesk -> 45),
    defaultWalkTimeMillis = Map(T1 -> 180000L, T2 -> 600000L, T3 -> 180000L),
    terminalPaxSplits = List(T1, T2, T3).map(t =>
      (
        t,
        SplitRatios(
          SplitSources.TerminalAverage,
          SplitRatio(eeaMachineReadableToDesk, 0.08335),
          SplitRatio(eeaMachineReadableToEGate, 0.7333),
          SplitRatio(eeaNonMachineReadableToDesk, 0.08335),
          SplitRatio(visaNationalToDesk, 0.05),
          SplitRatio(nonVisaNationalToDesk, 0.05)
        )
      )
    ).toMap,
    terminalProcessingTimes = Map(
      T1 -> standardProcessingTimes(t1ProcessingTimesInSeconds),
      T2 -> standardProcessingTimes(t2ProcessingTimesInSeconds),
      T3 -> standardProcessingTimes(t3ProcessingTimesInSeconds)
    ),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
          ),
        Queues.EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)
          ),
        Queues.NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(5, 5, 5, 5, 5, 5, 7, 7, 7, 7, 5, 6, 6, 6, 6, 6, 5, 5, 5, 6, 5, 5, 5, 5)
          )
      ),
      T2 -> Map(
        Queues.EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        Queues.EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(8, 8, 8, 8, 8, 5, 5, 5, 5, 5, 5, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8)
          ),
        Queues.NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(3, 3, 3, 3, 3, 8, 8, 8, 8, 8, 8, 3, 3, 3, 3, 3, 6, 6, 6, 6, 3, 3, 3, 3)
          )
      ),
      T3 -> Map(
        Queues.EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        Queues.EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)
          ),
        Queues.NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
          )
      )
    ),
    eGateBankSizes = Map(
      T1 -> Iterable(10),
      T2 -> Iterable(10),
      T3 -> Iterable(10)
    ),
    role = MAN,
    terminalPaxTypeQueueAllocation = Map(
      T1 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.7968,
            EeaDesk -> (1.0 - 0.7968)
          ))),
      T2 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.7140,
            EeaDesk -> (1.0 - 0.7140)
          ))),
      T3 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.7038,
            EeaDesk -> (1.0 - 0.7038)
          )))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](
      T1 -> 14,
      T2 -> 32,
      T3 -> 9
    ),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource)
  )
}
