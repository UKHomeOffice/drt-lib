package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.arrivals.{Predictions => Preds}
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports.{FeedSource, LiveFeedSource, PortCode}
import upickle.default.{ReadWriter, macroRW}



object LiveArrival {
  implicit val arrivalStatusRw: ReadWriter[ArrivalStatus] = macroRW
  implicit val voyageNumberRw: ReadWriter[VoyageNumber] = macroRW
  implicit val arrivalSuffixRw: ReadWriter[FlightCodeSuffix] = macroRW
  implicit val operatorRw: ReadWriter[Operator] = macroRW
  implicit val portCodeRw: ReadWriter[PortCode] = macroRW
  implicit val rw: ReadWriter[LiveArrival] = macroRW

  def apply(arrival: Arrival): LiveArrival = arrival match {
    case a: LiveArrival => a
    case other => throw new IllegalArgumentException(s"Cannot convert ${other.getClass} to a LiveArrival")
  }
}

case class LiveArrival(operator: Option[Operator],
                       carrierCode: CarrierCode,
                       voyageNumber: VoyageNumber,
                       maybeFlightCodeSuffix: Option[FlightCodeSuffix],
                       status: ArrivalStatus,
                       estimated: Option[Long],
                       actual: Option[Long],
                       estimatedChox: Option[Long],
                       actualChox: Option[Long],
                       gate: Option[String],
                       stand: Option[String],
                       totalPax: Option[Int],
                       transPax: Option[Int],
                       maxPax: Option[Int],
                       runwayID: Option[String],
                       baggageReclaimId: Option[String],
                       terminal: Terminal,
                       origin: PortCode,
                       scheduled: Long,
                       scheduledDeparture: Option[Long],
                       feedSource: FeedSource,
                      )
  extends Arrival {
  override def Operator: Option[Operator] = operator
  override def CarrierCode: CarrierCode = carrierCode
  override def VoyageNumber: VoyageNumber = voyageNumber
  override def FlightCodeSuffix: Option[FlightCodeSuffix] = maybeFlightCodeSuffix
  override def Status: ArrivalStatus = status
  override def Estimated: Option[Long] = estimated
  override def Actual: Option[Long] = actual
  override def Predictions: Predictions = Preds.empty
  override def EstimatedChox: Option[Long] = estimatedChox
  override def ActualChox: Option[Long] = actualChox
  override def Gate: Option[String] = gate
  override def Stand: Option[String] = stand
  override def MaxPax: Option[Int] = maxPax
  override def RunwayID: Option[String] = runwayID
  override def BaggageReclaimId: Option[String] = baggageReclaimId
  override def Terminal: Terminal = terminal
  override def Origin: PortCode = origin
  override def Scheduled: Long = scheduled
  override def PcpTime: Option[Long] = None
  override def FeedSources: Set[FeedSource] = Set(feedSource)
  override def CarrierScheduled: Option[Long] = None
  override def ScheduledDeparture: Option[Long] = None
  override def RedListPax: Option[Int] = None
  override def PassengerSources: Map[FeedSource, Passengers] = Map(feedSource -> Passengers(totalPax, transPax))
  override def withoutPcpTime: Arrival = this

  def toMergedArrival: MergedArrival = MergedArrival(
    Operator = Operator,
    CarrierCode = CarrierCode,
    VoyageNumber = VoyageNumber,
    FlightCodeSuffix = FlightCodeSuffix,
    Status = Status,
    Estimated = Estimated,
    Predictions = Predictions,
    Actual = Actual,
    EstimatedChox = EstimatedChox,
    ActualChox = ActualChox,
    Gate = Gate,
    Stand = Stand,
    MaxPax = MaxPax,
    RunwayID = RunwayID,
    BaggageReclaimId = BaggageReclaimId,
    Terminal = Terminal,
    Origin = Origin,
    Scheduled = Scheduled,
    PcpTime = PcpTime,
    FeedSources = FeedSources,
    CarrierScheduled = CarrierScheduled,
    ScheduledDeparture = ScheduledDeparture,
    RedListPax = RedListPax,
    PassengerSources = PassengerSources,
  )

}
