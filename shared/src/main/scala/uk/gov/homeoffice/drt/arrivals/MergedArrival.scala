package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.prediction.arrival.{OffScheduleModelAndFeatures, ToChoxModelAndFeatures}
import uk.gov.homeoffice.drt.time.MilliTimes.oneMinuteMillis
import uk.gov.homeoffice.drt.time.{MilliTimes, SDateLike}
import upickle.default.{ReadWriter, macroRW}

import scala.collection.immutable.{List, NumericRange}
import scala.concurrent.duration.{DurationLong, FiniteDuration}
import scala.util.Try
import scala.util.matching.Regex


trait WithUnique[I] {
  def unique: I
}

trait Updatable[I] {
  def update(incoming: I): I
}

case class Prediction[A](updatedAt: Long, value: A)

object Prediction {
  implicit val predictionLong: ReadWriter[Prediction[Long]] = macroRW
  implicit val predictionInt: ReadWriter[Prediction[Int]] = macroRW
}

case class Passengers(actual: Option[Int], transit: Option[Int]) {

  def diffInActualAndTrans: Option[Int] = actual.map(_ - transit.getOrElse(0))

  def getPcpPax: Option[Int] = {
    diffInActualAndTrans match {
      case Some(a) if a > 0 => Option(a)
      case Some(a) if a <= 0 => Option(0)
      case _ => None
    }
  }
}

case class PaxSource(feedSource: FeedSource, passengers: Passengers) {

  def getPcpPax: Option[Int] = passengers.getPcpPax

}

case class Predictions(lastChecked: Long, predictions: Map[String, Int])

object Predictions {
  val empty: Predictions = Predictions(0L, Map.empty)

  implicit val predictionsRw: ReadWriter[Predictions] = macroRW
}

case class MergedArrival(Operator: Option[Operator],
                         CarrierCode: CarrierCode,
                         VoyageNumber: VoyageNumber,
                         FlightCodeSuffix: Option[FlightCodeSuffix],
                         Status: ArrivalStatus,
                         Estimated: Option[Long],
                         Predictions: Predictions,
                         Actual: Option[Long],
                         EstimatedChox: Option[Long],
                         ActualChox: Option[Long],
                         Gate: Option[String],
                         Stand: Option[String],
                         MaxPax: Option[Int],
                         RunwayID: Option[String],
                         BaggageReclaimId: Option[String],
                         AirportID: PortCode,
                         Terminal: Terminal,
                         Origin: PortCode,
                         Scheduled: Long,
                         PcpTime: Option[Long],
                         FeedSources: Set[FeedSource],
                         CarrierScheduled: Option[Long],
                         ScheduledDeparture: Option[Long],
                         RedListPax: Option[Int],
                         PassengerSources: Map[FeedSource, Passengers]
                        )
  extends Arrival with WithUnique[UniqueArrival] with Updatable[MergedArrival] {
  override def update(incoming: MergedArrival): MergedArrival =
    incoming.copy(
      BaggageReclaimId = if (incoming.BaggageReclaimId.exists(_.nonEmpty)) incoming.BaggageReclaimId else this.BaggageReclaimId,
      Stand = if (incoming.Stand.exists(_.nonEmpty)) incoming.Stand else this.Stand,
      Gate = if (incoming.Gate.exists(_.nonEmpty)) incoming.Gate else this.Gate,
      RedListPax = if (incoming.RedListPax.nonEmpty) incoming.RedListPax else this.RedListPax,
      MaxPax = if (incoming.MaxPax.nonEmpty) incoming.MaxPax else this.MaxPax,
    )
}

object MergedArrival {
  val defaultMinutesToChox: Int = 5

  val flightCodeRegex: Regex = "^([A-Z0-9]{2,3}?)([0-9]{1,4})([A-Z]*)$".r

  def parseFlightNumber(code: String): Option[Int] = code match {
    case MergedArrival.flightCodeRegex(_, flightNumber, _) => Try(flightNumber.toInt).toOption
    case _ => None
  }

  def isInRange(rangeStart: Long, rangeEnd: Long)(needle: Long): Boolean =
    rangeStart <= needle && needle <= rangeEnd

  def isRelevantToPeriod(rangeStart: SDateLike, rangeEnd: SDateLike, sourceOrderPreference: List[FeedSource])(arrival: Arrival): Boolean = {
    val rangeCheck: Long => Boolean = isInRange(rangeStart.millisSinceEpoch, rangeEnd.millisSinceEpoch)

    rangeCheck(arrival.Scheduled) ||
      rangeCheck(arrival.Estimated.getOrElse(0)) ||
      rangeCheck(arrival.EstimatedChox.getOrElse(0)) ||
      rangeCheck(arrival.Actual.getOrElse(0)) ||
      rangeCheck(arrival.ActualChox.getOrElse(0)) ||
      arrival.hasPcpDuring(rangeStart, rangeEnd, sourceOrderPreference)
  }

  def summaryString(arrival: MergedArrival): String = arrival.AirportID + "/" + arrival.Terminal + "@" + arrival.Scheduled + "!" + arrival.flightCodeString

  def standardiseFlightCode(flightCode: String): String = {
    flightCode match {
      case flightCodeRegex(operator, flightNumber, suffix) =>
        val number = f"${flightNumber.toInt}%04d"
        f"$operator$number$suffix"
      case _ => flightCode
    }
  }

  implicit val arrivalStatusRw: ReadWriter[ArrivalStatus] = macroRW
  implicit val voyageNumberRw: ReadWriter[VoyageNumber] = macroRW
  implicit val arrivalSuffixRw: ReadWriter[FlightCodeSuffix] = macroRW
  implicit val operatorRw: ReadWriter[Operator] = macroRW
  implicit val portCodeRw: ReadWriter[PortCode] = macroRW
  implicit val predictionsRw: ReadWriter[Predictions] = macroRW
  implicit val arrivalRw: ReadWriter[MergedArrival] = macroRW
  implicit val totalPaxSourceRw: ReadWriter[PaxSource] = macroRW
  implicit val passengersSourceRw: ReadWriter[Passengers] = macroRW

  def apply(Operator: Option[Operator],
            Status: ArrivalStatus,
            Estimated: Option[Long],
            Predictions: Predictions,
            Actual: Option[Long],
            EstimatedChox: Option[Long],
            ActualChox: Option[Long],
            Gate: Option[String],
            Stand: Option[String],
            MaxPax: Option[Int],
            RunwayID: Option[String],
            BaggageReclaimId: Option[String],
            AirportID: PortCode,
            Terminal: Terminal,
            rawICAO: String,
            rawIATA: String,
            Origin: PortCode,
            Scheduled: Long,
            PcpTime: Option[Long],
            FeedSources: Set[FeedSource],
            CarrierScheduled: Option[Long] = None,
            ScheduledDeparture: Option[Long] = None,
            RedListPax: Option[Int] = None,
            PassengerSources: Map[FeedSource, Passengers] = Map.empty
           ): MergedArrival = {
    val (carrierCode: CarrierCode, voyageNumber: VoyageNumber, maybeSuffix: Option[FlightCodeSuffix]) = {
      val bestCode = (rawIATA, rawICAO) match {
        case (iata, _) if iata != "" => iata
        case (_, icao) if icao != "" => icao
        case _ => ""
      }

      FlightCode.flightCodeToParts(bestCode)
    }

    MergedArrival(
      Operator = Operator,
      CarrierCode = carrierCode,
      VoyageNumber = voyageNumber,
      FlightCodeSuffix = maybeSuffix,
      Status = Status,
      Estimated = Estimated,
      Predictions = Predictions,
      Actual = Actual,
      EstimatedChox = EstimatedChox,
      ActualChox = ActualChox,
      Gate = Gate,
      Stand = Stand,
      MaxPax = MaxPax,
      RunwayID = RunwayID,
      BaggageReclaimId = BaggageReclaimId,
      AirportID = AirportID,
      Terminal = Terminal,
      Origin = Origin,
      Scheduled = Scheduled,
      PcpTime = PcpTime,
      FeedSources = FeedSources,
      CarrierScheduled = CarrierScheduled,
      ScheduledDeparture = ScheduledDeparture,
      RedListPax = RedListPax,
      PassengerSources = PassengerSources
    )
  }
}
