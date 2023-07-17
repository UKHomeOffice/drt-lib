package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.auth.Roles.LTN
import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues._
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{SplitRatio, SplitRatios, SplitSources}
import uk.gov.homeoffice.drt.ports.Terminals._
import uk.gov.homeoffice.drt.ports._

import scala.collection.immutable.SortedMap

object Ltn extends AirportConfigLike {

  import AirportConfigDefaults._

  val config: AirportConfig = AirportConfig(
    portCode = PortCode("LTN"),
    queuesByTerminal = SortedMap(
      T1 -> Seq(EeaDesk, EGate, NonEeaDesk)
    ),
    slaByQueue = defaultSlas,
    defaultWalkTimeMillis = Map(T1 -> 300000L),
    terminalPaxSplits = Map(T1 -> defaultPaxSplits),
    terminalProcessingTimes = Map(T1 -> Map(
      b5jsskToDesk -> 64d / 60,
      b5jsskChildToDesk -> 64d / 60,
      eeaMachineReadableToDesk -> 41d / 60,
      eeaNonMachineReadableToDesk -> 41d / 60,
      eeaChildToDesk -> 41d / 60,
      gbrNationalToDesk -> 33d / 60,
      gbrNationalChildToDesk -> 33d / 60,
      b5jsskToEGate -> 40d / 60,
      eeaMachineReadableToEGate -> 41d / 60,
      gbrNationalToEgate -> 40d / 60,
      visaNationalToDesk -> 101d / 60,
      nonVisaNationalToDesk -> 93d / 60,
      visaNationalToEGate -> 48d / 60,
      nonVisaNationalToEGate -> 49d / 60,
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
    terminalPaxTypeQueueAllocation = Map(
      T1 -> (defaultQueueRatios + (
        GBRNational -> List(Queues.EGate -> 0.69, Queues.EeaDesk -> 0.31),
        EeaMachineReadable -> List(Queues.EGate -> 0.71, Queues.EeaDesk -> 0.29),
        B5JPlusNational -> List(Queues.EGate -> 0.77, Queues.EeaDesk -> 0.23),
        NonVisaNational -> List(Queues.EGate -> 0.01, Queues.NonEeaDesk -> 0.99),
        VisaNational -> List(Queues.EGate -> 0.02, Queues.NonEeaDesk -> 0.98),
      ))
    ),
    flexedQueues = Set(EeaDesk, NonEeaDesk),
    desksByTerminal = Map(T1 -> 14),
    feedSources = Seq(ApiFeedSource, LiveBaseFeedSource, LiveFeedSource, AclFeedSource)
  )
}
