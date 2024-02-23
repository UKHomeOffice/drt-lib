package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.{FeedSource, PortCode}
import uk.gov.homeoffice.drt.ports.Terminals.Terminal

trait ArrivalLike {
  def Operator: Option[Operator]
  def CarrierCode: CarrierCode
  def VoyageNumber: VoyageNumber
  def FlightCodeSuffix: Option[FlightCodeSuffix]
  def Status: ArrivalStatus
  def Estimated: Option[Long]
  def Predictions: Predictions
  def Actual: Option[Long]
  def EstimatedChox: Option[Long]
  def ActualChox: Option[Long]
  def Gate: Option[String]
  def Stand: Option[String]
  def MaxPax: Option[Int]
  def RunwayID: Option[String]
  def BaggageReclaimId: Option[String]
  def AirportID: PortCode
  def Terminal: Terminal
  def Origin: PortCode
  def Scheduled: Long
  def PcpTime: Option[Long]
  def FeedSources: Set[FeedSource]
  def CarrierScheduled: Option[Long]
  def ScheduledDeparture: Option[Long]
  def RedListPax: Option[Int]
  def PassengerSources: Map[FeedSource, Passengers]
}
