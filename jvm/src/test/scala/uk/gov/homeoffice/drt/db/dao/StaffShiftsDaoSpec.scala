package uk.gov.homeoffice.drt.db.dao

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import slick.dbio.DBIO
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.db.tables.StaffShiftRow
import uk.gov.homeoffice.drt.time.SDate

import java.sql.{Date, Timestamp}
import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import TestDatabase.profile.api._

class StaffShiftsDaoSpec extends Specification with BeforeEach {
  sequential

  val dao: StaffShiftsDao = StaffShiftsDao(TestDatabase)

  override def before: Unit = {
    Await.result(
      TestDatabase.run(DBIO.seq(
        dao.staffShiftsTable.schema.dropIfExists,
        dao.staffShiftsTable.schema.createIfNotExists)
      ), 2.second)
  }

  def getStaffShiftRow: StaffShiftRow = {
    StaffShiftRow(
      port = "LHR",
      terminal = "T5",
      shiftName = "Morning",
      startDate = new Date(SDate("2021-01-01").millisSinceEpoch),
      startTime = "08:00",
      endTime = "16:00",
      endDate = None,
      staffNumber = 10,
      createdBy = Some("test@drt.com"),
      frequency = Some("Daily"),
      createdAt = new Timestamp(Instant.now().toEpochMilli)
    )
  }

  "StaffShiftsDao" should {
    "insert or update a staff shift" in {
      val staffShiftsDao = StaffShiftsDao(TestDatabase)
      val staffShiftRow = getStaffShiftRow

      val insertResult = Await.result(staffShiftsDao.insertOrUpdate(staffShiftRow), 1.second)
      insertResult === 1

      val selectResult = Await.result(staffShiftsDao.getStaffShiftsByPortAndTerminal("LHR", "T5"), 1.second)

      selectResult === Seq(staffShiftRow)
    }

    "retrieve staff shifts by port" in {
      val staffShiftsDao = StaffShiftsDao(TestDatabase)
      val staffShiftRow = getStaffShiftRow

      Await.result(staffShiftsDao.insertOrUpdate(staffShiftRow), 1.second)

      val selectResult = Await.result(staffShiftsDao.getStaffShiftsByPort("LHR"), 1.second)
      selectResult.size === 1
      selectResult.head === staffShiftRow
    }

    "delete a staff shift" in {
      val staffShiftsDao = StaffShiftsDao(TestDatabase)
      val staffShiftRow = getStaffShiftRow

      Await.result(staffShiftsDao.insertOrUpdate(staffShiftRow), 1.second)

      val deleteResult = Await.result(staffShiftsDao.deleteStaffShift("LHR", "T5", "Morning"), 1.second)
      deleteResult === 1

      val selectResult = Await.result(staffShiftsDao.getStaffShiftsByPortAndTerminal("LHR", "T5"), 1.second)
      selectResult.isEmpty === true
    }

    "getStaffShiftByPortAndTerminal should return the correct shift for startDate 01-07-2025" in {
      val port = "LHR"
      val terminal = "T5"
      val shiftName = "Early"
      val startDate = java.sql.Date.valueOf("2025-07-01")
      val currentDate = new java.sql.Date(System.currentTimeMillis())
      val staffShift = getStaffShiftRow.copy(port = port, terminal = terminal, shiftName = shiftName, startDate = new Date(SDate("2021-07-01").millisSinceEpoch))

      Await.result(dao.insertOrUpdate(staffShift), 1.second)
      val result = Await.result(
        dao.getStaffShiftByPortAndTerminal(port, terminal, shiftName, currentDate),
        1.second
      )
      // result.get.copy(createdAt = staffShift.createdAt) === staffShift
          result.isDefined === true
      val retrievedShift = result.get
      retrievedShift.port === staffShift.port
      retrievedShift.terminal === staffShift.terminal
      retrievedShift.shiftName === staffShift.shiftName
      retrievedShift.startDate.toString === staffShift.startDate.toString()
      retrievedShift.startTime === staffShift.startTime
      retrievedShift.endTime === staffShift.endTime
      retrievedShift.endDate === staffShift.endDate
      retrievedShift.staffNumber === staffShift.staffNumber
      retrievedShift.createdBy === staffShift.createdBy
      retrievedShift.frequency === staffShift.frequency
    }

    "getStaffShiftByPortAndTerminal should return the correct shift" in {
      val staffShiftRow = getStaffShiftRow
      Await.result(dao.insertOrUpdate(staffShiftRow), 1.second)
      val result = Await.result(
        dao.getStaffShiftByPortAndTerminal(
          staffShiftRow.port,
          staffShiftRow.terminal,
          staffShiftRow.shiftName,
          staffShiftRow.startDate
        ),
        1.second
      )
      result.isDefined === true
      result.get === staffShiftRow
    }

    "getOverlappingStaffShifts should return overlapping shifts for searchShift" in {
      val baseShift, searchShift = getStaffShiftRow
      val overlappingShift = baseShift.copy(
        shiftName = "Overlapping",
        startDate = new Date(SDate("2020-12-31").millisSinceEpoch),
        startTime = "09:00",
        endTime = "17:00"
      )
      Await.result(dao.insertOrUpdate(baseShift), 1.second)
      Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.startTime) must contain(allOf(baseShift.startTime, overlappingShift.startTime))

      result.size === 2
      
    }

    "getOverlappingStaffShifts should not return shifts where start date is later than search shift and end Date empty one month or later" in {
      val baseShift, searchShift = getStaffShiftRow
      val oneMonthLater = new Date(SDate(baseShift.startDate.getTime).addMonths(1).millisSinceEpoch)
      val overlappingShift = baseShift.copy(
        shiftName = "MonthLater",
        startDate = oneMonthLater,
        startTime = "09:00",
        endTime = "17:00"
      )
      Await.result(dao.insertOrUpdate(baseShift), 1.second)
      Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must contain(baseShift.shiftName)
      result.size === 1
    }

    "getOverlappingStaffShifts should not return shifts where start date is before search shift and end Date previous one month" in {
      val baseShift, searchShift = getStaffShiftRow
      //      val oneMonthLater = new Date(SDate(baseShift.startDate.getTime).addMonths(1).millisSinceEpoch)
      val oneMonthEarlier = new Date(SDate(baseShift.startDate.getTime).addMonths(-1).millisSinceEpoch)
      val overlappingShift = baseShift.copy(
        shiftName = "MonthLater",
        endDate = Option(oneMonthEarlier),
        startTime = "09:00",
        endTime = "17:00"
      )
      Await.result(dao.insertOrUpdate(baseShift), 1.second)
      Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must contain(baseShift.shiftName)
      result.size === 1
    }


    "getOverlappingStaffShifts time overlap logic" should {
      
      "return shifts that start before search shift ends and end after search shift starts" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val overlappingShift = searchShift.copy(
          shiftName = "Overlapping1",
          startTime = "08:00",
          endTime = "12:00"
        )
        
        Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "Overlapping1"
      }

      "return shifts that start before search shift starts and end after search shift starts" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val overlappingShift = searchShift.copy(
          shiftName = "Overlapping2",
          startTime = "08:00",
          endTime = "11:00"
        )
        
        Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "Overlapping2"
      }

      "return shifts that start before search shift ends and end after search shift ends" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val overlappingShift = searchShift.copy(
          shiftName = "Overlapping3",
          startTime = "13:00",
          endTime = "16:00"
        )
        
        Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "Overlapping3"
      }

      "return shifts that completely contain the search shift" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val overlappingShift = searchShift.copy(
          shiftName = "ContainingShift",
          startTime = "08:00",
          endTime = "16:00"
        )
        
        Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "ContainingShift"
      }

      "return shifts that are completely contained within the search shift" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "08:00",
          endTime = "16:00"
        )
        val overlappingShift = searchShift.copy(
          shiftName = "ContainedShift",
          startTime = "10:00",
          endTime = "14:00"
        )
        
        Await.result(dao.insertOrUpdate(overlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "ContainedShift"
      }

      "not return shifts that end exactly when search shift starts" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val nonOverlappingShift = searchShift.copy(
          shiftName = "NonOverlapping1",
          startTime = "08:00",
          endTime = "10:00",
          startDate = new Date(SDate("2021-03-01").millisSinceEpoch)
        )
        
        Await.result(dao.insertOrUpdate(nonOverlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 0
      }

      "not return shifts that start date previous" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00",
          startDate = new Date(SDate("2021-03-01").millisSinceEpoch)
        )
        val nonOverlappingShift = searchShift.copy(
          shiftName = "NonOverlapping1",
          startTime = "08:00",
          endTime = "10:00",
          endDate = Some(new Date(SDate("2021-02-28").millisSinceEpoch))
        )

        val OverlappingShift = searchShift.copy(
          shiftName = "NonOverlapping1",
          startTime = "08:00",
          endTime = "10:00",
        )

        Await.result(dao.insertOrUpdate(nonOverlappingShift), 1.second)
        Await.result(dao.insertOrUpdate(OverlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )

        result.size === 1
      }

      "not return shifts that start exactly when search shift ends" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val nonOverlappingShift = searchShift.copy(
          shiftName = "NonOverlapping2",
          startTime = "14:00",
          endTime = "18:00"
        )
        
        Await.result(dao.insertOrUpdate(nonOverlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
      }

      "not return shifts that are completely before the search shift" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val nonOverlappingShift = searchShift.copy(
          shiftName = "BeforeShift",
          startTime = "06:00",
          endTime = "08:00"
        )
        
        Await.result(dao.insertOrUpdate(nonOverlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
      }

      "not return shifts that are completely after the search shift" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "10:00",
          endTime = "14:00"
        )
        val nonOverlappingShift = searchShift.copy(
          shiftName = "AfterShift",
          startTime = "16:00",
          endTime = "20:00"
        )
        
        Await.result(dao.insertOrUpdate(nonOverlappingShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
      }

      "only return shifts for the same port and terminal" in {
        val searchShift = getStaffShiftRow.copy(
          port = "LHR",
          terminal = "T5",
          startTime = "10:00",
          endTime = "14:00"
        )
        val samePortTerminalShift = searchShift.copy(
          shiftName = "SamePortTerminal",
          startTime = "12:00",
          endTime = "16:00"
        )
        val differentPortShift = searchShift.copy(
          port = "LGW",
          shiftName = "DifferentPort",
          startTime = "12:00",
          endTime = "16:00"
        )
        val differentTerminalShift = searchShift.copy(
          terminal = "T1",
          shiftName = "DifferentTerminal",
          startTime = "12:00",
          endTime = "16:00"
        )
        
        Await.result(dao.insertOrUpdate(samePortTerminalShift), 1.second)
        Await.result(dao.insertOrUpdate(differentPortShift), 1.second)
        Await.result(dao.insertOrUpdate(differentTerminalShift), 1.second)
        
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "SamePortTerminal"
      }

      "return shifts with no end date that start before or on the search shift start date" in {
        val baseDate = new Date(SDate("2021-01-01").millisSinceEpoch)
        val searchShift = getStaffShiftRow.copy(
          startDate = baseDate,
          startTime = "10:00",
          endTime = "14:00"
        )
        val openEndedShift = searchShift.copy(
          shiftName = "OpenEnded",
          startDate = new Date(SDate("2020-12-31").millisSinceEpoch),
          endDate = None,
          startTime = "12:00",
          endTime = "16:00"
        )
        
        Await.result(dao.insertOrUpdate(openEndedShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 1
        result.head.shiftName === "OpenEnded"
      }

      "not return shifts that start after the search shift start date" in {
        val baseDate = new Date(SDate("2021-01-01").millisSinceEpoch)
        val searchShift = getStaffShiftRow.copy(
          startDate = baseDate,
          startTime = "10:00",
          endTime = "14:00"
        )
        val laterShift = searchShift.copy(
          shiftName = "LaterShift",
          startDate = new Date(SDate("2021-01-02").millisSinceEpoch),
          startTime = "12:00",
          endTime = "16:00"
        )
        
        Await.result(dao.insertOrUpdate(laterShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 0
      }

      "not return shifts that end before the search shift start date" in {
        val baseDate = new Date(SDate("2021-01-01").millisSinceEpoch)
        val searchShift = getStaffShiftRow.copy(
          startDate = baseDate,
          startTime = "10:00",
          endTime = "14:00"
        )
        val expiredShift = searchShift.copy(
          shiftName = "ExpiredShift",
          startDate = new Date(SDate("2020-12-01").millisSinceEpoch),
          endDate = Some(new Date(SDate("2020-12-31").millisSinceEpoch)),
          startTime = "12:00",
          endTime = "16:00"
        )
        
        Await.result(dao.insertOrUpdate(expiredShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )
        
        result.size === 0
      }

      "return overlapping overnight shifts" in {
        val searchShift = getStaffShiftRow.copy(
          startTime = "21:00",
          endTime = "23:00"
        )
        val overnightShift = searchShift.copy(
          shiftName = "Overnight",
          startTime = "20:00",
          endTime = "06:00"
        )

        Await.result(dao.insertOrUpdate(overnightShift), 1.second)
        val result = Await.result(
          dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
          1.second
        )

        result.size === 1
        result.head.shiftName === "Overnight"
      }

    }

  }
}
