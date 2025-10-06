package uk.gov.homeoffice.drt

case class ShiftStaffRolling(port: String,
                             terminal: String,
                             rollingStartedDate: Long,
                             rollingEndedDate: Long,
                             updatedAt: Long,
                             appliedBy: String)
