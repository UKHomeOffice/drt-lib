package uk.gov.homeoffice.drt.ports

import ujson.Value.Value
import upickle.default._

import scala.concurrent.duration.{DurationInt, FiniteDuration}

trait FeedSource {
  val name: String

  val maybeLastUpdateThreshold: Option[FiniteDuration]

  val description: Boolean => String

  val displayName: Option[String] => String = dName => dName.getOrElse(name)

  override val toString: String = getClass.getSimpleName.split("\\$").last

}

case object HistoricApiFeedSource extends FeedSource {
  val name: String = "Historic API"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = None

  val description: Boolean => String = isLiveFeedAvailable => if (isLiveFeedAvailable)
    "Historic passenger nationality and age data when available."
  else
    "Historic passenger numbers and nationality data when available."
}

case object ApiFeedSource extends FeedSource {
  val name: String = "API"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = None

  val description: Boolean => String = isLiveFeedAvailable => if (isLiveFeedAvailable)
    "Actual passenger nationality and age data when available."
  else
    "Actual passenger numbers and nationality data when available."
}

case object AclFeedSource extends FeedSource {
  val name: String = "ACL"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = Option(36.hours)

  override val displayName: Option[String] => String = dName => dName.getOrElse("Forecast schedule feed")

  val description: Boolean => String = _ => "Flight schedule for up to 6 months."
}

case object ForecastFeedSource extends FeedSource {
  val name: String = "Port forecast"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = None

  val description: Boolean => String = _ => "Updated forecast of passenger numbers."
}

case object LiveFeedSource extends FeedSource {
  val name: String = "Port live"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = Option(12.hours)

  val description: Boolean => String = isCiriumAsLiveFeedSource => if (isCiriumAsLiveFeedSource)
    "Estimated and actual arrival time updates where not available from the port operator."
  else
    "Up-to-date passenger numbers, estimated and actual arrival times, gates and stands."

}

case object ScenarioSimulationSource extends FeedSource {
  val name: String = "Scenario Simulation"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = Option(12.hours)

  val description: Boolean => String = _ => "An altered arrival to explore a simulated scenario."
}

case object LiveBaseFeedSource extends FeedSource {
  val name: String = "Cirium live"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = Option(12.hours)

  override val displayName: Option[String] => String = dName => dName.getOrElse("Live arrival feed")

  val description: Boolean => String = isLiveFeedAvailable => if (isLiveFeedAvailable)
    "Estimated and actual arrival time updates where not available from live feed."
  else
    "Estimated and actual arrival time updates."
}

case object UnknownFeedSource extends FeedSource {
  val name: String = "Unknown"

  val maybeLastUpdateThreshold: Option[FiniteDuration] = None

  val description: Boolean => String = _ => ""
}

object FeedSource {
  def feedSources: Set[FeedSource] = Set(ApiFeedSource, AclFeedSource, ForecastFeedSource, HistoricApiFeedSource, LiveFeedSource, LiveBaseFeedSource, ScenarioSimulationSource)

  def apply(feedSource: String): Option[FeedSource] = feedSources.find(fs => fs.toString == feedSource)

  def findByName(feedSource: String): Option[FeedSource] = feedSources.find(fs => fs.name == feedSource)

  implicit val feedSourceReadWriter: ReadWriter[FeedSource] =
    readwriter[Value].bimap[FeedSource](
      feedSource => feedSource.toString,
      (s: Value) => apply(s.str).getOrElse(UnknownFeedSource)
    )
}
