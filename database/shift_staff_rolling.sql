CREATE TABLE shift_staff_rolling
(
    port                 VARCHAR NOT NULL,
    terminal             VARCHAR NOT NULL,
    rolling_started_date DATE,
    rolling_ended_date   DATE,
    updated_at           TIMESTAMP,
    applied_by           TEXT,
    CONSTRAINT shift_staff_rolling_pkey PRIMARY KEY (port, terminal)
);