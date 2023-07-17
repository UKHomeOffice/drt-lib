package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.BHX
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Bhx extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("BHX"),
    queuesByTerminal = SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk),
      T2 -> Seq(EeaDesk, NonEeaDesk)
    ),
    slaByQueue = defaultSlas,
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
        SplitRatio(eeaMachineReadableToDesk, 0.92),
        SplitRatio(eeaNonMachineReadableToDesk, 0),
        SplitRatio(visaNationalToDesk, 0.04),
        SplitRatio(nonVisaNationalToDesk, 0.04)
      )),
    terminalProcessingTimes = Map(
      T1 -> Map(
        b5jsskToDesk -> 70d / 60,
        b5jsskChildToDesk -> 70d / 60,
        eeaMachineReadableToDesk -> 44d / 60,
        eeaNonMachineReadableToDesk -> 44d / 60,
        eeaChildToDesk -> 44d / 60,
        gbrNationalToDesk -> 31d / 60,
        gbrNationalChildToDesk -> 31d / 60,
        b5jsskToEGate -> 48d / 60,
        eeaMachineReadableToEGate -> 48d / 60,
        gbrNationalToEgate -> 48d / 60,
        visaNationalToDesk -> 104d / 60,
        nonVisaNationalToDesk -> 108d / 60
      ),
      T2 -> Map(
        b5jsskToDesk -> 70d / 60,
        b5jsskChildToDesk -> 70d / 60,
        eeaMachineReadableToDesk -> 44d / 60,
        eeaNonMachineReadableToDesk -> 44d / 60,
        eeaChildToDesk -> 44d / 60,
        gbrNationalToDesk -> 31d / 60,
        gbrNationalChildToDesk -> 31d / 60,
        b5jsskToEGate -> 48d / 60,
        eeaMachineReadableToEGate -> 48d / 60,
        gbrNationalToEgate -> 48d / 60,
        visaNationalToDesk -> 104d / 60,
        nonVisaNationalToDesk -> 108d / 60
      )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.EGate -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
          List(2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2)),
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
          List(6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          List(8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8))
      ),
      T2 -> Map(
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
          List(4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4)),
        Queues.NonEeaDesk -> (List(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
          List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1))
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable(10, 5)),
    hasEstChox = false,
    role = BHX,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (
        GBRNational -> List(Queues.EGate -> 0.65, Queues.EeaDesk -> 0.35),
        EeaMachineReadable -> List(Queues.EGate -> 0.72, Queues.EeaDesk -> 0.28),
        B5JPlusNational -> List(Queues.EGate -> 0.69, Queues.EeaDesk -> 0.31),
        NonVisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
        VisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
      ),
      T2 -> defaultQueueRatiosWithoutEgates
    ),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, ForecastFeedSource, AclFeedSource),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map[Terminal, Int](T1 -> 9, T2 -> 5)
  )
}
