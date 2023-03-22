package uk.gov.homeoffice.drt.prediction.arrival

import uk.gov.homeoffice.drt.arrivals.Arrival
import uk.gov.homeoffice.drt.prediction.{FeaturesWithOneToManyValues, RegressionModel}

object OffScheduleModelAndFeatures {
  val targetName: String = "off-schedule"
}

case class OffScheduleModelAndFeatures(model: RegressionModel,
                                       features: FeaturesWithOneToManyValues,
                                       examplesTrainedOn: Int,
                                       improvementPct: Double,
                                      ) extends ArrivalModelAndFeatures {
  override val targetName: String = OffScheduleModelAndFeatures.targetName
}
