package uk.gov.homeoffice.drt.db

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import org.specs2.mutable.Specification
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class UserFeedbackDaoSpec extends Specification {
  sequential

  lazy val db = TestDatabase.db

  def setup() = {
    Await.result(
      db.run(DBIO.seq(
        TestDatabase.userFeedbackTable.schema.dropIfExists,
        TestDatabase.userFeedbackTable.schema.createIfNotExists)
      ), 2.second)
  }

  def getUserFeedBackRow(feedbackAt: Timestamp, actionedAt: Timestamp) = {
    UserFeedbackRow(email = "test@test.com",
      actionedAt = actionedAt,
      feedbackAt = Option(feedbackAt),
      closeBanner = false,
      bfRole = "test",
      drtQuality = "Good",
      drtLikes = Option("Arrivals"),
      drtImprovements = Option("Staffing"),
      participationInterest = true,
      feedbackType = Option("test"),
      aOrBTest = Option("A"))
  }

  "UserFeedbackDao" should {
    "should return a list of user feedback submitted" in {
      setup()
      val userFeedbackDao = UserFeedbackDao(TestDatabase.db)
      val userFeedbackRow = getUserFeedBackRow(new Timestamp(Instant.now().minusSeconds(60).toEpochMilli),
        new Timestamp(Instant.now().minusSeconds(60).toEpochMilli))

      Await.result(userFeedbackDao.insertOrUpdate(userFeedbackRow), 1.second)
      val userFeedbackResult = Await.result(userFeedbackDao.selectAll(), 1.second)

      userFeedbackResult.size === 1
      userFeedbackResult.head === userFeedbackRow
    }


    "should return a list of user feedback using stream" in {
      implicit val system = ActorSystem("test")
      setup()
      val userFeedbackDao = UserFeedbackDao(TestDatabase.db)
      val userFeedbackRow = getUserFeedBackRow(new Timestamp(Instant.now().minusSeconds(60).toEpochMilli),
        new Timestamp(Instant.now().minusSeconds(60).toEpochMilli))

      Await.result(userFeedbackDao.insertOrUpdate(userFeedbackRow), 1.second)
      userFeedbackDao.selectAllAsStream().runWith(Sink.seq)
        .map { userFeedbackResult =>
          userFeedbackResult.size === 1
          userFeedbackResult.head === userFeedbackRow
        }
    }

    "should return a list of user feedback for given email" in {
      setup()
      val userFeedbackDao = UserFeedbackDao(TestDatabase.db)
      val userFeedbackRow = getUserFeedBackRow(new Timestamp(Instant.now().minusSeconds(60).toEpochMilli),
        new Timestamp(Instant.now().minusSeconds(60).toEpochMilli))
     val secondRow = userFeedbackRow.copy(email="test1@test.com")
      Await.result(userFeedbackDao.insertOrUpdate(userFeedbackRow), 1.second)
      Await.result(userFeedbackDao.insertOrUpdate(secondRow), 1.second)
      userFeedbackDao.selectByEmail("test1@test.com")
        .map { userFeedbackResult =>
          userFeedbackResult.size === 1
          userFeedbackResult.head === secondRow
        }
    }
  }
}
