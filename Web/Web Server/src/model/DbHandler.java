package model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import custom_incidences.LibCurve;
import custom_incidences.LibIncidence;
import custom_incidences.LibPothole;
import custom_incidences.LibSlope;
import dao.Database;

public class DbHandler {
	Database database;
	Connection connection = null;

	public DbHandler() throws Exception {
		database = new Database();
		connection = database.connect();
		connection.setAutoCommit(true);
	}

	public ArrayList<LibPothole> findNearPotholes(double lat, double lon,
			float radius) throws SQLException {
		ArrayList<LibPothole> potholes = new ArrayList<LibPothole>();
		LibPothole pothole;
		try {
			PreparedStatement ps = connection
					.prepareStatement("select * from get_surrounding_potholes(?,?,?)");
			ps.setDouble(1, lat);
			ps.setDouble(2, lon);
			ps.setDouble(3, radius);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				pothole = new LibPothole();
				pothole.setIncidenceId(rs.getInt("incId"));
				pothole.setLatitude(rs.getDouble("lat"));
				pothole.setLongitude(rs.getDouble("lon"));
				pothole.setPrevLat(rs.getDouble("prevLat"));
				pothole.setPrevLon(rs.getDouble("prevLon"));
				pothole.setAccuracy(rs.getFloat("accu"));
				pothole.setMagnitude(rs.getInt("mag"));
				potholes.add(pothole);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return potholes;
	}

	public ArrayList<LibCurve> findNearCurves(double lat, double lon,
			float radius) throws SQLException {
		ArrayList<LibCurve> curves = new ArrayList<LibCurve>();
		LibCurve curve;
		/**/
		try {
			PreparedStatement ps = connection
					.prepareStatement("select * from get_surrounding_curves(?,?,?)");
			ps.setDouble(1, lat);
			ps.setDouble(2, lon);
			ps.setDouble(3, radius);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				curve = new LibCurve();
				curve.setIncidenceId(rs.getInt("incId"));
				curve.setLatitude(rs.getDouble("lat"));
				curve.setLongitude(rs.getDouble("lon"));
				curve.setPrevLat(rs.getDouble("prevLat"));
				curve.setPrevLon(rs.getDouble("prevLon"));
				curve.setAccuracy(rs.getFloat("accu"));
				curve.setMagnitude(rs.getInt("mag"));
				curve.setRight(rs.getBoolean("isRight"));
				curves.add(curve);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return curves;
	}

	public ArrayList<LibSlope> findNearSlopes(double lat, double lon,
			float radius) throws SQLException {
		ArrayList<LibSlope> slopes = new ArrayList<LibSlope>();
		LibSlope slope;
		try {
			PreparedStatement ps = connection
					.prepareStatement("select * from get_surrounding_slopes(?,?,?)");
			ps.setDouble(1, lat);
			ps.setDouble(2, lon);
			ps.setDouble(3, radius);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				slope = new LibSlope();
				slope.setIncidenceId(rs.getInt("incId"));
				slope.setLatitude(rs.getDouble("lat"));
				slope.setLongitude(rs.getDouble("lon"));
				slope.setPrevLat(rs.getDouble("prevLat"));
				slope.setPrevLon(rs.getDouble("prevLon"));
				slope.setAccuracy(rs.getFloat("accu"));
				slope.setMagnitude(rs.getInt("mag"));
				slope.setEndLat(rs.getDouble("endLat"));
				slope.setEndLon(rs.getDouble("endLon"));
				slope.setSlope(rs.getInt("slop"));
				slopes.add(slope);
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return slopes;
	}

	public void insertPotholes(ArrayList<LibIncidence> incidences)
			throws Exception {
		for (LibIncidence incidence : incidences) {
			LibPothole pothole = (LibPothole) incidence;
			try {
				PreparedStatement ps = connection
						.prepareStatement("select insert_pothole(?,?,?,?,?,?)");
				ps.setDouble(1, pothole.getLatitude());
				ps.setDouble(2, pothole.getLongitude());
				ps.setDouble(3, pothole.getPrevLat());
				ps.setDouble(4, pothole.getPrevLon());
				ps.setFloat(5, pothole.getAccuracy());
				ps.setInt(6, pothole.getMagnitude());
				ps.executeQuery();
			} catch (Exception ex) {
				System.out.println(ex);
				throw new Exception();
			}
		}
	}
	
	public void insertCurves(ArrayList<LibIncidence> incidences)
			throws Exception {
		for (LibIncidence incidence : incidences) {
			LibCurve curve = (LibCurve) incidence;
			try {
				PreparedStatement ps = connection
						.prepareStatement("select insert_curve(?,?,?,?,?,?,?)");
				ps.setDouble(1, curve.getLatitude());
				ps.setDouble(2, curve.getLongitude());
				ps.setDouble(3, curve.getPrevLat());
				ps.setDouble(4, curve.getPrevLon());
				ps.setFloat(5, curve.getAccuracy());
				ps.setInt(6, curve.getMagnitude());
				ps.setBoolean(7, curve.isRight());
				ps.executeQuery();
			} catch (Exception ex) {
				System.out.println(ex);
				throw new Exception();
			}
		}
	}

	public void insertSlopes(ArrayList<LibIncidence> incidences)
			throws Exception {
		for (LibIncidence incidence : incidences) {
			LibSlope slope = (LibSlope) incidence;
			try {
				PreparedStatement ps = connection
						.prepareStatement("select insert_slope(?,?,?,?,?,?,?,?,?)");
				ps.setDouble(1, slope.getLatitude());
				ps.setDouble(2, slope.getLongitude());
				ps.setDouble(3, slope.getPrevLat());
				ps.setDouble(4, slope.getPrevLon());
				ps.setFloat(5, slope.getAccuracy());
				ps.setInt(6, slope.getMagnitude());
				ps.setDouble(7, slope.getEndLat());
				ps.setDouble(8, slope.getEndLon());
				ps.setInt(9, slope.getSlope());
				ps.executeQuery();
			} catch (Exception ex) {
				System.out.println(ex);
				throw new Exception();
			}
		}
	}

	public void closeConnection() throws Exception {
		database.disconnect();
	}
	public void aaa(){
		try {
			System.out.println(connection.getAutoCommit());
			
			Thread.sleep(1000000);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void startTransaction() throws SQLException{
		connection.setAutoCommit(false);
	}
	public void rollbackTransaction() throws SQLException{
		connection.rollback();
	}
	public void commitTransaction() throws SQLException{
		connection.commit();
	}
}
