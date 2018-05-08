package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 30/09/2017 - 10:01 PM.
 */
public class HelpCommand {
    private static List<String> commands = new ArrayList<>();
    private static List<String> descriptions = new ArrayList<>();
    private static List<String> permissions = new ArrayList<>();

    public static void init() {
        commands.clear();
        descriptions.clear();
        permissions.clear();

        createCommandListEntry("help-o", "Shows this list, duh!", "No");
        createCommandListEntry("roles", "Role Assignment System", "Partly");
    }

    public static String getCommands() {
        String message = "";

        for (String s : commands) {
            message = message + Main.getDefaultCommandSymbol() + s + "\n";
        }

        return message;
    }

    public static String getDescriptions() {
        String message = "";

        for (String s : descriptions) {
            message = message + s + "\n";
        }

        return message;
    }

    public static String getPermissions() {
        String message = "";

        for (String s : permissions) {
            message = message + s + "\n";
        }

        return message;
    }

    private static void createCommandListEntry(String command, String description, String permission) {
        commands.add(command);
        descriptions.add(description);
        permissions.add(permission);
    }

    public static boolean helpCommand(TextChannel channel, Member member, String[] command, String cs) {
        HelpCommand.init();

        EmbedBuilder builder = new EmbedBuilder();

        if (command.length == 2) {
            switch (command[1]) {
                case "roles":
                    builder.setTitle("Help - roles");
                    builder.addField("Description: ", Utils.addLinebreaks("A little tool so people can assign their own roles on the server if needed.", 60), false);
                    builder.addField("Usages: ",
                            "**?roles [rolename]** - Let's you join a role on the list.\n" +
                            "**" + cs + "roles [rolename]** - Let's you join a role on the list.\n" +
                            "**" + cs + "roles add [rolename]** - Adds role to the list of assignable roles.\n" +
                            "**" + cs + "roles remove [rolename]** - Removes role from the list of assignable roles."
                            , false);
                    break;
            }
        } else {
            builder.setTitle("Help");
            builder.addField("Command Syntax", "**" + HelpCommand.getCommands() + "**", true);
            builder.addField("Command Description", HelpCommand.getDescriptions(), true);
            builder.addField("Admin?", HelpCommand.getPermissions(), true);

            builder.addField("Description: ", Utils.addLinebreaks("**" + cs + "help** is used to list the commands available and to get special help with certain commands.", 60), false);
            builder.addField("Usages: ",
                    "**" + cs + "help** - to list all available commands.\n" +
                    "**" + cs + "help [command]** - to get help with the specified command.",
                    false);
        }

        builder.setColor(Color.CYAN);
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());

        channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(5, TimeUnit.MINUTES));
        return true;
    }
}
