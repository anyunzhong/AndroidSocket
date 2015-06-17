package net.datafans.netty.common.shutdown;

import android.util.Log;

import net.datafans.netty.common.constant.Tag;

import java.util.ArrayList;
import java.util.List;

public class Shutdown {

    private static Shutdown shutdown;
    private List<ShutdownListener> listeners = new ArrayList<ShutdownListener>();

    public synchronized static Shutdown sharedInstance() {
        if (shutdown == null) {
            shutdown = new Shutdown();
            shutdown.addShutdownHook();
        }
        return shutdown;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (ShutdownListener listener : listeners) {
                    listener.shutdown();
                }
                Log.i(Tag.NETTY_CLIENT, "APP_SHUTDOWN_SUCCESSFULLY!");
            }
        });
    }

    public void addListener(ShutdownListener listener) {
        listeners.add(listener);
    }
}
