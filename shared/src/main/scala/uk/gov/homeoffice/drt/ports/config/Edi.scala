package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.EDI
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap

object Edi extends AirportConfigLike {

  import AirportConfigDefaults._

  private val processingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 24.0,
    eea = 32.0,
    b5jssk = 44.0,
    nvn = 67.0,
    vn = 74.0,
    egates = 47.0
  )

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("EDI"),
    portName = "Edinburgh",
    queuesByTerminal = SortedMap(LocalDate(2014, 1, 1) -> SortedMap(
      A1 -> Seq(EeaDesk, EGate, NonEeaDesk),
      A2 -> Seq(EeaDesk, EGate, NonEeaDesk)
    )),
    slaByQueue = defaultSlas,
    defaultWalkTimeMillis = Map(A1 -> 180000L, A2 -> 120000L),
    terminalPaxSplits = List(A1, A2).map(t => (t, defaultPaxSplits)).toMap,
    terminalProcessingTimes = Map(
      A1 -> standardProcessingTimes(processingTimesInSeconds),
      A2 -> standardProcessingTimes(processingTimesInSeconds)
    ),
    minMaxDesksByTerminalQueue24Hrs = Map(
      A1 -> Map(
        Queues.EGate ->
          (
            List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        Queues.EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9)
          ),
        Queues.NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(2, 2, 2, 2, 2, 2, 6, 6, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)
          )
      ),
      A2 -> Map(
        Queues.EGate ->
          (
            List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
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
    eGateBankSizes = Map(A1 -> Iterable(5), A2 -> Iterable(10)),
    role = EDI,
    terminalPaxTypeQueueAllocation = Map(
      A1 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.8140,
            EeaDesk -> (1.0 - 0.8140)
          ))),
      A2 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.7894,
            EeaDesk -> (1.0 - 0.7894)
          )))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](A1 -> 11, A2 -> 9),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource),
    feedSourceMonitorExemptions = Seq(LiveFeedSource),
    hasEstChox = false
  )
}
