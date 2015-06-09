package com.yuriydev.seeu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.yuriydev.seeu.openmode.SeeUConnectionThread;
import com.yuriydev.seeu.sslmode.SeeUConnectionThreadSSL;


public class MainThread
{
	private static SeeUConnectionThread connectionThread;
	private static BufferedReader consoleInputStream;
	private static boolean sslMode = false;
	
	private static void init()
	{
		connectionThread = new SeeUConnectionThread();
		
		consoleInputStream = new BufferedReader(new InputStreamReader(System.in));
	}
	
	private static int commandProcessing(String command)
	{
		switch (command)
		{
		
		case "exit":
			connectionThread.close();
			return 0;

		default:
			System.out.println("Unknown commad");
			return 1;
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Main thread started");
		init();
			
		while(true) {
			int code = commandProcessing(consoleInputStream.readLine());
			
			if(code == 0)
				break;
		}
		
		System.out.println("Main thread finished");
	}
}