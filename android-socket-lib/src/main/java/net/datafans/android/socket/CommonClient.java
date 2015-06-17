package net.datafans.android.socket;


import net.datafans.netty.common.client.NettyClient;
import net.datafans.netty.common.handler.ChannelHandlerFactory;

import java.util.List;

import io.netty.channel.ChannelHandler;

public abstract class CommonClient extends NettyClient {

    @Override
    public void setHandlerList(List<ChannelHandlerFactory> handlerList) {

        handlerList.add(new ChannelHandlerFactory() {
            @Override
            public ChannelHandler build() {
                return getDecoder();
            }
        });
        handlerList.add(new ChannelHandlerFactory() {

            @Override
            public ChannelHandler build() {
                return getEncoder();
            }
        });
        handlerList.add(new ChannelHandlerFactory() {

            @Override
            public ChannelHandler build() {
                return new DataPackageHandler();
            }
        });
    }



    protected abstract ChannelHandler getEncoder();
    protected abstract ChannelHandler getDecoder();
}
