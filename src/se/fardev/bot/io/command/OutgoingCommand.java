package se.fardev.bot.io.command;

public class OutgoingCommand {
	private final byte[] data;
	private static final String DELIMITER = " ";
	private static final char[] CLRF = new char[] {
			(char)0x0D, (char)0x0A
	};
	
	private OutgoingCommand(byte[] data) {
		this.data = data;
	}
	
	public static OutgoingCommand build(String command, String value) {
		StringBuilder builder = new StringBuilder();
		builder.append(command).append(DELIMITER).append(value).append(CLRF);
		byte[] data = builder.toString().getBytes();
		return new OutgoingCommand(data);
	}
	
	public byte[] getData() {
		return data;
	}
}
