package uk.gov.homeoffice.drt.db

case class UserFeedbackRow(email: String,
                           actionedAt: java.sql.Timestamp,
                           feedbackAt: java.sql.Timestamp,
                           closeBanner: Boolean,
                           bfRole: String,
                           drtQuality: String,
                           drtLikes: String,
                           drtImprovements: String,
                           participationInterest: Boolean)
