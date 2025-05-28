package uk.gov.homeoffice.drt.models

import upickle.default._

case class UserPreferences(userSelectedPlanningTimePeriod: Int,
                           hidePaxDataSourceDescription: Boolean,
                           showStaffingShiftView: Boolean,
                           desksAndQueuesIntervalMinutes: Int,
                           portDashboardIntervalMinutes: Map[String, Int],
                           portDashboardTerminals: Map[String, Set[String]])

object UserPreferences {
  implicit val rw: ReadWriter[UserPreferences] = macroRW

    implicit val portDashboardIntervalMinutesRW: ReadWriter[Map[String, Int]] =
    readwriter[String].bimap[Map[String, Int]](
      _.map { case (port, value) => s"$port:$value" }.mkString(";"),
      s => s.split(";").map(_.split(":") match {
        case Array(port, value) => port -> value.toInt
      }).toMap
    )

  implicit val portDashboardTerminalsRW: ReadWriter[Map[String, Set[String]]] =
    readwriter[String].bimap[Map[String, Set[String]]](
      _.map { case (key, values) => s"$key:${values.mkString(",")}" }.mkString(";"),
      s => s.split(";").map(_.split(":") match {
        case Array(key, values) => key -> values.split(",").toSet
      }).toMap
    )
}
