package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.{FeedSource, ForecastFeedSource, PortCode}
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.arrivals.{Predictions => Preds}

case class ForecastArrival(carrierCode: CarrierCode,
                           flightNumber: VoyageNumber,
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
  override def VoyageNumber: VoyageNumber = flightNumber
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
  override def AirportID: PortCode = origin
  override def Terminal: Terminal = terminal
  override def Origin: PortCode = origin
  override def Scheduled: Long = scheduled
  override def PcpTime: Option[Long] = None
  override def FeedSources: Set[FeedSource] = Set(ForecastFeedSource)
  override def CarrierScheduled: Option[Long] = None
  override def ScheduledDeparture: Option[Long] = None
  override def RedListPax: Option[Int] = None
  override def PassengerSources: Map[FeedSource, Passengers] = Map(ForecastFeedSource -> Passengers(totalPax, transPax))
}
