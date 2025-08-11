package uk.gov.homeoffice.drt.db.dao

import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import uk.gov.homeoffice.drt.Shift
import uk.gov.homeoffice.drt.db.CentralDatabase
import uk.gov.homeoffice.drt.db.tables.{StaffShiftRow, StaffShiftsTable}
import uk.gov.homeoffice.drt.time.LocalDate
import uk.gov.homeoffice.drt.util.ShiftUtil.{fromStaffShiftRow, toStaffShiftRow}

import java.sql.Date
import scala.concurrent.{ExecutionContext, Future}

trait IStaffShiftsDao {
  def insertOrUpdate(shift: Shift): Future[Int]

  def updateStaffShift(previousShift: Shift, shiftRow: Shift)(implicit ec: ExecutionContext): Future[Shift]

  def createNewShiftWhileEditing(previousShift: Shift, shiftRow: Shift)(implicit ec: ExecutionContext): Future[Shift]

  def getStaffShiftsByPort(port: String)(implicit ec: ExecutionContext): Future[Seq[Shift]]

  def getStaffShiftsByPortAndTerminal(port: String, terminal: String)(implicit ec: ExecutionContext): Future[Seq[Shift]]

  def getOverlappingStaffShifts(port: String, terminal: String, shift: Shift)(implicit ec: ExecutionContext): Future[Seq[Shift]]

  def searchStaffShift(port: String, terminal: String, shiftName: String, startDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[Shift]]

  def latestStaffShiftForADate(port: String, terminal: String, startDate: LocalDate, startTime: String)(implicit ec: ExecutionContext): Future[Option[Shift]]

  def isShiftAfterStartDateExists(shiftRow: Shift): Future[Boolean]

  def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int]

  def deleteStaffShifts(): Future[Int]
}

case class StaffShiftsDao(db: CentralDatabase) extends IStaffShiftsDao {
  val staffShiftsTable: TableQuery[StaffShiftsTable] = TableQuery[StaffShiftsTable]

  override def insertOrUpdate(shift: Shift): Future[Int] =
    db.run(staffShiftsTable.insertOrUpdate(toStaffShiftRow(shift.copy(createdAt = System.currentTimeMillis()))))

  def updateStaffShift(previousShift: Shift, shiftRow: Shift)(implicit ec: ExecutionContext): Future[Shift] = {
    val previousStaffShiftRow: StaffShiftRow = toStaffShiftRow(previousShift)
    val staffShiftRow: StaffShiftRow = toStaffShiftRow(shiftRow)

    val deleteAction = staffShiftsTable
      .filter(row =>
        row.port === previousStaffShiftRow.port &&
          row.terminal === previousStaffShiftRow.terminal &&
          row.shiftName === previousStaffShiftRow.shiftName &&
          row.startDate === previousStaffShiftRow.startDate &&
          row.startTime === previousStaffShiftRow.startTime
      ).delete

    if (previousShift.startDate.compare(shiftRow.startDate) == 0) {
      val updatesStaffShiftRow = if (previousStaffShiftRow.endDate.isDefined) staffShiftRow.copy(endDate = previousStaffShiftRow.endDate) else staffShiftRow.copy(endDate = None)
      val insertAction = staffShiftsTable += updatesStaffShiftRow
      db.run(deleteAction.andThen(insertAction)).map(_ => fromStaffShiftRow(updatesStaffShiftRow))
    } else {
      Future.failed(new Exception("Cannot update a shift with a different start date"))
    }
  }

  override def getStaffShiftsByPort(port: String)(implicit ec: ExecutionContext): Future[Seq[Shift]] =
    db.run(staffShiftsTable.filter(_.port === port).sortBy(_.startDate.desc).result).map(_.map(fromStaffShiftRow))

  override def getStaffShiftsByPortAndTerminal(port: String, terminal: String)(implicit ec: ExecutionContext): Future[Seq[Shift]] =
    db.run(staffShiftsTable.filter(s => s.port === port && s.terminal === terminal).sortBy(_.startDate.desc).result).map(_.map(fromStaffShiftRow))

  override def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int] =
    db.run(staffShiftsTable.filter(row => row.port === port && row.terminal === terminal && row.shiftName === shiftName).delete)

  override def deleteStaffShifts(): Future[Int] = db.run(staffShiftsTable.delete)

  override def searchStaffShift(port: String, terminal: String, shiftName: String, startDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[Shift]] = {
    val startDateSql = Date.valueOf(startDate.toString)
    db.run(staffShiftsTable.filter(s => s.port === port &&
      s.terminal === terminal &&
      s.shiftName.toLowerCase === shiftName.toLowerCase &&
      s.startDate === startDateSql
    ).sortBy(_.startDate.desc).result.headOption).map(_.map(fromStaffShiftRow))
  }

  override def getOverlappingStaffShifts(port: String, terminal: String, shift: Shift)(implicit ec: ExecutionContext): Future[Seq[Shift]] = {
    val staffShiftRow = toStaffShiftRow(shift)
    db.run(
      staffShiftsTable.filter { s =>
        s.port === port &&
          s.terminal === terminal &&
          s.startDate <= staffShiftRow.startDate &&
          (s.endDate.isEmpty || s.endDate.map(_ > staffShiftRow.startDate).getOrElse(false))
      }.sortBy(_.startDate.desc).result
    ).map(_.map(fromStaffShiftRow))
  }

  override def latestStaffShiftForADate(port: String, terminal: String, date: LocalDate, startTime: String)(implicit ec: ExecutionContext): Future[Option[Shift]] = {
    val startDateSql = Date.valueOf(date.toString)
    val (startTimeHours, startTimeMinutes) = startTime.split(":") match {
      case Array(hours, minutes) => (hours.toInt, minutes.toInt)
      case _ => throw new IllegalArgumentException(s"Invalid start time format: $startTime")
    }
    db.run(staffShiftsTable.filter(s => s.port === port && s.terminal === terminal &&
      (s.startDate === startDateSql || s.startDate <= startDateSql && (s.endDate >= startDateSql || s.endDate.isEmpty)) &&
      s.startTime >= startTime && s.startTime < f"${startTimeHours + 3}%02d:$startTimeMinutes%02d"
    ).sortBy(_.startDate.desc).result.headOption).map(_.map(fromStaffShiftRow))
  }

  override def createNewShiftWhileEditing(previousShift: Shift, newShift: Shift)(implicit ec: ExecutionContext): Future[Shift] = {
    val previousStaffShiftRow: StaffShiftRow = toStaffShiftRow(previousShift)
    val staffShiftRow: StaffShiftRow = toStaffShiftRow(newShift)

    val deleteAction = staffShiftsTable
      .filter(row =>
        row.port === previousStaffShiftRow.port &&
          row.terminal === previousStaffShiftRow.terminal &&
          row.shiftName === previousStaffShiftRow.shiftName &&
          row.startDate === previousStaffShiftRow.startDate &&
          row.startTime === previousStaffShiftRow.startTime
      ).delete

    val endDateADayBefore = new java.sql.Date(staffShiftRow.startDate.getTime - 24L * 60 * 60 * 1000)
    val updatesStaffShiftRow = previousStaffShiftRow.copy(endDate = Option(endDateADayBefore))
    val insertUpdateAction = staffShiftsTable += updatesStaffShiftRow

    val insertNewAction = staffShiftsTable += staffShiftRow
    db.run(deleteAction.andThen(insertUpdateAction).andThen(insertNewAction)).map(_ => fromStaffShiftRow(staffShiftRow))
  }

  override def isShiftAfterStartDateExists(newShift: Shift): Future[Boolean] = {
    val newShiftRow = toStaffShiftRow(newShift)
    val (startTimeHours, startTimeMinutes) = newShift.startTime.split(":") match {
      case Array(hours, minutes) => (hours.toInt, minutes.toInt)
      case _ => throw new IllegalArgumentException(s"Invalid start time format: $newShift.startTime")
    }
    db.run(
      staffShiftsTable.filter { s =>
        s.port === newShiftRow.port &&
          s.terminal === newShiftRow.terminal &&
          s.startDate >= newShiftRow.startDate &&
          s.startTime >= newShiftRow.startTime &&
          s.startTime >= newShiftRow.startTime && s.startTime < f"${startTimeHours + 3}%02d:$startTimeMinutes%02d"
      }.exists.result
    )
  }



}
