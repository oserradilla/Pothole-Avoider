package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	// Connection to a postgresql database server

	public static String forName = "org.postgresql.Driver";

	public static String user = "potholeavoider";
	public static String password = "ritDUG7QIsQ3IDtUcgdXnvXJt0CHgf5I";
	public static String ip = "localhost";
	public static String port = "5432";
	public static String db_name = "potholeavoider";
	public static String driver = "jdbc:postgresql";

	private Connection connection;
	public Connection connect() throws Exception {
		try {
			connection = null;
			Class.forName(forName);
			String path = driver + "://" + ip + ":" + port + "/" + db_name;
			connection = DriverManager.getConnection(path, user, password);
			return connection;
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public void disconnect() throws Exception {
		if(connection!=null)
			connection.close();
	}
}
