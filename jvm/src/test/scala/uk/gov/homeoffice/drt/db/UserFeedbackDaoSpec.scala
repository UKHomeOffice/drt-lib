package uk.gov.homeoffice.drt.db

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class UserFeedbackDaoSpec extends Specification with BeforeEach {
  sequential

  lazy val db = TestDatabase.db

  override protected def before = {
    Await.ready(
      db.run(DBIO.seq(
        TestDatabase.userFeedbackTable.schema.dropIfExists,
        TestDatabase.userFeedbackTable.schema.createIfNotExists)
      ), 2.second)
  }

  def getUserFeedBackRow(feedbackAt: Timestamp, actionedAt: Timestamp) = {
    UserFeedbackRow(email = "test@test.com",
      actionedAt = actionedAt,
      feedbackAt = feedbackAt,
      closeBanner = false,
      bfRole = "test",
      drtQuality = "Good",
      drtLikes = "Arrivals",
      drtImprovements = "Staffing",
      participationInterest = true)
  }

  "UserFeedbackDao list" >> {
    "should return a list of user feedback submitted" >> {
      val userFeedbackDao = UserFeedbackDao(TestDatabase.db)
      val userFeedbackRow = getUserFeedBackRow(new Timestamp(Instant.now().minusSeconds(60).toEpochMilli),
        new Timestamp(Instant.now().minusSeconds(60).toEpochMilli))

      Await.ready(userFeedbackDao.insertOrUpdate(userFeedbackRow), 1.second)
      val userFeedbackResult = Await.result(userFeedbackDao.selectAll(), 1.second)

      userFeedbackResult.size mustEqual 1
      userFeedbackResult.head mustEqual userFeedbackRow
    }
  }
}
