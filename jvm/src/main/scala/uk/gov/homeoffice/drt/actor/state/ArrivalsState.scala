package uk.gov.homeoffice.drt.actor.state

import uk.gov.homeoffice.drt.arrivals.{Arrival, UniqueArrival}
import uk.gov.homeoffice.drt.feeds.{FeedSourceStatuses, FeedStateLike}
import uk.gov.homeoffice.drt.ports.FeedSource
import uk.gov.homeoffice.drt.services.ArrivalMerger

import scala.collection.immutable.SortedMap

case class ArrivalsState(arrivals: SortedMap[UniqueArrival, Arrival],
                         feedSource: FeedSource,
                         maybeSourceStatuses: Option[FeedSourceStatuses]) extends FeedStateLike {
  def clear(): ArrivalsState = {
    copy(arrivals = SortedMap(), maybeSourceStatuses = None)
  }

  def ++(incomingArrivals: Iterable[Arrival]): ArrivalsState = {
    copy(arrivals = arrivals ++ incomingArrivals.map(incoming => {
      val maybeMerged = arrivals
        .get(incoming.unique)
        .map(existing => ArrivalMerger.merge(existing, incoming))
      (incoming.unique, maybeMerged.getOrElse(incoming))
    }))
  }

  def ++(incoming: Iterable[Arrival], statuses: Option[FeedSourceStatuses]): ArrivalsState =
    ++(incoming).copy(maybeSourceStatuses = statuses)
}

object ArrivalsState {
  def empty(feedSource: FeedSource): ArrivalsState = ArrivalsState(SortedMap(), feedSource, None)
}
