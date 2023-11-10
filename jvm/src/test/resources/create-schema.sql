CREATE TABLE IF NOT EXISTS user_feedback
(
    email text NOT NULL,
    actioned_at timestamp NOT NULL,
    feedback_at timestamp,
    close_banner boolean NULL,
    bf_role text NULL,
    drt_quality text NULL,
    drt_likes text NULL,
    drt_improvements text NULL,
    participation_interest  boolean NULL,
    PRIMARY KEY (email,actioned_at)
);
