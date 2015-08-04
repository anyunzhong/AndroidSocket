package net.datafans.netty.common.session;

import android.util.Log;

import net.datafans.netty.common.constant.Tag;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

public class Session {

	private ChannelHandlerContext ctx;

	private Map<String, Object> map = new HashMap<String, Object>();

	public Session(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public synchronized void set(String key, Object value) {
		map.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T get(String key) {
		return (T) map.get(key);
	}

	public void setLastActiveTimeToNow() {
		set(Key.LAST_ACTIVE_TIME, System.currentTimeMillis());
	}

	public long getLastActiveTime() {
		return get(Key.LAST_ACTIVE_TIME);
	}

	public synchronized void write(Object pkg) {
		ctx.writeAndFlush(pkg);
	}

	public synchronized void writeAndClose(Object pkg) {
		ChannelFuture future = ctx.writeAndFlush(pkg);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	public synchronized void close() {
		ChannelFuture future = ctx.close();
		future.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					Log.i(Tag.NETTY_CLIENT, "CHANNEL_CLOSED ");
				}
			}
		});
	}

	private static class Key {
		private final static String LAST_ACTIVE_TIME = "last_active_time";
	}
}
