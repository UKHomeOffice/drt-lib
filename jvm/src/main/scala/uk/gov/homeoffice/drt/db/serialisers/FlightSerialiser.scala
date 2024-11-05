package uk.gov.homeoffice.drt.db.serialisers

import spray.json._
import uk.gov.homeoffice.drt.arrivals._
import uk.gov.homeoffice.drt.db.tables.{FlightRow, FlightTimings}
import uk.gov.homeoffice.drt.ports.Queues.Queue
import uk.gov.homeoffice.drt.ports.SplitRatiosNs.SplitSource
import uk.gov.homeoffice.drt.ports.Terminals.Terminal
import uk.gov.homeoffice.drt.ports._
import uk.gov.homeoffice.drt.time.SDate
import uk.gov.homeoffice.drt.{Nationality, arrivals}

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.sql.Timestamp
import java.util.zip.{DeflaterOutputStream, InflaterInputStream}


trait FlightJsonFormats extends DefaultJsonProtocol {
  implicit val flightJsonFormat: RootJsonFormat[Predictions] = jsonFormat2(Predictions)

  implicit object FeedSourceJsonFormat extends RootJsonFormat[FeedSource] {
    override def read(json: JsValue): FeedSource = json match {
      case JsString(value) => FeedSource.byId(value)
    }

    override def write(obj: FeedSource): JsValue = obj.id.toJson
  }


  implicit val passengersJsonFormat: RootJsonFormat[Passengers] = jsonFormat2(Passengers)

  implicit object QueueJsonFormat extends RootJsonFormat[Queue] {
    override def read(json: JsValue): Queue = json match {
      case JsString(value) => Queue(value)
    }

    override def write(obj: Queue): JsValue = obj.stringValue.toJson
  }

  implicit object PaxTypeJsonFormat extends RootJsonFormat[PaxType] {
    override def read(json: JsValue): PaxType = json match {
      case JsNumber(value) => PaxType(value.toInt)
    }

    override def write(obj: PaxType): JsValue = obj.id.toJson
  }

  implicit val nationalityJsonFormat: RootJsonFormat[Nationality] = jsonFormat1(Nationality.apply)
  implicit val paxAgeJsonFormat: RootJsonFormat[PaxAge] = jsonFormat1(PaxAge.apply)
  implicit val paxTypeAndQueueCountJsonFormat: RootJsonFormat[ApiPaxTypeAndQueueCount] = jsonFormat(
    ApiPaxTypeAndQueueCount.apply,
    "passengerType",
    "queueType",
    "paxCount",
    "nationalities",
    "ages",
  )

  implicit object SplitSourceJsonFormat extends RootJsonFormat[SplitSource] {
    override def read(json: JsValue): SplitSource = json match {
      case JsNumber(value) => SplitSource(value.toInt)
    }

    override def write(obj: SplitSource): JsValue = obj.id.toJson
  }

  implicit object eventTypeJsonFormat extends RootJsonFormat[EventType] {
    override def read(json: JsValue): EventType = json match {
      case JsString(value) => EventType(value)
    }

    override def write(obj: EventType): JsValue = obj.name.toJson
  }

  implicit object splitStyleJsonFormat extends RootJsonFormat[SplitStyle] {
    override def read(json: JsValue): SplitStyle = json match {
      case JsString(value) => SplitStyle(value)
    }

    override def write(obj: SplitStyle): JsValue = obj.name.toJson
  }

  implicit val splitsJsonFormat: RootJsonFormat[Splits] = jsonFormat(
    Splits.apply,
    "splits",
    "source",
    "maybeEventType",
    "splitStyle",
  )
}

object FlightSerialiser extends FlightJsonFormats {
  val toRow: PortCode => ApiFlightWithSplits => FlightRow =
    portCode => {
      case ApiFlightWithSplits(flight, splits, lastUpdated) =>
        val timings = FlightTimings(
          scheduled = new Timestamp(flight.Scheduled),
          estimated = flight.Estimated.map(new Timestamp(_)),
          actual = flight.Actual.map(new Timestamp(_)),
          estimatedChox = flight.EstimatedChox.map(new Timestamp(_)),
          actualChox = flight.ActualChox.map(new Timestamp(_)),
          pcpTime = flight.PcpTime.map(new Timestamp(_)),
          carrierScheduled = flight.CarrierScheduled.map(new Timestamp(_)),
          scheduledDeparture = flight.ScheduledDeparture.map(new Timestamp(_)),
        )

        FlightRow(
          port = portCode.iata,
          origin = flight.Origin.iata,
          terminal = flight.Terminal.toString,
          voyageNumber = flight.VoyageNumber.numeric,
          carrierCode = flight.CarrierCode.code,
          flightCodeSuffix = flight.FlightCodeSuffix.map(_.suffix),
          status = flight.Status.description,
          scheduledDateUtc = SDate(flight.Scheduled).toUtcDate.toISOString,
          timings = timings,
          predictions = flight.Predictions.toJson.compactPrint,
          gate = flight.Gate,
          stand = flight.Stand,
          maxPax = flight.MaxPax,
          baggageReclaimId = flight.BaggageReclaimId,
          paxSources = compress(flight.PassengerSources.toJson.compactPrint),
          redListPax = flight.RedListPax,
          splits = compress(splits.toJson.compactPrint),
          updatedAt = new Timestamp(lastUpdated.getOrElse(0)),
        )
    }

  val fromRow: FlightRow => arrivals.ApiFlightWithSplits = {
    f =>
      val passengerSources = decompress(f.paxSources).parseJson.convertTo[Map[FeedSource, Passengers]]
      val arrival = Arrival(
        Origin = PortCode(f.origin),
        Scheduled = f.timings.scheduled.getTime,
        Status = ArrivalStatus(f.status),
        Estimated = f.timings.estimated.map(_.getTime),
        Actual = f.timings.actual.map(_.getTime),
        EstimatedChox = f.timings.estimatedChox.map(_.getTime),
        ActualChox = f.timings.actualChox.map(_.getTime),
        Gate = f.gate,
        Stand = f.stand,
        MaxPax = f.maxPax,
        BaggageReclaimId = f.baggageReclaimId,
        PcpTime = f.timings.pcpTime.map(_.getTime),
        VoyageNumber = VoyageNumber(f.voyageNumber),
        CarrierCode = CarrierCode(f.carrierCode),
        FlightCodeSuffix = f.flightCodeSuffix.map(FlightCodeSuffix),
        Terminal = Terminal(f.terminal),
        PassengerSources = passengerSources,
        Predictions = f.predictions.parseJson.convertTo[Predictions],
        Operator = None,
        RunwayID = None,
        AirportID = PortCode(""),
        FeedSources = passengerSources.keySet,
        CarrierScheduled = f.timings.carrierScheduled.map(_.getTime),
        ScheduledDeparture = f.timings.scheduledDeparture.map(_.getTime),
        RedListPax = f.redListPax,
      )
      val splits = decompress(f.splits).parseJson.convertTo[Set[Splits]]
      ApiFlightWithSplits(arrival, splits, Option(f.updatedAt.getTime))
  }

  def compress(bytes: String): Array[Byte] = {
    val deflater = new java.util.zip.Deflater
    val baos = new ByteArrayOutputStream
    val dos = new DeflaterOutputStream(baos, deflater)
    dos.write(bytes.getBytes("ASCII"))
    baos.close()
    dos.finish()
    dos.close()
    baos.toByteArray
  }

  def decompress(bytes: Array[Byte]): String = {
    val deflater = new java.util.zip.Inflater()
    val baos = new ByteArrayOutputStream(512)
    val bytesIn = new ByteArrayInputStream(bytes)
    val in = new InflaterInputStream(bytesIn, deflater)
    var go = true
    while (go) {
      val b = in.read
      if (b == -1)
        go = false
      else
        baos.write(b)
    }
    baos.close()
    in.close()

    new String(baos.toByteArray, "ASCII")
  }
}