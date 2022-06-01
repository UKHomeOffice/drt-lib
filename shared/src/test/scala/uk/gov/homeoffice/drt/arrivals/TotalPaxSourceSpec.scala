package uk.gov.homeoffice.drt.arrivals

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports.{AclFeedSource, LiveFeedSource}

import scala.collection.SortedSet

class TotalPaxSourceSpec extends Specification {
  "TotalPax sorted by total passenger numbers" >> {
    val set = SortedSet(TotalPaxSource(12, LiveFeedSource, None),
      TotalPaxSource(1, LiveFeedSource, None),
      TotalPaxSource(13, LiveFeedSource, None),
      TotalPaxSource(23, AclFeedSource, None))

    set.head === TotalPaxSource(13,LiveFeedSource,None)
  }

}
