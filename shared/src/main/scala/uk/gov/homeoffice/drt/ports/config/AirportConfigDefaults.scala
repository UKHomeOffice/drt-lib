package uk.gov.homeoffice.drt.ports.config

import uk.gov.homeoffice.drt.ports.PaxTypes._
import uk.gov.homeoffice.drt.ports.PaxTypesAndQueues._
import uk.gov.homeoffice.drt.ports.Queues.{ EGate, EeaDesk, NonEeaDesk, Queue }
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.{ SplitRatio, SplitRatios, SplitSources }
import uk.gov.homeoffice.drt.ports.{ PaxTypes, _ }

object AirportConfigDefaults {
  val defaultSlas: Map[Queue, Int] = Map(
    EeaDesk -> 20,
    EGate -> 25,
    NonEeaDesk -> 45
  )

  val defaultPaxSplits: SplitRatios = SplitRatios(
    SplitSources.TerminalAverage,
    SplitRatio(eeaMachineReadableToDesk, 0.175),
    SplitRatio(eeaMachineReadableToEGate, 0.55),
    SplitRatio(eeaNonMachineReadableToDesk, 0.175),
    SplitRatio(visaNationalToDesk, 0.05),
    SplitRatio(nonVisaNationalToDesk, 0.05)
  )

  val defaultPaxSplitsWithoutEgates: SplitRatios = SplitRatios(
    SplitSources.TerminalAverage,
    SplitRatio(eeaMachineReadableToDesk, 0.725),
    SplitRatio(eeaNonMachineReadableToDesk, 0.175),
    SplitRatio(visaNationalToDesk, 0.05),
    SplitRatio(nonVisaNationalToDesk, 0.05)
  )

  val defaultQueueRatios: Map[PaxType, Seq[(Queue, Double)]] = Map(
    GBRNational -> List(EGate -> 0.8, EeaDesk -> 0.2),
    GBRNationalBelowEgateAge -> List(EeaDesk -> 1.0),
    EeaMachineReadable -> List(EGate -> 0.8, EeaDesk -> 0.2),
    EeaBelowEGateAge -> List(EeaDesk -> 1.0),
    EeaNonMachineReadable -> List(EeaDesk -> 1.0),
    NonVisaNational -> List(NonEeaDesk -> 1.0),
    VisaNational -> List(NonEeaDesk -> 1.0),
    B5JPlusNational -> List(EGate -> 0.7, EeaDesk -> 0.3),
    B5JPlusNationalBelowEGateAge -> List(EeaDesk -> 1),
    PaxTypes.Transit -> List()
  )

  val defaultQueueRatiosWithoutEgates: Map[PaxType, Seq[(Queue, Double)]] = defaultQueueRatios ++ Map(
    GBRNational -> List(EeaDesk -> 1.0),
    EeaMachineReadable -> List(EeaDesk -> 1.0),
    B5JPlusNational -> List(EeaDesk -> 1.0)
  )

  final case class ProcessingTimesInSeconds(
      gbr: Double,
      eea: Double,
      b5jssk: Double,
      nvn: Double,
      vn: Double,
      egates: Double
  )

  private implicit class SecondsToMinutes(private val seconds: Double) extends AnyVal {
    def toMinutes: Double = seconds / 60
  }

  def standardProcessingTimes(times: ProcessingTimesInSeconds): Map[PaxTypeAndQueue, Double] = Map(
    b5jsskToDesk -> times.b5jssk.toMinutes,
    b5jsskChildToDesk -> times.b5jssk.toMinutes,
    eeaChildToDesk -> times.eea.toMinutes,
    eeaMachineReadableToDesk -> times.eea.toMinutes,
    eeaNonMachineReadableToDesk -> times.eea.toMinutes,
    gbrNationalToDesk -> times.gbr.toMinutes,
    gbrNationalChildToDesk -> times.gbr.toMinutes,
    b5jsskToEGate -> times.egates.toMinutes,
    eeaMachineReadableToEGate -> times.egates.toMinutes,
    gbrNationalToEgate -> times.egates.toMinutes,
    visaNationalToDesk -> times.vn.toMinutes,
    nonVisaNationalToDesk -> times.nvn.toMinutes
  )

  val defaultProcessingTimes: Map[PaxTypeAndQueue, Double] = standardProcessingTimes(
    ProcessingTimesInSeconds(
      gbr = 22.0,
      eea = 26.0,
      b5jssk = 44.0,
      nvn = 91.0,
      vn = 89.0,
      egates = 36.0
    )
  )

  val fallbackProcessingTime: Double = defaultProcessingTimes.values.sum / defaultProcessingTimes.size
}
