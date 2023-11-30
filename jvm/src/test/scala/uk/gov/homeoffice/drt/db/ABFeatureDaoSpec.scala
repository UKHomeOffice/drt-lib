package uk.gov.homeoffice.drt.db

import org.specs2.mutable.Specification
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import java.time.Instant

class ABFeatureDaoSpec extends Specification {

  sequential

  lazy val db = TestDatabase.db

  def setup() = {
    Await.result(
      db.run(DBIO.seq(
        TestDatabase.abFeatureTable.schema.dropIfExists,
        TestDatabase.abFeatureTable.schema.createIfNotExists)
      ), 2.second)
  }

  def getABFeatureRow() = {
    ABFeatureRow(email = "test@test.com",
      functionName = "feedback",
      presentedAt = new Timestamp(Instant.now().toEpochMilli),
      testType = "A")
  }

  "ABFeatureDao" should {
    "should return a list of AB Features" in {
      setup()
      val abFeatureDao = ABFeatureDao(TestDatabase.db)
      val abFeatureRow = getABFeatureRow()

      Await.result(abFeatureDao.insertOrUpdate(abFeatureRow), 1.second)
      val abFeatureSelectResult = Await.result(abFeatureDao.getABFeatures, 1.second)

      abFeatureSelectResult.size === 1
      abFeatureSelectResult.head === abFeatureRow
    }

    "should return AB Features for a given functionName" in {
      setup()
      val abFeatureDao = ABFeatureDao(TestDatabase.db)
      val abFeatureRow = getABFeatureRow()

      val arrivalFeature = abFeatureRow.copy(functionName = "arrival")
      Await.result(abFeatureDao.insertOrUpdate(abFeatureRow), 1.second)
      Await.result(abFeatureDao.insertOrUpdate(arrivalFeature), 1.second)

      val abFeatureSelectResult = Await.result(abFeatureDao.getABFeatureByFunctionName("arrival"), 1.second)

      abFeatureSelectResult.size === 1
      abFeatureSelectResult.head === arrivalFeature
    }

    "should return AB Features for a given functionName and email" in {
      setup()
      val abFeatureDao = ABFeatureDao(TestDatabase.db)
      val abFeatureRow = getABFeatureRow()

      val arrivalFeature = abFeatureRow.copy(functionName = "arrival")
      val arrivalFeature2 = abFeatureRow.copy(functionName = "arrival",email="test1@test.com")
      Await.result(abFeatureDao.insertOrUpdate(abFeatureRow), 1.second)
      Await.result(abFeatureDao.insertOrUpdate(arrivalFeature), 1.second)

      val abFeatureSelectResult = Await.result(abFeatureDao.getABFeaturesByEmailForFunction("test@test.com","arrival"), 1.second)

      abFeatureSelectResult.size === 1
      abFeatureSelectResult.head === arrivalFeature
    }
  }
}
