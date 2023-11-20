package uk.gov.homeoffice.drt.db

import slick.lifted.{ProvenShape, TableQuery, Tag}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}



class UserFeedbackTable(tag: Tag) extends Table[UserFeedbackRow](tag, "user_feedback") {

  def email = column[String]("email")

  def actionedAt = column[java.sql.Timestamp]("actioned_at")

  def feedbackAt = column[java.sql.Timestamp]("feedback_at")

  def closeBanner = column[Boolean]("close_banner")

  def bfRole = column[String]("bf_role")

  def drtQuality = column[String]("drt_quality")

  def drtLikes = column[String]("drt_likes")

  def drtImprovements = column[String]("drt_improvements")

  def participationInterest = column[Boolean]("participation_interest")

  val pk = primaryKey("user_feedback_pkey", (email, actionedAt))

  def * : ProvenShape[UserFeedbackRow] = (email, actionedAt ,feedbackAt, closeBanner, bfRole, drtQuality, drtLikes, drtImprovements, participationInterest).mapTo[UserFeedbackRow]
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
