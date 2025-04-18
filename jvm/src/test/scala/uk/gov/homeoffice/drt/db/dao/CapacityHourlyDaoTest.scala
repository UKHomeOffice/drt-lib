package uk.gov.homeoffice.drt.db.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.db.serialisers.CapacityHourlySerialiser
import uk.gov.homeoffice.drt.db.tables.CapacityHourly
import uk.gov.homeoffice.drt.ports.PortCode
import uk.gov.homeoffice.drt.ports.Terminals.{T1, T2, T3, Terminal}
import uk.gov.homeoffice.drt.time.{LocalDate, SDate, UtcDate}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class CapacityHourlyDaoTest extends AnyWordSpec with Matchers with BeforeAndAfter {
  import TestDatabase.profile.api._

  private val dao: CapacityHourlyDao.type = CapacityHourlyDao

  SchemaUtils.printStatements(dao.table.schema.createStatements)

  before {
    Await.result(
      TestDatabase.run(DBIO.seq(
        dao.table.schema.dropIfExists,
        dao.table.schema.createIfNotExists)
      ), 2.second)
  }

  "CapacityHourlyQueries replaceHours" should {
    "insert records into an empty table" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val paxHourly = List(
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 2, 2),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 3, 3),
      )
      val paxHourlyToInsert = paxHourly.map(ph => CapacityHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourlyToInsert)), 2.second)

      val rows = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(CapacityHourlySerialiser.fromRow) should be(paxHourly.toSet)
    }

    "replace existing queues are replaced with new records for same time periods" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val paxHourly = List(
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))
      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)

      val paxHourlyUpdate = List(
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 2, 2),
      ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))
      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourlyUpdate)), 2.second)

      val expected = List(
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 2, 2),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 3, 3),
      )

      val rows = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => CapacityHourlySerialiser.fromRow(ph)) should be(expected.toSet)
    }

    "only insert/replace entries for the port and terminal specified" in {
      val portCode = PortCode("LHR")
      val otherPortCode = PortCode("JFK")
      val terminal = T2
      val otherTerminal = T1
      val paxHourly = List(
        CapacityHourly(otherPortCode, otherTerminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(otherPortCode, otherTerminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(otherPortCode, otherTerminal, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val rows = TestDatabase.run(dao.get(otherPortCode.iata, otherTerminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => CapacityHourlySerialiser.fromRow(ph)) should be(Set())
    }

    "only insert/replace entries for the port specified" in {
      val portCode = PortCode("LHR")
      val otherPortCode = PortCode("JFK")
      val terminal = T2
      val paxHourly = List(
        CapacityHourly(otherPortCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(otherPortCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(otherPortCode, terminal, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val rows = TestDatabase.run(dao.get(otherPortCode.iata, terminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => CapacityHourlySerialiser.fromRow(ph)) should be(Set())
    }

    "only insert/replace entries for the terminal specified" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val otherTerminal = T1
      val paxHourly = List(
        CapacityHourly(portCode, otherTerminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, otherTerminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, otherTerminal, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val rows = TestDatabase.run(dao.get(portCode.iata, otherTerminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => CapacityHourlySerialiser.fromRow(ph)) should be(Set())
    }
  }

  private val portCode: PortCode = PortCode("LHR")

  private def insertHourlyPax(terminal: Terminal, eeaPax: Int, egatePax: Int, date: LocalDate): Unit = {
    val sdate = SDate(date)
    val sDate = sdate.getLocalNextMidnight.addMinutes(-1)
    val utcDate = sDate.toUtcDate
    val utcDayBefore = sDate.addDays(-1).toUtcDate
    val paxHourly = List(
      CapacityHourly(portCode, terminal, utcDayBefore, 22, 10),
      CapacityHourly(portCode, terminal, utcDayBefore, 23, eeaPax),
      CapacityHourly(portCode, terminal, utcDate, 1, egatePax),
      CapacityHourly(portCode, terminal, utcDate, 23, 10),
    ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))
    Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
  }

  "CapacityHourlyQueries totalForPortAndDate" should {
    "return the total passengers for a port and local date (spanning 2 utc dates)" in {
      insertHourlyPax(T2, 50, 25, LocalDate(2023, 6, 10))

      val result = TestDatabase.run(dao.totalForPortAndDate(portCode.iata, None)(global)(LocalDate(2023, 6, 10))).futureValue

      result should be(75)
    }

    "return the total passengers for a port, terminal and local date (spanning 2 utc dates)" in {
      val portCode = PortCode("LHR")
      insertHourlyPax(T2, 50, 25, LocalDate(2023, 6, 10))
      insertHourlyPax(T3, 50, 25, LocalDate(2023, 6, 10))

      val resultT2 = TestDatabase.run(dao.totalForPortAndDate(portCode.iata, Option(T2.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT2 should be(75)

      val resultT3 = TestDatabase.run(dao.totalForPortAndDate(portCode.iata, Option(T3.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT3 should be(75)
    }

    "return the hourly passengers for a port, terminal and local date (spanning 2 utc dates)" in {
      val portCode = PortCode("LHR")
      insertHourlyPax(T2, 50, 25, LocalDate(2023, 6, 10))
      insertHourlyPax(T3, 100, 50, LocalDate(2023, 6, 10))

      val resultT2 = TestDatabase.run(dao.hourlyForPortAndDate(portCode.iata, Option(T2.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT2 should be(Map(
        SDate(2023, 6, 9, 23, 0).millisSinceEpoch -> 50,
        SDate(2023, 6, 10, 1, 0).millisSinceEpoch -> 25,
      ))

      val resultT3 = TestDatabase.run(dao.hourlyForPortAndDate(portCode.iata, Option(T3.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT3 should be(Map(
        SDate(2023, 6, 9, 23, 0).millisSinceEpoch -> 100,
        SDate(2023, 6, 10, 1, 0).millisSinceEpoch -> 50,
      ))
    }
  }

  "CapacityHourlyQueries removeAllBefore" should {
    "only remove rows with a date earlier than the data specified" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val capHourly = List(
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 2), 2, 2),
        CapacityHourly(portCode, terminal, UtcDate(2020, 1, 3), 3, 3),
      ).map(ph => CapacityHourlySerialiser.toRow(ph, 0L))
      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, capHourly)), 2.second)

      Seq(
        (1, Seq(CapacityHourly(portCode, terminal, UtcDate(2020, 1, 1), 1, 1))),
        (2, Seq(CapacityHourly(portCode, terminal, UtcDate(2020, 1, 2), 2, 2))),
        (3, Seq(CapacityHourly(portCode, terminal, UtcDate(2020, 1, 3), 3, 3))),
      ).map {
        case (date, expected) =>
          val rows = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, date).toISOString)).futureValue
          rows.toSet.map(CapacityHourlySerialiser.fromRow) should be(expected.toSet)
      }

      val date = UtcDate(2020, 1, 3)
      Await.result(TestDatabase.run(dao.removeAllBefore(date)), 2.second)

      Seq(
        (1, Seq.empty),
        (2, Seq.empty),
        (3, Seq(CapacityHourly(portCode, terminal, UtcDate(2020, 1, 3), 3, 3))),
      ).map {
        case (date, expected) =>
          val rows = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, date).toISOString)).futureValue
          rows.toSet.map(CapacityHourlySerialiser.fromRow) should be(expected.toSet)
      }
    }
  }
}
