package uk.gov.homeoffice.drt.db.serialisers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.homeoffice.drt.models.UserPreferences
import UserPreferencesSerialisation._
import spray.json.{JsBoolean, JsNumber, JsObject, JsString, enrichAny}
import uk.gov.homeoffice.drt.db.tables.UserRow
import uk.gov.homeoffice.drt.models.UserPreferences.deserializeMap

class UserPreferencesSerialisationTest extends AnyFlatSpec with Matchers {
  it should "parse valid JSON single port data correctly" in {
    val json = "bhx:30"
    val result = deserializeMap(Some(json), _.toInt)
    result shouldEqual Map("bhx" -> 30)
  }


  it should "parse valid JSON data correctly" in {
    val json = "bhx:30;lhr:60"
    val result = deserializeMap(Some(json), _.toInt)
    result shouldEqual Map("bhx" -> 30, "lhr" -> 60)
  }

  it should "throw an exception for invalid JSON data" in {
    val json = """invalid"""
    an[IllegalArgumentException] should be thrownBy deserializeMap(Some(json), _.toInt)
  }

  "parsePortDashboardTerminals" should "return empty map when data is None" in {
    val result = deserializeMap(None, _.split(",").toSet)
    result shouldEqual Map.empty[String, Set[String]]
  }

  it should "parse valid JSON data correctly" in {
    val json = "lhr:T2,T3,T5;lgw:N"
    val result = deserializeMap(Some(json), _.split(",").toSet)
    result shouldEqual Map("lhr" -> Set("T2", "T3", "T5"), "lgw" -> Set("N"))
  }

  it should "throw an exception for invalid JSON data" in {
    val json = """invalid"""
    an[IllegalArgumentException] should be thrownBy deserializeMap(Some(json), _.split(",").toSet)
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

      val result = UserRowSerialisation.toUserPreferences(userRow)
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

    val userPreferences = UserRowSerialisation.toUserPreferences(userRow)

    val expectedJson = JsObject(
      "userSelectedPlanningTimePeriod" -> JsNumber(30),
      "hidePaxDataSourceDescription" -> JsBoolean(true),
      "showStaffingShiftView" -> JsBoolean(false),
      "desksAndQueuesIntervalMinutes" -> JsNumber(20),
      "portDashboardIntervalMinutes" -> JsString("LHR:30"),
      "portDashboardTerminals" -> JsString("LHR:T1,T2")
    )

    val result = userPreferences.toJson
    result shouldEqual expectedJson
  }

  "UserPreferences" should "serialize and deserialize portDashboardIntervalMinutes correctly" in {
    val input = Map("port1" -> 10, "port2" -> 20)
    val serialized = UserPreferences.serializeMap(input, (value: Int) => value.toString)
    val deserialized = UserPreferences.deserializeMap(Option(serialized), _.toInt)
    deserialized shouldEqual input
  }

  it should "serialize and deserialize portDashboardTerminals correctly" in {
    val input = Map("lhr" -> Set("t2", "t3"), "bhx" -> Set("t2"))
    val serialized = UserPreferences.serializeMap(input, (values: Set[String]) => values.mkString(","))
    val deserialized = UserPreferences.deserializeMap(Option(serialized), _.split(",").toSet)
    deserialized shouldEqual input
  }

  it should "serialize and deserialize UserPreferences correctly" in {
    val userPreferences = UserPreferences(
      userSelectedPlanningTimePeriod = 30,
      hidePaxDataSourceDescription = true,
      showStaffingShiftView = false,
      desksAndQueuesIntervalMinutes = 15,
      portDashboardIntervalMinutes = Map("port1" -> 10, "port2" -> 20),
      portDashboardTerminals = Map("lhr" -> Set("t2", "t3"), "lgw" -> Set("N"))
    )

    val serialized = userPreferences.toJson
    val deserialized = serialized.convertTo[UserPreferences]
    deserialized shouldEqual userPreferences
  }

  it should "deserialize 'lhr:30' to Map[String, Int]" in {
    val input = "lhr:30;bhx:60"
    val expected = Map("lhr" -> 30, "bhx" -> 60)
    val result = UserPreferences.deserializeMap(Some(input), _.toInt)
    result shouldEqual expected
  }

}
