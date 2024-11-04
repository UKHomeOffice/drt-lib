package uk.gov.homeoffice.drt.db.tables

import slick.lifted.Tag
import uk.gov.homeoffice.drt.db.Db.slickProfile.api._

import java.sql.Timestamp

case class FlightRow(port: String,
                     origin: String,
                     terminal: String,
                     voyageNumber: Int,
                     carrierCode: String,
                     flightCodeSuffix: Option[String],
                     status: String,
                     timings: FlightTimings,
                     predictions: String,
                     gate: Option[String],
                     stand: Option[String],
                     maxPax: Option[Int],
                     baggageReclaimId: Option[String],
                     paxSources: String,
                     redListPax: Option[Int],
                     splits: String,
                     updatedAt: Timestamp,
                    )

case class FlightTimings(scheduled: Timestamp,
                         estimated: Option[Timestamp],
                         actual: Option[Timestamp],
                         estimatedChox: Option[Timestamp],
                         actualChox: Option[Timestamp],
                         pcpTime: Option[Timestamp],
                         carrierScheduled: Option[Timestamp],
                         scheduledDeparture: Option[Timestamp],
                       )

class FlightTable(tag: Tag)
  extends Table[FlightRow](tag, "flight") {

  def port: Rep[String] = column[String]("port")

  def origin: Rep[String] = column[String]("origin")

  def terminal: Rep[String] = column[String]("terminal")

  def scheduled: Rep[Timestamp] = column[Timestamp]("scheduled")

  def voyageNumber: Rep[Int] = column[Int]("voyage_number")

  def carrierCode: Rep[String] = column[String]("carrier_code")

  def flightCodeSuffix: Rep[Option[String]] = column[Option[String]]("flight_code_suffix")

  def status: Rep[String] = column[String]("status")

  def estimated: Rep[Option[Timestamp]] = column[Option[Timestamp]]("estimated")

  def actual: Rep[Option[Timestamp]] = column[Option[Timestamp]]("actual")

  def estimatedChox: Rep[Option[Timestamp]] = column[Option[Timestamp]]("estimated_chox")

  def actualChox: Rep[Option[Timestamp]] = column[Option[Timestamp]]("actual_chox")

  def predictions: Rep[String] = column[String]("predictions")

  def gate: Rep[Option[String]] = column[Option[String]]("gate")

  def stand: Rep[Option[String]] = column[Option[String]]("stand")

  def maxPax: Rep[Option[Int]] = column[Option[Int]]("max_pax")

  def baggageReclaimId: Rep[Option[String]] = column[Option[String]]("baggage_reclaim_id")

  def pcpTime: Rep[Option[Timestamp]] = column[Option[Timestamp]]("pcp_time")

  def paxSources: Rep[String] = column[String]("pax_sources")

  def carrierScheduled: Rep[Option[Timestamp]] = column[Option[Timestamp]]("carrier_scheduled")

  def scheduledDeparture: Rep[Option[Timestamp]] = column[Option[Timestamp]]("scheduled_departure")

  def redListPax: Rep[Option[Int]] = column[Option[Int]]("red_list_pax")

  def splits: Rep[String] = column[String]("splits")

  def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

  def pk = primaryKey("pk_flight", (port, origin, terminal, voyageNumber, scheduled))

  private def timingProjection = (
    scheduled,
    estimated,
    actual,
    estimatedChox,
    actualChox,
    pcpTime,
    carrierScheduled,
    scheduledDeparture
  ).mapTo[FlightTimings]

  def * = (
    port,
    origin,
    terminal,
    voyageNumber,
    carrierCode,
    flightCodeSuffix,
    status,
    timingProjection,
    predictions,
    gate,
    stand,
    maxPax,
    baggageReclaimId,
    paxSources,
    redListPax,
    splits,
    updatedAt).mapTo[FlightRow]
}
