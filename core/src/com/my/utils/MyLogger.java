package com.my.utils;

public class MyLogger {

    public static int debugLevel = 1;

    public static void log(int level, String text) {
        if(level >= debugLevel) {
            System.out.println(text);
        }
    }
}
