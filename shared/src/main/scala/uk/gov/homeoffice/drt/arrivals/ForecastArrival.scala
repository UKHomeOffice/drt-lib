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

  def apply(arrival: ArrivalLike): ForecastArrival = arrival match {
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
                          ) extends ArrivalLike {
  def Operator: Option[Operator] = None
  def CarrierCode: CarrierCode = carrierCode
  def VoyageNumber: VoyageNumber = voyageNumber
  def FlightCodeSuffix: Option[FlightCodeSuffix] = maybeFlightCodeSuffix
  def Status: ArrivalStatus = ArrivalStatus("Scheduled")
  def Estimated: Option[Long] = None
  def Actual: Option[Long] = None
  def Predictions: Predictions = Preds.empty
  def EstimatedChox: Option[Long] = None
  def ActualChox: Option[Long] = None
  def Gate: Option[String] = None
  def Stand: Option[String] = None
  def MaxPax: Option[Int] = maxPax
  def RunwayID: Option[String] = None
  def BaggageReclaimId: Option[String] = None
  def AirportID: PortCode = origin
  def Terminal: Terminal = terminal
  def Origin: PortCode = origin
  def Scheduled: Long = scheduled
  def PcpTime: Option[Long] = None
  def FeedSources: Set[FeedSource] = Set(ForecastFeedSource)
  def CarrierScheduled: Option[Long] = None
  def ScheduledDeparture: Option[Long] = None
  def RedListPax: Option[Int] = None
  def PassengerSources: Map[FeedSource, Passengers] = Map(ForecastFeedSource -> Passengers(totalPax, transPax))

  def toArrival: Arrival = Arrival(
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
    AirportID = AirportID,
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

  override def withoutPcpTime: ArrivalLike = this

  override def update(incoming: ArrivalLike): ArrivalLike = incoming match {
    case a: ForecastArrival => a.toArrival
    case a: Arrival => a.copy(
      BaggageReclaimId = if (incoming.BaggageReclaimId.exists(_.nonEmpty)) incoming.BaggageReclaimId else this.BaggageReclaimId,
      Stand = if (incoming.Stand.exists(_.nonEmpty)) incoming.Stand else this.Stand,
      Gate = if (incoming.Gate.exists(_.nonEmpty)) incoming.Gate else this.Gate,
      RedListPax = if (incoming.RedListPax.nonEmpty) incoming.RedListPax else this.RedListPax,
      MaxPax = if (incoming.MaxPax.nonEmpty) incoming.MaxPax else this.MaxPax,
    )
  }
}
