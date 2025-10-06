package uk.gov.homeoffice.drt.db.dao

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import uk.gov.homeoffice.drt.ShiftStaffRolling
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.db.TestDatabase.profile.api._
import uk.gov.homeoffice.drt.db.tables.ShiftStaffRollingRow
import uk.gov.homeoffice.drt.time.SDate
import uk.gov.homeoffice.drt.time.TimeZoneHelper.europeLondonTimeZone

import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class ShiftStaffRollingDaoSpec extends Specification with BeforeEach {
  sequential

  val dao: ShiftStaffRollingDao = ShiftStaffRollingDao(TestDatabase)

  override def before: Unit = {
    Await.result(
      TestDatabase.run(DBIO.seq(
        dao.shiftStaffRollingTable.schema.dropIfExists,
        dao.shiftStaffRollingTable.schema.createIfNotExists)
      ), 2.second)
  }

  val currentTimeInMillis: Long = Instant.now().toEpochMilli

  val startDate = SDate("2024-06-01", europeLondonTimeZone).millisSinceEpoch
  val endDate = SDate("2024-06-02", europeLondonTimeZone).millisSinceEpoch

  val currentTimestamp = new java.sql.Timestamp(currentTimeInMillis)

  def getShiftStaffRolling: ShiftStaffRolling =
    ShiftStaffRolling(
      port = "LHR",
      terminal = "T5",
      rollingStartedDate = startDate,
      rollingEndedDate = endDate,
      updatedAt = currentTimeInMillis,
      appliedBy = "auto-roll"
    )

  def getShiftStaffRollingRow: ShiftStaffRollingRow =
    ShiftStaffRollingRow(
      port = "LHR",
      terminal = "T5",
      rollingStartedDate = new java.sql.Date(startDate),
      rollingEndedDate = new java.sql.Date(endDate),
      updatedAt = currentTimestamp,
      appliedBy = "auto-roll"
    )

  "getShiftMetaInfo" should {
    "insert or select a shift meta info " in {
      val shiftStaffRolling = getShiftStaffRolling

      val insertResult = Await.result(dao.upsertShiftStaffRolling(shiftStaffRolling), 1.second)

      insertResult === 1

      val selectResult: Seq[ShiftStaffRolling] = Await.result(dao.getShiftStaffRolling("LHR", "T5"), 1.second)

      selectResult.head === shiftStaffRolling
    }
  }
}