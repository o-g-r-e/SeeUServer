package com.yuriydev.seeu;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class DataBaseService
{
	private Connection connection;
	private String schemaName;
	
	public DataBaseService(Connection connection, String schemaName)
	{
		this.connection = connection;
		this.schemaName = schemaName;
	}
	
	private class QueryExecutor extends Thread
	{
		PreparedStatement preparedStatement;
		
		QueryExecutor(PreparedStatement preparedStatement)
		{
			this.preparedStatement = preparedStatement;
			start();
		}
		
		@Override
		public void run()
		{
			try {
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void insertNewRegisteredId(String clientId)
	{
		if(connection != null)
		{
			String queryString = "INSERT INTO `seeu_db`.`generated_ids` (`client_id`, `register_date`, `last_online_date`, `messages_total_num`) VALUES ( ?, ?, ?, '0');";
			try {
				PreparedStatement preparedStatement = connection.prepareStatement(queryString);
				preparedStatement.setString(1, clientId);
				Date date = new Date(Calendar.getInstance().getTimeInMillis());
				preparedStatement.setDate(2, date);
				preparedStatement.setDate(3, date);
				new QueryExecutor(preparedStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void insertConnection(String clientId)
	{
		if(connection != null)
		{
			String queryString = "INSERT INTO `seeu_db`.`connections` (`client_id`) VALUES (?);";
			try {
				PreparedStatement preparedStatement = connection.prepareStatement(queryString);
				preparedStatement.setString(1, clientId);
				new QueryExecutor(preparedStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void deleteConnection(String clientId)
	{
		if(connection != null)
		{
			String queryString = "DELETE FROM `seeu_db`.`connections` WHERE `client_id`=?;";
			try {
				PreparedStatement preparedStatement = connection.prepareStatement(queryString);
				preparedStatement.setString(1, clientId);
				new QueryExecutor(preparedStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}