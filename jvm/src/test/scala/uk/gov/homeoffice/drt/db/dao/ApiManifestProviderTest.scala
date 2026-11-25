package uk.gov.homeoffice.drt.db.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.arrivals.VoyageNumber
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.models.{ InTransit, UniqueArrivalKey }
import uk.gov.homeoffice.drt.ports.PortCode
import uk.gov.homeoffice.drt.time.SDate

import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class ApiManifestProviderTest extends AnyWordSpec with Matchers with BeforeAndAfter {
  import TestDatabase.profile.api._

  private val arrivalKey: UniqueArrivalKey = UniqueArrivalKey(
    PortCode("BHX"),
    PortCode("ALC"),
    VoyageNumber(1222),
    SDate("2026-10-13T12:00:00Z")
  )

  before {
    Await.result(TestDatabase.run(DBIO.seq(
      sqlu"DROP TABLE IF EXISTS voyage_manifest_passenger_info",
      sqlu"""CREATE TABLE voyage_manifest_passenger_info (
        event_code VARCHAR NOT NULL,
        arrival_port_code VARCHAR NOT NULL,
        departure_port_code VARCHAR NOT NULL,
        voyage_number INTEGER NOT NULL,
        scheduled_date TIMESTAMP NOT NULL,
        document_type VARCHAR NOT NULL,
        document_issuing_country_code VARCHAR NOT NULL,
        eea_flag VARCHAR NOT NULL,
        age INTEGER NOT NULL,
        disembarkation_port_code VARCHAR,
        in_transit_flag VARCHAR NOT NULL,
        disembarkation_port_country_code VARCHAR NOT NULL,
        nationality_country_code VARCHAR NOT NULL,
        passenger_identifier VARCHAR NOT NULL
      )"""
    )), 2.seconds)
  }

  after {
    Await.result(TestDatabase.run(sqlu"DROP TABLE IF EXISTS voyage_manifest_passenger_info"), 2.seconds)
  }

  "ApiManifestProvider" should {
    val cases: List[(String, Option[String], String, Option[PortCode], Boolean)] = List(
      ("an empty port", Some(""), "N", None, false),
      ("a whitespace-only port", Some(" \t\r\n "), "N", None, false),
      ("a SQL NULL port", None, "N", None, false),
      ("a matching port", Some("BHX"), "N", Some(PortCode("BHX")), false),
      ("a different port", Some("LHR"), "N", Some(PortCode("LHR")), true),
      ("an explicitly flagged transit with an empty port", Some(""), "Y", None, true),
      ("an explicitly flagged transit with a SQL NULL port", None, "Y", None, true)
    )

    cases.foreach {
      case (description, databasePort, flag, expectedPort, expectedTransit) =>
        s"map $description without losing the manifest" in {
          val scheduled = new Timestamp(arrivalKey.scheduled.millisSinceEpoch)
          Await.result(TestDatabase.run(sqlu"""
            INSERT INTO voyage_manifest_passenger_info (
              event_code, arrival_port_code, departure_port_code, voyage_number, scheduled_date,
              document_type, document_issuing_country_code, eea_flag, age, disembarkation_port_code,
              in_transit_flag, disembarkation_port_country_code, nationality_country_code, passenger_identifier
            ) VALUES (
              'DC', ${arrivalKey.arrivalPort.iata}, ${arrivalKey.departurePort.iata},
              ${arrivalKey.voyageNumber.numeric}, $scheduled,
              'P', 'GBR', 'N', 30, $databasePort, $flag, 'GBR', 'GBR', 'passenger-1'
            )
          """), 2.seconds)

          val result = Await.result(ApiManifestProvider(TestDatabase).apply(arrivalKey), 2.seconds)

          result shouldBe defined
          val passengers = result.get.PassengerList
          passengers.size shouldBe 1
          passengers.head.DisembarkationPortCode shouldBe expectedPort
          passengers.head.InTransitFlag shouldBe InTransit(flag)
          passengers.head.isInTransit(arrivalKey.arrivalPort) shouldBe expectedTransit
        }
    }

    "return None when no matching passengers exist" in {
      Await.result(ApiManifestProvider(TestDatabase).apply(arrivalKey), 2.seconds) shouldBe None
    }
  }
}
