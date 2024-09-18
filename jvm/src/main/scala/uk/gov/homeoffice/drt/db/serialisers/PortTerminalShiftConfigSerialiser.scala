package uk.gov.homeoffice.drt.db.serialisers

import uk.gov.homeoffice.drt.db.tables.{PortTerminalShiftConfig, PortTerminalShiftConfigRow}
import uk.gov.homeoffice.drt.ports.PortCode
import uk.gov.homeoffice.drt.ports.Terminals.Terminal

import java.sql.Timestamp

object PortTerminalShiftConfigSerialiser {
  val toRow: PortTerminalShiftConfig => PortTerminalShiftConfigRow = {
    case PortTerminalShiftConfig(
    portCode, terminal, shiftName, startAt, period, endAt, frequency, actualStaff, minimumRosteredStaff, updatedAt, email) =>
      PortTerminalShiftConfigRow(
        portCode.iata,
        terminal.toString,
        shiftName,
        new Timestamp(startAt),
        period,
        endAt.map(new Timestamp(_)),
        frequency,
        actualStaff,
        minimumRosteredStaff,
        new Timestamp(updatedAt),
        email
      )
  }

  val fromRow: PortTerminalShiftConfigRow => PortTerminalShiftConfig = {
    case PortTerminalShiftConfigRow(port, terminal, shiftName, startAt, period, endAt, frequency, actualStaff,
    minimumRosteredStaff, updatedAt, email) =>
      PortTerminalShiftConfig(
        PortCode(port),
        Terminal(terminal),
        shiftName,
        startAt.getTime,
        period,
        endAt.map(_.getTime),
        frequency,
        actualStaff,
        minimumRosteredStaff,
        updatedAt.getTime,
        email
      )
  }
}
