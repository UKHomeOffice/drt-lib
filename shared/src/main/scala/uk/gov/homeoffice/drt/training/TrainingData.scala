package uk.gov.homeoffice.drt.training


import upickle.default._

case class TrainingData(id: Option[Int], uploadTime: Long, fileName: Option[String], title: Option[String], markdownContent: String)

object TrainingData {

  implicit val rw: ReadWriter[TrainingData] = macroRW

  def getTrainingDataConversion(string: String): Seq[TrainingData] = read[Seq[TrainingData]](string)
}
