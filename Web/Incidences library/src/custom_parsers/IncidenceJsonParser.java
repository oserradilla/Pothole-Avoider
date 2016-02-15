package custom_parsers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import changedpath.org.json.CustomJSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import custom_incidences.LibCurve;
import custom_incidences.LibIncidence;
import custom_incidences.LibPothole;
import custom_incidences.LibSlope;

public class IncidenceJsonParser {
	Gson gson;
	final Type listPothole = new TypeToken<List<LibPothole>>() {}.getType();
	final Type listCurve = new TypeToken<List<LibCurve>>() {}.getType();
	final Type listSlope = new TypeToken<List<LibSlope>>() {}.getType();
	public IncidenceJsonParser(){
		gson=new Gson();
	}
	public ArrayList<ArrayList<LibIncidence>> deserializeIncidenceArrayList(String jsonFile) {
		ArrayList<ArrayList<LibIncidence>> arrayIncidences=new ArrayList<ArrayList<LibIncidence>>();
		ArrayList<LibIncidence>potholes=new ArrayList<LibIncidence>();
		ArrayList<LibIncidence> curves=new ArrayList<LibIncidence>();
		ArrayList<LibIncidence> slopes=new ArrayList<LibIncidence>();
		CustomJSONObject jObj=new CustomJSONObject(jsonFile);
		String jsonText=jObj.getJSONArray("potholes").toString();
		potholes.addAll(deserializePotholeArrayList(jsonText));
		jsonText=jObj.getJSONArray("curves").toString();
		curves.addAll(deserializeCurveArrayList(jsonText));
		jsonText=jObj.getJSONArray("slopes").toString();
		slopes.addAll(deserializeSlopeArrayList(jsonText));
		arrayIncidences.add(0,potholes);
		arrayIncidences.add(1,curves);
		arrayIncidences.add(2,slopes);
		return arrayIncidences;
	}
	public String serializeIncidenceArrayList(ArrayList<LibPothole> potholes,
		ArrayList<LibCurve> curves, ArrayList<LibSlope> slopes){
		CustomJSONObject jObj=new CustomJSONObject();
		jObj.put("potholes", potholes);
		jObj.put("curves", curves);
		jObj.put("slopes", slopes);
		return  jObj.toString();
	}
	public ArrayList<LibPothole> deserializePotholeArrayList(String jsonFile) {
		return  gson.fromJson(jsonFile, listPothole);
	}
	public String serializePotholeArrayList(ArrayList<LibPothole> potholes){
		return  gson.toJson(potholes, listPothole);
	}
	public ArrayList<LibCurve> deserializeCurveArrayList(String jsonFile) {
		return  gson.fromJson(jsonFile, listCurve);
	}
	public String serializeCurveArrayList(ArrayList<LibCurve> curves){
		return  gson.toJson(curves, listCurve);
	}
	public ArrayList<LibSlope> deserializeSlopeArrayList(String jsonFile) {
		return  gson.fromJson(jsonFile, listSlope);
	}
	public String serializeSlopeArrayList(ArrayList<LibSlope> slopes){
		return  gson.toJson(slopes, listSlope);
	}
}
