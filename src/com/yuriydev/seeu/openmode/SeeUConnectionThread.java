package com.yuriydev.seeu.openmode;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.yuriydev.seeu.DataBaseConnection;


public class SeeUConnectionThread extends Thread
{
	private ServerSocket serverSocket;
	private List<SeeUClientThread> clients;
	private Cleaner cleaner;
	private SeeUCommutator commutator;
	private DataBaseConnection dbConnection;
	
	public SeeUConnectionThread()
	{
		try {
			serverSocket = new ServerSocket(4444);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		clients = new ArrayList<SeeUClientThread>();
		cleaner = new Cleaner(clients);
		commutator = new SeeUCommutator(this);
		dbConnection = new DataBaseConnection();
		this.start();
	}
	
	public SeeUClientThread findClientThreadByID(String id)
	{
		synchronized(clients)
		{
			int size = clients.size();
			
			for (int i = 0; i < size; i++)
			{
				SeeUClientThread clientThread = clients.get(i);
				
				if((clientThread != null)&&(clientThread.getClientId().equals(id))&&(!clientThread.isSocketClosed())&&(clientThread.isAlive()))
				{
					return clientThread;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void run()
	{
		System.out.println("Connection thread started");
		while (true)
		{
			try {
				Socket clientSocket = serverSocket.accept();
				
				if(clientSocket != null)
				{
					synchronized(clients) {
						clients.add(new SeeUClientThread(clientSocket, commutator, dbConnection));
						System.out.println("Connection thread: New connection accepted");
					}
				}
			} catch (IOException e) {
				if(serverSocket.isClosed())
					break;
			}
		}
		
		System.out.println("Connection thread finished");
	}
	
	public void close()
	{
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		commutator.close();
		
		cleaner.cancelCleaner();
		
		for (int i = 0; i < clients.size(); i++) {
			clients.get(i).disconnect();
		}
		
		dbConnection.close();
	}
	
	public List<SeeUClientThread> getClients() {
		return clients;
	}
	
	public SeeUCommutator getCommutator() {
		return commutator;
	}
	
	public DataBaseConnection getDbConnection() {
		return dbConnection;
	}
}