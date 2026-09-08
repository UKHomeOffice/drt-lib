package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LGW
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{ SplitRatio, SplitRatios, SplitSources }
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap

object Lgw extends AirportConfigLike {

  import AirportConfigDefaults._

  private val northProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 28.0,
    eea = 37.0,
    b5jssk = 48.0,
    nvn = 86.0,
    vn = 90.0,
    egates = 47.0
  )

  private val southProcessingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 29.0,
    eea = 39.0,
    b5jssk = 51.0,
    nvn = 88.0,
    vn = 95.0,
    egates = 47.0
  )

  private val egateUptake = 0.89

  private val queueRatios: Map[PaxType, Seq[(Queue, Double)]] = defaultQueueRatios ++ Map(
    EeaMachineReadable -> List(EGate -> egateUptake, EeaDesk -> (1.0 - egateUptake)),
    GBRNational -> List(EGate -> egateUptake, EeaDesk -> (1.0 - egateUptake)),
    B5JPlusNational -> List(EGate -> egateUptake, EeaDesk -> (1.0 - egateUptake))
  )

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LGW"),
    portName = "London Gatwick",
    queuesByTerminal = SortedMap(LocalDate(2014, 1, 1) -> SortedMap(
      N -> Seq(EeaDesk, EGate, NonEeaDesk),
      S -> Seq(EeaDesk, EGate, NonEeaDesk)
    )),
    slaByQueue = Map(
      EeaDesk -> 25,
      EGate -> 10,
      NonEeaDesk -> 45
    ),
    defaultWalkTimeMillis = Map(N -> 180000L, S -> 180000L),
    terminalPaxSplits = List(N, S).map(t =>
      (
        t,
        SplitRatios(
          SplitSources.TerminalAverage,
          SplitRatio(eeaMachineReadableToDesk, 0.85 * 0.17),
          SplitRatio(eeaMachineReadableToEGate, 0.85 * 0.83),
          SplitRatio(eeaNonMachineReadableToDesk, 0d),
          SplitRatio(visaNationalToDesk, 0.06),
          SplitRatio(nonVisaNationalToDesk, 0.09)
        )
      )
    ).toMap,
    terminalProcessingTimes = Map(
      N -> standardProcessingTimes(northProcessingTimesInSeconds),
      S -> standardProcessingTimes(southProcessingTimesInSeconds)
    ),
    minMaxDesksByTerminalQueue24Hrs = Map(
      N -> Map(
        Queues.EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
          ),
        Queues.EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13)
          ),
        Queues.NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15, 15)
          )
      ),
      S -> Map(
        Queues.EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
          ),
        Queues.EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(15, 15, 15, 15, 15, 15, 13, 10, 10, 10, 10, 10, 10, 10, 10, 13, 13, 13, 13, 13, 13, 13, 13, 13)
          ),
        Queues.NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(10, 10, 10, 10, 10, 10, 12, 15, 15, 15, 15, 15, 15, 15, 15, 13, 13, 13, 13, 13, 13, 13, 13, 13)
          )
      )
    ),
    eGateBankSizes = Map(
      N -> Iterable(10, 10, 5),
      S -> Iterable(10, 10, 5)
    ),
    role = LGW,
    terminalPaxTypeQueueAllocation = Map(
      N -> queueRatios,
      S -> queueRatios
    ),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, ForecastFeedSource, AclFeedSource),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](N -> 31, S -> 28)
  )
}
