package uk.gov.homeoffice.drt.db.dao

import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import uk.gov.homeoffice.drt.db.CentralDatabase
import uk.gov.homeoffice.drt.db.tables.{StaffShiftRow, StaffShiftsTable}

import java.sql.Date
import scala.concurrent.Future

trait IStaffShiftsDao {
  def insertOrUpdate(staffShiftRow: StaffShiftRow): Future[Int]

  def updateStaffShift(previousStaffShiftRow: StaffShiftRow, staffShiftRow: StaffShiftRow): Future[Int]

  def getStaffShiftsByPort(port: String): Future[Seq[StaffShiftRow]]

  def getStaffShiftsByPortAndTerminal(port: String, terminal: String): Future[Seq[StaffShiftRow]]

  def getStaffShiftByPortAndTerminalAndShiftName(port: String, terminal: String, shiftName: String): Future[Option[StaffShiftRow]]

  def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int]

  def deleteStaffShifts(): Future[Int]
}

case class StaffShiftsDao(db: CentralDatabase) extends IStaffShiftsDao {
  val staffShiftsTable: TableQuery[StaffShiftsTable] = TableQuery[StaffShiftsTable]

  override def insertOrUpdate(staffShiftRow: StaffShiftRow): Future[Int] =
    db.run(staffShiftsTable.insertOrUpdate(staffShiftRow))

  def updateStaffShift(previousStaffShiftRow: StaffShiftRow, staffShiftRow: StaffShiftRow): Future[Int] = {
    val deleteAction = staffShiftsTable
      .filter(row =>
        row.port === previousStaffShiftRow.port &&
          row.terminal === previousStaffShiftRow.terminal &&
          row.shiftName === previousStaffShiftRow.shiftName &&
          row.startDate === previousStaffShiftRow.startDate &&
          row.startTime === previousStaffShiftRow.startTime
      ).delete

    val insertAction = staffShiftsTable += staffShiftRow

    if (staffShiftRow.startDate.getTime > new Date(System.currentTimeMillis()).getTime) {
      // If the new shift is in the future, we can just insert it
      db.run(staffShiftsTable.insertOrUpdate(previousStaffShiftRow.copy(endDate = Option(staffShiftRow.startDate))).andThen(insertAction))
    } else {
      db.run(deleteAction.andThen(insertAction))
    }
  }

  override def getStaffShiftsByPort(port: String): Future[Seq[StaffShiftRow]] =
    db.run(staffShiftsTable.filter(_.port === port).result)

  override def getStaffShiftsByPortAndTerminal(port: String, terminal: String): Future[Seq[StaffShiftRow]] =
    db.run(staffShiftsTable.filter(s => s.port === port && s.terminal === terminal).result)

  override def getStaffShiftByPortAndTerminalAndShiftName(port: String, terminal: String, shiftName: String): Future[Option[StaffShiftRow]] =
    db.run(staffShiftsTable.filter(s => s.port === port && s.terminal === terminal && s.shiftName.toLowerCase === shiftName.toLowerCase).result.headOption)

  override def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int] =
    db.run(staffShiftsTable.filter(row => row.port === port && row.terminal === terminal && row.shiftName === shiftName).delete)

  override def deleteStaffShifts(): Future[Int] = db.run(staffShiftsTable.delete)
}
