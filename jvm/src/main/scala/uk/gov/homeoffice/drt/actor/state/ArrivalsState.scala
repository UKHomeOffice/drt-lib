package uk.gov.homeoffice.drt.actor.state

import uk.gov.homeoffice.drt.arrivals.{Arrival, ArrivalLike, UniqueArrival}
import uk.gov.homeoffice.drt.feeds.{FeedSourceStatuses, FeedStateLike}
import uk.gov.homeoffice.drt.ports.FeedSource

import scala.collection.immutable.SortedMap

case class ArrivalsState(arrivals: SortedMap[UniqueArrival, ArrivalLike],
                         feedSource: FeedSource,
                         maybeSourceStatuses: Option[FeedSourceStatuses]) extends FeedStateLike {
  def clear(): ArrivalsState = {
    copy(arrivals = SortedMap(), maybeSourceStatuses = None)
  }

  def ++(incoming: Iterable[ArrivalLike]): ArrivalsState = {
    copy(arrivals = arrivals ++ incoming.map(a => (a.unique, arrivals.get(a.unique).map(_.update(a)).getOrElse(a))))
  }

  def ++(incoming: Iterable[ArrivalLike], statuses: Option[FeedSourceStatuses]): ArrivalsState =
    ++(incoming).copy(maybeSourceStatuses = statuses)
}

object ArrivalsState {
  def empty(feedSource: FeedSource): ArrivalsState = ArrivalsState(SortedMap(), feedSource, None)
}
