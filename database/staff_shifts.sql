create table staff_shifts (
      port		text	  NOT NULL,
      terminal    text	  NOT NULL,
      shift_name  text 	  NOT NULL,
      start_time 	text      NOT NULL,
      end_time text 	  NOT NULL,
      staff_number INTEGER   NOT NULL,
      created_by  text,
      frequency   text,
      created_at timestamp NOT NULL
);