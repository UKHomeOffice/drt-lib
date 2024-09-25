package uk.gov.homeoffice.drt.db.dao

import slick.lifted.TableQuery
import uk.gov.homeoffice.drt.db.tables.{PortTerminalShiftConfig, PortTerminalShiftConfigRow, PortTerminalShiftConfigTable}
import uk.gov.homeoffice.drt.ports.PortCode
import uk.gov.homeoffice.drt.db.Db.slickProfile.api._
import uk.gov.homeoffice.drt.db.serialisers.PortTerminalShiftConfigSerialiser
import uk.gov.homeoffice.drt.ports.Terminals.Terminal

import scala.concurrent.ExecutionContext

object PortTerminalShiftConfigDao {
  val table: TableQuery[PortTerminalShiftConfigTable] = TableQuery[PortTerminalShiftConfigTable]

  def insertOrUpdate(portCode: PortCode): PortTerminalShiftConfig => DBIOAction[Int, NoStream, Effect.Write] =
    config => {
      if (config.port == portCode) table.insertOrUpdate(PortTerminalShiftConfigSerialiser.toRow(config))
      else DBIO.successful(0)
    }

  def get(portCode: PortCode)
         (implicit ec: ExecutionContext): Terminal => DBIOAction[Option[PortTerminalShiftConfig], NoStream, Effect.Read] =
    terminal =>
      filterPortTerminalDate(portCode, terminal)
        .result
        .map { rows =>
          rows.map(PortTerminalShiftConfigSerialiser.fromRow).headOption
        }

  private def filterPortTerminalDate(portCode: PortCode, terminal: Terminal): Query[PortTerminalShiftConfigTable, PortTerminalShiftConfigRow, Seq] =
    table
      .filter(row =>
        row.port === portCode.iata &&
          row.terminal === terminal.toString
      )
}
