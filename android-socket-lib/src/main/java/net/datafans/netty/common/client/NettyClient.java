package net.datafans.netty.common.client;

import android.util.Log;

import net.datafans.netty.common.boot.UncaughtExceptionUtil;
import net.datafans.netty.common.client.config.Config;
import net.datafans.netty.common.config.GlobalConfig;
import net.datafans.netty.common.constant.ChannelState;
import net.datafans.netty.common.constant.Tag;
import net.datafans.netty.common.handler.ChannelHandlerFactory;
import net.datafans.netty.common.session.Session;
import net.datafans.netty.common.shutdown.Shutdown;
import net.datafans.netty.common.shutdown.ShutdownListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public abstract class NettyClient {

    private Channel channel;

    private ExecutorService pool;

    private Bootstrap bootstrap;
    private EventLoopGroup workGroup;

    private ChannelState state;

    private boolean isTerminate = false;

    private int restartTryTimes = 0;
    private int heartbeatFailTimes = 0;

    private List<ChannelHandlerFactory> handlerList = new ArrayList<ChannelHandlerFactory>();

    protected NettyClient() {

        setState(ChannelState.CLOSED);
    }

    protected abstract void setHandlerList(final List<ChannelHandlerFactory> handlerList);

    protected abstract int getPort();

    private void init() throws Exception {

        setTerminate(false);

        UncaughtExceptionUtil.declare();

        Shutdown.sharedInstance().addListener(shutdownListener);

        setHandlerList(handlerList);

        workGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(workGroup);
        bootstrap.channel(NioSocketChannel.class);

        if (handlerList.isEmpty()) {
            throw new Exception("HANDLER_LIST_EMPTY");
        }

        setBootstrapHandler(bootstrap, handlerList);
        setBootstrapOption(bootstrap);


        pool = Executors.newCachedThreadPool();

        autoRestartWhenClosed();
        autoSendHeartbeat();
        autoDetectHeartbeart();
    }

    public void connect() {

        Log.i(Tag.NETTY_CLIENT,state.toString());

        if (state != ChannelState.CLOSED) {
            return;
        }


        try {

            init();

            if (getPort() <= 0) {
                throw new Exception("LISTEN_PORT_ILLEGAL");
            }
            ChannelFuture future = bootstrap.connect(getHost(), getPort()).sync();
            setState(ChannelState.CONNECTING);
            future.addListener(channelStartListener);

            future.channel().closeFuture().sync();

        } catch (Exception e) {
            setState(ChannelState.CLOSED);
            Log.e(Tag.NETTY_CLIENT, e.toString());
        }


    }

    public void disconnect() {

        Log.i(Tag.NETTY_CLIENT,state.toString());
        if (state == ChannelState.CLOSED) {
            return;
        }

        setState(ChannelState.CLOSED);


        if (channel != null) {
            ChannelFuture future = channel.close();
            setState(ChannelState.CLOSING);
            future.addListener(channelStopListener);
        }

        setTerminate(true);
        if (workGroup != null)
            workGroup.shutdownGracefully();
        Log.i(Tag.NETTY_CLIENT, "WORKGROUP_SHUTDOWN_GRACEFULLY");
        if (pool != null)
            pool.shutdownNow();
        Log.i(Tag.NETTY_CLIENT, "POOL_SHUTDOWN_GRACEFULLY");

    }


    private void autoRestartWhenClosed() {
        pool.execute(autoRestartTask);
    }

    private void autoSendHeartbeat() {
        pool.execute(autoSendHeartbeatTask);
    }

    private void autoDetectHeartbeart() {
        pool.execute(autoDetectHeartbeatTask);
    }

    private void setBootstrapHandler(final Bootstrap bootstrap,
                                     final List<ChannelHandlerFactory> handlers) {

        bootstrap.handler(new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {

                if (enableFrameDecoder()) {
                    GlobalConfig.FrameDecoder config = new GlobalConfig.FrameDecoder();
                    setFrameDecoderConfig(config);
                    ch.pipeline().addLast(
                            new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,
                                    config.getOffset(), config.getLength(),
                                    config.getAdjustment(), 0));
                }

                for (ChannelHandlerFactory factory : handlers) {
                    ch.pipeline().addLast(factory.build());
                }
            }
        });
    }

    private void setBootstrapOption(final Bootstrap bootstrap) {

        bootstrap.option(ChannelOption.ALLOCATOR, optionByteBufAllocator());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, optionSocketKeepAlive());
    }

    protected ByteBufAllocator optionByteBufAllocator() {
        return PooledByteBufAllocator.DEFAULT;
    }

    protected boolean optionSocketKeepAlive() {
        return true;
    }

    protected boolean enableFrameDecoder() {
        return false;
    }

    protected void setFrameDecoderConfig(final GlobalConfig.FrameDecoder config) {

    }

    protected int autoReconnectTimesThreshold() {
        return Config.Client.AUTO_RECONNECT_TIMES_THRESSHOLD;
    }

    protected int defaultHeartbeatInterval() {
        return Config.Client.DEFAULT_HEARTBEAT_INTERVAL_MINISECONDS;
    }

    protected long defaultHeartbeatTimeout() {
        return Config.Client.DEFAULT_HEARTBEAT_TIMEOUT_MINISECONDS;
    }

    protected int defaultHeartbeatSendFailThreshold() {
        return Config.Client.DEFAULT_HEARTBEAT_SEND_FAIL_THRESSHOLD;
    }

    protected abstract Object getHeartbeatDataPacket();

    protected abstract String getHost();

    protected abstract void onChannelStateChanged(ChannelState state);

    public boolean isTerminate() {
        return isTerminate;
    }

    private void setTerminate(boolean isTerminate) {
        this.isTerminate = isTerminate;
    }


    public void setState(ChannelState state) {
        this.state = state;
        onChannelStateChanged(state);
    }


    private ChannelFutureListener channelStartListener = new ChannelFutureListener() {

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {

            if (future.isSuccess()) {
                Log.i(Tag.NETTY_CLIENT, "CHANNEL_OPENED " + channel);
                restartTryTimes = 0;
                channel = future.channel();

                setState(ChannelState.RUNNING);
            } else {
                Log.e(Tag.NETTY_CLIENT, "CHANNEL_CONNECTION_ERROR " + future.cause());
                setState(ChannelState.CLOSED);
                channel = null;
            }

        }
    };

    private ChannelFutureListener channelStopListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            channel = null;
            setState(ChannelState.CLOSED);
            Log.i(Tag.NETTY_CLIENT, "CHANNEL_CLOSED");
        }
    };

    private ChannelFutureListener heartbeatSendListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
                Log.i(Tag.NETTY_CLIENT, "AUTO_SEND_HEARTBEAT_DONE");
                heartbeatFailTimes = 0;
            } else {
                Log.e(Tag.NETTY_CLIENT, "AUTO_SEND_HEARTBEAT_ERROR");
                heartbeatFailTimes++;
                // 心跳失败
                if (heartbeatFailTimes >= defaultHeartbeatSendFailThreshold()) {
                    disconnect();
                }
            }

        }
    };

    private Runnable autoRestartTask = new Runnable() {
        public void run() {

            while (true) {

                if (isTerminate()) {
                    Log.i(Tag.NETTY_CLIENT, "AUTO_RESTART_TERMINATE");
                    break;
                }

                if (restartTryTimes > autoReconnectTimesThreshold()) {
                    Log.i(Tag.NETTY_CLIENT, "AUTO_RESTART_TERMINATE");
                    NettyClient.this.disconnect();
                    break;
                }
                try {
                    Thread.sleep(1000);
                    if (state == ChannelState.CLOSED) {
                        restartTryTimes++;
                        Log.i(Tag.NETTY_CLIENT, "AUTO_RESTART_TIME " + restartTryTimes);
                        NettyClient.this.connect();
                    }
                } catch (Exception e) {
                    Log.e(Tag.NETTY_CLIENT, "AUTO_RESTART_THREAD_EXCEPTION " + e);
                }
            }

        }
    };

    private Runnable autoSendHeartbeatTask = new Runnable() {
        public void run() {
            while (true) {
                if (isTerminate()) {
                    Log.i(Tag.NETTY_CLIENT, "AUTO_SEND_HEARTBEAT_TERMINATE");
                    break;
                }
                try {

                    if (channel != null) {
                        ChannelFuture future = channel
                                .writeAndFlush(getHeartbeatDataPacket());
                        future.addListener(heartbeatSendListener);
                    }

                    Thread.sleep(defaultHeartbeatInterval());

                } catch (Exception e) {
                    Log.e(Tag.NETTY_CLIENT, "AUTO_SEND_HEARTBEAT_THREAD_EXCEPTION " + e);
                }
            }

        }
    };

    private Runnable autoDetectHeartbeatTask = new Runnable() {
        public void run() {
            while (true) {
                try {
                    if (isTerminate()) {
                        Log.i(Tag.NETTY_CLIENT, "AUTO_DETECT_HEARTBEAT_TERMINATE");
                        break;
                    }
                    Thread.sleep(1000);

                    Session session = getSession();
                    if (session != null) {
                        long interval = System.currentTimeMillis()
                                - session.getLastActiveTime();

                        // 心跳超时
                        if (interval > defaultHeartbeatTimeout()) {
                            Log.i(Tag.NETTY_CLIENT, "AUTO_DETECT_HEARTBEAT_TIMEOUT");
                            disconnect();
                        }
                    }

                } catch (Exception e) {
                    Log.e(Tag.NETTY_CLIENT, "AUTO_DETECT_HEARTBEAT_THREAD_EXCEPTION " + e);
                }
            }

        }
    };

    private ShutdownListener shutdownListener = new ShutdownListener() {

        @Override
        public void shutdown() {
            disconnect();
        }
    };


    private Session getSession() {

        if (channel != null) {
            Object o = channel.attr(Config.Key.sk).get();
            if (o != null && o instanceof Session) {
                Session session = (Session) o;
                return session;
            }
        }

        return null;
    }

    public void write(Object pkg) {
        Session session = getSession();
        if (session == null) {
            return;
        }

        session.write(pkg);
    }

}
