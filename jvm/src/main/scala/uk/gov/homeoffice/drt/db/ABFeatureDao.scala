package uk.gov.homeoffice.drt.db

import slick.lifted.{ProvenShape, TableQuery, Tag}
import uk.gov.homeoffice.drt.ABFeature
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.concurrent.Future

trait IABFeatureDao {
  def getABFeatures: Future[Seq[ABFeatureRow]]
  def getABFeatureByFunctionName(functionName: String): Future[Seq[ABFeatureRow]]
}

case class ABFeatureRow(presented_at: Timestamp, function_name: String, test_type: String) {
  def toABFeature = ABFeature(presented_at.getTime, function_name, test_type)
}

class ABFeatureTable(tag: Tag) extends Table[ABFeatureRow](tag, "ab_feature") {

  def presentedAt = column[java.sql.Timestamp]("presented_at")

  def functionName = column[String]("function_name")

  def testType = column[String]("test_type")

  val pk = primaryKey("ab_feature_pkey", (presentedAt, functionName))

  def * : ProvenShape[ABFeatureRow] = (presentedAt, functionName, testType).mapTo[ABFeatureRow]
}

case class ABFeatureDao(db: Database) extends IABFeatureDao {
  val abFeatureTable: TableQuery[ABFeatureTable] = TableQuery[ABFeatureTable]

  def getABFeatures: Future[Seq[ABFeatureRow]] = {
    db.run(abFeatureTable.result).mapTo[Seq[ABFeatureRow]]
  }

  override def getABFeatureByFunctionName(functionName: String): Future[Seq[ABFeatureRow]] = {
    db.run(abFeatureTable.filter(_.functionName === functionName).result).mapTo[Seq[ABFeatureRow]]
  }
}
