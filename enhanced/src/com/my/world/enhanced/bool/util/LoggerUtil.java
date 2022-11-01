package com.my.world.enhanced.bool.util;

public class LoggerUtil {

    public static String logTag = "Bool";
    public static int logLevel = 0;

    public static void log(int level, String text) {
        if (level >= logLevel) {
            System.out.println("[" + logTag + "] " + text);
        }
    }
}
