package com.yuriydev.seeu.sslmode;

import java.io.IOException;
import java.util.ArrayList;

import com.yuriydev.seeu.MessageType;
import com.yuriydev.seeu.SeeUMessage;
import com.yuriydev.seeu.openmode.SeeUClientThread;
import com.yuriydev.seeu.openmode.SeeUConnectionThread;

public class SeeUCommutatorSSL extends Thread
{
	private SeeUConnectionThreadSSL context;
	private ArrayList<SeeUMessage> messages = new ArrayList<SeeUMessage>();
	private boolean exit = false;
	
	SeeUCommutatorSSL(SeeUConnectionThreadSSL context)
	{
		this.context = context;
		start();
		System.out.println("Commutator started");
	}
	
	public synchronized void addMessage(SeeUMessage message)
	{
		synchronized (messages)
		{
			messages.add(message);
			System.out.println("Commutator new message");
		}
	}
	
	@Override
	public void run()
	{
			while(!exit)
			{
				if(messages.size() > 0)
				{
					System.out.println("Commutator working, messages num:"+messages.size());
					synchronized (messages)
					{
						SeeUClientThreadSSL recipient = findByID(((SeeUMessage) messages.get(0)).getRecipientId(), context.getClients());
						
						if(recipient != null)
						{
							//recipient.addMessageForSending(messages.get(0));
							try {
								recipient.getOutputObjectStream().writeObject(messages.get(0));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else
						{
							SeeUClientThreadSSL sender = findByID(((SeeUMessage) messages.get(0)).getSenderId(), context.getClients());
							
							try {
								sender.getOutputObjectStream().writeObject(new SeeUMessage(MessageType.USER_OFFLINE_MESSAGE, messages.get(0).getRecipientId()));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						messages.remove(0);
						System.out.println("Commutator processed, messages num:"+messages.size());
					}
				}
			}
	}
	
	private SeeUClientThreadSSL findByID(String id, ArrayList<SeeUClientThreadSSL> clients)
	{
		SeeUClientThreadSSL clientThread;
		int size = clients.size();
		
		for (int i = 0; i < size; i++)
		{
			clientThread = clients.get(i);
			
			if((clientThread != null)&&(clientThread.getClientId().equals(id))&&(!clientThread.isSocketClosed())&&(clientThread.isAlive()))
			{
				return clientThread;
			}
		}
		
		return null;
	}
}