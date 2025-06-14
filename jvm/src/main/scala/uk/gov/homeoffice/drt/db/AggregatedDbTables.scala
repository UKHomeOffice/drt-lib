package uk.gov.homeoffice.drt.db

import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.PostgresProfile
import uk.gov.homeoffice.drt.db.tables.{ArrivalStatsRow, FlightTable, ProcessedJsonRow, ProcessedZipRow, QueueSlotTable, StaffShiftRow, StatusDailyTable, UserRow, VoyageManifestPassengerInfoRow}

import java.sql.Timestamp
import scala.concurrent.Future

object AggregatedDbTables {
  def apply(databaseType: String): AggregatedDbTables =
    databaseType match {
      case "persistent" => AggregateDb
      case "in-memory" => AggregateDbH2
    }
}


/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait AggregatedDbTables extends CentralDatabase {
  val profile: slick.jdbc.JdbcProfile

  def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R]

  import profile.api._

  lazy val schema: profile.SchemaDescription = voyageManifestPassengerInfo.schema ++ processedJson.schema ++ processedZip.schema

  private val maybeSchema = profile match {
    case _: PostgresProfile =>
      Some("public")
    case _ =>
      None
  }

  class ProcessedZipTable(_tableTag: Tag) extends profile.api.Table[ProcessedZipRow](_tableTag, maybeSchema, "processed_zip") {
    val zip_file_name: Rep[String] = column[String]("zip_file_name")
    val success: Rep[Boolean] = column[Boolean]("success")
    val processed_at: Rep[Timestamp] = column[Timestamp]("processed_at")
    val created_on: Rep[Option[String]] = column[Option[String]]("created_on")

    def * = (zip_file_name, success, processed_at, created_on).mapTo[ProcessedZipRow]
  }

  class ProcessedJsonTable(_tableTag: Tag) extends Table[ProcessedJsonRow](_tableTag, Option("public"), "processed_json") {
    val zip_file_name: Rep[String] = column[String]("zip_file_name")
    val json_file_name: Rep[String] = column[String]("json_file_name")
    val suspicious_date: Rep[Boolean] = column[Boolean]("suspicious_date")
    val success: Rep[Boolean] = column[Boolean]("success")
    val processed_at: Rep[Timestamp] = column[Timestamp]("processed_at")
    val arrival_port_code: Rep[Option[String]] = column[Option[String]]("arrival_port_code")
    val departure_port_code: Rep[Option[String]] = column[Option[String]]("departure_port_code")
    val voyage_number: Rep[Option[Int]] = column[Option[Int]]("voyage_number")
    val carrier_code: Rep[Option[String]] = column[Option[String]]("carrier_code")
    val scheduled: Rep[Option[Timestamp]] = column[Option[Timestamp]]("scheduled")
    val event_code: Rep[Option[String]] = column[Option[String]]("event_code")
    val non_interactive_total_count: Rep[Option[Int]] = column[Option[Int]]("non_interactive_total_count")
    val non_interactive_trans_count: Rep[Option[Int]] = column[Option[Int]]("non_interactive_trans_count")
    val interactive_total_count: Rep[Option[Int]] = column[Option[Int]]("interactive_total_count")
    val interactive_trans_count: Rep[Option[Int]] = column[Option[Int]]("interactive_trans_count")

    def * = (zip_file_name, json_file_name, suspicious_date, success, processed_at,
      arrival_port_code, departure_port_code, voyage_number, carrier_code, scheduled,
      event_code, non_interactive_total_count, non_interactive_trans_count, interactive_total_count, interactive_trans_count).mapTo[ProcessedJsonRow]
  }

  /** Table description of table arrival. Objects of this class serve as prototypes for rows in queries. */
  class VoyageManifestPassengerInfoTable(_tableTag: Tag) extends profile.api.Table[VoyageManifestPassengerInfoRow](_tableTag, maybeSchema, "voyage_manifest_passenger_info") {
    def * = (event_code, arrival_port_code, departure_port_code, voyage_number, carrier_code, scheduled_date, day_of_week, week_of_year, document_type, document_issuing_country_code, eea_flag, age, disembarkation_port_code, in_transit_flag, disembarkation_port_country_code, nationality_country_code, passenger_identifier, in_transit, json_file) <> (VoyageManifestPassengerInfoRow.tupled, VoyageManifestPassengerInfoRow.unapply)

    val event_code: Rep[String] = column[String]("event_code")
    val arrival_port_code: Rep[String] = column[String]("arrival_port_code")
    val departure_port_code: Rep[String] = column[String]("departure_port_code")
    val voyage_number: Rep[Int] = column[Int]("voyage_number")
    val carrier_code: Rep[String] = column[String]("carrier_code")
    val scheduled_date: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("scheduled_date")
    val day_of_week: Rep[Int] = column[Int]("day_of_week")
    val week_of_year: Rep[Int] = column[Int]("week_of_year")
    val document_type: Rep[String] = column[String]("document_type")
    val document_issuing_country_code: Rep[String] = column[String]("document_issuing_country_code")
    val eea_flag: Rep[String] = column[String]("eea_flag")
    val age: Rep[Int] = column[Int]("age")
    val disembarkation_port_code: Rep[String] = column[String]("disembarkation_port_code")
    val in_transit_flag: Rep[String] = column[String]("in_transit_flag")
    val disembarkation_port_country_code: Rep[String] = column[String]("disembarkation_port_country_code")
    val nationality_country_code: Rep[String] = column[String]("nationality_country_code")
    val passenger_identifier: Rep[String] = column[String]("passenger_identifier")
    val in_transit: Rep[Boolean] = column[Boolean]("in_transit")
    val json_file: Rep[String] = column[String]("json_file")
  }

  class UserTable(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, maybeSchema, "user") {
    def * = (id, userName, email, latest_login, inactive_email_sent, revoked_access, drop_in_notification_at, created_at, feedback_banner_closed_at, staff_planning_interval_minutes, hide_pax_data_source_description, show_staffing_shift_view, desks_and_queues_interval_minutes, port_dashboard_interval_minutes, port_dashboard_terminals) <> (UserRow.tupled, UserRow.unapply)

    val id: Rep[String] = column[String]("id")
    val userName: Rep[String] = column[String]("username")
    val email: Rep[String] = column[String]("email")
    val latest_login: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("latest_login")
    val inactive_email_sent: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("inactive_email_sent")
    val revoked_access: Rep[Option[java.sql.Timestamp]] = column[Option[java.sql.Timestamp]]("revoked_access")
    val drop_in_notification_at = column[Option[java.sql.Timestamp]]("drop_in_notification_at")
    val created_at = column[Option[Timestamp]]("created_at")
    val feedback_banner_closed_at = column[Option[java.sql.Timestamp]]("feedback_banner_closed_at")
    val staff_planning_interval_minutes = column[Option[Int]]("staff_planning_interval_minutes")
    val hide_pax_data_source_description = column[Option[Boolean]]("hide_pax_data_source_description")
    val show_staffing_shift_view = column[Option[Boolean]]("show_staffing_shift_view")
    val desks_and_queues_interval_minutes = column[Option[Int]]("desks_and_queues_interval_minutes")
    val port_dashboard_interval_minutes = column[Option[String]]("port_dashboard_interval_minutes")
    val port_dashboard_terminals = column[Option[String]]("port_dashboard_terminals")
    val pk = primaryKey("user_pkey", (id))

    index("username", userName)
    index("email", email)
    index("latest_login", latest_login)
  }

  class ArrivalStatsTable(_tableTag: Tag) extends profile.api.Table[ArrivalStatsRow](_tableTag, maybeSchema, "arrival_stats") {
    val portCode: Rep[String] = column[String]("port_code")
    val terminal: Rep[String] = column[String]("terminal")
    val date: Rep[String] = column[String]("date")
    val daysAhead: Rep[Int] = column[Int]("days_ahead")
    val dataType: Rep[String] = column[String]("data_type")
    val flights: Rep[Int] = column[Int]("flights")
    val capacity: Rep[Int] = column[Int]("capacity")
    val pax: Rep[Int] = column[Int]("pax")
    val averageLoad: Rep[Double] = column[Double]("average_load")
    val createdAt: Rep[Long] = column[Long]("created_at")

    def * = (portCode, terminal, date, daysAhead, dataType, flights, capacity, pax, averageLoad, createdAt).mapTo[ArrivalStatsRow]

    val pk = primaryKey("arrival_stats_pkey", (portCode, terminal, date, daysAhead, dataType))
  }

  class StaffShiftsTable(_tableTag: Tag) extends profile.api.Table[StaffShiftRow](_tableTag, maybeSchema, "staff_shifts") {
    val port: Rep[String] = column[String]("port")
    val terminal: Rep[String] = column[String]("terminal")
    val shift_name: Rep[String] = column[String]("shift_name")
    val start_date: Rep[java.sql.Date] = column[java.sql.Date]("start_date")
    val start_time: Rep[String] = column[String]("start_time")
    val end_time: Rep[String] = column[String]("end_time")
    val end_date: Rep[Option[java.sql.Date]] = column[Option[java.sql.Date]]("end_date")
    val staff_number: Rep[Int] = column[Int]("staff_number")
    val created_by: Rep[Option[String]] = column[Option[String]]("created_by")
    val frequency: Rep[Option[String]] = column[Option[String]]("frequency")
    val created_at: Rep[java.sql.Timestamp] = column[java.sql.Timestamp]("created_at")

    def * = (port, terminal, shift_name, start_date, start_time, end_time, end_date, staff_number, created_by, frequency, created_at).mapTo[StaffShiftRow]

    val pk = primaryKey("staff_shifts_pkey", (port, terminal, shift_name, start_date, start_time))
  }

  /** Collection-like TableQuery object for table VoyageManifestPassengerInfo */
  val voyageManifestPassengerInfo = new TableQuery(tag => new VoyageManifestPassengerInfoTable(tag))
  val processedJson = new TableQuery(tag => new ProcessedJsonTable(tag))
  val processedZip = new TableQuery(tag => new ProcessedZipTable(tag))
  val arrivalStats = new TableQuery(tag => new ArrivalStatsTable(tag))
  val user = new TableQuery(tag => new UserTable(tag))
  val statusDaily = new TableQuery(tag => new StatusDailyTable(tag))
  val flight = new TableQuery(tag => new FlightTable(tag))
  val queueSlot = new TableQuery(tag => new QueueSlotTable(tag))
  val staffShifts = new TableQuery(tag => new StaffShiftsTable(tag))
  val tables = Seq(arrivalStats, processedZip, processedJson, statusDaily, voyageManifestPassengerInfo, flight, queueSlot, staffShifts)
}
