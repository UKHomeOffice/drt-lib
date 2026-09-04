package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.HUY
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.SortedMap
import scala.concurrent.duration.DurationInt

object Huy extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("HUY"),
    portName = "Humberside",
    queuesByTerminal = SortedMap(
      LocalDate(2014, 1, 1) -> SortedMap(
        T1 -> Seq(EeaDesk, NonEeaDesk)
      ),
      LocalDate(2026, 9, 4) -> SortedMap(
        T1 -> Seq(QueueDesk)
      )
    ),
    divertedQueues = Map(
      EeaDesk -> QueueDesk,
      NonEeaDesk -> QueueDesk
    ),
    slaByQueue = Map(
      EeaDesk -> 25,
      NonEeaDesk -> 45,
      QueueDesk -> 25
    ),
    defaultWalkTimeMillis = Map(T1 -> 5.minutes.toMillis),
    terminalPaxSplits = Map(T1 -> defaultPaxSplitsWithoutEgates),
    terminalProcessingTimes = Map(T1 -> defaultProcessingTimes),
    minMaxDesksByTerminalQueue24Hrs = Map(T1 -> Map(
      EeaDesk ->
        (
          List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
        ),
      NonEeaDesk ->
        (
          List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
        ),
      QueueDesk ->
        (
          List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)
        )
    )),
    eGateBankSizes = Map(),
    role = HUY,
    terminalPaxTypeQueueAllocation = Map(T1 -> defaultQueueRatiosWithoutEgates),
    feedSources = Seq(ApiFeedSource, LiveFeedSource),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 2)
  )
}
