package uk.gov.homeoffice.drt.db.dao

import slick.dbio.Effect
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import slick.sql.FixedSqlStreamingAction
import uk.gov.homeoffice.drt.ShiftStaffRolling
import uk.gov.homeoffice.drt.db.CentralDatabase
import uk.gov.homeoffice.drt.db.tables.{ShiftStaffRollingRow, ShiftStaffRollingTable}

import java.sql.{Date, Timestamp}
import scala.concurrent.{ExecutionContext, Future}


trait IShiftStaffRollingDaoLike {
  def upsertShiftStaffRolling(shiftStaffRolling: ShiftStaffRolling)(implicit ex: ExecutionContext): Future[Int]

  def getShiftStaffRolling(port: String, terminal: String)(implicit ex: ExecutionContext): Future[Seq[ShiftStaffRolling]]
}

case class ShiftStaffRollingDao(central: CentralDatabase) extends IShiftStaffRollingDaoLike {
  val shiftStaffRollingTable: TableQuery[ShiftStaffRollingTable] = TableQuery[ShiftStaffRollingTable]

//  def insertShiftStaffRolling(shiftStaffRolling: ShiftStaffRolling)(implicit ex: ExecutionContext): Future[Int] = {
//    val query = shiftStaffRollingTable += ShiftStaffRollingRow(shiftStaffRolling.port,
//      shiftStaffRolling.terminal,
//      new Date(shiftStaffRolling.rollingStartedDate),
//      new Date(shiftStaffRolling.rollingEndedDate),
//      new Timestamp(shiftStaffRolling.updatedAt),
//      shiftStaffRolling.appliedBy)
//    central.db.run(query)
//  }


  def upsertShiftStaffRolling(shiftStaffRolling: ShiftStaffRolling)(implicit ex: ExecutionContext): Future[Int] = {
    val row = ShiftStaffRollingRow(
      shiftStaffRolling.port,
      shiftStaffRolling.terminal,
      new Date(shiftStaffRolling.rollingStartedDate),
      new Date(shiftStaffRolling.rollingEndedDate),
      new Timestamp(shiftStaffRolling.updatedAt),
      shiftStaffRolling.appliedBy
    )
    val insertOrUpdate = shiftStaffRollingTable
      .insertOrUpdate(row)
    central.db.run(insertOrUpdate)
  }


  override def getShiftStaffRolling(port: String, terminal: String)(implicit ex: ExecutionContext): Future[Seq[ShiftStaffRolling]] = {
    val query = shiftStaffRollingTable.filter(row => row.port === port && row.terminal === terminal)
    val action: FixedSqlStreamingAction[Seq[ShiftStaffRollingRow], ShiftStaffRollingRow, Effect.Read] = query.result
    central.db.run(action)
      .map(rows => rows.map(row => ShiftStaffRolling(
        row.port,
        row.terminal,
        row.rollingStartedDate.getTime,
        row.rollingEndedDate.getTime,
        row.updatedAt.getTime,
        row.appliedBy)))
  }
}
