package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.SplitRatiosNs.SplitSources
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.SplitSources.ApiSplitsWithHistoricalEGateAndFTPercentages
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.SDateLike
import upickle.default.{ReadWriter, macroRW}


trait WithLastUpdated {
  def lastUpdated: Option[Long]
}

object ApiFlightWithSplits {
  implicit val rw: ReadWriter[ApiFlightWithSplits] = macroRW

  def fromArrival(arrival: Arrival): ApiFlightWithSplits = ApiFlightWithSplits(arrival, Set())
}

case class ApiFlightWithSplits(apiFlight: Arrival, splits: Set[Splits], lastUpdated: Option[Long] = None)
  extends WithUnique[UniqueArrival]
    with Updatable[ApiFlightWithSplits]
    with WithLastUpdated {

  def totalPaxFromApi: Option[TotalPaxSource] = splits.collectFirst {
    case splits if splits.source == ApiSplitsWithHistoricalEGateAndFTPercentages =>
      TotalPaxSource(ApiFeedSource, Passengers(Option(Math.round(splits.totalPax).toInt), None))
  }

  def totalPaxFromApiExcludingTransfer: Option[TotalPaxSource] =
    splits.collectFirst { case splits if splits.source == ApiSplitsWithHistoricalEGateAndFTPercentages =>
      TotalPaxSource(ApiFeedSource, Passengers(Option(Math.round(splits.totalExcludingTransferPax).toInt), None))
    }

  def pcpPaxEstimate: TotalPaxSource =
    totalPaxFromApiExcludingTransfer match {
      case Some(totalPaxSource) if hasValidApi => totalPaxSource
      case _ => apiFlight.bestPcpPaxEstimate
    }

  def totalPax: Option[TotalPaxSource] =
    if (hasValidApi) totalPaxFromApi
    else bestSource

  def equals(candidate: ApiFlightWithSplits): Boolean =
    this.copy(lastUpdated = None) == candidate.copy(lastUpdated = None)

  def bestSource: Option[TotalPaxSource] = {
    this.apiFlight.FeedSources match {
      case feedSource if feedSource.contains(LiveFeedSource) =>
        apiFlight.TotalPax.get(LiveFeedSource).map(TotalPaxSource(LiveFeedSource, _))
      case feedSource if feedSource.contains(ForecastFeedSource) =>
        apiFlight.TotalPax.get(ForecastFeedSource).map(TotalPaxSource(ForecastFeedSource, _))
      case feedSource if feedSource.contains(AclFeedSource) =>
        apiFlight.TotalPax.get(AclFeedSource).map(TotalPaxSource(AclFeedSource, _))
      case _ =>
        None
    }
  }

  def bestSplits: Option[Splits] = {
    val apiSplitsDc = splits.find(s => s.source == SplitSources.ApiSplitsWithHistoricalEGateAndFTPercentages)
    val scenarioSplits = splits.find(s => s.source == SplitSources.ScenarioSimulationSplits)
    val historicalSplits = splits.find(_.source == SplitSources.Historical)
    val terminalSplits = splits.find(_.source == SplitSources.TerminalAverage)

    val apiSplits: List[Option[Splits]] = if (hasValidApi) List(apiSplitsDc) else List(scenarioSplits)

    val splitsForConsideration: List[Option[Splits]] = apiSplits ::: List(historicalSplits, terminalSplits)

    splitsForConsideration.find {
      case Some(_) => true
      case _ => false
    }.flatten
  }

  val hasApi: Boolean = splits.exists(_.source == SplitSources.ApiSplitsWithHistoricalEGateAndFTPercentages)

  def hasValidApi: Boolean = {
    val maybeApiSplits = splits.find(_.source == SplitSources.ApiSplitsWithHistoricalEGateAndFTPercentages)
    val totalPaxSourceIntroductionMillis = 1655247600000L // 2022-06-15 midnight BST

    val paxSourceAvailable = apiFlight.Scheduled >= totalPaxSourceIntroductionMillis
    val hasLiveSource = if (paxSourceAvailable)
      apiFlight.TotalPax.get(LiveFeedSource).exists(_.actual.nonEmpty)
    else
      apiFlight.FeedSources.contains(LiveFeedSource)

    val hasSimulationSource = apiFlight.FeedSources.contains(ScenarioSimulationSource)
    (maybeApiSplits, hasLiveSource, hasSimulationSource) match {
      case (Some(_), _, true) => true
      case (Some(_), false, _) => true
      case (Some(api), true, _) if isWithinThreshold(api) => true
      case _ => false
    }
  }

  def isWithinThreshold(apiSplits: Splits): Boolean = {
    val apiPaxNo = apiSplits.totalExcludingTransferPax
    val threshold: Double = 0.05
    val portDirectPax: Double = apiFlight.bestPcpPaxEstimate.getPcpPax.getOrElse(0).toDouble
    apiPaxNo != 0 && Math.abs(apiPaxNo - portDirectPax) / apiPaxNo < threshold
  }

  def hasPcpPaxIn(start: SDateLike, end: SDateLike): Boolean = apiFlight.hasPcpDuring(start, end)

  override val unique: UniqueArrival = apiFlight.unique

  override def update(incoming: ApiFlightWithSplits): ApiFlightWithSplits =
    incoming.copy(apiFlight = apiFlight.update(incoming.apiFlight))
}
