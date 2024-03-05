package uk.gov.homeoffice.drt.arrivals

import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports._
import upickle.default.{ReadWriter, macroRW}


trait WithUnique[I] {
  def unique: I
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
  extends Arrival {
  override def withoutPcpTime: MergedArrival = copy(PcpTime = None)
}

object MergedArrival {
  implicit val arrivalStatusRw: ReadWriter[ArrivalStatus] = macroRW
  implicit val voyageNumberRw: ReadWriter[VoyageNumber] = macroRW
  implicit val arrivalSuffixRw: ReadWriter[FlightCodeSuffix] = macroRW
  implicit val operatorRw: ReadWriter[Operator] = macroRW
  implicit val portCodeRw: ReadWriter[PortCode] = macroRW
  implicit val predictionsRw: ReadWriter[Predictions] = macroRW
  implicit val totalPaxSourceRw: ReadWriter[PaxSource] = macroRW
  implicit val passengersSourceRw: ReadWriter[Passengers] = macroRW
  implicit val rw: ReadWriter[MergedArrival] = macroRW

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
