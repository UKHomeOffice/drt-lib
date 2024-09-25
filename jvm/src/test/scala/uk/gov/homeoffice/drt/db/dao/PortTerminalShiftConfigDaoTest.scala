package uk.gov.homeoffice.drt.db.dao

import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import specs2.arguments.sequential
import uk.gov.homeoffice.drt.db.TestDatabase
import uk.gov.homeoffice.drt.db.tables.PortTerminalShiftConfig
import uk.gov.homeoffice.drt.ports.{PortCode, Terminals}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class PortTerminalShiftConfigDaoTest extends AnyFlatSpec with Matchers with BeforeAndAfter {
  sequential

  private val db = TestDatabase.db

  import TestDatabase.profile.api._

  before {
    Await.result(
      db.run(DBIO.seq(
        PortTerminalShiftConfigDao.table.schema.dropIfExists,
        PortTerminalShiftConfigDao.table.schema.createIfNotExists)
      ), 2.second)
  }

  "insertOrUpdate" should "insert a new config when none exists" in {
    val portCode = PortCode("LHR")
    val terminal = Terminals.T1
    val config = PortTerminalShiftConfig(portCode, terminal, "shifts-1", 1L, 30, Some(2L), Some("daily"), Some(2), Some(1), 1L, "test.com")
    val action = PortTerminalShiftConfigDao.insertOrUpdate(portCode)(config)
    val result = Await.result(db.run(action.transactionally), 1.second)
    result shouldBe 1
  }

  "get" should "return a config when one exists" in {
    val portCode = PortCode("LHR")
    val terminal = Terminals.T1
    val config = PortTerminalShiftConfig(portCode, Terminals.T1, "shifts-1", 1L, 30, Some(2L), Some("daily"), Some(2), Some(1), 1L, "test.com")
    val insertAction = PortTerminalShiftConfigDao.insertOrUpdate(portCode)(config)
    db.run(insertAction.transactionally)
    val get = PortTerminalShiftConfigDao.get(portCode)
    val maybePortTerminalConfig = Await.result(db.run(get(terminal)), 1.second)
    maybePortTerminalConfig should be(Some(config))
  }

  it should "return a updated config" in {
    val portCode = PortCode("LHR")
    val terminal = Terminals.T1
    val config = PortTerminalShiftConfig(portCode, Terminals.T1, "shifts-1", 1L, 30, Some(2L), Some("daily"), Some(2), Some(1), 1L, "test.com")
    val insertAction = PortTerminalShiftConfigDao.insertOrUpdate(portCode)(config)
    db.run(insertAction.transactionally)
    val get = PortTerminalShiftConfigDao.get(portCode)
    val maybePortTerminalConfig = Await.result(db.run(get(terminal)), 1.second)
    maybePortTerminalConfig should be(Some(config))
    val updatedConfig = config.copy(shiftName = "shifts-2")
    val updateAction = PortTerminalShiftConfigDao.insertOrUpdate(portCode)(updatedConfig)
    db.run(updateAction.transactionally)
    val updatedPortTerminalConfig = Await.result(db.run(get(terminal)), 1.second)
    updatedPortTerminalConfig should be(Some(updatedConfig))

  }

  it should "return None when no config exists" in {
    val portCode = PortCode("LHR")
    val terminal = Terminals.T1
    val get = PortTerminalShiftConfigDao.get(portCode)
    val result = Await.result(db.run(get(terminal)), 1.second)
    result shouldBe None
  }

  it should "return None when a config exists for a different terminal" in {
    val portCode = PortCode("LHR")
    val config = PortTerminalShiftConfig(portCode, Terminals.T1, "shifts-1", 1L, 30, Some(2L), Some("daily"), Some(2), Some(1), 1L, "test.com")
    val insertAction = PortTerminalShiftConfigDao.insertOrUpdate(portCode)(config)
    db.run(insertAction.transactionally)
    val getAction = PortTerminalShiftConfigDao.get(portCode)
    val result = Await.result(db.run(getAction(Terminals.T2)), 1.second)
    result shouldBe None
  }
}
