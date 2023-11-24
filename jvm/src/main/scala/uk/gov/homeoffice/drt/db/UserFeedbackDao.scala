package uk.gov.homeoffice.drt.db

import slick.lifted.{ProvenShape, TableQuery, Tag}
import slick.jdbc.PostgresProfile.api._
import uk.gov.homeoffice.drt.feedback.UserFeedback

import scala.concurrent.{ExecutionContext, Future}

case class UserFeedbackRow(email: String,
                           actionedAt: java.sql.Timestamp,
                           feedbackAt: Option[java.sql.Timestamp],
                           closeBanner: Boolean,
                           feedbackType: Option[String],
                           bfRole: String,
                           drtQuality: String,
                           drtLikes: Option[String],
                           drtImprovements: Option[String],
                           participationInterest: Boolean,
                           aOrBTest: Option[String]) {
  def toUserFeedback = UserFeedback(email, actionedAt.getTime, feedbackAt.map(_.getTime), closeBanner, feedbackType, bfRole, drtQuality, drtLikes, drtImprovements, participationInterest, aOrBTest)
}


class UserFeedbackTable(tag: Tag) extends Table[UserFeedbackRow](tag, "user_feedback") {

  def email = column[String]("email")

  def actionedAt = column[java.sql.Timestamp]("actioned_at")

  def feedbackAt = column[Option[java.sql.Timestamp]]("feedback_at")

  def closeBanner = column[Boolean]("close_banner")

  def feedbackType = column[Option[String]]("feedback_type")

  def bfRole = column[String]("bf_role")

  def drtQuality = column[String]("drt_quality")

  def drtLikes = column[Option[String]]("drt_likes")

  def drtImprovements = column[Option[String]]("drt_improvements")

  def participationInterest = column[Boolean]("participation_interest")

  def aORbTest = column[Option[String]]("a_or_b_test")

  val pk = primaryKey("user_feedback_pkey", (email, actionedAt))

  def * : ProvenShape[UserFeedbackRow] = (email, actionedAt, feedbackAt, closeBanner, feedbackType, bfRole, drtQuality, drtLikes, drtImprovements, participationInterest, aORbTest).mapTo[UserFeedbackRow]
}

trait IUserFeedbackDao {
  def insertOrUpdate(userFeedbackRow: UserFeedbackRow): Future[Int]

  def selectAll()(implicit executionContext: ExecutionContext): Future[Seq[UserFeedbackRow]]

  def selectByEmail(email: String): Future[Seq[UserFeedbackRow]]

}

case class UserFeedbackDao(db: Database) extends IUserFeedbackDao {
  val userFeedbackTable: TableQuery[UserFeedbackTable] = TableQuery[UserFeedbackTable]

  def insertOrUpdate(userFeedbackRow: UserFeedbackRow): Future[Int] = {
    db.run(userFeedbackTable insertOrUpdate userFeedbackRow)
  }

  def selectAll()(implicit executionContext: ExecutionContext): Future[Seq[UserFeedbackRow]] = {
    db.run(userFeedbackTable.result).mapTo[Seq[UserFeedbackRow]]
  }

  def selectByEmail(email: String): Future[Seq[UserFeedbackRow]] = db.run(userFeedbackTable.filter(_.email === email).result)
}
