package com.fabbe50.teemobeats.handlers;

import java.util.Scanner;

/**
 * Created by fabbe on 27/04/2018 - 9:41 PM.
 */
public class ConsoleHandler {
    public ConsoleHandler() {
        Runnable runnable = () -> {
            while (true) {
                Scanner input = new Scanner(System.in);
                if (input.next().equalsIgnoreCase("stop")) {
                    System.exit(0);
                }
            }
        };
        new Thread(runnable).start();
    }
}
