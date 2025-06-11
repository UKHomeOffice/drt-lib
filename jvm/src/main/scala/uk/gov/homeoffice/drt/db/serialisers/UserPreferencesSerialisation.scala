package uk.gov.homeoffice.drt.db.serialisers

import spray.json.{DefaultJsonProtocol, DeserializationException, JsBoolean, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}
import uk.gov.homeoffice.drt.db.tables.UserRow
import uk.gov.homeoffice.drt.models.UserPreferences
import spray.json._

object UserPreferencesSerialisation extends DefaultJsonProtocol {

  def serializeMap[K, V](data: Map[K, V], valueToString: V => String): String = {
    data.map { case (key, value) => s"$key:${valueToString(value)}" }.mkString(";")
  }

  def deserializeMap[K, V](data: Option[String], keyParser: String => K, valueParser: String => V): Map[K, V] = {
    data match {
      case Some(s) =>
        s.split(";").map(_.split(":") match {
          case Array(key, value) => keyParser(key) -> valueParser(value)
          case _ => throw new IllegalArgumentException(s"Invalid format: $s")
        }).toMap
      case None => Map.empty[K, V]
    }
  }

  def serializePortDashboardIntervalMinutes(data: Map[String, Int]): String = {
    serializeMap(data, (value: Int) => value.toString)
  }

  def deserializePortDashboardIntervalMinutes(data: Option[String]): Map[String, Int] = {
    deserializeMap(data, identity, _.toInt)
  }

  def serializePortDashboardTerminals(data: Map[String, Set[String]]): String = {
    serializeMap(data, (values: Set[String]) => values.mkString(","))
  }

  def deserializePortDashboardTerminals(data: Option[String]): Map[String, Set[String]] = {
    deserializeMap(data, identity, _.split(",").toSet)
  }

  implicit val portDashboardTerminalsFormat: RootJsonFormat[Map[String, Set[String]]] = new RootJsonFormat[Map[String, Set[String]]] {
    def write(obj: Map[String, Set[String]]): JsValue = JsString(serializePortDashboardTerminals(obj))

    def read(json: JsValue): Map[String, Set[String]] = json match {
      case JsString(s) => deserializePortDashboardTerminals(Some(s))
      case _ => Map.empty
    }
  }

  implicit val portDashboardIntervalMinutesFormat: RootJsonFormat[Map[String, Int]] = new RootJsonFormat[Map[String, Int]] {
    def write(obj: Map[String, Int]): JsValue = JsString(serializePortDashboardIntervalMinutes(obj))

    def read(json: JsValue): Map[String, Int] = json match {
      case JsString(s) => deserializePortDashboardIntervalMinutes(Some(s))
      case _ => Map.empty
    }
  }

  implicit val userPreferencesFormat: RootJsonFormat[UserPreferences] = new RootJsonFormat[UserPreferences] {
    def write(obj: UserPreferences): JsValue = JsObject(
      "userSelectedPlanningTimePeriod" -> JsNumber(obj.userSelectedPlanningTimePeriod),
      "hidePaxDataSourceDescription" -> JsBoolean(obj.hidePaxDataSourceDescription),
      "showStaffingShiftView" -> JsBoolean(obj.showStaffingShiftView),
      "desksAndQueuesIntervalMinutes" -> JsNumber(obj.desksAndQueuesIntervalMinutes),
      "portDashboardIntervalMinutes" -> portDashboardIntervalMinutesFormat.write(obj.portDashboardIntervalMinutes),
      "portDashboardTerminals" -> portDashboardTerminalsFormat.write(obj.portDashboardTerminals)
    )

    def read(json: JsValue): UserPreferences = json.asJsObject.getFields(
      "userSelectedPlanningTimePeriod",
      "hidePaxDataSourceDescription",
      "showStaffingShiftView",
      "desksAndQueuesIntervalMinutes",
      "portDashboardIntervalMinutes",
      "portDashboardTerminals"
    ) match {
      case Seq(
      JsNumber(staffPlanningIntervalMinutes),
      JsBoolean(hidePaxDataSourceDescription),
      JsBoolean(showStaffingShiftView),
      JsNumber(desksAndQueuesIntervalMinutes),
      portDashboardIntervalMinutes,
      portDashboardTerminals
      ) =>
        UserPreferences(
          staffPlanningIntervalMinutes.toInt,
          hidePaxDataSourceDescription,
          showStaffingShiftView,
          desksAndQueuesIntervalMinutes.toInt,
          portDashboardIntervalMinutesFormat.read(portDashboardIntervalMinutes),
          portDashboardTerminalsFormat.read(portDashboardTerminals)
        )
      case _ => throw new DeserializationException("Invalid UserPreferences JSON format")
    }
  }

  def toJson(userPreferences: UserPreferences): JsValue = {
    userPreferences.toJson
  }

  def toUserPreferencesJson(userPreferences: UserPreferences): String = {
    userPreferences.toJson.compactPrint
  }

  def fromJson(json: JsValue): UserPreferences = {
    json.convertTo[UserPreferences]
  }

  def fromJson(json: String): UserPreferences = {
    json.parseJson.convertTo[UserPreferences]
  }

  def userRowToJson(userRow: UserRow): JsValue = {
    UserPreferencesSerialisation.deserialize(userRow).toJson
  }

  def deserialize(userRow: UserRow): UserPreferences = {
    UserPreferences(
      userRow.staff_planning_interval_minutes.getOrElse(60),
      userRow.hide_pax_data_source_description.getOrElse(false),
      userRow.show_staffing_shift_view.getOrElse(false),
      userRow.desks_and_queues_interval_minutes.getOrElse(15),
      deserializePortDashboardIntervalMinutes(userRow.port_dashboard_interval_minutes),
      deserializePortDashboardTerminals(userRow.port_dashboard_terminals)
    )
  }
}