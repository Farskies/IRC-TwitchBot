package se.fardev.bot.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IRCSocketChannel implements Runnable {
	private final Logger log = LogManager.getLogger();
	
	private Socket socket;
	private InputStream inputStream;
	private InputStreamReader inputStreamReader;
	private OutputStream outputStream;
	private StringBuilder stringBuffer;
	private Thread thread;
	private CommandParser commandParser;
	private List<IncomingCommand> incomingCommandQueue;
	private List<OutgoingCommand> outgoingCommandQueue;
	
	private volatile boolean running = false;
	
	public IRCSocketChannel() {
		incomingCommandQueue = new ArrayList<IncomingCommand>();
		outgoingCommandQueue = new ArrayList<OutgoingCommand>();
		commandParser = new CommandParser();
		stringBuffer = new StringBuilder();
	}
	
	public synchronized void connect(String host, int port) throws IOException {
		if(isConnected()) {
			log.warn("SocketChannel is already connected when trying to connect to {}:{}", host, port);
			return;
		}
		
		socket = new Socket();
		socket.connect(new InetSocketAddress(host, port));
		socket.setTcpNoDelay(false);
		socket.setSoTimeout(1000);
		
		inputStream = socket.getInputStream();
		inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
		outputStream = socket.getOutputStream();
		
		if(thread == null || !thread.isAlive()) {
			running = true;
			String threadName = String.format("SCThread(%s:%d)", host, port);
			thread = new Thread(this, threadName);
			thread.start();
		}
		
		log.info("Connected to {}:{}", host, port);
	}
	
	public synchronized void disconnect() throws IOException {
		if(!isConnected()) {
			log.warn("Tried to disconnect SocketChannel which is not connected");
			return;
		}
		
		try {
			running = false;
			try {
				thread.join(10000L);
			} catch (InterruptedException e) {
				log.error("Error while joining thread {}, failing silently", thread.getName());
			}
			inputStream.close();
			outputStream.close();
			socket.close();
		} finally {
			socket = null;
			thread = null;
		}
	}
	
	public synchronized boolean isConnected() {
		if(socket == null)
			return false;
		return socket.isConnected();
	}
	
	public boolean hasNext() {
		synchronized(incomingCommandQueue) {
			return !incomingCommandQueue.isEmpty();
		}
	}
	
	public IncomingCommand getNext() {
		synchronized(incomingCommandQueue) {
			if(hasNext()) {
				IncomingCommand next = incomingCommandQueue.get(0);
				incomingCommandQueue.remove(0);
				return next;
			}
			return null;
		}
	}
	
	public void writeOutgoing(OutgoingCommand command) {
		if(command == null) {
			log.warn("Got null outgoing command");
			return;
		}
		
		synchronized(outgoingCommandQueue) {
			outgoingCommandQueue.add(command);
		}
	}
	
	@Override
	public void run() {
		while(running) {
			try {
				
				synchronized(outgoingCommandQueue) {
					if(!outgoingCommandQueue.isEmpty()) {
						OutgoingCommand next = outgoingCommandQueue.remove(0);
						outputStream.write(next.getData());
						outputStream.flush();
					}
				}
				
				char[] buffer = new char[1024];
				int bytesRead = 0;
				try {
					if((bytesRead = inputStreamReader.read(buffer, 0, buffer.length)) > 0) {
						processData(buffer, bytesRead);
					}
				} catch(SocketTimeoutException e) {
					//ignore this exception, it's thrown when the read reaches
					//the timeout limit set in socket.setSoTimeout.
					//it is done so that the read doesn't block the thread indefinitely
					//if there is no data to be read.
				}
				
			} catch(IOException e) {
				log.error("Error while processing I/O", e);
			}
		}
	}
	
	private void processData(char[] input, int length) {
		for(int i = 0; i < length; i++) {
			char currentChar = input[i];
			//once we reach CRLF (\0x0A\0x0D), the data in the buffer is one command
			if(currentChar == 0x0A && stringBuffer.charAt(stringBuffer.length() - 1) == 0x0D) {
				String commandData = stringBuffer.substring(0, stringBuffer.length() - 1);
				IncomingCommand command = commandParser.parse(commandData);
				
				if(command != null) {
					synchronized(incomingCommandQueue) {
						incomingCommandQueue.add(command);
					}
				}
				//empty the buffer and continue with the next char
				stringBuffer.setLength(0);
				continue;
			}
			
			stringBuffer.append(currentChar);
		}
	}
}
