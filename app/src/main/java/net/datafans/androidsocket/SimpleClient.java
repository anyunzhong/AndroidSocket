package net.datafans.androidsocket;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import net.datafans.android.socket.CommonClient;
import net.datafans.androidsocket.handler.common.constant.Protocal;
import net.datafans.androidsocket.handler.common.entity.DataPackage;
import net.datafans.androidsocket.handler.common.handler.DataPackageDecoder;
import net.datafans.androidsocket.handler.common.handler.DataPackageEncoder;
import net.datafans.netty.common.config.GlobalConfig;
import net.datafans.netty.common.constant.ChannelState;
import net.datafans.netty.common.constant.Tag;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandler;

/**
 * Created by zhonganyun on 15/6/16.
 */
public class SimpleClient extends CommonClient {

    @Override
    protected ChannelHandler getEncoder() {
        return new DataPackageEncoder();
    }

    @Override
    protected ChannelHandler getDecoder() {
        return new DataPackageDecoder();
    }

    @Override
    protected Object getHeartbeatDataPackage() {
        return DataPackage.HEARTBEAT_PACKAGE;
    }

    @Override
    protected String getHost() {
        return "112.124.28.196";
    }

    @Override
    public int getPort() {
        return 40010;
    }

    @Override
    protected boolean enableFrameDecoder() {
        return true;
    }

    @Override
    protected void setFrameDecoderConfig(GlobalConfig.FrameDecoder config) {
        config.setOffset(0);
        config.setLength(Protocal.FIELD_PACKAGE_SIZE_LENGTH);
        config.setAdjustment(-Protocal.FIELD_PACKAGE_SIZE_LENGTH);
    }

    @Override
    protected int autoReconnectTimesThreshold() {
        return 10;
    }


    @Override
    protected void onChannelStateChanged(ChannelState state) {
        Log.d(Tag.NETTY_CLIENT, state.toString());

        if (state == ChannelState.RUNNING){
            //登陆
            write(getLoginPackage());
        }
    }

    @Override
    public void onMsgReceived(Object msg) {
        Log.i(Tag.NETTY_CLIENT, msg.toString());
    }

    private static SimpleClient client;

    public synchronized static SimpleClient sharedInstance() {
        if (client == null)
            client = new SimpleClient();
        return client;
    }

    private DataPackage getLoginPackage(){

        DataPackage pkg = DataPackage.LOGIN_PACKAGE;
        Map<String, String> content = new HashMap<String,String>();
        content.put("token", "e9037ed404fd49bc2d1e1529bece0b35");
        content.put("syncKey","0");
        pkg.setContent(JSON.toJSONString(content).getBytes());
        return pkg;
    }
}
