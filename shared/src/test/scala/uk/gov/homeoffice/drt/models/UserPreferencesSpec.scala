package uk.gov.homeoffice.drt.models

import org.specs2.mutable.Specification
import upickle.default._

class UserPreferencesSpec extends Specification {

  "UserPreferences" should {

    "serialize and deserialize portDashboardIntervalMinutes correctly" in {
      val input = Map("port1" -> 10, "port2" -> 20)
      val serialized = write(input)(UserPreferences.portDashboardIntervalMinutesRW)
      val deserialized = read[Map[String, Int]](serialized)(UserPreferences.portDashboardIntervalMinutesRW)
      deserialized mustEqual input
    }

    "serialize and deserialize portDashboardTerminals correctly" in {
      val input = Map("lhr" -> Set("t2", "t3"), "bhx" -> Set("t2"))
      val serialized = write(input)(UserPreferences.portDashboardTerminalsRW)

      println(s"serialized portDashboardTerminals: $serialized")
      val deserialized = read[Map[String, Set[String]]](serialized)(UserPreferences.portDashboardTerminalsRW)
      deserialized mustEqual input
    }

    "serialize and deserialize UserPreferences correctly" in {
      val userPreferences = UserPreferences(
        userSelectedPlanningTimePeriod = 30,
        hidePaxDataSourceDescription = true,
        showStaffingShiftView = false,
        desksAndQueuesIntervalMinutes = 15,
        portDashboardIntervalMinutes = Map("port1" -> 10, "port2" -> 20),
        portDashboardTerminals = Map("lhr" -> Set("t2", "t3"), "lgw" -> Set("N"))
      )

      val serialized = write(userPreferences)
      println(s"serialized UserPreferences: $serialized")
      val deserialized = read[UserPreferences](serialized)

      deserialized mustEqual userPreferences
    }

    "deserialize 'lhr:30' to Map[String, Int]" in {
      val input = "\"lhr:30;bhx:60\""
      val expected = Map("lhr" -> 30, "bhx" -> 60)

      val deserialized = read[Map[String, Int]](input)(UserPreferences.portDashboardIntervalMinutesRW)
      deserialized mustEqual expected
    }
  }
}