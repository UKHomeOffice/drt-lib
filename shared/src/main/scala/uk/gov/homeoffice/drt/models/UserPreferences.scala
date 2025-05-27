package uk.gov.homeoffice.drt.models

import upickle.default._

case class UserPreferences(userSelectedPlanningTimePeriod: Int,
                           hidePaxDataSourceDescription: Boolean,
                           showStaffingShiftView: Boolean,
                           desksAndQueuesIntervalMinutes: Int,
                           portDashboardIntervalMinutes: Int,
                           portDashboardTerminals: Set[String])

object UserPreferences {
  implicit val rw: ReadWriter[UserPreferences] = macroRW
  implicit val rwSet: ReadWriter[Set[String]] = upickle.default.readwriter[String].bimap[Set[String]](
    _.mkString(","),
    s => s.split(",").toSet
  )
}
