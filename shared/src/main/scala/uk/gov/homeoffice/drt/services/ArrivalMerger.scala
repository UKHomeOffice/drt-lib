package uk.gov.homeoffice.drt.services

import uk.gov.homeoffice.drt.arrivals.{ApiFlightWithSplits, Arrival, ForecastArrival, LiveArrival, MergedArrival}

object ArrivalMerger {
  def merge(arrivals: Seq[Arrival]): Arrival = arrivals.reduce((a1, a2) => merge(a1, a2))

  def merge(arrival1: Arrival, arrival2: Arrival): Arrival = {
    val arrival = arrival2 match {
      case a: ForecastArrival => a.toMergedArrival
      case a: LiveArrival => a.toMergedArrival
      case a: MergedArrival => a
    }
    arrival.copy(
      BaggageReclaimId = if (arrival2.BaggageReclaimId.exists(_.nonEmpty)) arrival2.BaggageReclaimId else arrival1.BaggageReclaimId,
      Stand = if (arrival2.Stand.exists(_.nonEmpty)) arrival2.Stand else arrival1.Stand,
      Gate = if (arrival2.Gate.exists(_.nonEmpty)) arrival2.Gate else arrival1.Gate,
      RedListPax = if (arrival2.RedListPax.nonEmpty) arrival2.RedListPax else arrival1.RedListPax,
      MaxPax = if (arrival2.MaxPax.nonEmpty) arrival2.MaxPax else arrival1.MaxPax,
      PassengerSources = arrival1.PassengerSources ++ arrival2.PassengerSources,
      FeedSources = arrival1.FeedSources ++ arrival2.FeedSources
    )
  }

  def merge(flight1: ApiFlightWithSplits, flight2: ApiFlightWithSplits): ApiFlightWithSplits = {
    val mergedArrival = merge(flight1.apiFlight, flight2.apiFlight)
    flight2.copy(apiFlight = mergedArrival)
  }
}
