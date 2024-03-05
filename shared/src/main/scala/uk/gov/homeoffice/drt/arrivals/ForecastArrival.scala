package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.{FeedSource, ForecastFeedSource, PortCode}
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.arrivals.{Predictions => Preds}
import upickle.default.{ReadWriter, macroRW}

object ForecastArrival {
  implicit val voyageNumberRw: ReadWriter[VoyageNumber] = macroRW
  implicit val arrivalSuffixRw: ReadWriter[FlightCodeSuffix] = macroRW
  implicit val portCodeRw: ReadWriter[PortCode] = macroRW
  implicit val rw: ReadWriter[ForecastArrival] = macroRW

  def apply(arrival: Arrival): ForecastArrival = arrival match {
    case a: ForecastArrival => a
    case other => throw new IllegalArgumentException(s"Cannot convert ${other.getClass} to a ForecastArrival")
  }
}

case class ForecastArrival(carrierCode: CarrierCode,
                           voyageNumber: VoyageNumber,
                           maybeFlightCodeSuffix: Option[FlightCodeSuffix],
                           origin: PortCode,
                           terminal: Terminal,
                           scheduled: Long,
                           totalPax: Option[Int],
                           transPax: Option[Int],
                           maxPax: Option[Int],
                          ) extends Arrival {
  override def Operator: Option[Operator] = None
  override def CarrierCode: CarrierCode = carrierCode
  override def VoyageNumber: VoyageNumber = voyageNumber
  override def FlightCodeSuffix: Option[FlightCodeSuffix] = maybeFlightCodeSuffix
  override def Status: ArrivalStatus = ArrivalStatus("Scheduled")
  override def Estimated: Option[Long] = None
  override def Actual: Option[Long] = None
  override def Predictions: Predictions = Preds.empty
  override def EstimatedChox: Option[Long] = None
  override def ActualChox: Option[Long] = None
  override def Gate: Option[String] = None
  override def Stand: Option[String] = None
  override def MaxPax: Option[Int] = maxPax
  override def RunwayID: Option[String] = None
  override def BaggageReclaimId: Option[String] = None
  override def Terminal: Terminal = terminal
  override def Origin: PortCode = origin
  override def Scheduled: Long = scheduled
  override def PcpTime: Option[Long] = None
  override def FeedSources: Set[FeedSource] = Set(ForecastFeedSource)
  override def CarrierScheduled: Option[Long] = None
  override def ScheduledDeparture: Option[Long] = None
  override def RedListPax: Option[Int] = None
  override def PassengerSources: Map[FeedSource, Passengers] = Map(ForecastFeedSource -> Passengers(totalPax, transPax))
  override def withoutPcpTime: Arrival = this

  def toArrival: MergedArrival = MergedArrival(
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
