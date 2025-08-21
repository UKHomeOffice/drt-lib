package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LTN
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.LocalDate

import scala.collection.immutable.{List, SortedMap}

object Ltn extends AirportConfigLike {

  import AirportConfigDefaults._

  private object ProcTimes {
    val gbr = 31.0
    val eea = 40.0
    val b5jssk = 55.0
    val nvn = 64.0
    val vn = 78.0
    val egates = 47d
  }

  private val egateUptakePct = 0.8559

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LTN"),
    portName = "Luton",
    queuesByTerminal = SortedMap(LocalDate(2014, 1, 1) -> SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk)
    )),
    slaByQueue = defaultSlas,
    defaultWalkTimeMillis = Map(T1 -> 300000L),
    terminalPaxSplits = Map(T1 -> defaultPaxSplits),
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
      nonVisaNationalToDesk -> ProcTimes.nvn / 60,
    )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.EGate -> (List.fill(24)(1), List.fill(24)(2)),
        Queues.EeaDesk -> (List.fill(24)(1), List(9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9)),
        Queues.NonEeaDesk -> (List.fill(24)(1), List(5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5))
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable(10, 5)),
    hasEstChox = false,
    role = LTN,
    assumedAdultsPerChild = 1.52,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (defaultQueueRatios ++ Map(
        GBRNational -> List(EGate -> egateUptakePct, EeaDesk -> (1 - egateUptakePct)),
        EeaMachineReadable -> List(EGate -> egateUptakePct, EeaDesk -> (1 - egateUptakePct)),
        B5JPlusNational -> List(EGate -> egateUptakePct, EeaDesk -> (1 - egateUptakePct)),
      ))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 14),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource),
  )
}
