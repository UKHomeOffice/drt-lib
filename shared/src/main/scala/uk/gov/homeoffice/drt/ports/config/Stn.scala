package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.STN
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap


object Stn extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("STN"),
    queuesByTerminal = SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk)
    ),
    slaByQueue = Map(EeaDesk -> 25, EGate -> 5, NonEeaDesk -> 45),
    crunchOffsetMinutes = 240,
    defaultWalkTimeMillis = Map(T1 -> 600000L),
    terminalPaxSplits = Map(T1 -> SplitRatios(
      SplitSources.TerminalAverage,
      SplitRatio(eeaMachineReadableToDesk, 0.13),
      SplitRatio(eeaMachineReadableToEGate, 0.8),
      SplitRatio(eeaNonMachineReadableToDesk, 0.05),
      SplitRatio(visaNationalToDesk, 0.01),
      SplitRatio(nonVisaNationalToDesk, 0.01)
    )),
    terminalProcessingTimes = Map(T1 -> Map(
      b5jsskToDesk -> 64d / 60,
      b5jsskChildToDesk -> 64d / 60,
      eeaMachineReadableToDesk -> 40d / 60,
      eeaNonMachineReadableToDesk -> 40d / 60,
      eeaChildToDesk -> 40d / 60,
      gbrNationalToDesk -> 33d / 60,
      gbrNationalChildToDesk -> 33d / 60,
      b5jsskToEGate -> 41d / 60,
      eeaMachineReadableToEGate -> 42d / 60,
      gbrNationalToEgate -> 41d / 60,
      visaNationalToDesk -> 101d / 60,
      nonVisaNationalToDesk -> 94d / 60,
      visaNationalToEGate -> 51d / 60,
      nonVisaNationalToEGate -> 51d / 60,
    )),
    minMaxDesksByTerminalQueue24Hrs = Map(
      T1 -> Map(
        Queues.EGate -> (List(1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(3, 3, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3)),
        Queues.EeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13, 13)),
        Queues.NonEeaDesk -> (List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), List(8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8))
      )
    ),
    eGateBankSizes = Map(T1 -> Iterable(10, 10, 10)),
    fixedPointExamples = Seq("Roving Officer, 00:00, 23:59, 1",
      "Referral Officer, 00:00, 23:59, 1",
      "Forgery Officer, 00:00, 23:59, 1"),
    role = STN,
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.69, Queues.EeaDesk -> 0.31),
        EeaMachineReadable -> List(Queues.EGate -> 0.75, Queues.EeaDesk -> 0.25),
        B5JPlusNational -> List(Queues.EGate -> 0.74, Queues.EeaDesk -> 0.26),
        NonVisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
        VisaNational -> List(Queues.EGate -> 0.03, Queues.NonEeaDesk -> 0.97),
      ))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 22),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, ForecastFeedSource, AclFeedSource)
  )
}
