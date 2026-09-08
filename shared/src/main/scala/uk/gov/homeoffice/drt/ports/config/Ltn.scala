package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LTN
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap

object Ltn extends AirportConfigLike {

  import AirportConfigDefaults._

  private val processingTimesInSeconds = ProcessingTimesInSeconds(
    gbr = 30.0,
    eea = 38.0,
    b5jssk = 54.0,
    nvn = 63.0,
    vn = 70.0,
    egates = 47.0
  )

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LTN"),
    portName = "Luton",
    queuesByTerminal = SortedMap(LocalDate(2014, 1, 1) -> SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk)
    )),
    slaByQueue = defaultSlas,
    defaultWalkTimeMillis = Map(T1 -> 300000L),
    terminalPaxSplits = Map(T1 -> defaultPaxSplits),
    terminalProcessingTimes = Map(T1 -> standardProcessingTimes(processingTimesInSeconds)),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.EGate -> (List.fill(24)(1), List.fill(24)(2)),
        Queues.EeaDesk ->
          (List.fill(24)(1), List(9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9)),
        Queues.NonEeaDesk ->
          (List.fill(24)(1), List(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5))
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable(10, 5)),
    hasEstChox = false,
    role = LTN,
    terminalPaxTypeQueueAllocation = Map(
      T1 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.7922,
            EeaDesk -> (1.0 - 0.7922)
          )))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 13),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource)
  )
}
