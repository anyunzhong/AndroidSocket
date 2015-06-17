package net.datafans.netty.common.client.config;

import net.datafans.netty.common.session.Session;

import io.netty.util.AttributeKey;

public class Config {

	public static class Key{
		public static final AttributeKey<Session> sk = AttributeKey.valueOf("session");
	}
	public static class Client {
		public final static int AUTO_RECONNECT_TIMES_THRESSHOLD = 5;
		public final static int DEFAULT_HEARTBEAT_INTERVAL_MINISECONDS = 15000;
		public final static int DEFAULT_HEARTBEAT_TIMEOUT_MINISECONDS = 15000 * 5;
		public final static int DEFAULT_HEARTBEAT_SEND_FAIL_THRESSHOLD = 3;
	}
}
