package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.BHX
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{ SplitRatio, SplitRatios, SplitSources }
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap

object Bhx extends AirportConfigLike {

  import AirportConfigDefaults._

  private val processingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 24.0,
    eea = 36.0,
    b5jssk = 52.0,
    nvn = 91.0,
    vn = 85.0,
    egates = 48.0
  )

  private val egateUtilisation = 0.7968
  private val nonEgateUtilisation: Double = 1.0 - egateUtilisation

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("BHX"),
    portName = "Birmingham",
    queuesByTerminal = SortedMap(
      LocalDate(2014, 1, 1) -> SortedMap(
        T1 -> Seq(EeaDesk, EGate, NonEeaDesk),
        T2 -> Seq(EeaDesk, NonEeaDesk)
      ),
      LocalDate(2025, 8, 4) -> SortedMap(
        T1 -> Seq(EeaDesk, EGate, NonEeaDesk),
        T2 -> Seq(QueueDesk)
      ),
      LocalDate(2026, 5, 26) -> SortedMap(
        T1 -> Seq(EeaDesk, EGate, NonEeaDesk),
        T2 -> Seq(QueueDesk, EGate)
      )
    ),
    slaByQueue = defaultSlas ++ Map(
      QueueDesk -> 60
    ),
    defaultWalkTimeMillis = Map(T1 -> 240000L, T2 -> 240000L),
    terminalPaxSplits = Map(
      T1 -> SplitRatios(
        SplitSources.TerminalAverage,
        SplitRatio(eeaMachineReadableToDesk, 0.92 * 0.2446),
        SplitRatio(eeaMachineReadableToEGate, 0.92 * 0.7554),
        SplitRatio(eeaNonMachineReadableToDesk, 0),
        SplitRatio(visaNationalToDesk, 0.04),
        SplitRatio(nonVisaNationalToDesk, 0.04)
      ),
      T2 -> SplitRatios(
        SplitSources.TerminalAverage,
        SplitRatio(eeaMachineReadableToDesk, 0.92 * 0.2446),
        SplitRatio(eeaMachineReadableToEGate, 0.92 * 0.7554),
        SplitRatio(eeaNonMachineReadableToDesk, 0),
        SplitRatio(visaNationalToDesk, 0.04),
        SplitRatio(nonVisaNationalToDesk, 0.04)
      )
    ),
    terminalProcessingTimes = Map(
      T1 -> standardProcessingTimes(processingTimesInSeconds),
      T2 -> standardProcessingTimes(processingTimesInSeconds)
    ),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
          ),
        EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)
          ),
        NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8)
          )
      ),
      T2 -> Map(
        EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)
          ),
        NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        QueueDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7)
          )
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable(10, 5), T2 -> Iterable(5)),
    hasEstChox = false,
    role = BHX,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (
        defaultQueueRatios +
          (EeaMachineReadable -> List(EGate -> egateUtilisation, EeaDesk -> nonEgateUtilisation))
      ),
      T2 -> (
        defaultQueueRatios +
          (EeaMachineReadable -> List(EGate -> egateUtilisation, EeaDesk -> nonEgateUtilisation))
      )
    ),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, ForecastFeedSource, AclFeedSource),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](T1 -> 9, T2 -> 7)
  )
}
