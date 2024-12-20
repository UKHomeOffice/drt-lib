package uk.gov.homeoffice.drt.db

import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import uk.gov.homeoffice.drt.db.tables.{ABFeatureTable, UserFeedbackTable}

object TestDatabase {
  val profile: JdbcProfile = slick.jdbc.H2Profile
  val db: profile.backend.Database = profile.api.Database.forConfig("h2-db")
}
