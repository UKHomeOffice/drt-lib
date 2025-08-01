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
      val currentDate = new java.sql.Date(System.currentTimeMillis())
      val staffShift = getStaffShiftRow.copy(port = port, terminal = terminal, shiftName = shiftName, startDate = new Date(SDate("2021-07-01").millisSinceEpoch))

      Await.result(dao.insertOrUpdate(staffShift), 1.second)
      val result = Await.result(
        dao.getStaffShiftByPortAndTerminal(port, terminal, shiftName, currentDate),
        1.second
      )
      result.isDefined === true
      val retrievedShift = result.get
      retrievedShift.port === staffShift.port
      retrievedShift.terminal === staffShift.terminal
      retrievedShift.shiftName === staffShift.shiftName
      retrievedShift.startDate.toString === staffShift.startDate.toString
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

    "getOverlappingStaffShifts should not return shifts where start date is later than search shift and end Date empty or one month or later" in {
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
      val oneMonthEarlier = new Date(SDate(baseShift.startDate.getTime).addMonths(-1).millisSinceEpoch)
      val overlappingShift = baseShift.copy(
        shiftName = "MonthEarly",
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

    "getOverlappingStaffShifts should not return shift where endDate equals searchShift.startDate" in {
      val searchShift = getStaffShiftRow
      val edgeShift = searchShift.copy(
        shiftName = "EdgeCase",
        startDate = new Date(SDate("2020-12-31").millisSinceEpoch),
        endDate = Some(searchShift.startDate)
      )
      Await.result(dao.insertOrUpdate(edgeShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must not contain("EdgeCase")
    }

    "getOverlappingStaffShifts should not return shift where endDate is before startDate" in {
      val searchShift = getStaffShiftRow
      val invalidShift = searchShift.copy(
        shiftName = "Invalid",
        startDate = new Date(SDate("2020-12-31").millisSinceEpoch),
        endDate = Some(new Date(SDate("2020-12-30").millisSinceEpoch))
      )
      Await.result(dao.insertOrUpdate(invalidShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must not contain("Invalid")
    }

    "getOverlappingStaffShifts should return multiple overlapping shifts" in {
      val searchShift = getStaffShiftRow
      val overlap1 = searchShift.copy(
        shiftName = "Overlap1",
        startDate = new Date(SDate("2020-12-30").millisSinceEpoch),
        endDate = None
      )
      val overlap2 = searchShift.copy(
        shiftName = "Overlap2",
        startDate = new Date(SDate("2020-12-29").millisSinceEpoch),
        endDate = None
      )
      Await.result(dao.insertOrUpdate(overlap1), 1.second)
      Await.result(dao.insertOrUpdate(overlap2), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must contain(allOf("Overlap1", "Overlap2"))
    }

  }
}
