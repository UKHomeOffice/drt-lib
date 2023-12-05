package uk.gov.homeoffice.drt.feedback

case class UserFeedback(email: String,
                        createdAt: Long,
                        closeBanner: Boolean,
                        feedbackType: Option[String],
                        bfRole: String,
                        drtQuality: String,
                        drtLikes: Option[String],
                        drtImprovements: Option[String],
                        participationInterest: Boolean,
                        abVersion: Option[String])

