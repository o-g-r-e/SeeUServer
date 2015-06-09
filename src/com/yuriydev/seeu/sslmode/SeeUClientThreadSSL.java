package com.yuriydev.seeu.sslmode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import com.yuriydev.seeu.DataBaseConnection;
import com.yuriydev.seeu.DataBaseService;
import com.yuriydev.seeu.IDGenerator;
import com.yuriydev.seeu.MessageType;
import com.yuriydev.seeu.SeeUMessage;
import com.yuriydev.seeu.openmode.SeeUConnectionThread;

public class SeeUClientThreadSSL extends Thread
{
	private int messagesTotalNum;
	private String id = null;
	private SSLSocket socket = null;
	private SeeUConnectionThreadSSL context = null;
	private ObjectInputStream inputObjectStream = null;
	private ObjectOutputStream outputObjectStream = null;
	private boolean exit = false;
	private DataBaseService dbService;
	
	SeeUClientThreadSSL(SSLSocket socket, SeeUConnectionThreadSSL context)
	{
		this.socket = socket;
		this.context = context;
		this.exit = false;
		DataBaseConnection dbConn = context.getDbConnection();
		this.dbService = new DataBaseService(dbConn.getConnection(), dbConn.getSchemaName());
		
		try {
			this.inputObjectStream = new ObjectInputStream(this.socket.getInputStream());
			this.outputObjectStream = new ObjectOutputStream(this.socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.start();
	}
	
	public void run()
	{
		SeeUMessage message = null;
		while(!exit)
		{
			
			message = new SeeUMessage();
			try {
				message = (SeeUMessage) inputObjectStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("connection aborted");
				exit = true;
			}
			
			if(message.getType() == MessageType.EXCEPTION_MESSAGE)
			{
				System.out.println("hello_message from "+id+": "+message.getMess());
			}
			
			if((message.getType() == MessageType.LOCATION_REQUEST)||(message.getType() == MessageType.LOCATION_RESPONSE)||(message.getType() == MessageType.DENIED))
			{
				context.getCommutator().addMessage(message);
			}
			
			/*if(message.getType() == MessageType.CLOSE_CONNECTION)
			{
				System.out.println("close_connection message");
				exit = true;
			}*/
			
			if(message.getType() == MessageType.ID_MESSAGE)
			{
				id = message.getSenderId();
				
				if((id == null)||(id.length() == 0/*IDGenerator.SIZE_ID*/))
				{
					id = IDGenerator.generateID();
					try {
						outputObjectStream.writeObject(new SeeUMessage(MessageType.ID_MESSAGE, id, null, null));
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.out.println("Generated new id: "+id);
					
					dbService.insertNewRegisteredId(id);
				}
				else
				{
					System.out.println("Received id: "+this.id);
				}
			}
		}
		
	    try {
	    	outputObjectStream.close();
	    	inputObjectStream.close();
	    	socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println("Thread "+id+" finished, connection close");
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
		return id;
	}
	public boolean isExit() {
		return exit;
	}
	
	public ObjectOutputStream getOutputObjectStream() {
		return outputObjectStream;
	}
}