package com.qc.printers;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        LocalTime localTime = LocalDateTime.now().toLocalTime();
        System.out.println(localTime);
    }
}