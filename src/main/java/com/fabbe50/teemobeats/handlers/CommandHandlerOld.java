package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.*;
import com.fabbe50.teemobeats.commands.HelpCommand;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.GuildController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.handlers.DataHandler.*;

/**
 * Created by fabbe on 07/10/2017 - 10:43 PM.
 */
public class CommandHandlerOld {
    private static int rTimer = 2;

    public static boolean handleCommand(MessageReceivedEvent event) {
        TextChannel channel = event.getTextChannel();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Message message = event.getMessage();
        String[] commandBase = message.getContentRaw().toLowerCase().split(" ", 2);
        String cs = Main.getCommandSymbol(guild);
        commandBase[0] = commandBase[0].replace(cs, "");

        if (commandBase[0].substring(0, 1).equals("?")) {
            commandBase[0] = commandBase[0].replace("?", "");
            executeRoleAssignment(channel, guild, member, message, commandBase);
            return true;
        }

        switch (commandBase[0]) {
            case "help-o":
                message.delete().queueAfter(rTimer, TimeUnit.SECONDS);
                return HelpCommand.helpCommand(event.getTextChannel(), member, commandBase, cs);
            case "roles":
                message.delete().queueAfter(rTimer, TimeUnit.SECONDS);
                return executeAssignRole(channel, guild, member, message, commandBase);
        }
        return false;
    }

    private static boolean executeAssignRole(TextChannel channel, Guild guild, Member member, Message message, String[] command) {
        command = command[1].split(" ", 2);
        if (command.length > 0) {
            if (command.length > 1 && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (command[0].equals("add")) {
                    for (Role g : guild.getRoles()) {
                        if (g.getName().equalsIgnoreCase(command[1])) {
                            try {
                                writeTextToFile(guild, "roles.cfg", g.getName(), true, false);
                                channel.sendMessage("Saved role " + g.getName() + " to list.").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (command[0].equals("remove")) {
                    for (Role g : guild.getRoles()) {
                        if (g.getName().equalsIgnoreCase(command[1])) {
                            try {
                                writeTextToFile(guild, "roles.cfg", g.getName(), false, false);
                                channel.sendMessage("Removed role " + g.getName() + " from list.").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                message.delete().queueAfter(5, TimeUnit.SECONDS);
            } else {
                if (command[0].equals("list")) {
                    try {
                        List<String> lines = DataHandler.getFileContents(guild, "roles.cfg");
                        StringBuilder builder = new StringBuilder();
                        EmbedBuilder builder1 = new EmbedBuilder();
                        builder1.setTitle("Applicable roles");
                        for (String s : lines) {
                            builder.append(s).append("\n");
                        }
                        builder1.setDescription(builder);
                        channel.sendMessage(builder1.build()).queue();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    message.delete().queueAfter(5, TimeUnit.SECONDS);
                } else {
                    executeRoleAssignment(channel, guild, member, message, command);
                }
            }
        }
        return true;
    }

    private static void executeRoleAssignment(TextChannel channel, Guild guild, Member member, Message message, String[] command) {
        try {
            GuildController controller = new GuildController(guild);
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
            List<String> lines = DataHandler.getFileContents(guild, "roles.cfg");
            if (lines.contains(command[0])) {
                for (Role g : guild.getRoles()) {
                    if (g.getName().equalsIgnoreCase(command[0])) {
                        if (member.getRoles().contains(g)) {
                            controller.removeRolesFromMember(member, g).queue();
                            builder.setTitle("Removed role.");
                            builder.setDescription("**" + g.getName() + "** from " + member.getAsMention());
                            channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                            message.delete().queueAfter(rTimer, TimeUnit.SECONDS);
                        } else {
                            controller.addRolesToMember(member, g).queue();
                            builder.setTitle("Assigned role.");
                            builder.setDescription("**" + g.getName() + "** to " + member.getAsMention());
                            channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                            message.delete().queueAfter(rTimer, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
