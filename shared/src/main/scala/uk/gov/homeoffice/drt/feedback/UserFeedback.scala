package uk.gov.homeoffice.drt.feedback

case class UserFeedback(email: String,
                        actionedAt: Long,
                        feedbackAt: Option[Long],
                        closeBanner: Boolean,
                        bfRole: String,
                        drtQuality: String,
                        drtLikes: String,
                        drtImprovements: String,
                        participationInterest: Boolean)

