package uk.gov.homeoffice.drt.db.dao

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import uk.gov.homeoffice.drt.{Shift, ShiftMeta}
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.db.tables.ShiftMetaInfoRow
import java.time.Instant
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import TestDatabase.profile.api._

class ShiftMetaInfoDaoSpec extends Specification with BeforeEach {
  sequential

  val dao: ShiftMetaInfoDao = ShiftMetaInfoDao(TestDatabase)

  override def before: Unit = {
    Await.result(
      TestDatabase.run(DBIO.seq(
        dao.shiftMetaInfoTable.schema.dropIfExists,
        dao.shiftMetaInfoTable.schema.createIfNotExists)
      ), 2.second)
  }

  val currentTimeInMillis: Long = Instant.now().toEpochMilli

  val currentTimestamp = new java.sql.Timestamp(currentTimeInMillis)

  def getShiftMetaRow: ShiftMetaInfoRow =
    ShiftMetaInfoRow(
      port = "LHR",
      terminal = "T5",
      shiftAssignmentsMigratedAt = Some(currentTimestamp),
      latestShiftAppliedAt = None,
    )

  "getShiftMetaInfo" should {
    "insert or select a shift meta info " in {
      val row = getShiftMetaRow

      val insertResult = Await.result(dao.insertShiftMetaInfo(port = row.port,
        terminal = row.terminal,
        shiftAssignmentsMigratedAt = row.shiftAssignmentsMigratedAt,
        latestShiftAppliedAt = row.latestShiftAppliedAt), 1.second)

      insertResult === 1

      val selectResult: Option[ShiftMeta] = Await.result(dao.getShiftMetaInfo("LHR", "T5"), 1.second)

      val expectedResultShift = ShiftMeta(row.port, row.terminal, row.shiftAssignmentsMigratedAt, row.latestShiftAppliedAt)
      selectResult.get === expectedResultShift
    }

    "update shift meta info latestShiftAppliedAt column" in {
      val row = getShiftMetaRow

      val insertResult = Await.result(dao.insertShiftMetaInfo(port = row.port,
        terminal = row.terminal,
        shiftAssignmentsMigratedAt = row.shiftAssignmentsMigratedAt,
        latestShiftAppliedAt = row.latestShiftAppliedAt), 1.second)

      insertResult === 1

      val updatedLatestShiftAppliedAt = new java.sql.Timestamp(currentTimeInMillis + 10000)

      val updateResult: Option[ShiftMeta] = Await.result(dao.updateLastShiftAppliedAt("LHR", "T5", updatedLatestShiftAppliedAt), 1.second)

      val expectedResultShift = ShiftMeta(row.port, row.terminal, row.shiftAssignmentsMigratedAt, Some(updatedLatestShiftAppliedAt))
      updateResult.get === expectedResultShift

      val selectResult: Option[ShiftMeta] = Await.result(dao.getShiftMetaInfo("LHR", "T5"), 1.second)
      selectResult.get === expectedResultShift
    }

    "update shift meta info shiftAssignmentsMigratedAt column" in {
      val row = getShiftMetaRow

      val insertResult = Await.result(dao.insertShiftMetaInfo(port = row.port,
        terminal = row.terminal,
        shiftAssignmentsMigratedAt = row.shiftAssignmentsMigratedAt,
        latestShiftAppliedAt = row.latestShiftAppliedAt), 1.second)

      insertResult === 1

      val updatedShiftAssignmentsMigratedAt = new java.sql.Timestamp(currentTimeInMillis + 20000)

      val updateResult: Option[ShiftMeta] = Await.result(dao.updateShiftAssignmentsMigratedAt("LHR", "T5", Some(updatedShiftAssignmentsMigratedAt)), 1.second)

      val expectedResultShift = ShiftMeta(row.port, row.terminal, Some(updatedShiftAssignmentsMigratedAt), row.latestShiftAppliedAt)
      updateResult.get === expectedResultShift

      val selectResult: Option[ShiftMeta] = Await.result(dao.getShiftMetaInfo("LHR", "T5"), 1.second)
      selectResult.get === expectedResultShift
    }
  }
}