package com.yuriydev.seeu;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataBaseConnection extends Thread
{
	private Connection connection;
	//private String driver = "com.mysql.jdbc.Driver"; 
	private String server = "localhost";
	private String schemaName = "seeu_db";
	private String url = "jdbc:mysql://" + server + "/" + schemaName;
	private String username = "root";
	private String password = "Kxsyh84XBzCZRZI";
	
	public DataBaseConnection() {
		start();
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void close()
	{
		try {
			if(connection != null) connection.close();
			System.out.println("DB connection: closed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run()
	{
		try {
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("DB connection: "+connection.getCatalog());
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("DB connection: error");
		}
	}
	
	public String getSchemaName() {
		return schemaName;
	}
}