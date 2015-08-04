package net.datafans.android.socket;

import android.util.Log;

import net.datafans.netty.common.client.config.Config;
import net.datafans.netty.common.constant.Tag;
import net.datafans.netty.common.session.Session;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

public class DataPackageHandler extends ChannelInboundHandlerAdapter {

    private Session session;

    private MsgListener listener;


    public  DataPackageHandler(MsgListener listener){
        this.listener = listener;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (session != null) {
            session.setLastActiveTimeToNow();
        }

        if (listener != null) listener.onMsgReceived(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Log.e(Tag.NETTY_CLIENT, "CHANNEL_EXCEPTION " + cause);
        ChannelFuture future = ctx.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Log.e(Tag.NETTY_CLIENT, "CHANNEL_CLOSED");
            }
        });

    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        Log.i(Tag.NETTY_CLIENT, "CHANNEL_ACTIVE " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.e(Tag.NETTY_CLIENT, "CHANNEL_INACTIVE " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Log.i(Tag.NETTY_CLIENT, "CHANNEL_READ_COMPLETED " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Log.i(Tag.NETTY_CLIENT, "CHANNEL_REGISTERED");
        session = new Session(ctx);
        session.setLastActiveTimeToNow();
        ctx.channel().attr(Config.Key.sk).set(session);
    }

    public Session getSession() {
        return session;
    }
}
