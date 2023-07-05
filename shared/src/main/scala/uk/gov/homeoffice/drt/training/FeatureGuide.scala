package uk.gov.homeoffice.drt.training


import upickle.default._

case class FeatureGuide(id: Option[Int], uploadTime: Long, fileName: Option[String], title: Option[String], markdownContent: String)

object FeatureGuide {

  implicit val rw: ReadWriter[FeatureGuide] = macroRW

  def getFeatureGuideConversion(string: String): Seq[FeatureGuide] = read[Seq[FeatureGuide]](string)
}
