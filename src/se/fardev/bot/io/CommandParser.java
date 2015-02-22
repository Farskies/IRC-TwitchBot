package se.fardev.bot.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandParser {
	
	private final Logger logger = LogManager.getLogger();
	
	private final int GROUP_PREFIX      = 1;
	private final int GROUP_TYPE        = 2;
	private final int GROUP_DESTINATION = 3;
	private final int GROUP_MESSAGE     = 4;
	
	private final String regex = "^(?:[:](\\S+) )?(\\S+)(?: (?!:)(.+?))?(?: [:]{0,}([\\+|\\-]{0,}.+))?$";
	private final Pattern pattern;
	
	private String prefix;
	private String type;
	private String destination;
	private String message;
	
	public CommandParser() {
		pattern = Pattern.compile(regex);
	}
	
	public IncomingCommand parse(String data) {
		Matcher matcher = pattern.matcher(data);
		
		if(!matcher.find()) {
			logger.info("Could not find matches for {}", data);
			return null;
		}
		
		prefix      = matcher.group(GROUP_PREFIX);
		type        = matcher.group(GROUP_TYPE);
		destination = matcher.group(GROUP_DESTINATION);
		message     = matcher.group(GROUP_MESSAGE);
		return new IncomingCommand(prefix, type, destination, message);
	}

	public String getPrefix() {
		return prefix;
	}
	
	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String getDestination() {
		return destination;
	}
}
