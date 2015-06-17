package net.datafans.netty.common.boot;

import android.util.Log;

import net.datafans.netty.common.constant.Tag;

import java.lang.Thread.UncaughtExceptionHandler;

public class UncaughtExceptionUtil {

    private UncaughtExceptionUtil() {

    }

    public static void declare() {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Log.e(Tag.NETTY_CLIENT, "THREAD_TERMINATE " + t + "   " + e);
                e.printStackTrace();
            }
        });
    }
}
