package uk.gov.homeoffice.drt.db.tables

import java.sql.Timestamp
import slick.lifted.Tag
import uk.gov.homeoffice.drt.db.Db.slickProfile.api._

case class StaffShiftRow(
                          port: String,
                          terminal: String,
                          shiftName: String,
                          startTime: String,
                          endTime: String,
                          staffNumber: Int,
                          createdBy: Option[String],
                          frequency: Option[String],
                          createdAt: Timestamp
                        )

class StaffShiftsTable(tag: Tag) extends Table[StaffShiftRow](tag, "staff_shifts") {
  def port: Rep[String] = column[String]("port")

  def terminal: Rep[String] = column[String]("terminal")

  def shiftName: Rep[String] = column[String]("shift_name")

  def startTime: Rep[String] = column[String]("start_time")

  def endTime: Rep[String] = column[String]("end_time")

  def staffNumber: Rep[Int] = column[Int]("staff_number")

  def createdBy: Rep[Option[String]] = column[Option[String]]("created_by")

  def frequency: Rep[Option[String]] = column[Option[String]]("frequency")

  def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

  val pk = primaryKey("staff_shifts_pkey", (port, terminal, shiftName))

  def * = (
    port,
    terminal,
    shiftName,
    startTime,
    endTime,
    staffNumber,
    createdBy,
    frequency,
    createdAt
  ).mapTo[StaffShiftRow]
}