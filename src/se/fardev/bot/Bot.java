package se.fardev.bot;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.fardev.bot.io.IncomingCommand;
import se.fardev.bot.io.IRCSocketChannel;
import se.fardev.bot.io.OutgoingCommand;
import se.fardev.bot.util.Config;

public class Bot implements Runnable {
	private final Logger logger = LogManager.getLogger();
	
	private final IRCSocketChannel socketChannel;
	private final Config config;
	private Thread thread;
	private volatile boolean running;
	
	
	public Bot(Config config) {
		this.config = config;
		socketChannel = new IRCSocketChannel();
		init();
	}
	
	private final void init() {
		Runnable shutdownHook = new Runnable() {
			@Override
			public void run() {
				destroy();
			}
		};
		Runtime.getRuntime().addShutdownHook(new Thread(shutdownHook, "ShutdownHook"));
		
		String ircHost = config.getProperty("irc.host");
		int ircPort = Integer.valueOf(config.getProperty("irc.port"));
		String ircUser = config.getProperty("irc.user");
		String ircPass = config.getProperty("irc.pass");
		
		try {
			socketChannel.connect(ircHost, ircPort);
		} catch (IOException e) {
			logger.fatal("Could not connect to irc {}:{}", ircHost, ircPort);
			System.exit(-1);
		} finally {
			socketChannel.writeOutgoing(OutgoingCommand.build("PASS", ircPass));
			socketChannel.writeOutgoing(OutgoingCommand.build("NICK", ircUser));
			socketChannel.writeOutgoing(OutgoingCommand.build("JOIN", "#esl_hearthstone"));
			//socketChannel.writeOutgoing(OutgoingCommand.build("JOIN", "#fiftyseven__"));
		}
		logger.info("Bot successfully initialized");
	}
	
	private final void destroy() {
		logger.info("Got terminate signal, destroying.");
		
		running = false;
		try {
			thread.join(10000L);
		} catch (InterruptedException e) {
			logger.error("Error while joining thread {}, failing silently", thread.getName());
		}
		
		if(socketChannel.isConnected()) {
			try {
				socketChannel.disconnect();
			} catch (IOException e) {
				logger.error("Failed to disconnect socketchannel", e);
			}
		}
		
	}
	
	public final void start() {
		if(running) {
			logger.warn("Tried to start bot while already running");
			return;
		}
		running = true;
		thread = new Thread(this, "BotThread");
		thread.start();
	}
	
	@Override
	public final void run() {
		while(running) {
			if(socketChannel.hasNext()) {
				IncomingCommand c = socketChannel.getNext();
				logger.debug("Command: {}", c.toString());
				if(c.getType().equals("PING")) {
					//reply with PONG $HOST to let them know we have not disconnected
					socketChannel.writeOutgoing(OutgoingCommand.build("PONG", c.getMessage()));
					logger.debug("PING -> PONG");
				}
			}
			
			try {
				Thread.sleep(10L);
			} catch (InterruptedException e) {
				logger.error("Interrupted while sleeping");
			}
		}
	}
	
	public static void main(String[] args) {
		Config config = null;
		String configFilename = "bot.conf";
		
		try {
			config = Config.fromClasspath(configFilename);
		} catch (IOException e) {
			LogManager.getLogger().fatal("Failed to load config file \"{}\", e: \"{}\"", configFilename, e.getMessage());
			System.exit(-1);
		}
		
		Bot bot = new Bot(config);
		bot.start();
	}
}
