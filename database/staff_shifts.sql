create table staff_shifts (
      port		  text	  NOT NULL,
      terminal    text	  NOT NULL,
      shift_name  text 	  NOT NULL,
      start_date  date   NOT NULL,
      start_time  text    NOT NULL,
      end_time    text 	  NOT NULL,
      end_date    date ,
      staff_number INTEGER   NOT NULL,
      created_by  text,
      frequency   text,
      created_at timestamp NOT NULL
);