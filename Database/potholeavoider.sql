\c potholeavoider;

/*vacuum full analyze*/
CREATE TABLE incidence(
 incidence_id SERIAL PRIMARY KEY,
 geom geometry(POINT, 4326),
 prev_geom geometry(POINT, 4326),
 accuracy FLOAT,
 magnitude SMALLINT NOT NULL
);
CREATE INDEX ix_spatial_event_geom ON incidence USING gist(geom);

CREATE TABLE pothole(

)INHERITS(incidence);

CREATE TABLE curve(
 is_right BOOLEAN NOT NULL
)INHERITS(incidence);

CREATE TABLE slope(
 end_geom geometry(POINT, 4326),
 slope SMALLINT
)INHERITS (incidence);

CREATE TABLE users(
 user_id SERIAL PRIMARY KEY,
 nick text UNIQUE NOT NULL,
 password text NOT NULL,
 name text,
 last_name text,
 email text
);

CREATE TABLE user_car(
 user_id INTEGER,
 car_id INTEGER,
 CONSTRAINT USER_CAR_PK PRIMARY KEY (user_id,car_id)
);

CREATE TABLE car(
 car_id SERIAL PRIMARY KEY,
 car_name text NOT NULL,
 car_description text
);