package uk.gov.homeoffice.drt.models

case class UserPreferences(userSelectedPlanningTimePeriod: Int,
                           hidePaxDataSourceDescription: Boolean,
                           showStaffingShiftView: Boolean,
                           desksAndQueuesIntervalMinutes: Int,
                           portDashboardIntervalMinutes: Map[String, Int],
                           portDashboardTerminals: Map[String, Set[String]]) {
  val serializedPortDashboardIntervalMinutes: String = portDashboardIntervalMinutes.map {
    case (port, value) => s"$port:$value"
  }.mkString(";")

  val serializedPortDashboardTerminals: String = portDashboardTerminals.map {
    case (key, values) => s"$key:${values.mkString(",")}"
  }.mkString(";")
}
