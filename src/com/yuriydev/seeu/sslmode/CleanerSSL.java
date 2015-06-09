package com.yuriydev.seeu.sslmode;

import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;

public class CleanerSSL extends TimerTask
{
	private Timer timer = null;
	private SeeUConnectionThreadSSL context = null;
	
	CleanerSSL(SeeUConnectionThreadSSL context)
	{
		this.context = context;
		this.timer = new Timer();
		this.timer.schedule(this, 30000, 30000);
	}
	
	public void cancelCleaner()
	{
		timer.cancel();
	}
	
	public void run()
	{
		//System.out.println("Cleaner run: ");
		int size = context.getClients().size();
		//System.out.println("connections: "+size);
		for(ListIterator<SeeUClientThreadSSL> i = context.getClients().listIterator(); i.hasNext(); )
		{
			SeeUClientThreadSSL el = i.next();
			if(el.isSocketClosed())
			{
				//System.out.println("connection: "+el.getClientId()+" closed");
				i.remove();
			}
		}
		//System.out.println("connections: "+context.getClients().size());
	}
}