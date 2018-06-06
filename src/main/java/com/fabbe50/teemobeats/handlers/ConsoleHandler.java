package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

/**
 * Created by fabbe on 27/04/2018 - 9:41 PM.
 */
public class ConsoleHandler {
    public static void readConsoleCommand(String input) {
        String[] command = input.split(" ", 2);
        if (command.length > 0) {
            if (command[0].equalsIgnoreCase("stop")) {
                System.exit(0);
            } else if (command[0].equalsIgnoreCase("msg")) {
                if (command.length == 2) {
                    for (Guild guild : Main.getAllGuilds()) {
                        boolean foundBotChannel = false;
                        for (TextChannel channel : guild.getTextChannels()) {
                            if (channel.getName().contains("bot")) {
                                channel.sendMessage(command[1]).queue();
                                foundBotChannel = true;
                                break;
                            }
                        }
                        if (!foundBotChannel) {
                            for (TextChannel channel : guild.getTextChannels()) {
                                if (channel.getName().contains("general")) {
                                    channel.sendMessage(command[1]).queue();
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    System.out.println("Error: You need to input a message.");
                }
            }
        }
    }
}
