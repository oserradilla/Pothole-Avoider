/*Cambiar datos + devolver
	select ST_X(geom),ST_Y(geom),ST_X(prevGeom),ST_Y(prevGeom)... from pothole
*/
/*SELECT ST_X(geom) from incidence;
SELECT ST_Y(geom) from incidence;*/

/*Functions to insert incidences in the database*/
CREATE OR REPLACE FUNCTION insert_pothole(latitude FLOAT8, longitude FLOAT8, prevLatitude FLOAT8, prevLongitude FLOAT8, accuracy FLOAT, magnitude INTEGER)
RETURNS VOID
AS $$
DECLARE geom geometry;
		prevGeom geometry;
BEGIN
	if(is_pothole_reported(latitude,longitude) = FALSE) THEN
		geom:=ST_SetSrid(ST_MakePoint(longitude, latitude), 4326);
		prevGeom:=ST_SetSrid(ST_MakePoint(prevLongitude, prevLatitude), 4326);
		INSERT INTO pothole VALUES (DEFAULT,geom,prevGeom,accuracy,magnitude);
	END IF;
END $$
LANGUAGE plpgsql;

/*SELECT COUNT(incidence_id) from pothole;*/

/*select insert_pothole(88.8252209060397,156.17907520625675,-157.92805357839063,48.702143271299065,45.781890215616166,1);*/

CREATE OR REPLACE FUNCTION insert_curve(latitude FLOAT8, longitude FLOAT8, prevLatitude FLOAT8, prevLongitude FLOAT8, accuracy FLOAT, magnitude INTEGER, isRight BOOLEAN)
RETURNS VOID
AS $$
DECLARE geom geometry;
		prevGeom geometry;
BEGIN
	if(is_curve_reported(latitude,longitude) = FALSE) THEN
		geom:=ST_SetSrid(ST_MakePoint(longitude, latitude), 4326);
		prevGeom:=ST_SetSrid(ST_MakePoint(prevLongitude, prevLatitude), 4326);
		INSERT INTO curve VALUES (DEFAULT,geom,prevGeom,accuracy,magnitude,isRight);
	END IF;
END $$
LANGUAGE plpgsql;

/*SELECT COUNT(incidence_id) from curve;*/

/*select insert_curve(-99.74404830828583,-129.36966237840943,155.64324576971205,12.29314389878968,23.733889367249443,0,true);*/

CREATE OR REPLACE FUNCTION insert_slope(latitude FLOAT8, longitude FLOAT8, prevLatitude FLOAT8, prevLongitude FLOAT8, accuracy FLOAT, magnitude INTEGER, endLatitude FLOAT8,endLongitude FLOAT8,slope INTEGER)
RETURNS VOID
AS $$
DECLARE geom geometry;
		prevGeom geometry;
		endGeom geometry;
BEGIN
	IF(is_slope_reported(latitude,longitude) = FALSE) THEN
		geom:=ST_SetSrid(ST_MakePoint(longitude, latitude), 4326);
		prevGeom:=ST_SetSrid(ST_MakePoint(prevLongitude, prevLatitude), 4326);
		endGeom:=ST_SetSrid(ST_MakePoint(endLongitude, endLatitude), 4326);
		INSERT INTO slope VALUES (DEFAULT,geom,prevGeom,accuracy,magnitude,endGeom,slope);
	END IF;	
END $$
LANGUAGE plpgsql;

/*SELECT COUNT(incidence_id) from slope;*/

/*select insert_slope(-63.739852384640346,117.10593119669744,-152.08921035494606,9.667449189621948,12.390342610326611,1,-147.98146970986565,-70.64350204098439,-14);*/

CREATE OR REPLACE FUNCTION is_pothole_reported(latitude FLOAT8, longitude FLOAT8)
RETURNS BOOLEAN
AS $$
DECLARE radiusGrades FLOAT8;
	numInstances INT;
	isReported BOOLEAN;
BEGIN
	radiusGrades:=km_to_grades(0.01);
	numInstances=	(SELECT COUNT(incidence_id)
			FROM pothole 
			WHERE ST_MakeBox2D(ST_MakePoint(longitude - radiusGrades, latitude - radiusGrades), 
						ST_MakePoint(longitude + radiusGrades, latitude + radiusGrades)) && geom);
	isReported = numInstances!=0;
	return isReported;
END $$
LANGUAGE plpgsql;

/*select is_pothole_reported(43.061906, -2.502082);*/

/*select * from pothole;*/

CREATE OR REPLACE FUNCTION is_curve_reported(latitude FLOAT8, longitude FLOAT8)
RETURNS BOOLEAN
AS $$
DECLARE radiusGrades FLOAT8;
	numInstances INT;
	isReported BOOLEAN;
BEGIN
	radiusGrades:=km_to_grades(0.01);
	numInstances=	(SELECT COUNT(incidence_id)
			FROM curve 
			WHERE ST_MakeBox2D(ST_MakePoint(longitude - radiusGrades, latitude - radiusGrades), 
						ST_MakePoint(longitude + radiusGrades, latitude + radiusGrades)) && geom);
	isReported = numInstances!=0;
	return isReported;
END $$
LANGUAGE plpgsql;

/*select is_curve_reported(43.061906, -2.502082);*/

/*select * from curve;*/

CREATE OR REPLACE FUNCTION is_slope_reported(latitude FLOAT8, longitude FLOAT8)
RETURNS BOOLEAN
AS $$
DECLARE radiusGrades FLOAT8;
	numInstances INT;
	isReported BOOLEAN;
BEGIN
	radiusGrades:=km_to_grades(0.01);
	numInstances=	(SELECT COUNT(incidence_id)
			FROM slope 
			WHERE ST_MakeBox2D(ST_MakePoint(longitude - radiusGrades, latitude - radiusGrades), 
						ST_MakePoint(longitude + radiusGrades, latitude + radiusGrades)) && geom);
	isReported = numInstances!=0;
	return isReported;
END $$
LANGUAGE plpgsql;

/*select is_slope_reported(43.061906, -2.502082);*/

/*select * from slope;*/

/*Functions to get near incidences*/
/*This function seeks for the incidences that are next to the coordenate point passed as argument. To make this calculus,
creates a 2D plane (containing the current coord in the midle of it) and seeks for incidences of coord that are into it.
This calculus is much more simpler than comparing to a circunference plane, therefore is much faster */
/*DROP FUNCTION get_surrounding_potholes(latitude FLOAT8, longitude FLOAT8, radius FLOAT8)*/

CREATE OR REPLACE FUNCTION get_surrounding_potholes(latitude FLOAT8, longitude FLOAT8, radius FLOAT8)
RETURNS  TABLE(incId INT,lon FLOAT8,lat FLOAT8,prevLon FLOAT8,prevLat FLOAT8, accu FLOAT,mag SMALLINT)
AS $$
DECLARE radiusGrades FLOAT8;
BEGIN
	radiusGrades:=km_to_grades(radius);
	return query
	SELECT incidence_id,
		ST_X(geom),
		ST_Y(geom),
		ST_X(prev_geom),
		ST_Y(prev_geom),
		accuracy,
		magnitude
	FROM pothole 
	WHERE ST_MakeBox2D(ST_MakePoint(longitude - radiusGrades, latitude - radiusGrades), 
					ST_MakePoint(longitude + radiusGrades, latitude + radiusGrades)) && geom;
END;
$$ LANGUAGE PLPGSQL;

/*select * from get_surrounding_potholes(43.062684, -2.493794,0.5)*/

/*DROP FUNCTION get_surrounding_curves(latitude FLOAT8, longitude FLOAT8, radius FLOAT8)*/

CREATE OR REPLACE FUNCTION get_surrounding_curves(latitude FLOAT8, longitude FLOAT8, radius FLOAT8)
RETURNS TABLE(incId INT,lon FLOAT8,lat FLOAT8,prevLon FLOAT8,prevLat FLOAT8, accu FLOAT,mag SMALLINT,isRight BOOLEAN)
AS $$
DECLARE radiusGrades FLOAT8;
BEGIN
	radiusGrades:=km_to_grades(radius);
	return query
	SELECT incidence_id,
		ST_X(geom),
		ST_Y(geom),
		ST_X(prev_geom),
		ST_Y(prev_geom),
		accuracy,
		magnitude,
		is_right
	FROM curve 
	WHERE ST_MakeBox2D(ST_MakePoint(longitude - radiusGrades, latitude - radiusGrades), 
					ST_MakePoint(longitude + radiusGrades, latitude + radiusGrades)) && geom;
END;
$$ LANGUAGE PLPGSQL;

/*select * from get_surrounding_curves(1.0,1.0,10000)*/

/*DROP FUNCTION get_surrounding_slopes(latitude FLOAT8, longitude FLOAT8, radius FLOAT8)*/

CREATE OR REPLACE FUNCTION get_surrounding_slopes(latitude FLOAT8, longitude FLOAT8, radius FLOAT8)
RETURNS  TABLE(incId INT,lon FLOAT8,lat FLOAT8,prevLon FLOAT8,prevLat FLOAT8, accu FLOAT,mag SMALLINT,endLon FLOAT8,endLat FLOAT8,slop SMALLINT)
AS $$
DECLARE radiusGrades FLOAT8;
BEGIN
	radiusGrades:=km_to_grades(radius);
	return query
	SELECT incidence_id,
		ST_X(geom),
		ST_Y(geom),
		ST_X(prev_geom),
		ST_Y(prev_geom),
		accuracy,
		magnitude,
		ST_X(end_geom),
		ST_Y(end_geom),
		slope
	FROM slope 
	WHERE ST_MakeBox2D(ST_MakePoint(longitude - radiusGrades, latitude - radiusGrades), 
					ST_MakePoint(longitude + radiusGrades, latitude + radiusGrades)) && geom;
END;
$$ LANGUAGE PLPGSQL;

/*select * from get_surrounding_slopes(43.062684, -2.493794,12)*/

/*To make this calculus simpler (therefore fastster), this function will assume that the earth is a perfect elipse, so 
1 grade (latitude or longitude) will be converted to 110Km*/
CREATE OR REPLACE FUNCTION km_to_grades(km FLOAT8)
RETURNS FLOAT8
AS $$
BEGIN
	RETURN km/110;
END $$
LANGUAGE plpgsql;
