package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fabbe on 21/04/2018 - 1:26 AM.
 */
public class CommandHandler {
    private static Map<String, Class> commands = new HashMap<>();

    public <T extends Command> void registerCommand(String name, Class<T> command) {
        commands.put(name, command);
        createHelpEntry(name, command);
    }

    static Map<String, List<String>> help = new HashMap<>();
    private <T extends Command> void createHelpEntry(String name, Class<T> command) {
        try {
            List<String> s = new ArrayList<>();
            Object clazz = command.newInstance();
            s.add(name);
            StringBuilder description = new StringBuilder();
            List<String> descriptionList = ((Command) clazz).addDescription(new ArrayList<>());
            if (descriptionList != null) {
                for (String string : descriptionList) {
                    description.append(string).append("\n");
                }
            }
            s.add(description.toString());
            StringBuilder usage = new StringBuilder();
            List<String> usageList = ((Command) clazz).addUsage(new ArrayList<>());
            if (usageList != null) {
                for (String string : usageList) {
                    usage.append(Main.getDefaultCommandSymbol()).append(name).append(" ").append(string).append("\n");
                }
            }
            s.add(usage.toString());
            s.add(((Command) clazz).shortDesc());
            int permission = ((Command) clazz).permission();
            String permissionLevel;
            if (permission == 4)
                permissionLevel = "admin";
            else if (permission == 3)
                permissionLevel = "manager";
            else if (permission == 2)
                permissionLevel = "moderator";
            else if (permission == 1)
                permissionLevel = "partly";
            else
                permissionLevel = "everyone";
            s.add(permissionLevel);
            s.add(((Command) clazz).group());
            help.put(name, s);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static EmbedBuilder getHelp(Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(message.getAuthor().getName(), message.getAuthor().getAvatarUrl(), message.getAuthor().getAvatarUrl());
        builder.setColor(Color.CYAN);
        String[] command = message.getContentRaw().split(" ", 2);
        if (command.length == 2) {
            String name = command[1];
            List<String> helpList = help.get(name);
            builder.setTitle("Help - " + name);
            builder.addField("Description", helpList.get(1), false);
            builder.addField("Usage", helpList.get(2), false);
        } else {
            builder.setTitle("Help");
            StringBuilder commandNames = new StringBuilder();
            StringBuilder descriptions = new StringBuilder();
            StringBuilder permissions = new StringBuilder();

            for (List<String> helpInfo : help.values()) {
                commandNames.append(helpInfo.get(0)).append("\n");
                descriptions.append(helpInfo.get(3)).append("\n");
                permissions.append(helpInfo.get(4)).append("\n");
            }
            builder.addField("Command", commandNames.toString(), true);
            builder.addField("Description", descriptions.toString(), true);
            builder.addField("Permission", permissions.toString(), true);
            builder.addBlankField(false);
            builder.addField("Description", "Need help? I'm your guy!\nWe are currently switching to a new backbone,\nif you want an old list of commands type `" + Main.getDefaultCommandSymbol() + "help-o`", false);
            builder.addField("Usage", Main.getDefaultCommandSymbol() + "help - Shows you this message.\n" + Main.getDefaultCommandSymbol() + "help [command] - Shows help for that command.", false);
            builder.setFooter("Help", "http://tsdeluxe.com/hostedmedia/questionmarkgray.png");
        }
        return builder;
    }

    public static boolean runCommand(String commandCall, TextChannel channel, Guild guild, Member member, Message message) {
        if (commands.containsKey(commandCall)) {
            try {
                Class<? extends Command> commandCalled = commands.get(commandCall);
                if (hasPermission(member, commandCalled.newInstance().permission())) {
                    return (commandCalled.newInstance()).execute(channel, guild, member, message);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean hasPermission(Member member, int perms) {
        if (perms == 4) {
            return member.hasPermission(Permission.ADMINISTRATOR);
        } else if (perms == 3) {
            return member.hasPermission(Permission.MANAGE_SERVER);
        } else if (perms == 2) {
            return member.hasPermission(Permission.KICK_MEMBERS) && member.hasPermission(Permission.BAN_MEMBERS);
        } else if (perms == 1) {
            return true;
        } else return perms == 0;
    }
}
