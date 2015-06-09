package com.yuriydev.seeu.openmode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import com.yuriydev.seeu.DataBaseConnection;
import com.yuriydev.seeu.DataBaseService;
import com.yuriydev.seeu.IDGenerator;
import com.yuriydev.seeu.MessageType;
import com.yuriydev.seeu.SeeUMessage;
import com.yuriydev.seeu.SeeUTimestamp;


public class SeeUClientThread extends Thread
{
	private String clientId;
	private Socket socket;
	private SeeUCommutator commutator;
	private ObjectInputStream inputObjectStream;
	private ObjectOutputStream outputObjectStream;
	private DataBaseService dbService;
	
	SeeUClientThread(Socket socket, SeeUCommutator commutator, DataBaseConnection dbConnection)
	{
		this.socket = socket;
		this.commutator = commutator;
		
		try {
			this.inputObjectStream = new ObjectInputStream(this.socket.getInputStream());
			this.outputObjectStream = new ObjectOutputStream(this.socket.getOutputStream());
			this.dbService = new DataBaseService(dbConnection.getConnection(), dbConnection.getSchemaName());
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void messageProcessing(SeeUMessage message)
	{
		switch (message.getType()) {
		case MessageType.EXCEPTION_MESSAGE:
			System.out.println("Exception from "+clientId+":\n"+message.getMess());
			break;
			
		case MessageType.LOCATION_REQUEST:
			commutator.addMessage(message);
			break;
			
		case MessageType.LOCATION_RESPONSE:
			commutator.addMessage(message);
			break;
			
		case MessageType.DENIED:
			commutator.addMessage(message);
			break;
			
		case MessageType.COULD_NOT_DETERMINE_LOCATION:
			commutator.addMessage(message);
			break;
			
		case MessageType.ID_MESSAGE:
			clientId = message.getMess();
			
			if((clientId == null)||(clientId.length() == 0/*IDGenerator.SIZE_ID*/))
			{
				clientId = IDGenerator.generateID();
				try {
					outputObjectStream.writeObject(new SeeUMessage(MessageType.ID_MESSAGE, clientId, null, null));
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Generated new id: "+clientId);
				
				dbService.insertNewRegisteredId(clientId);
			}
			else
			{
				System.out.println("Thread "+clientId+": Received id: "+this.clientId);
			}
			
			dbService.insertConnection(clientId);
			break;

		default:
			break;
		}
	}
	
	public void run()
	{
		while(true)
		{
			try {
				
				messageProcessing((SeeUMessage) inputObjectStream.readObject());
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Thread "+clientId+": connection aborted");
				break;
			}
		}
		
	    try {
	    	outputObjectStream.close();
	    	inputObjectStream.close();
	    	socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    dbService.deleteConnection(clientId);
	    System.out.println("Thread "+clientId+": finished");
	}
	
	public void disconnect()
	{
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSocketClosed() {
		return socket.isClosed();
	}
	public String getClientId() {
		return clientId;
	}
	
	public ObjectOutputStream getOutputObjectStream() {
		return outputObjectStream;
	}
}