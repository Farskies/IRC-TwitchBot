package se.fardev.bot.io.command;

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
	
	public CommandParser() {
		pattern = Pattern.compile(regex);
	}
	
	public IncomingCommand parse(String data) {
		Matcher matcher = pattern.matcher(data);
		
		if(!matcher.find()) {
			logger.info("Could not find matches for {}", data);
			return null;
		}
		
		String prefix      = matcher.group(GROUP_PREFIX);
		String type        = matcher.group(GROUP_TYPE);
		String destination = matcher.group(GROUP_DESTINATION);
		String message     = matcher.group(GROUP_MESSAGE);
		return new IncomingCommand(prefix, type, destination, message);
	}
}
