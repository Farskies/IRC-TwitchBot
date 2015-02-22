package se.fardev.bot.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Config {
	private Properties properties;
	private Logger logger = LogManager.getLogger();
	
	private Config(Properties properties) {
		this.properties = properties;
	}

	public String getProperty(String key) {
		if(!properties.containsKey(key)) {
			logger.warn("Key \"{}\" does not exist", key);
			return null;
		}
		return properties.getProperty(key);
	}
	
	public static Config fromClasspath(String filename) throws IOException {
		InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(filename);
		
		if(inputStream == null)
			throw new FileNotFoundException(String.format("Cannot find \"%s\" in classpath", filename));
			
		Properties properties = new Properties();
		properties.load(inputStream);
		
		return new Config(properties);
	}
}
