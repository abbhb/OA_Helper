package com.qc.printers.common.signin.utlis;

public class TimeUtil {
    public static Integer stringTimeToSecond(String time) {
        String[] split = time.split(":");
        String hour = split[0];
        String min = split[1];

        return Integer.valueOf(hour) * 3600 + Integer.valueOf(min) * 60;
    }
}
