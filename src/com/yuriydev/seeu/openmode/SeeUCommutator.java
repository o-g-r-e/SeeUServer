package com.yuriydev.seeu.openmode;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.yuriydev.seeu.MessageType;
import com.yuriydev.seeu.SeeUMessage;

public class SeeUCommutator extends Thread
{
	private SeeUConnectionThread connectionThreadContext;
	private List<SeeUMessage> messages;
	private boolean exit;
	
	SeeUCommutator(SeeUConnectionThread connectionThreadContext)
	{
		this.connectionThreadContext = connectionThreadContext;
		this.messages = new ArrayList<SeeUMessage>();
		start();
	}
	
	public void addMessage(SeeUMessage message)
	{
		synchronized(messages) {
			messages.add(message);
			System.out.println("Commutator: new message");
		}
	}
	
	public void close()
	{
		exit = true;
	}
	
	@Override
	public void run()
	{
		System.out.println("Commutator thread: started");
		
		while(!exit)
		{
			try {
				sleep(1);
			} catch (InterruptedException e) {}
			
			if(messages.size() > 0)
			{
				System.out.println("Commutator working, messages num:"+messages.size());
					
				SeeUMessage currentMessage = messages.get(0);
					
				SeeUClientThread recipient = connectionThreadContext.findClientThreadByID(currentMessage.getRecipientId());
						
				if(recipient != null)
				{
					sendMessage(recipient.getOutputObjectStream(), currentMessage);
				}
				else
				{
					SeeUClientThread sender = connectionThreadContext.findClientThreadByID(currentMessage.getSenderId());
					SeeUMessage message = new SeeUMessage(MessageType.USER_OFFLINE_MESSAGE, currentMessage.getRecipientId());
					sendMessage(sender.getOutputObjectStream(), message);
				}
					
				synchronized(messages) {
					messages.remove(0);
				}
				
				System.out.println("Commutator processed, messages num:"+messages.size());
			}
		}
		
		System.out.println("Commutator thread: finished");
	}
	
	private void sendMessage(ObjectOutputStream stream, SeeUMessage message)
	{
		try {
			stream.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}