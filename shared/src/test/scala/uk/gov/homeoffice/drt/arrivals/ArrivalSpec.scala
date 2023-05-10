package uk.gov.homeoffice.drt.arrivals

import org.specs2.mutable.Specification
import uk.gov.homeoffice.drt.ports._

class ArrivalSpec extends Specification {
  "An Arrival" should {
    "Know it has no source of passengers when there are no sources" in {
      ArrivalGenerator.arrival(passengerSources = Map()).hasNoPaxSource shouldEqual (true)
    }
    "Know it has no source of passengers when there are no sources with a pax figure" in {
      ArrivalGenerator.arrival(passengerSources = Map(LiveFeedSource -> Passengers(None, None))).hasNoPaxSource shouldEqual (true)
    }
    "Know it has a source of passengers when there is a source with a pax figure" in {
      ArrivalGenerator.arrival(passengerSources = Map(LiveFeedSource -> Passengers(Option(100), None))).hasNoPaxSource shouldEqual (false)
    }
  }

  "Arrival bestPcpPaxEstimate" should {
    val arrivalBase = ArrivalGenerator.arrival()
    val liveFeedPaxSource = (LiveFeedSource -> Passengers(Option(10), None))
    val portForecastFeedPaxSource = (ForecastFeedSource -> Passengers(Option(10), None))
    val apiFeedPaxSource = (ApiFeedSource -> Passengers(Option(10), None))
    val historicApiFeedPaxSource = (HistoricApiFeedSource -> Passengers(Option(10), None))
    val aclFeedPaxSource = (AclFeedSource -> Passengers(Option(10), None))

    "Give LiveFeedSource as bestPcpPaxEstimate, When LiveFeedSource and ApiFeedSource is present in total pax" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(liveFeedPaxSource, apiFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(liveFeedPaxSource._1, liveFeedPaxSource._2)
    }

    "Give Forecast feed source as bestPcpPaxEstimate, " +
      "When Forecast feed source and HistoricApiFeedSource is present in total pax" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(historicApiFeedPaxSource, portForecastFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(portForecastFeedPaxSource._1, portForecastFeedPaxSource._2)
    }

    "Give Forecast feed source as bestPcpPaxEstimate, " +
      "When Forecast feed source and Acl Feed source is present in total pax set" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(aclFeedPaxSource, portForecastFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(portForecastFeedPaxSource._1, portForecastFeedPaxSource._2)
    }

    "When totalPax " +
      " contain all feed source" +
      " bestPcpPaxEstimate gives Live feed source" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(
        liveFeedPaxSource,
        portForecastFeedPaxSource,
        apiFeedPaxSource,
        historicApiFeedPaxSource,
        aclFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(liveFeedPaxSource._1, liveFeedPaxSource._2)
    }

    "When totalPax" +
      " does not contain Live feed source" +
      " bestPcpPaxEstimate gives apiFeedSource" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(
        portForecastFeedPaxSource,
        apiFeedPaxSource,
        historicApiFeedPaxSource,
        aclFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(apiFeedPaxSource._1, apiFeedPaxSource._2)
    }

    "When totalPax " +
      " does not contain Live feed source and ApiFeedSource" +
      " bestPcpPaxEstimate gives port forecast feed" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(
        portForecastFeedPaxSource,
        historicApiFeedPaxSource,
        aclFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(portForecastFeedPaxSource._1, portForecastFeedPaxSource._2)
    }

    "When totalPax" +
      " does not contain Live feed source, ApiFeedSource and port forecast feed source" +
      " bestPcpPaxEstimate gives api with historic feed" in {
      val arrival = arrivalBase.copy(PassengerSources = Map(
        historicApiFeedPaxSource,
        aclFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(historicApiFeedPaxSource._1, historicApiFeedPaxSource._2)
    }

    "When totalPax" +
      " does not contain Live feed source, ApiFeedSource ," +
      " HistoricApiFeedSource , port forecast feed source and ApiFeed Source without splits" +
      " bestPcpPaxEstimate gives aclFeed " in {
      val arrival = arrivalBase.copy(PassengerSources = Map(aclFeedPaxSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(aclFeedPaxSource._1, aclFeedPaxSource._2)
    }

    "When totalPax" +
      " for a LiveFeedSource is less than Transfer passenger numbers and AclFeedSource is more than Transfer passenger number," +
      " then bestPcpPaxEstimate gives LiveFeedSource with zero pax " in {
      val arrival = arrivalBase.copy(
        PassengerSources = Map(AclFeedSource -> Passengers(Option(250), None), LiveFeedSource -> Passengers(Option(50), Option(100))))
      arrival.bestPaxEstimate mustEqual BestPaxSource(LiveFeedSource, Passengers(Option(0), Option(0)))
    }

    "When totalPax" +
      " does not contain any SourceData," +
      " then bestPcpPaxEstimate fallback to FeedSource " in {
      val arrival = arrivalBase.copy(PassengerSources = Map(AclFeedSource -> Passengers(Option(10), None)), FeedSources = Set(AclFeedSource))
      arrival.bestPaxEstimate mustEqual BestPaxSource(aclFeedPaxSource._1, aclFeedPaxSource._2)
    }

    "fallBackToFeedSource should return known pax when the arrival feed sources contain one of live, forecast or acl" >> {
      "when there is no act or trans we should get TotalPaxSource(None, _)" >> {
        val arrival = ArrivalGenerator.arrival(feedSources = Set(LiveFeedSource))

        arrival.fallBackToFeedSource === Option(BestPaxSource(LiveFeedSource, Passengers(None, None)))
      }

      "when there is no act but some trans we should get TotalPaxSource(None, _)" >> {
        val arrival = ArrivalGenerator.arrival(feedSources = Set(LiveFeedSource), passengerSources = Map(LiveFeedSource -> Passengers(actual = None, transit = Option(100))))

        arrival.fallBackToFeedSource === Option(BestPaxSource(LiveFeedSource, Passengers(None, None)))
      }

      "when there is some act but no trans we should get TotalPaxSource(Some(act), _)" >> {
        val arrival = ArrivalGenerator.arrival(feedSources = Set(LiveFeedSource), passengerSources = Map(LiveFeedSource -> Passengers(actual = Option(100), transit = None)))

        arrival.fallBackToFeedSource === Option(BestPaxSource(LiveFeedSource, Passengers(Option(100), None)))
      }

      "when there is some act and trans we should get TotalPaxSource(Some(act - trans), _)" >> {
        val arrival = ArrivalGenerator.arrival(feedSources = Set(LiveFeedSource), passengerSources = Map(LiveFeedSource -> Passengers(actual = Option(100), transit = Option(25))))

        arrival.fallBackToFeedSource === Option(BestPaxSource(LiveFeedSource, Passengers(Option(100), Option(25))))
      }

      "when there is some act and trans where trans > act we should get TotalPaxSource(Some(0), _)" >> {
        val arrival = ArrivalGenerator.arrival(feedSources = Set(LiveFeedSource), passengerSources = Map(LiveFeedSource -> Passengers(actual = Option(100), transit = Option(125))))

        arrival.fallBackToFeedSource === Option(BestPaxSource(LiveFeedSource, Passengers(Option(0), None)))
      }
    }
  }
}
