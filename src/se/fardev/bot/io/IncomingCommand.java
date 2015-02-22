package se.fardev.bot.io;

public class IncomingCommand {
	private final String[] data;
	
	public static final int SEQUENCE_PREFIX = 0;
	public static final int SEQUENCE_TYPE = 1;
	public static final int SEQUENCE_DESTINATION = 2;
	public static final int SEQUENCE_MESSAGE = 3;
	
	
	public IncomingCommand(String prefix, String type, String destination, String message) {
		data = new String[4];
		setSequence(IncomingCommand.SEQUENCE_PREFIX, prefix);
		setSequence(IncomingCommand.SEQUENCE_TYPE, type);
		setSequence(IncomingCommand.SEQUENCE_DESTINATION, destination);
		setSequence(IncomingCommand.SEQUENCE_MESSAGE, message);
	}
	
	public String getSequence(int sequence) {
		if(sequence >= data.length || sequence < 0) {
			//TODO LOG ERROR
		}
		
		return data[sequence];
	}
	
	private void setSequence(int sequence, String seqData) {
		if(sequence >= data.length || sequence < 0) {
			//TODO LOG ERROR
		}
		
		data[sequence] = seqData == null ? "" : seqData;
	}
	
	public String getPrefix() {
		return getSequence(IncomingCommand.SEQUENCE_PREFIX);
	}
	
	public String getType() {
		return getSequence(IncomingCommand.SEQUENCE_TYPE);
	}
	
	public String getDestination() {
		return getSequence(IncomingCommand.SEQUENCE_DESTINATION);
	}
	
	public String getMessage() {
		return getSequence(IncomingCommand.SEQUENCE_MESSAGE);
	}
	
	@Override
	public String toString() {
		String s = String.format("%s (%s) -> %s: %s", data); 
		return s;
	}
}
