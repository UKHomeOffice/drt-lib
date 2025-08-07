package uk.gov.homeoffice.drt.db.dao

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import slick.dbio.DBIO
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.time.LocalDate

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import TestDatabase.profile.api._
import uk.gov.homeoffice.drt.Shift
import uk.gov.homeoffice.drt.util.ShiftUtil
import uk.gov.homeoffice.drt.util.ShiftUtil.{localDateAddDays, localDateAddMonth}

import scala.concurrent.ExecutionContext.Implicits.global

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

  def getStaffShiftRow: Shift = {
    Shift(
      port = "LHR",
      terminal = "T5",
      shiftName = "Morning",
      startDate = LocalDate(2021, 1, 1),
      startTime = "08:00",
      endTime = "16:00",
      endDate = None,
      staffNumber = 10,
      createdBy = Some("test@drt.com"),
      frequency = Some("Daily"),
      createdAt = Instant.now().toEpochMilli
    )
  }

  "StaffShiftsDao" should {
    "insert or update a staff shift" in {
      val staffShiftsDao = StaffShiftsDao(TestDatabase)
      val staffShiftRow = getStaffShiftRow

      val insertResult = Await.result(staffShiftsDao.insertOrUpdate(staffShiftRow), 1.second)
      insertResult === 1

      val selectResult: Seq[Shift] = Await.result(staffShiftsDao.getStaffShiftsByPortAndTerminal("LHR", "T5"), 1.second)

      val expectedResultShift = selectResult.map(s => staffShiftRow.copy(createdAt = s.createdAt))
      selectResult === expectedResultShift
    }

    "retrieve staff shifts by port" in {
      val staffShiftsDao = StaffShiftsDao(TestDatabase)
      val staffShiftRow = getStaffShiftRow

      Await.result(staffShiftsDao.insertOrUpdate(staffShiftRow), 1.second)

      val selectResult = Await.result(staffShiftsDao.getStaffShiftsByPort("LHR"), 1.second)

      val expectedResultShift = selectResult.map(s => staffShiftRow.copy(createdAt = s.createdAt))

      selectResult.size === 1
      selectResult.head === expectedResultShift.head
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
      val currentDate = LocalDate(2025, 7, 1)
      val staffShift = getStaffShiftRow.copy(port = port, terminal = terminal, shiftName = shiftName, startDate = LocalDate(2021, 7, 1))

      Await.result(dao.insertOrUpdate(staffShift), 1.second)
      val result = Await.result(
        dao.searchStaffShift(port, terminal, shiftName, currentDate),
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
        dao.searchStaffShift(
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
        startDate = LocalDate(2020, 12, 31),
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
      val oneMonthLater = localDateAddMonth(baseShift.startDate, 1)
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
      val oneMonthEarlier = localDateAddMonth(baseShift.startDate, -1)
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
        startDate = LocalDate(2020, 12, 31),
        endDate = Some(searchShift.startDate)
      )
      Await.result(dao.insertOrUpdate(edgeShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must not contain ("EdgeCase")
    }

    "getOverlappingStaffShifts should not return shift where endDate is before startDate" in {
      val searchShift = getStaffShiftRow
      val invalidShift = searchShift.copy(
        shiftName = "Invalid",
        startDate = LocalDate(2020, 12, 31),
        endDate = Some(LocalDate(2020, 12, 30))
      )
      Await.result(dao.insertOrUpdate(invalidShift), 1.second)
      val result = Await.result(
        dao.getOverlappingStaffShifts(searchShift.port, searchShift.terminal, searchShift),
        1.second
      )
      result.map(_.shiftName) must not contain ("Invalid")
    }

    "getOverlappingStaffShifts should return multiple overlapping shifts" in {
      val searchShift = getStaffShiftRow
      val overlap1 = searchShift.copy(
        shiftName = "Overlap1",
        startDate = LocalDate(2020, 12, 30),
        endDate = None
      )
      val overlap2 = searchShift.copy(
        shiftName = "Overlap2",
        startDate = LocalDate(2020, 12, 29),
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

    "updateStaffShift should set endDate to a day before new startDate if previous shift has no endDate and spans today" in {
      val today = ShiftUtil.currentLocalDate
      val prevShift = getStaffShiftRow.copy(startDate = localDateAddMonth(today, -1), endDate = None)
      val newShift = getStaffShiftRow.copy(startDate = localDateAddMonth(today, 1), endDate = None)

      Await.result(dao.insertOrUpdate(prevShift), 1.second)
      val result = Await.result(dao.updateStaffShift(prevShift, newShift), 1.second)
      val updatedPrev = Await.result(dao.searchStaffShift(prevShift.port, prevShift.terminal, prevShift.shiftName, prevShift.startDate), 1.second)

      result === 1
      val expectedEndDate = localDateAddDays(localDateAddMonth(today, 1), -1)
      updatedPrev.flatMap(_.endDate) must beSome(expectedEndDate)

    }

    "updateStaffShift should delete and insert when previous shift has endDate or does not span today" in {
      val prevShift = getStaffShiftRow.copy(startDate = LocalDate(2020, 1, 1), endDate = Some(LocalDate(2020, 1, 2)))
      val newShift = getStaffShiftRow.copy(startDate = LocalDate(2025, 1, 1), endDate = None)

      Await.result(dao.insertOrUpdate(prevShift), 1.second)
      val result = Await.result(dao.updateStaffShift(prevShift, newShift), 1.second)
      val old = Await.result(dao.searchStaffShift(prevShift.port, prevShift.terminal, prevShift.shiftName, prevShift.startDate), 1.second)
      val inserted = Await.result(dao.searchStaffShift(newShift.port, newShift.terminal, newShift.shiftName, newShift.startDate), 1.second)

      result === 1
      old must beNone
      inserted.isDefined === true
    }

    "updateStaffShift should update previous shift endDate to start date minus one day of new shift and also search shift for different shiftDates" in {
      val prevShiftInAug = getStaffShiftRow.copy(startDate = LocalDate(2025, 8, 1), endDate = None)
      val newShiftRequestedWithOctStartDate = getStaffShiftRow.copy(startDate = LocalDate(2025, 10, 1), endDate = None)
      Await.result(dao.insertOrUpdate(prevShiftInAug), 1.second)
      val updatedShiftsResults = Await.result(dao.updateStaffShift(prevShiftInAug, newShiftRequestedWithOctStartDate), 1.second)
      val prevShiftInAugResultAfterUpdates = Await.result(dao.searchStaffShift(prevShiftInAug.port, prevShiftInAug.terminal, prevShiftInAug.shiftName, prevShiftInAug.startDate), 1.second)
      val newShiftResultWithOctStartDate = Await.result(dao.searchStaffShift(newShiftRequestedWithOctStartDate.port, newShiftRequestedWithOctStartDate.terminal, newShiftRequestedWithOctStartDate.shiftName, newShiftRequestedWithOctStartDate.startDate), 1.second)

      val searchShiftForStartDateInSep = getStaffShiftRow.copy(startDate = LocalDate(2025, 9, 1))
      val searchResultForStartDateInSep = Await.result(dao.searchStaffShift(searchShiftForStartDateInSep.port, searchShiftForStartDateInSep.terminal , searchShiftForStartDateInSep.shiftName, searchShiftForStartDateInSep.startDate), 1.second)
      val searchShiftForStartDateInNov = getStaffShiftRow.copy(startDate = LocalDate(2025, 11, 1))
      val searchResultForStartDateInNov = Await.result(dao.searchStaffShift(searchShiftForStartDateInNov.port, searchShiftForStartDateInNov.terminal , searchShiftForStartDateInNov.shiftName, searchShiftForStartDateInNov.startDate), 1.second)

      updatedShiftsResults === 1
      prevShiftInAugResultAfterUpdates.get === prevShiftInAug.copy(endDate = Some(LocalDate(2025, 9, 30))) // Has endDate set to the day before new shift start date
      newShiftResultWithOctStartDate.isDefined === true
      newShiftResultWithOctStartDate.get === newShiftRequestedWithOctStartDate.copy(createdAt = newShiftResultWithOctStartDate.get.createdAt)
      searchResultForStartDateInSep.get === prevShiftInAugResultAfterUpdates.get // Should return the updated previous shift with endDate set
      searchResultForStartDateInNov.get === newShiftResultWithOctStartDate.get // Should return the new shift with startDate in October
    }
  }
}
