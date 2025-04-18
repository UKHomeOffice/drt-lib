package uk.gov.homeoffice.drt.actor

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.ask
import org.apache.pekko.persistence.testkit.scaladsl.PersistenceTestKit
import org.apache.pekko.persistence.testkit.{PersistenceTestKitPlugin, PersistenceTestKitSnapshotPlugin}
import org.apache.pekko.testkit.{ImplicitSender, TestKit}
import org.apache.pekko.util.Timeout
import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import uk.gov.homeoffice.drt.actor.TerminalDayFeedArrivalActor.GetState
import uk.gov.homeoffice.drt.arrivals.{FeedArrival, FeedArrivalGenerator, UniqueArrival}
import uk.gov.homeoffice.drt.ports.Terminals.T1
import uk.gov.homeoffice.drt.ports.{AclFeedSource, LiveFeedSource}
import uk.gov.homeoffice.drt.protobuf.messages.FeedArrivalsMessage.{ForecastFeedArrivalsDiffMessage, LiveFeedArrivalsDiffMessage}
import uk.gov.homeoffice.drt.protobuf.serialisation.FeedArrivalMessageConversion

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class TerminalDayFeedArrivalActorTest extends TestKit(ActorSystem("terminal-day-feed-arrival-actor-test-system",
  PersistenceTestKitPlugin.config.withFallback(PersistenceTestKitSnapshotPlugin.config.withFallback(ConfigFactory.load()))))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach {
  private implicit val timeout: Timeout = new Timeout(1.second)

  val testKit: PersistenceTestKit = PersistenceTestKit(system)

  override def beforeEach(): Unit = {
    testKit.clearAll()
  }

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val myNow: () => Long = () => 1L

  "liveDiffToMaybeMessage" should {
    "return a diff message containing the arrival not already existing in the state" in {
      val arrival = FeedArrivalGenerator.live()
      val feedArrivals = Seq(arrival)
      val maybeMessage = FeedArrivalMessageConversion.liveArrivalsToMaybeDiffMessage(myNow, processRemovals = false)(feedArrivals, Map.empty)
      val arrivalMsg = FeedArrivalMessageConversion.liveArrivalToMessage(arrival)

      maybeMessage shouldBe Option(LiveFeedArrivalsDiffMessage(Option(1L), List(), List(arrivalMsg)))
    }
    "return an empty diff message when the arrival already exists in the state" in {
      val arrival = FeedArrivalGenerator.live()
      val feedArrivals = Seq(arrival)
      val maybeMessage = FeedArrivalMessageConversion.liveArrivalsToMaybeDiffMessage(myNow, processRemovals = false)(feedArrivals, Map(arrival.unique -> arrival))
      maybeMessage.isDefined shouldBe false
    }
  }

  "forecastDiffToMaybeMessage" should {
    "return a diff message containing the arrival not already existing in the state" in {
      val arrival = FeedArrivalGenerator.forecast()
      val feedArrivals = Seq(arrival)
      val maybeMessage = FeedArrivalMessageConversion.forecastArrivalsToMaybeDiffMessage(myNow, processRemovals = false)(feedArrivals, Map.empty)
      val arrivalMsg = FeedArrivalMessageConversion.forecastArrivalToMessage(arrival)

      maybeMessage shouldBe Option(ForecastFeedArrivalsDiffMessage(Option(1L), List(), List(arrivalMsg)))
    }
    "return an empty diff message when the arrival already exists in the state" in {
      val arrival = FeedArrivalGenerator.forecast()
      val feedArrivals = Seq(arrival)
      val maybeMessage = FeedArrivalMessageConversion.forecastArrivalsToMaybeDiffMessage(myNow, processRemovals = false)(feedArrivals, Map(arrival.unique -> arrival))
      maybeMessage.isDefined shouldBe false
    }
  }

  "forecastStateFromMessage" should {
    "return a map containing the arrival from the message" in {
      val arrival = FeedArrivalGenerator.forecast()
      val arrivalMsg = ForecastFeedArrivalsDiffMessage(Option(1L), List(), List(FeedArrivalMessageConversion.forecastArrivalToMessage(arrival)))
      val state = FeedArrivalMessageConversion.forecastStateFromMessage(arrivalMsg, Map.empty)
      state shouldBe Map(arrival.unique -> arrival)
    }
  }

  "liveStateFromMessage" should {
    "return a map containing the arrival from the message" in {
      val arrival = FeedArrivalGenerator.live()
      val arrivalMsg = LiveFeedArrivalsDiffMessage(Option(1L), List(), List(FeedArrivalMessageConversion.liveArrivalToMessage(arrival)))
      val state = FeedArrivalMessageConversion.liveStateFromMessage(arrivalMsg, Map.empty)
      state shouldBe Map(arrival.unique -> arrival)
    }
  }

  "TerminalDayFeedArrivalActor for live arrivals" should {
    val arrival = FeedArrivalGenerator.live()
    def props(snapshotThreshold: Int) = TerminalDayFeedArrivalActor.props(2024, 6, 1, T1, LiveFeedSource, None, myNow, snapshotThreshold)
    "respond with an empty map when asked for the latest arrivals" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(2))
      terminalDayFeedArrivalActor ! GetState
      expectMsg(Map.empty[UniqueArrival, FeedArrival])
    }
    "take a FeedArrivalsDiff and respond with the updated state" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(2))
      val feedArrivals = Seq(arrival)
      Await.ready(terminalDayFeedArrivalActor.ask(feedArrivals), 1.second)
      terminalDayFeedArrivalActor ! GetState
      expectMsg(Map(arrival.unique -> arrival))
    }
    "recover previously persisted state using a snapshot" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(1))
      val feedArrivals = Seq(arrival)
      Await.ready(terminalDayFeedArrivalActor.ask(feedArrivals), 1.second)
      watch(terminalDayFeedArrivalActor)
      terminalDayFeedArrivalActor ! actor.PoisonPill
      expectMsgClass(classOf[actor.Terminated])

      val terminalDayFeedArrivalActor2 = system.actorOf(props(1))

      terminalDayFeedArrivalActor2 ! GetState
      expectMsg(Map(arrival.unique -> arrival))
    }
    "recover previously persisted state using a replaying events" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(2))
      val feedArrivals = Seq(arrival)
      Await.ready(terminalDayFeedArrivalActor.ask(feedArrivals), 1.second)
      watch(terminalDayFeedArrivalActor)
      terminalDayFeedArrivalActor ! actor.PoisonPill
      expectMsgClass(classOf[actor.Terminated])

      val terminalDayFeedArrivalActor2 = system.actorOf(props(2))

      terminalDayFeedArrivalActor2 ! GetState
      expectMsg(Map(arrival.unique -> arrival))
    }
  }

  "TerminalDayFeedArrivalActor for forecast arrivals" should {
    val arrival = FeedArrivalGenerator.forecast()
    def props(snapshotThreshold: Int) = TerminalDayFeedArrivalActor.props(2024, 6, 1, T1, AclFeedSource, None, myNow, snapshotThreshold)
    "respond with an empty map when asked for the latest arrivals" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(2))
      terminalDayFeedArrivalActor ! GetState
      expectMsg(Map.empty[UniqueArrival, FeedArrival])
    }
    "take a FeedArrivalsDiff and respond with the updated state" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(2))
      val feedArrivals = Seq(arrival)
      Await.ready(terminalDayFeedArrivalActor.ask(feedArrivals), 1.second)
      terminalDayFeedArrivalActor ! GetState
      expectMsg(Map(arrival.unique -> arrival))
    }
    "recover previously persisted state using a snapshot" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(1))
      val feedArrivals = Seq(arrival)
      Await.ready(terminalDayFeedArrivalActor.ask(feedArrivals), 1.second)
      watch(terminalDayFeedArrivalActor)
      terminalDayFeedArrivalActor ! actor.PoisonPill
      expectMsgClass(classOf[actor.Terminated])

      val terminalDayFeedArrivalActor2 = system.actorOf(props(1))

      terminalDayFeedArrivalActor2 ! GetState
      expectMsg(Map(arrival.unique -> arrival))
    }
    "recover previously persisted state using a replaying events" in {
      val terminalDayFeedArrivalActor = system.actorOf(props(2))
      val feedArrivals = Seq(arrival)
      Await.ready(terminalDayFeedArrivalActor.ask(feedArrivals), 1.second)
      watch(terminalDayFeedArrivalActor)
      terminalDayFeedArrivalActor ! actor.PoisonPill
      expectMsgClass(classOf[actor.Terminated])

      val terminalDayFeedArrivalActor2 = system.actorOf(props(2))

      terminalDayFeedArrivalActor2 ! GetState
      expectMsg(Map(arrival.unique -> arrival))
    }
  }
}
