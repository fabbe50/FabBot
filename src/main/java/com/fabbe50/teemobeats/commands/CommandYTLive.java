package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.handlers.DataHandler;
import com.fabbe50.teemobeats.handlers.YTAnnounceHandler;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 27/04/2018 - 8:26 PM.
 */
public class CommandYTLive implements Command {
    @Override
    public String getName() {
        return "yt-live";
    }

    @Override
    public String shortDesc() {
        return "YouTube Live Announcement";
    }

    @Override
    public String group() {
        return "tools";
    }

    @Override
    public int permission() {
        return 3;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("YouTube live announcements? Because YouTube can't do it right.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("add [channel ID] - Adds channel to announcer.");
        usage.add("remove [channel ID] - Remove channel from announcer.");
        usage.add("channel [textChannelName] - Set a text channel for the announcer.");
        usage.add("desc [text] - Add a message to your announcements.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 3);
        try {
            if (command.length == 3) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
                builder.setTitle("YTLive");
                if (command[1].equalsIgnoreCase("add")) {
                    builder.setDescription("Added " + command[2] + " to monitored YouTube channels.");
                    DataHandler.writeTextToFile(guild, "ytchannels.cfg", command[2], true, false);
                    DataHandler.updateYouTubeAnnouncements();
                    channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                } else if (command[1].equalsIgnoreCase("remove")) {
                    builder.setDescription("Removed " + command[2] + " from monitored YouTube channels.");
                    DataHandler.writeTextToFile(guild, "ytchannels.cfg", command[2], false, false);
                    DataHandler.updateYouTubeAnnouncements();
                    channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                } else if (command[1].equalsIgnoreCase("channel")) {
                    builder.setDescription("Set " + command[2] + " to be the announcement channel.");
                    DataHandler.addChannelToTextFile(guild, "announcementchannel.cfg", command[2]);
                    DataHandler.updateytchannels();
                    channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                } else if (command[1].equalsIgnoreCase("desc")) {
                    builder.setDescription("Set announcement message to: \n").appendDescription(command[2]);
                    DataHandler.overrideTextFile(guild, "announcemessage.cfg", command[2]);
                    DataHandler.updateYTMessage();
                    channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(60, TimeUnit.SECONDS));
                } else if (command[1].equalsIgnoreCase("clear")) {
                    builder.setDescription("Cleared active channels list, will announce all channels soon™");
                    YTAnnounceHandler.clearActiveChannels();
                    channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }


        return true;
    }
}
