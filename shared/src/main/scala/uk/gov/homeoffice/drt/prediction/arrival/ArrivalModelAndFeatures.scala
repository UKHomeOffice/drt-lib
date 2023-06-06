package uk.gov.homeoffice.drt.prediction.arrival

import uk.gov.homeoffice.drt.arrivals.Arrival
import uk.gov.homeoffice.drt.prediction.ModelAndFeatures
import uk.gov.homeoffice.drt.time.{BankHolidays, LocalDate, SDateLike}

import scala.annotation.tailrec


trait ArrivalModelAndFeatures extends ModelAndFeatures {
  def prediction(arrival: Arrival): Option[Int] = {
    val maybeMaybePrediction = for {
      oneToManyValues <- ArrivalFeatureValuesExtractor.oneToManyFeatureValues(arrival, features.features)
      singleValues <- ArrivalFeatureValuesExtractor.singleFeatureValues(arrival, features.features)
    } yield {
      val oneToManyFeatureValues: Seq[Option[Double]] = oneToManyValues.map { featureValue =>
        val featureIdx = features.oneToManyValues.indexOf(featureValue)
        model.coefficients.toIndexedSeq.lift(featureIdx)
      }
      val singleFeatureValues: Seq[Option[Double]] = singleValues.zipWithIndex.map { case (featureValue, idx) =>
        model.coefficients.toIndexedSeq.lift(idx).map(_ * featureValue)
      }
      val allFeatureValues = oneToManyFeatureValues ++ singleFeatureValues
      if (allFeatureValues.forall(_.isDefined)) {
        Some((model.intercept + allFeatureValues.map(_.get).sum).round.toInt)
      } else {
        None
      }
    }
    maybeMaybePrediction.flatten
  }
}

object FeatureColumns {
  sealed trait Feature[T] {
    val label: String
    val prefix: String
  }

  sealed trait Single[T] extends Feature[T] {
    val label: String
    val value: T => Option[Double]
  }

  object Single {
    def fromLabel(label: String)
                 (implicit elapsedDays: Long => Int): Single[_] = label match {
      case BestPax.label => BestPax
    }
  }

  case object BestPax extends Single[Arrival] {
    override val label: String = "bestPax"
    override val prefix: String = "bestpax"
    override val value: Arrival => Option[Double] = _.bestPaxEstimate.passengers.getPcpPax.map(_.toDouble)
  }

  sealed trait OneToMany[T] extends Feature[T] {
    val label: String
    val value: T => Option[String]
  }

  object OneToMany {
    def fromLabel(label: String)
                 (implicit
                  sDateProvider: Long => SDateLike,
                  sDateFromLocalDate: LocalDate => SDateLike,
                  now: () => SDateLike,
                 ): OneToMany[_] = label match {
      case DayOfWeek.label => DayOfWeek()
      case WeekendDay.label => WeekendDay()
      case PartOfDay.label => PartOfDay()
      case Carrier.label => Carrier
      case Origin.label => Origin
      case FlightNumber.label => FlightNumber
      case MonthOfYear.label => MonthOfYear()
      case Year.label => Year()
      case DayOfMonth.label => DayOfMonth()
      case ChristmasDay.label => ChristmasDay()
      case OctoberHalfTerm.label => OctoberHalfTerm()
      case ChristmasHoliday.label => ChristmasHoliday()
      case SpringHalfTerm.label => SpringHalfTerm()
      case EasterHoliday.label => EasterHoliday()
      case SummerHalfTerm.label => SummerHalfTerm()
      case SummerHoliday.label => SummerHoliday()
      case BankHolidayWeekend.label =>
        BankHolidayWeekend(ts => BankHolidays.isHolidayOrHolidayWeekend(sDateProvider(ts).toLocalDate))
      case Since6MonthsAgo.label => Since6MonthsAgo(now)
    }
  }

  case class BankHolidayWeekend(isBankHolidayWeekend: Long => Boolean) extends OneToMany[Arrival] {
    override val label: String = BankHolidayWeekend.label
    override val prefix: String = "bhw"
    override val value: Arrival => Option[String] =
      (a: Arrival) => Option(isBankHolidayWeekend(a.Scheduled).toString)
  }

  object BankHolidayWeekend {
    val label: String = "bankHolidayWeekend"
  }

  case class MonthOfYear()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = MonthOfYear.label
    override val prefix: String = "moy"
    override val value: Arrival => Option[String] =
      (a: Arrival) => Option(sDateProvider(a.Scheduled).getMonth.toString)
  }

  object MonthOfYear {
    val label: String = "monthOfYear"
  }

  case class Since6MonthsAgo(now: () => SDateLike)(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = Since6MonthsAgo.label
    override val prefix: String = "snc6"
    override val value: Arrival => Option[String] =
      (a: Arrival) => {
        val isSince6Months: String = if (sDateProvider(a.Scheduled) >= now()) "y" else "n"
        Option(isSince6Months)
      }
  }

  object Since6MonthsAgo {
    val label: String = "since6Months"
  }

  object DayOfMonth {
    val label: String = "dayOfMonth"
  }

  case class DayOfMonth()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = DayOfMonth.label
    override val prefix: String = "dom"
    override val value: Arrival => Option[String] =
      (a: Arrival) => Option(sDateProvider(a.Scheduled).getDate.toString)
  }

  object Year {
    val label: String = "year"
  }

  case class Year()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = Year.label
    override val prefix: String = "year"
    override val value: Arrival => Option[String] =
      (a: Arrival) => Option(sDateProvider(a.Scheduled).getFullYear.toString)
  }

  case class DayOfWeek()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = DayOfWeek.label
    override val prefix: String = "dow"
    override val value: Arrival => Option[String] =
      (a: Arrival) => Option(sDateProvider(a.Scheduled).getDayOfWeek.toString)
  }

  object DayOfWeek {
    val label: String = "dayOfTheWeek"
  }

  case class ChristmasDay()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = ChristmasDay.label
    override val prefix: String = "dow"
    override val value: Arrival => Option[String] =
      (a: Arrival) => {
        val date = sDateProvider(a.Scheduled).toLocalDate
        val xmas = date.month == 12 && date.day == 25
        Option(if (xmas) "1" else "0")
      }
  }

  object ChristmasDay {
    val label: String = "xmas"
  }

  case class SummerHalfTerm()
                            (implicit
                             val sDateTs: Long => SDateLike,
                             val sDateLocalDate: LocalDate => SDateLike,
                            ) extends OneToMany[Arrival] with HolidayLike {
    override val label: String = SummerHalfTerm.label
    override val prefix: String = "sumht"
    override val hols: Seq[(LocalDate, LocalDate)] = Seq(
      (LocalDate(2022, 5, 30), LocalDate(2022, 6, 3)),
      (LocalDate(2023, 5, 29), LocalDate(2023, 6, 2)),
    )
  }

  object SummerHalfTerm {
    val label: String = "summerHalfTerm"
  }

  case class SummerHoliday()
                            (implicit
                             val sDateTs: Long => SDateLike,
                             val sDateLocalDate: LocalDate => SDateLike,
                            ) extends OneToMany[Arrival] with HolidayLike {
    override val label: String = SummerHoliday.label
    override val prefix: String = "sumhol"
    override val startBufferDays: Int = 10
    override val endBufferDays: Int = 10
    override val hols: Seq[(LocalDate, LocalDate)] = Seq(
      (LocalDate(2022, 7, 25), LocalDate(2022, 8, 31)),
      (LocalDate(2023, 7, 24), LocalDate(2023, 9, 1)),
    )
  }

  object SummerHoliday {
    val label: String = "summerHoliday"
  }

  case class OctoberHalfTerm()
                            (implicit
                             val sDateTs: Long => SDateLike,
                             val sDateLocalDate: LocalDate => SDateLike,
                            ) extends OneToMany[Arrival] with HolidayLike {
    override val label: String = OctoberHalfTerm.label
    override val prefix: String = "octht"
    override val hols: Seq[(LocalDate, LocalDate)] = Seq(
      (LocalDate(2022, 10, 24), LocalDate(2022, 10, 28)),
      (LocalDate(2023, 10, 23), LocalDate(2023, 10, 27)),
    )
  }

  object OctoberHalfTerm {
    val label: String = "octHalfTerm"
  }

  case class SpringHalfTerm()
                            (implicit
                             val sDateTs: Long => SDateLike,
                             val sDateLocalDate: LocalDate => SDateLike,
                            ) extends OneToMany[Arrival] with HolidayLike {
    override val label: String = SpringHalfTerm.label
    override val prefix: String = "sprht"
    override val startBufferDays: Int = 5
    override val endBufferDays: Int = 10
    override val hols: Seq[(LocalDate, LocalDate)] = Seq(
      (LocalDate(2022, 2, 14), LocalDate(2022, 2, 18)),
      (LocalDate(2023, 2, 13), LocalDate(2023, 2, 17)),
    )
  }

  object SpringHalfTerm {
    val label: String = "springHalfTerm"
  }

  case class EasterHoliday()
                          (implicit
                           val sDateTs: Long => SDateLike,
                           val sDateLocalDate: LocalDate => SDateLike,
                          ) extends OneToMany[Arrival] with HolidayLike {
    override val label: String = EasterHoliday.label
    override val prefix: String = "easter"
    override val startBufferDays: Int = 7
    override val endBufferDays: Int = 7
    override val hols: Seq[(LocalDate, LocalDate)] = Seq(
      (LocalDate(2022, 4, 4), LocalDate(2022, 4, 18)),
      (LocalDate(2023, 4, 3), LocalDate(2023, 4, 14)),
    )
  }

  object EasterHoliday {
    val label: String = "easterHoliday"
  }

  case class ChristmasHoliday()
                             (implicit
                              val sDateTs: Long => SDateLike,
                              val sDateLocalDate: LocalDate => SDateLike,
                             ) extends OneToMany[Arrival] with HolidayLike {
    override val label: String = ChristmasHoliday.label
    override val prefix: String = "xmas"
    override val hols: Seq[(LocalDate, LocalDate)] = Seq(
      (LocalDate(2022, 12, 19), LocalDate(2023, 1, 2)),
      (LocalDate(2023, 12, 22), LocalDate(2024, 1, 5)),
    )
  }

  object ChristmasHoliday {
    val label: String = "christmasHoliday"
  }

  trait HolidayLike {
    val hols: Seq[(LocalDate, LocalDate)]
    val startBufferDays: Int = 2
    val endBufferDays: Int = 2

    val sDateTs: Long => SDateLike
    implicit val sDateLocalDate: LocalDate => SDateLike
    val value: Arrival => Option[String] = (a: Arrival) => dayOfHoliday(sDateTs(a.Scheduled).toLocalDate)

    def dayOfHoliday(localDate: LocalDate): Option[String] = {
      val date = sDateLocalDate(localDate)
      val day = hols
        .find { case (start, end) =>
          val afterStart = date >= sDateLocalDate(start).addDays(-startBufferDays)
          val beforeEnd = date <= sDateLocalDate(end).addDays(endBufferDays)
          afterStart && beforeEnd
        }
        .map { case (start, end) =>
          val startWithBuffer = sDateLocalDate(start).addDays(-2).toLocalDate
          val endWithBuffer = sDateLocalDate(end).addDays(2).toLocalDate
          localDateRange(startWithBuffer, endWithBuffer).indexOf(localDate).toString
        }
        .getOrElse("no")
      Option(day)
    }

    def localDateRange(start: LocalDate, end: LocalDate)
                      (implicit sdate: LocalDate => SDateLike): Seq[LocalDate] = {
      @tailrec
      def constructRange(date: LocalDate, acc: List[LocalDate]): List[LocalDate] = {
        if (date == end) (date :: acc).reverse
        else constructRange(sdate(date).addDays(1).toLocalDate, date :: acc)
      }

      constructRange(start, List.empty)
    }
  }

  case class WeekendDay()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = WeekendDay.label
    override val prefix: String = "wkd"
    override val value: Arrival => Option[String] =
      (a: Arrival) => {
        val isWeekend = List(6, 7).contains(sDateProvider(a.Scheduled).getDayOfWeek)
        Option(if (isWeekend) "1" else "0")
      }
  }

  object WeekendDay {
    val label: String = "weekendDay"
  }

  case class PartOfDay()(implicit sDateProvider: Long => SDateLike) extends OneToMany[Arrival] {
    override val label: String = PartOfDay.label
    override val prefix: String = "pod"
    override val value: Arrival => Option[String] =
      (a: Arrival) => Option((sDateProvider(a.Scheduled).getHours / 12).toString)
  }

  object PartOfDay {
    val label: String = "partOfDay"
  }

  case object Carrier extends OneToMany[Arrival] {
    override val label: String = "carrier"
    override val prefix: String = "car"
    override val value: Arrival => Option[String] = (a: Arrival) => Option(a.flightCode.carrierCode.code)
  }

  case object Origin extends OneToMany[Arrival] {
    override val label: String = "origin"
    override val prefix: String = "ori"
    override val value: Arrival => Option[String] = (a: Arrival) => Option(a.Origin.iata)
  }

  case object FlightNumber extends OneToMany[Arrival] {
    override val label: String = "flightNumber"
    override val prefix: String = "fln"
    override val value: Arrival => Option[String] = (a: Arrival) => Option(a.flightCode.voyageNumberLike.numeric.toString)
  }
}
