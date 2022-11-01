package com.my.world.enhanced.bool.util;

public class MyLogger {

    public static int debugLevel = 0;

    public static void log(int level, String text) {
        if (level >= debugLevel) {
            System.out.println(text);
        }
    }
}
