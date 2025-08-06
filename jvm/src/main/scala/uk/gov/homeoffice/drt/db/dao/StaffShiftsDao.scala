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

  def updateStaffShift(previousShift: Shift, shiftRow: Shift)(implicit ec: ExecutionContext): Future[Int]

  def getStaffShiftsByPort(port: String)(implicit ec: ExecutionContext): Future[Seq[Shift]]

  def getStaffShiftsByPortAndTerminal(port: String, terminal: String)(implicit ec: ExecutionContext): Future[Seq[Shift]]

  def getOverlappingStaffShifts(port: String, terminal: String, shift: Shift)(implicit ec: ExecutionContext): Future[Seq[Shift]]

  def getStaffShift(port: String, terminal: String, shiftName: String, startDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[Shift]]

  def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int]

  def deleteStaffShifts(): Future[Int]
}

case class StaffShiftsDao(db: CentralDatabase) extends IStaffShiftsDao {
  val staffShiftsTable: TableQuery[StaffShiftsTable] = TableQuery[StaffShiftsTable]

  override def insertOrUpdate(shift: Shift): Future[Int] =
    db.run(staffShiftsTable.insertOrUpdate(toStaffShiftRow(shift.copy(createdAt = System.currentTimeMillis()))))

  def updateStaffShift(previousShift: Shift, shiftRow: Shift)(implicit ec: ExecutionContext): Future[Int] = {
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

    val updatesStaffShiftRow = if (previousStaffShiftRow.endDate.isDefined) staffShiftRow.copy(endDate = previousStaffShiftRow.endDate) else staffShiftRow.copy(endDate = None)

    val insertAction = staffShiftsTable += updatesStaffShiftRow

    if (staffShiftRow.startDate.getTime > new Date(System.currentTimeMillis()).getTime && previousStaffShiftRow.startDate.getTime < new Date(System.currentTimeMillis()).getTime && previousStaffShiftRow.endDate.isEmpty) {
      val endDateADayBefore = new java.sql.Date(staffShiftRow.startDate.getTime - 24L * 60 * 60 * 1000)
      db.run(staffShiftsTable.insertOrUpdate(previousStaffShiftRow.copy(endDate = Option(endDateADayBefore))).andThen(insertAction))
    } else {
      db.run(deleteAction.andThen(insertAction))
    }
  }

  override def getStaffShiftsByPort(port: String)(implicit ec: ExecutionContext): Future[Seq[Shift]] =
    db.run(staffShiftsTable.filter(_.port === port).sortBy(_.startDate.desc).result).map(_.map(fromStaffShiftRow))

  override def getStaffShiftsByPortAndTerminal(port: String, terminal: String)(implicit ec: ExecutionContext): Future[Seq[Shift]] =
    db.run(staffShiftsTable.filter(s => s.port === port && s.terminal === terminal).sortBy(_.startDate.desc).result).map(_.map(fromStaffShiftRow))

  override def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int] =
    db.run(staffShiftsTable.filter(row => row.port === port && row.terminal === terminal && row.shiftName === shiftName).delete)

  override def deleteStaffShifts(): Future[Int] = db.run(staffShiftsTable.delete)

  override def getStaffShift(port: String, terminal: String, shiftName: String, startDate: LocalDate)(implicit ec: ExecutionContext): Future[Option[Shift]] = {
    val startDateSql = Date.valueOf(startDate.toString)
    db.run(staffShiftsTable.filter(s => s.port === port && s.terminal === terminal && s.shiftName.toLowerCase === shiftName.toLowerCase &&
      (s.startDate === startDateSql || s.startDate <= startDateSql && (s.endDate >= startDateSql || s.endDate.isEmpty))
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

}
