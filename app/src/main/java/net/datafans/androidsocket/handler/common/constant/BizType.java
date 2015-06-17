package net.datafans.androidsocket.handler.common.constant;

public class BizType {
	public final static byte[] HEARTBEAT = { 0, 1 };
	public final static byte[] LOGIN = { 0, 2 };
	
	
	public final static byte[] MSG_PSH = { 0, 10 };
	public final static byte[] MSG_SYNC = { 0, 11 };
	public final static byte[] MSG_FIN = { 0, 12 };
	public final static byte[] MSG_FIN_ACK = { 0, 13 };
	
	
	
	public final static byte[] PRIVATE_CHAT = { 1, 0 };
	public final static byte[] PRIVATE_CHAT_RECEIPT = { 1, 1 };
//	public final static byte[] GROUP_CHAT = { 2, 0 };
//	public final static byte[] CHAT_ROOM = { 3, 0 };

	public final static byte[] SYSTEM_NOTIFY = { 100, 86 };
	
	
	
}
