package uk.gov.homeoffice.drt.models

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.db.serialisers.UserPreferencesSerialisation


class UserPreferencesSpec extends Specification {

  "UserPreferences" should {

    "serialize and deserialize portDashboardIntervalMinutes correctly" in {
      val input = Map("port1" -> 10, "port2" -> 20)
      val serialized = UserPreferencesSerialisation.serializePortDashboardIntervalMinutes(input)
      val deserialized = UserPreferencesSerialisation.deserializePortDashboardIntervalMinutes(Option(serialized))
      deserialized mustEqual input
    }

    "serialize and deserialize portDashboardTerminals correctly" in {
      val input = Map("lhr" -> Set("t2", "t3"), "bhx" -> Set("t2"))
      val serialized = UserPreferencesSerialisation.serializePortDashboardTerminals(input)

      val deserialized = UserPreferencesSerialisation.deserializePortDashboardTerminals(Option(serialized))
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

      val serialized = UserPreferencesSerialisation.toUserPreferencesJson(userPreferences)
      println(s"serialized UserPreferences: $serialized")
      val deserialized = UserPreferencesSerialisation.fromJson(serialized)

      deserialized mustEqual userPreferences
    }

    "deserialize 'lhr:30' to Map[String, Int]" in {
      val input = "lhr:30;bhx:60"
      val expected = Map("lhr" -> 30, "bhx" -> 60)

      val deserialized = UserPreferencesSerialisation.deserializePortDashboardIntervalMinutes(Some(input))
      deserialized mustEqual expected
    }

    "serialize portDashboardIntervalMinutes correctly" in {
      val input = Map("port1" -> 10, "port2" -> 20)
      val userPreferences = UserPreferences(
        userSelectedPlanningTimePeriod = 30,
        hidePaxDataSourceDescription = true,
        showStaffingShiftView = false,
        desksAndQueuesIntervalMinutes = 15,
        portDashboardIntervalMinutes = input,
        portDashboardTerminals = Map.empty
      )

      val expected = "port1:10;port2:20"
      userPreferences.serializedPortDashboardIntervalMinutes mustEqual expected
    }

    "serialize portDashboardTerminals correctly" in {
      val input = Map("lhr" -> Set("t2", "t3"), "bhx" -> Set("t2"))
      val userPreferences = UserPreferences(
        userSelectedPlanningTimePeriod = 30,
        hidePaxDataSourceDescription = true,
        showStaffingShiftView = false,
        desksAndQueuesIntervalMinutes = 15,
        portDashboardIntervalMinutes = Map.empty,
        portDashboardTerminals = input
      )

      val expected = "lhr:t2,t3;bhx:t2"
      userPreferences.serializedPortDashboardTerminals mustEqual expected
    }
  }
}