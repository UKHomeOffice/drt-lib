package uk.gov.homeoffice.drt

case class ShiftMeta(
                      port: String,
                      terminal: String,
                      shiftAssignmentsMigratedAt: Option[java.sql.Timestamp],
                      latestShiftAppliedAt: Option[java.sql.Timestamp]
                    )
