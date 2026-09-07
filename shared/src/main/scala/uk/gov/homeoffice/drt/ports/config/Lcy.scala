package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LCY
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{ SplitRatio, SplitRatios, SplitSources }
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate
import uk.gov.homeoffice.drt.time.MilliTimes.oneMinuteMillis

import scala.collection.immutable.SortedMap

object Lcy extends AirportConfigLike {

  import AirportConfigDefaults._

  private object ProcTimes {
    val gbr = 21.0
    val eea = 25.0
    val b5jssk = 45.0
    val nvn = 88.0
    val vn = 85.0
    val egates = 48d
  }

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LCY"),
    portName = "London City",
    queuesByTerminal = SortedMap(LocalDate(2014, 1, 1) -> SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk)
    )),
    slaByQueue = Map(
      EeaDesk -> 25,
      NonEeaDesk -> 45,
      EGate -> 25
    ),
    defaultWalkTimeMillis = Map(T1 -> 2 * oneMinuteMillis),
    terminalPaxSplits = Map(T1 -> SplitRatios(
      SplitSources.TerminalAverage,
      SplitRatio(eeaMachineReadableToDesk, 0.99 * 0.2),
      SplitRatio(eeaMachineReadableToEGate, 0.99 * 0.8),
      SplitRatio(eeaNonMachineReadableToDesk, 0),
      SplitRatio(visaNationalToDesk, 0.0),
      SplitRatio(nonVisaNationalToDesk, 0.01)
    )),
    terminalProcessingTimes = Map(T1 -> Map(
      b5jsskToDesk -> ProcTimes.b5jssk / 60,
      b5jsskChildToDesk -> ProcTimes.b5jssk / 60,
      eeaMachineReadableToDesk -> ProcTimes.eea / 60,
      eeaNonMachineReadableToDesk -> ProcTimes.eea / 60,
      eeaChildToDesk -> ProcTimes.eea / 60,
      gbrNationalToDesk -> ProcTimes.gbr / 60,
      gbrNationalChildToDesk -> ProcTimes.gbr / 60,
      b5jsskToEGate -> ProcTimes.egates / 60,
      eeaMachineReadableToEGate -> ProcTimes.egates / 60,
      gbrNationalToEgate -> ProcTimes.egates / 60,
      visaNationalToDesk -> ProcTimes.vn / 60,
      nonVisaNationalToDesk -> ProcTimes.nvn / 60
    )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        EGate ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
          ),
        EeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5)
          ),
        NonEeaDesk ->
          (
            List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
            List(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)
          )
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable(10)),
    role = LCY,
    terminalPaxTypeQueueAllocation = Map(
      T1 ->
        (defaultQueueRatios +
          (EeaMachineReadable -> List(
            EGate -> 0.6993,
            EeaDesk -> (1.0 - 0.6993)
          )))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 9),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource)
  )
}
