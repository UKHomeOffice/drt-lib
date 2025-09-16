package uk.gov.homeoffice.drt.db.tables

import slick.lifted.Tag

import java.sql.Timestamp
import slick.jdbc.PostgresProfile.api._

case class ShiftMetaInfoRow(
                             port: String,
                             terminal: String,
                             shiftAssignmentsMigratedAt: Option[java.sql.Timestamp],
                             latestShiftAppliedAt: Option[java.sql.Timestamp]
                           )

class ShiftMetaInfoTable(_tableTag: Tag) extends Table[ShiftMetaInfoRow](_tableTag, "shift_meta_info") {
  def port: Rep[String] = column[String]("port")

  def terminal: Rep[String] = column[String]("terminal")

  def shiftAssignmentsMigratedAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("shift_assignments_migrated_at")

  def latestShiftAppliedAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("latest_shift_applied_at")

  def * = (port, terminal, shiftAssignmentsMigratedAt, latestShiftAppliedAt).mapTo[ShiftMetaInfoRow]

  val pk = primaryKey("shift_meta_info_pkey", (port, terminal))
}