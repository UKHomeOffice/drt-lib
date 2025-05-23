package uk.gov.homeoffice.drt.db.tables

import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import java.sql.Timestamp

case class FlightRow(port: String,
                     origin: String,
                     previousPort: Option[String],
                     terminal: String,
                     voyageNumber: Int,
                     carrierCode: String,
                     flightCodeSuffix: Option[String],
                     status: String,
                     scheduledDateUtc: String,
                     timings: FlightTimings,
                     predictions: String,
                     gate: Option[String],
                     stand: Option[String],
                     maxPax: Option[Int],
                     baggageReclaimId: Option[String],
                     paxSourcesJson: String,
                     redListPax: Option[Int],
                     splitsJson: String,
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

  def previousPort: Rep[Option[String]] = column[Option[String]]("previous_port")

  def terminal: Rep[String] = column[String]("terminal")

  def scheduled: Rep[Timestamp] = column[Timestamp]("scheduled")

  def voyageNumber: Rep[Int] = column[Int]("voyage_number")

  def carrierCode: Rep[String] = column[String]("carrier_code")

  def flightCodeSuffix: Rep[Option[String]] = column[Option[String]]("flight_code_suffix")

  def status: Rep[String] = column[String]("status")

  def scheduledDateUtc: Rep[String] = column[String]("scheduled_date_utc")

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

  def paxSourcesJson: Rep[String] = column[String]("pax_sources_json")

  def carrierScheduled: Rep[Option[Timestamp]] = column[Option[Timestamp]]("carrier_scheduled")

  def scheduledDeparture: Rep[Option[Timestamp]] = column[Option[Timestamp]]("scheduled_departure")

  def redListPax: Rep[Option[Int]] = column[Option[Int]]("red_list_pax")

  def splitsJson: Rep[String] = column[String]("splits_json")

  def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

  def pk = primaryKey("pk_flight", (port, origin, terminal, voyageNumber, scheduled))

  def flightsForPortAndDateIndex = index("idx_flight_port_date", (port, scheduledDateUtc), unique = false)

  def flightsForPortDateAndTerminalIndex = index("idx_flight_port_date_terminal", (port, scheduledDateUtc, terminal), unique = false)

  def flightsScheduleIndex = index("idx_flight_schedule", scheduled, unique = false)

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
    previousPort,
    terminal,
    voyageNumber,
    carrierCode,
    flightCodeSuffix,
    status,
    scheduledDateUtc,
    timingProjection,
    predictions,
    gate,
    stand,
    maxPax,
    baggageReclaimId,
    paxSourcesJson,
    redListPax,
    splitsJson,
    updatedAt).mapTo[FlightRow]
}
