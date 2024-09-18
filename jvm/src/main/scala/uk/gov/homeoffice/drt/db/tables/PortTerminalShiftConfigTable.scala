package uk.gov.homeoffice.drt.db.tables

import slick.lifted.Tag
import uk.gov.homeoffice.drt.ports.PortCode
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

case class PortTerminalShiftConfig(port: PortCode,
                                   terminal: Terminal,
                                   shiftName: String,
                                   startAt: Long,
                                   periodInMinutes: Int,
                                   endAt: Option[Long],
                                   frequency: Option[String],
                                   actualStaff: Option[Int],
                                   minimumRosteredStaff: Option[Int],
                                   updatedAt: Long,
                                   email: String
                                  )

case class PortTerminalShiftConfigRow(port: String,
                                      terminal: String,
                                      shiftName: String,
                                      startAt: Timestamp,
                                      periodInMinutes: Int,
                                      endAt: Option[Timestamp],
                                      frequency: Option[String],
                                      actualStaff: Option[Int],
                                      minimumRosteredStaff: Option[Int],
                                      updatedAt: Timestamp,
                                      email: String)

class PortTerminalShiftConfigTable(tag: Tag) extends Table[PortTerminalShiftConfigRow](tag, "port_terminal_shift_config") {

  def port: Rep[String] = column[String]("port")

  def terminal: Rep[String] = column[String]("terminal")

  def startAt: Rep[Timestamp] = column[Timestamp]("start_at")

  def periodInMinutes: Rep[Int] = column[Int]("period_in_minutes")

  def endAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("end_at")

  def shiftName: Rep[String] = column[String]("shift_name")

  def frequency: Rep[Option[String]] = column[Option[String]]("frequency")

  def actualStaff: Rep[Option[Int]] = column[Option[Int]]("actual_staff")

  def minimumRosteredStaff: Rep[Option[Int]] = column[Option[Int]]("minimum_rostered_staff")

  def updatedAt: Rep[Timestamp] = column[Timestamp]("updated_at")

  def email: Rep[String] = column[String]("email")

  val pk = primaryKey("port_terminal_shift_config_pkey", (port, terminal, startAt))

  def * = (port, terminal, shiftName, startAt, periodInMinutes, endAt, frequency, actualStaff, minimumRosteredStaff, updatedAt, email) <> (PortTerminalShiftConfigRow.tupled, PortTerminalShiftConfigRow.unapply)
}
