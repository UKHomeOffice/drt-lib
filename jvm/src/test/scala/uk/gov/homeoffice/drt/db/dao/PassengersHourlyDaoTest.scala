package uk.gov.homeoffice.drt.db.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.db.serialisers.PassengersHourlySerialiser
import uk.gov.homeoffice.drt.db.tables.PassengersHourly
import uk.gov.homeoffice.drt.ports.PortCode
import uk.gov.homeoffice.drt.ports.Queues.{EGate, EeaDesk, FastTrack, NonEeaDesk}
import uk.gov.homeoffice.drt.ports.Terminals.{T1, T2, T3, Terminal}
import uk.gov.homeoffice.drt.time.{LocalDate, SDate, UtcDate}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class PassengersHourlyDaoTest extends AnyWordSpec with Matchers with BeforeAndAfter {
  import TestDatabase.profile.api._

  private val dao: PassengersHourlyDao.type = PassengersHourlyDao

  SchemaUtils.printStatements(dao.table.schema.createStatements)

  before {
    Await.result(
      TestDatabase.run(DBIO.seq(
        dao.table.schema.dropIfExists,
        dao.table.schema.createIfNotExists)
      ), 2.second)
  }

  "PassengersHourlyQueries replaceHours" should {
    "insert records into an empty table" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val paxHourly = List(
        PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, terminal, EGate, UtcDate(2020, 1, 1), 2, 2),
        PassengersHourly(portCode, terminal, NonEeaDesk, UtcDate(2020, 1, 1), 3, 3),
      )
      val paxHourlyToInsert = paxHourly.map(ph => PassengersHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourlyToInsert)), 2.second)

      val rows = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(PassengersHourlySerialiser.fromRow) should be(paxHourly.toSet)
    }

    "replace existing queues are replaced with new records for same time periods" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val paxHourly = List(
        PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, terminal, EGate, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, terminal, NonEeaDesk, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val paxHourlyUpdate = List(
        PassengersHourly(portCode, terminal, FastTrack, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, terminal, FastTrack, UtcDate(2020, 1, 1), 2, 2),
      ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))
      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourlyUpdate)), 2.second)

      val expected = List(
        PassengersHourly(portCode, terminal, FastTrack, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, terminal, FastTrack, UtcDate(2020, 1, 1), 2, 2),
        PassengersHourly(portCode, terminal, NonEeaDesk, UtcDate(2020, 1, 1), 3, 3),
      )

      val rows = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => PassengersHourlySerialiser.fromRow(ph)) should be(expected.toSet)
    }

    "only insert/replace entries for the port and terminal specified" in {
      val portCode = PortCode("LHR")
      val otherPortCode = PortCode("JFK")
      val terminal = T2
      val otherTerminal = T1
      val paxHourly = List(
        PassengersHourly(otherPortCode, otherTerminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(otherPortCode, otherTerminal, EGate, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(otherPortCode, otherTerminal, NonEeaDesk, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val rows = TestDatabase.run(dao.get(otherPortCode.iata, otherTerminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => PassengersHourlySerialiser.fromRow(ph)) should be(Set())
    }

    "only insert/replace entries for the port specified" in {
      val portCode = PortCode("LHR")
      val otherPortCode = PortCode("JFK")
      val terminal = T2
      val paxHourly = List(
        PassengersHourly(otherPortCode, terminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(otherPortCode, terminal, EGate, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(otherPortCode, terminal, NonEeaDesk, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val rows = TestDatabase.run(dao.get(otherPortCode.iata, terminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => PassengersHourlySerialiser.fromRow(ph)) should be(Set())
    }

    "only insert/replace entries for the terminal specified" in {
      val portCode = PortCode("LHR")
      val terminal = T2
      val otherTerminal = T1
      val paxHourly = List(
        PassengersHourly(portCode, otherTerminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, otherTerminal, EGate, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, otherTerminal, NonEeaDesk, UtcDate(2020, 1, 1), 3, 3),
      ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))

      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
      val rows = TestDatabase.run(dao.get(portCode.iata, otherTerminal.toString, UtcDate(2020, 1, 1).toISOString)).futureValue
      rows.toSet.map(ph => PassengersHourlySerialiser.fromRow(ph)) should be(Set())
    }
  }

  private val portCode: PortCode = PortCode("LHR")

  private def insertHourlyPax(terminal: Terminal, eeaPax: Int, egatePax: Int, date: LocalDate): Unit = {
    val sdate = SDate(date)
    val sDate = sdate.getLocalNextMidnight.addMinutes(-1)
    val utcDate = sDate.toUtcDate
    val utcDayBefore = sDate.addDays(-1).toUtcDate
    val paxHourly = List(
      PassengersHourly(portCode, terminal, EeaDesk, utcDayBefore, 22, 10),
      PassengersHourly(portCode, terminal, EeaDesk, utcDayBefore, 23, eeaPax),
      PassengersHourly(portCode, terminal, EGate, utcDate, 1, egatePax),
      PassengersHourly(portCode, terminal, EGate, utcDate, 23, 10),
    ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))
    Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)
  }

  "PassengerHourlyQueries totalForPortAndDate" should {
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
        SDate(2023, 6, 9, 23, 0).millisSinceEpoch -> Map(EeaDesk -> 50),
        SDate(2023, 6, 10, 1, 0).millisSinceEpoch -> Map(EGate -> 25))
      )

      val resultT3 = TestDatabase.run(dao.hourlyForPortAndDate(portCode.iata, Option(T3.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT3 should be(Map(
        SDate(2023, 6, 9, 23, 0).millisSinceEpoch -> Map(EeaDesk -> 100),
        SDate(2023, 6, 10, 1, 0).millisSinceEpoch -> Map(EGate -> 50))
      )
    }
  }

  "PassengerHourlyQueries queueTotalsForPortAndDate" should {
    "return the total passengers for a port and local date (spanning 2 utc dates)" in {
      insertHourlyPax(T2, 50, 25, LocalDate(2023, 6, 10))

      val result = TestDatabase.run(dao.queueTotalsForPortAndDate(portCode.iata, None)(global)(LocalDate(2023, 6, 10))).futureValue

      result should be(Map(EeaDesk -> 50, EGate -> 25))
    }

    "return the total passengers for a port and local date during GMT" in {
      insertHourlyPax(T2, 50, 25, LocalDate(2023, 1, 10))

      val result = TestDatabase.run(dao.queueTotalsForPortAndDate(portCode.iata, None)(global)(LocalDate(2023, 1, 10))).futureValue

      val before2300 = 25
      val after2300 = 10
      val expectedEgatePax = before2300 + after2300

      result should be(Map(EGate -> expectedEgatePax))
    }

    "return the total passengers for a port and local date on an october clock change date" in {
      val clockChangeDate2023 = LocalDate(2023, 10, 29)
      insertHourlyPax(T2, 50, 25, clockChangeDate2023)

      val result = TestDatabase.run(dao.queueTotalsForPortAndDate(portCode.iata, None)(global)(clockChangeDate2023)).futureValue

      val before2300 = 25
      val after2300 = 10
      val expectedEgatePax = before2300 + after2300

      result should be(Map(EeaDesk -> 50, EGate -> expectedEgatePax))
    }

    "return the total passengers for a port, terminal and local date (spanning 2 utc dates)" in {
      val portCode = PortCode("LHR")
      insertHourlyPax(T2, 50, 25, LocalDate(2023, 6, 10))
      insertHourlyPax(T3, 100, 50, LocalDate(2023, 6, 10))

      val resultT2 = TestDatabase.run(dao.queueTotalsForPortAndDate(portCode.iata, Option(T2.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT2 should be(Map(EeaDesk -> 50, EGate -> 25))

      val resultT3 = TestDatabase.run(dao.queueTotalsForPortAndDate(portCode.iata, Option(T3.toString))(global)(LocalDate(2023, 6, 10))).futureValue

      resultT3 should be(Map(EeaDesk -> 100, EGate -> 50))
    }
  }

  "PassengersHourlyDao removeAllBefore" should {
    "only remove rows with a date earlier than the data specified" in {
      val portCode = PortCode("LHR")
      val terminal = T2

      val paxHourly = List(
        PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1),
        PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 2), 2, 2),
        PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 3), 3, 3),
      ).map(ph => PassengersHourlySerialiser.toRow(ph, 0L))
      Await.result(TestDatabase.run(dao.replaceHours(portCode)(terminal, paxHourly)), 2.second)

      Seq(
        (1, Seq(PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 1), 1, 1))),
        (2, Seq(PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 2), 2, 2))),
        (3, Seq(PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 3), 3, 3))),
      ).map {
        case (day, expected) =>
          val result = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, day).toISOString)).futureValue
          result.toSet.map(PassengersHourlySerialiser.fromRow) should be(expected.toSet)
      }

      val date = UtcDate(2020, 1, 3)
      Await.result(TestDatabase.run(dao.removeAllBefore(date)), 2.second)

      Seq(
        (1, Seq.empty),
        (2, Seq.empty),
        (3, Seq(PassengersHourly(portCode, terminal, EeaDesk, UtcDate(2020, 1, 3), 3, 3))),
      ).map {
        case (day, expected) =>
          val result = TestDatabase.run(dao.get(portCode.iata, terminal.toString, UtcDate(2020, 1, day).toISOString)).futureValue
          result.toSet.map(PassengersHourlySerialiser.fromRow) should be(expected.toSet)
      }
    }
  }
}
