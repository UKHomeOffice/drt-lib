package uk.gov.homeoffice.drt.db.dao

import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import uk.gov.homeoffice.drt.db.tables.{StaffShiftRow, StaffShiftsTable}
import scala.concurrent.Future

trait IStaffShiftsDao {
  def insertOrUpdate(staffShiftRow: StaffShiftRow): Future[Int]

  def getStaffShifts: Future[Seq[StaffShiftRow]]

  def getStaffShiftsByPort(port: String): Future[Seq[StaffShiftRow]]

  def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int]
}

case class StaffShiftsDao(db: Database) extends IStaffShiftsDao {
  val staffShiftsTable: TableQuery[StaffShiftsTable] = TableQuery[StaffShiftsTable]

  override def insertOrUpdate(staffShiftRow: StaffShiftRow): Future[Int] = {
    db.run(staffShiftsTable.insertOrUpdate(staffShiftRow))
  }

  override def getStaffShifts: Future[Seq[StaffShiftRow]] = {
    db.run(staffShiftsTable.result)
  }

  override def getStaffShiftsByPort(port: String): Future[Seq[StaffShiftRow]] = {
    db.run(staffShiftsTable.filter(_.port === port).result)
  }

  override def deleteStaffShift(port: String, terminal: String, shiftName: String): Future[Int] = {
    db.run(staffShiftsTable.filter(row => row.port === port && row.terminal === terminal && row.shiftName === shiftName).delete)
  }
}
