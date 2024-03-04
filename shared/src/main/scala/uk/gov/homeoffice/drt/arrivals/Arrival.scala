package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.SDateLike
import upickle.default.{ReadWriter, macroRW}

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

case class Arrival(Operator: Option[Operator],
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
  extends ArrivalLike with WithUnique[UniqueArrival] with Updatable[ArrivalLike] {
  override def withoutPcpTime: Arrival = copy(PcpTime = None)

  override def update(incoming: ArrivalLike): ArrivalLike = incoming match {
    case a: ForecastArrival => a
    case a: Arrival => a.copy(
      BaggageReclaimId = if (incoming.BaggageReclaimId.exists(_.nonEmpty)) incoming.BaggageReclaimId else this.BaggageReclaimId,
      Stand = if (incoming.Stand.exists(_.nonEmpty)) incoming.Stand else this.Stand,
      Gate = if (incoming.Gate.exists(_.nonEmpty)) incoming.Gate else this.Gate,
      RedListPax = if (incoming.RedListPax.nonEmpty) incoming.RedListPax else this.RedListPax,
      MaxPax = if (incoming.MaxPax.nonEmpty) incoming.MaxPax else this.MaxPax,
    )
  }
}

object Arrival {
  val defaultMinutesToChox: Int = 5

  val flightCodeRegex: Regex = "^([A-Z0-9]{2,3}?)([0-9]{1,4})([A-Z]*)$".r

  def parseFlightNumber(code: String): Option[Int] = code match {
    case Arrival.flightCodeRegex(_, flightNumber, _) => Try(flightNumber.toInt).toOption
    case _ => None
  }

  def isInRange(rangeStart: Long, rangeEnd: Long)(needle: Long): Boolean =
    rangeStart <= needle && needle <= rangeEnd

  def isRelevantToPeriod(rangeStart: SDateLike, rangeEnd: SDateLike, sourceOrderPreference: List[FeedSource])(arrival: ArrivalLike): Boolean = {
    val rangeCheck: Long => Boolean = isInRange(rangeStart.millisSinceEpoch, rangeEnd.millisSinceEpoch)

    rangeCheck(arrival.Scheduled) ||
      rangeCheck(arrival.Estimated.getOrElse(0)) ||
      rangeCheck(arrival.EstimatedChox.getOrElse(0)) ||
      rangeCheck(arrival.Actual.getOrElse(0)) ||
      rangeCheck(arrival.ActualChox.getOrElse(0)) ||
      arrival.hasPcpDuring(rangeStart, rangeEnd, sourceOrderPreference)
  }

  def summaryString(arrival: Arrival): String = arrival.AirportID + "/" + arrival.Terminal + "@" + arrival.Scheduled + "!" + arrival.flightCodeString

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
  implicit val totalPaxSourceRw: ReadWriter[PaxSource] = macroRW
  implicit val passengersSourceRw: ReadWriter[Passengers] = macroRW
  implicit val rw: ReadWriter[Arrival] = macroRW

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
           ): Arrival = {
    val (carrierCode: CarrierCode, voyageNumber: VoyageNumber, maybeSuffix: Option[FlightCodeSuffix]) = {
      val bestCode = (rawIATA, rawICAO) match {
        case (iata, _) if iata != "" => iata
        case (_, icao) if icao != "" => icao
        case _ => ""
      }

      FlightCode.flightCodeToParts(bestCode)
    }

    Arrival(
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
