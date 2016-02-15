package webService;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import common_resources.CommonConstants;
import model.DbHandler;
import custom_incidences.LibCurve;
import custom_incidences.LibIncidence;
import custom_incidences.LibPothole;
import custom_incidences.LibSlope;
import custom_parsers.IncidenceJsonParser;

@Path("/web-service")
public class WebService {
	DbHandler dbHandler;
	IncidenceJsonParser jsonParser;

	@PostConstruct
	public void initDbConnection() {
		try {
			dbHandler = new DbHandler();
		} catch (Exception e) {
			e.printStackTrace();
		}
		jsonParser = new IncidenceJsonParser();
	}

	@GET
	@Path("/near-incidences")
	@Produces("application/json")
	@Consumes("text/plain")  
	public String nearIncidences(@QueryParam("inc") String inc,
			@QueryParam("lat") double lat, @QueryParam("lon") double lon,
			@QueryParam("dis") float dis) {
		String binary = Integer.toBinaryString((int) Integer.valueOf(inc));
		String jsonFile = null;
		try {
			System.out.println(inc);
			ArrayList<LibPothole> potholes = new ArrayList<LibPothole>();
			ArrayList<LibCurve> curves = new ArrayList<LibCurve>();
			ArrayList<LibSlope> slopes = new ArrayList<LibSlope>();
			int counter = 0;
			for (int i = binary.length() - 1; i >= 0; i--) {
				counter++;
				char character = binary.charAt(i);
				if (character == '1') {
					switch (counter) {
					case 1:
						potholes = dbHandler.findNearPotholes(lat, lon, dis);
						break;
					case 2:
						curves = dbHandler.findNearCurves(lat, lon, dis);
						break;
					case 3:
						slopes = dbHandler.findNearSlopes(lat, lon, dis);
						break;
					}
				}
			}
			jsonFile = jsonParser.serializeIncidenceArrayList(potholes, curves,
					slopes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonFile;
	}

	// To test connection:
	// http://127.0.0.1:8080/pha_rpi/web-service/near-potholes?lat=10.12&lon=10.04&dis=100000
	// Do not forget to change bd password and the .war's name when deploying to
	// the real server
	// http://localhost:8080/potholeAvoiderWebServer/web-service/report-incidences
	// RESTClient:
	// {"slopes":[],"potholes":[{"prevLat":0,"magnitude":5,"latitude":43.066709,"prevLon":0,"incidenceId":0,"longitude":-2.490627,"accuracy":0}],"curves":[]}
	@GET
	@Path("/near-potholes")
	@Produces("application/json")
	@Consumes("text/plain")  
	public String nearPotholes(@QueryParam("lat") double lat,
			@QueryParam("lon") double lon, @QueryParam("dis") float dis) {
		String jsonFile = null;
		try {
			ArrayList<LibPothole> potholes = dbHandler.findNearPotholes(lat,
					lon, dis);
			jsonFile = jsonParser.serializePotholeArrayList(potholes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonFile;
	}

	@GET
	@Path("/near-curves")
	@Produces("application/json")
	@Consumes("text/plain")  
	public String nearCurves(@QueryParam("lat") double lat,
			@QueryParam("lon") double lon, @QueryParam("dis") float dis) {
		String jsonFile = null;
		try {
			ArrayList<LibCurve> curves = dbHandler
					.findNearCurves(lat, lon, dis);
			jsonFile = jsonParser.serializeCurveArrayList(curves);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonFile;
	}

	@GET
	@Path("/near-slopes")
	@Produces("application/json")
	@Consumes("text/plain")  
	public String nearSlopes(@QueryParam("lat") double lat,
			@QueryParam("lon") double lon, @QueryParam("dis") float dis) {
		String jsonFile = null;
		try {
			ArrayList<LibSlope> slopes = dbHandler
					.findNearSlopes(lat, lon, dis);
			jsonFile = jsonParser.serializeSlopeArrayList(slopes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonFile;
	}

	@POST
	@Path("/report-incidences")
	@Consumes("application/json")
	@Produces("text/plain")
	public String reportIncidences(final String incidencesJson) {
		String result;
		try {
			dbHandler.startTransaction();
			ArrayList<ArrayList<LibIncidence>> incidencesList = jsonParser
					.deserializeIncidenceArrayList(incidencesJson);
			for (ArrayList<LibIncidence> incidences : incidencesList) {
				if (incidences != null && !incidences.isEmpty()) {
					LibIncidence incidence = incidences.get(0);
					if (incidence instanceof LibPothole) {
						dbHandler.insertPotholes(incidences);
					} else if (incidence instanceof LibCurve)
						dbHandler.insertCurves(incidences);
					else if (incidence instanceof LibSlope)
						dbHandler.insertSlopes(incidences);
				}
			}
			dbHandler.commitTransaction();
			result=CommonConstants.OK_REPORTING;
		} catch (Exception e) {
			result=CommonConstants.ERROR_REPORTING;
			e.printStackTrace();
			try {
				dbHandler.rollbackTransaction();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/report-potholes")
	@Consumes("application/json")
	@Produces("text/plain")  
	public String reportPothole(final String potholesJson) {
		String result;
		
		try {
			dbHandler.startTransaction();
			@SuppressWarnings("rawtypes")
			ArrayList potholes = jsonParser
					.deserializePotholeArrayList(potholesJson);
			dbHandler.insertPotholes(potholes);
			dbHandler.commitTransaction();
			result=CommonConstants.OK_REPORTING;
		} catch (Exception e) {
			result=CommonConstants.ERROR_REPORTING;
			e.printStackTrace();
			try {
				dbHandler.rollbackTransaction();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/report-curves")
	@Consumes("application/json")
	@Produces("text/plain")  
	public String reportCurve(final String curvesJson) {
		String result;
		
		try {
			dbHandler.startTransaction();
			@SuppressWarnings("rawtypes")
			ArrayList curves = jsonParser
					.deserializeCurveArrayList(curvesJson);
			dbHandler.insertCurves(curves);
			dbHandler.commitTransaction();
			result=CommonConstants.OK_REPORTING;
		} catch (Exception e) {
			result=CommonConstants.ERROR_REPORTING;
			e.printStackTrace();
			try {
				dbHandler.rollbackTransaction();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@POST
	@Path("/report-slopes")
	@Consumes("application/json")
	@Produces("text/plain") 
	public String reportSlope(final String slopesJson) {
		String result;
		try {
			dbHandler.startTransaction();
			@SuppressWarnings("rawtypes")
			ArrayList slopes = jsonParser
					.deserializeSlopeArrayList(slopesJson);
			dbHandler.insertSlopes(slopes);
			dbHandler.commitTransaction();
			result=CommonConstants.OK_REPORTING;
		} catch (Exception e) {
			result=CommonConstants.ERROR_REPORTING;
			e.printStackTrace();
			try {
				dbHandler.rollbackTransaction();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	// TODO-s For this week

	// TODO para la siguiente semana
	// TODO Poder elegir cada cuanto tiempo se reportan las incidencias
	// (semanal, diario o al momento de capturar, wifi...)
	// TODO Pensar si convendria tener la bd de las incidencias sin conexión a
	// internet
	// TODO Al reportar una incidencia, asegurarse de que no está reportada ya en la base de datos para que no haya información redundante (si hay misma incidencia registrada en 200m, no volver a registrarla)

	// TODO-s for end
	// TODO Balance economico (graficos), mercado...
	// TODO Terminar ciertos aspectos app android: Que no casque, bluetooth
	// opcional, asegurarse bt y gps activado, modo noche automatico...
	// TODO Securizar servidor (usuario y contraseña y metodo post)
	// TODO Cajita para blend micro y acelerometro (adesiva)
	// TODO presentación: enseñar gráfico de bache
	// TODO OBD + velocidad + orientacion vehiculo
	// TODO Limpiar código, sysos...
	//TODO Modelo detector de curve y slope
}
