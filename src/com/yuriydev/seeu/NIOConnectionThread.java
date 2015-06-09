package com.yuriydev.seeu;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class NIOConnectionThread extends Thread
{
	private boolean exit;
	private List<SocketChannel> clients = new ArrayList<SocketChannel>();
	private byte[] bytes = new byte[512];
	private ByteBuffer buffer = ByteBuffer.wrap(bytes);
	//ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
	//ByteArrayInputStream bais;
	//private ObjectOutputStream outObjectStream;
	//private ObjectInputStream inObjectStream;
	
	NIOConnectionThread()
	{
		exit = false;
		this.start();
	}
	
	public void run()
	{
		System.out.println("Connection thread start");
		
		try {
			Selector selector = Selector.open();
			System.out.println("selector: "+selector);
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.socket().bind(new java.net.InetSocketAddress(4444));
			serverSocket.configureBlocking(false);
			System.out.println("selector opened: "+selector.isOpen());
			System.out.println("server socket closed: "+serverSocket.socket().isClosed());
			System.out.println("port: "+serverSocket.socket().getLocalPort());
			System.out.println("addres: "+serverSocket.socket().getInetAddress());
			SelectionKey serverkey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	    
			while(!exit)
			{
				selector.select();
				System.out.println("key detected");
				Set keys = selector.selectedKeys();
	        
				Iterator it = keys.iterator();
	        
				while(it.hasNext())
				{
					SelectionKey key = (SelectionKey) it.next();
					
					if(key == serverkey)//if((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT)
					{
						System.out.println("connection key detected");
						SocketChannel client = serverSocket.accept();
						
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						
						System.out.println("connection access");
						clients.add(client);
						
						/*SeeuMessage message = new SeeuMessage();
						buffer.put(message.create(SeeuMessage.HELLO_MESSAGE, "I see U too"));
						buffer.flip();
						client.write(buffer);*/
							
						key.cancel();
					}
					else
					{
						System.out.println("read data key detected");
						SocketChannel client = (SocketChannel) key.channel();
						
						client.read(buffer);
						/*Charset charset = Charset.forName("ISO-8859-1");
					    CharsetEncoder encoder = charset.newEncoder();
					    CharsetDecoder decoder = charset.newDecoder();*/
					    /*buffer.flip();
					    byte[] b = new byte[512];
					    while (buffer.hasRemaining()) {
							byte t = buffer.get();
						      System.out.print((char) t+" "+t);
						    }
					    
						buffer.get(b, 0, buffer.remaining());*/
						
						NIOSeeuMessage message = new NIOSeeuMessage(buffer);
	        		
							if(message.getType() == NIOSeeuMessage.HELLO_MESSAGE)
							{
								System.out.println(message.getParameter());
								message = new NIOSeeuMessage();
								buffer.put(message.create(NIOSeeuMessage.HELLO_MESSAGE, "I see U too"));
								buffer.flip();
								for (int i = 0; i < clients.size(); i++)
								{
									System.out.println("write");
									//clients.get(i).write(buffer);
									client.write(buffer);
									System.out.println("write ok");
								}
							}
						key.cancel();
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    System.out.println("Connection thread closed");
	}

	public void setExit(boolean exit) {
		this.exit = exit;
	}
}