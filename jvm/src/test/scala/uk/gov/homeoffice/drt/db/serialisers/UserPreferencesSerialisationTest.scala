package uk.gov.homeoffice.drt.db.serialisers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.homeoffice.drt.models.UserPreferences
import UserPreferencesSerialisation._
import spray.json.{JsBoolean, JsNumber, JsObject, JsString}
import uk.gov.homeoffice.drt.db.tables.UserRow

class UserPreferencesSerialisationTest extends AnyFlatSpec with Matchers {
  it should "parse valid JSON single port data correctly" in {
    val json = "bhx:30"
    val result = deserializePortDashboardIntervalMinutes(Some(json))
    result shouldEqual Map("bhx" -> 30)
  }


  it should "parse valid JSON data correctly" in {
    val json = "bhx:30;lhr:60"
    val result = deserializePortDashboardIntervalMinutes(Some(json))
    result shouldEqual Map("bhx" -> 30, "lhr" -> 60)
  }

  it should "throw an exception for invalid JSON data" in {
    val json = """invalid"""
    an[IllegalArgumentException] should be thrownBy deserializePortDashboardIntervalMinutes(Some(json))
  }

  "parsePortDashboardTerminals" should "return empty map when data is None" in {
    val result = deserializePortDashboardTerminals(None)
    result shouldEqual Map.empty[String, Set[String]]
  }

  it should "parse valid JSON data correctly" in {
    val json = "lhr:T2,T3,T5;lgw:N"
    val result = deserializePortDashboardTerminals(Some(json))
    result shouldEqual Map("lhr" -> Set("T2", "T3", "T5"), "lgw" -> Set("N"))
  }

  it should "throw an exception for invalid JSON data" in {
    val json = """invalid"""
    an[IllegalArgumentException] should be thrownBy deserializePortDashboardTerminals(Some(json))
  }


  it should "create UserPreferences from UserRow" in {
      val userRow = UserRow(
        id = "test-id",
        username = "test-user",
        email = "test@example.com",
        latest_login = java.sql.Timestamp.valueOf("2023-01-01 00:00:00"),
        inactive_email_sent = None,
        revoked_access = None,
        drop_in_notification_at = None,
        created_at = None,
        feedback_banner_closed_at = None,
        staff_planning_interval_minutes = Some(30),
        hide_pax_data_source_description = Some(true),
        show_staffing_shift_view = Some(false),
        desks_and_queues_interval_minutes = Some(20),
        port_dashboard_interval_minutes = Some("LHR:30"),
        port_dashboard_terminals = Some("LHR:T1,T2")
      )

      val result = deserialize(userRow)
      result shouldEqual UserPreferences(
        userSelectedPlanningTimePeriod = 30,
        hidePaxDataSourceDescription = true,
        showStaffingShiftView = false,
        desksAndQueuesIntervalMinutes = 20,
        portDashboardIntervalMinutes = Map("LHR" -> 30),
        portDashboardTerminals = Map("LHR" -> Set("T1", "T2"))
      )
    }

  it should "serialize UserRow to JSON correctly" in {
    val userRow = UserRow(
      id = "test-id",
      username = "test-user",
      email = "test@example.com",
      latest_login = java.sql.Timestamp.valueOf("2023-01-01 00:00:00"),
      inactive_email_sent = None,
      revoked_access = None,
      drop_in_notification_at = None,
      created_at = None,
      feedback_banner_closed_at = None,
      staff_planning_interval_minutes = Some(30),
      hide_pax_data_source_description = Some(true),
      show_staffing_shift_view = Some(false),
      desks_and_queues_interval_minutes = Some(20),
      port_dashboard_interval_minutes = Some("LHR:30"),
      port_dashboard_terminals = Some("LHR:T1,T2")
    )

    val expectedJson = JsObject(
      "userSelectedPlanningTimePeriod" -> JsNumber(30),
      "hidePaxDataSourceDescription" -> JsBoolean(true),
      "showStaffingShiftView" -> JsBoolean(false),
      "desksAndQueuesIntervalMinutes" -> JsNumber(20),
      "portDashboardIntervalMinutes" -> JsString("LHR:30"),
      "portDashboardTerminals" -> JsString("LHR:T1,T2")
    )

    val result = UserPreferencesSerialisation.userRowToJson(userRow)
    result shouldEqual expectedJson
  }

}
