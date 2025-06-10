package uk.gov.homeoffice.drt.db.serialisers

import uk.gov.homeoffice.drt.db.tables.UserRow
import uk.gov.homeoffice.drt.models.UserPreferences

import scala.util.{Failure, Success, Try}

object UserPreferencesSerialisation {

  import upickle.default._
  import uk.gov.homeoffice.drt.models.UserPreferences._

  def parsePortDashboardIntervalMinutes(data: Option[String], portCode: String): Map[String, Int] = {
    data match {
      case Some(json) =>
        Try(read[Map[String, Int]](s"\"$json\"")(portDashboardIntervalMinutesRW)) match {
          case Success(parsed) => parsed
          case Failure(e) =>
            throw new IllegalArgumentException(s"Failed to parse portDashboardIntervalMinutes: $json", e)
        }
      case None => Map(portCode -> 60)
    }
  }

  def parsePortDashboardTerminals(data: Option[String]): Map[String, Set[String]] = {
    data match {
      case Some(json) =>
        Try(read[Map[String, Set[String]]](s"\"$json\"")(portDashboardTerminalsRW)) match {
          case Success(parsed) => parsed
          case Failure(e) =>
            throw new IllegalArgumentException(s"Failed to parse portDashboardTerminals: $json", e)
        }
      case None => Map.empty[String, Set[String]]
    }
  }

  def deserialize(userRow: UserRow, portCode: String): UserPreferences = {
    UserPreferences(
      userRow.staff_planning_interval_minutes.getOrElse(60),
      userRow.hide_pax_data_source_description.getOrElse(false),
      userRow.show_staffing_shift_view.getOrElse(false),
      userRow.desks_and_queues_interval_minutes.getOrElse(15),
      parsePortDashboardIntervalMinutes(userRow.port_dashboard_interval_minutes, portCode),
      parsePortDashboardTerminals(userRow.port_dashboard_terminals)
    )
  }
}