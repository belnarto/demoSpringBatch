CREATE TABLE PUBLIC.PEOPLE (
                                    person_id SERIAL NOT NULL,
                                    first_name CHARACTER VARYING(255),
                                    last_name CHARACTER VARYING(255),
                                    CONSTRAINT CONSTRAINT_F PRIMARY KEY (person_id)
);
