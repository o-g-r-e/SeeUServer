package com.yuriydev.seeu.sslmode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.yuriydev.seeu.DataBaseConnection;
import com.yuriydev.seeu.openmode.Cleaner;
import com.yuriydev.seeu.openmode.SeeUClientThread;
import com.yuriydev.seeu.openmode.SeeUCommutator;

public class SeeUConnectionThreadSSL extends Thread
{
	private SSLServerSocket serverSocket = null;
	private ArrayList<SeeUClientThreadSSL> clients = null;
	private boolean exit = false;
	private CleanerSSL cleaner = null;
	private SeeUCommutatorSSL commutator;
	private DataBaseConnection dbConnection;
	
	public SeeUConnectionThreadSSL()
	{
		try {
			KeyStore ksTrust = KeyStore.getInstance("BKS");
			File file = new File("c:\\Users\\Ura\\workspace\\SeeUServer\\res\\sslcert");
			FileInputStream fis = new FileInputStream(file);
			ksTrust.load(fis, "android".toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			tmf.init(ksTrust);
			 
			// Create a SSLContext with the certificate
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
		
		
		 
			// Create a HTTPS connection
			//URL url = new URL("https", "10.0.2.2", 8443, "/ssltest");
			//HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			 
			/* Uncomment the following line of code if you want to skip SSL */
			/* hostname verification.  But it should only be done for testing. */
			/* See http://randomizedsort.blogspot.com/2010/09/programmatically-disabling-java-ssl.html */
			/* conn.setHostnameVerifier(new NullVerifier()); */
			 
			//conn.setSSLSocketFactory(sslContext.getSocketFactory());
			
			//SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
			//SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(4444);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		clients = new ArrayList<SeeUClientThreadSSL>();
		exit = false;
		cleaner = new CleanerSSL(this);
		commutator = new SeeUCommutatorSSL(this);
		dbConnection = new DataBaseConnection();
		this.start();
	}
	
	@Override
	public void run()
	{
		System.out.println("Connection thread started");
		SSLSocket clientSocket = null;
		while (!exit)
		{
			try {
				clientSocket = (SSLSocket) serverSocket.accept();
				if(clientSocket != null)
				{
					clients.add(new SeeUClientThreadSSL(clientSocket, this));
					System.out.println("New connection accepted");
					
				}
			} catch (IOException e) {
				e.printStackTrace();
				exit = true;
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
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
		
		cleaner.cancel();
		
		synchronized (clients)
		{
			for (int i = 0; i < clients.size(); i++)
			{
				clients.get(i).disconnect();
				
				long sTime = System.currentTimeMillis();
				while (((System.currentTimeMillis() - sTime) < 3000)&&(clients.get(i).isAlive()));
			}
		}
		
		dbConnection.close();
	}
	
	public ArrayList<SeeUClientThreadSSL> getClients() {
		return clients;
	}
	
	public SeeUCommutatorSSL getCommutator() {
		return commutator;
	}
	
	public DataBaseConnection getDbConnection() {
		return dbConnection;
	}
}