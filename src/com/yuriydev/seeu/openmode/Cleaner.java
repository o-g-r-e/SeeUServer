package com.yuriydev.seeu.openmode;

import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;


public class Cleaner extends TimerTask
{
	private Timer timer;
	private List<SeeUClientThread> clientThreads;
	
	Cleaner(List<SeeUClientThread> clientThreads)
	{
		this.clientThreads = clientThreads;
		this.timer = new Timer();
		this.timer.schedule(this, 30000, 30000);
	}
	
	public void cancelCleaner()
	{
		timer.cancel();
		System.out.println("Cleaner canceled");
	}
	
	public void run()
	{
		for(ListIterator<SeeUClientThread> i = clientThreads.listIterator(); i.hasNext();)
		{
			SeeUClientThread el = i.next();
			if(el.isSocketClosed())
			{
				i.remove();
			}
		}
	}
}