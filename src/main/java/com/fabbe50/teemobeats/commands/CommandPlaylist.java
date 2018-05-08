package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.Main.*;

/**
 * Created by fabbe on 25/04/2018 - 7:38 PM.
 */
public class CommandPlaylist implements Command {
    @Override
    public String getName() {
        return "playlist";
    }

    @Override
    public String shortDesc() {
        return "Playlist management.";
    }

    @Override
    public String group() {
        return "music";
    }

    @Override
    public int permission() {
        return 1;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Nggh, save those sexy beats.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("save [name] - Save a playlist.");
        usage.add("load [name] - Load up a playlist.");
        usage.add("list - Lists all available playlists.");
        usage.add("get [name] - Sends the playlist as a file in chat.");
        usage.add("remove [name] - Deletes playlist.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 3);
        switch (command[1]) {
            case "save":
                if (command.length == 3) {
                    if (!session.isEmpty()) {
                        music.removePlaylist(channel, command[2]);
                        music.createPlaylist(channel, command[2], session.get(guild));
                        session.clear();
                    } else {
                        channel.sendMessage("Tracklist empty, please build up a queue before saving.").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                    }
                } else {
                    channel.sendMessage("WrongFormat: please use: `" + getCommandSymbol(guild) + "playlist save [name]`").queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return true;
            case "load":
                if (command.length == 3) {
                    try {
                        String temp;
                        temp = System.getProperty("user.dir") + "\\data\\" + channel.getGuild().getName().toLowerCase() + "\\playlists\\" + command[2] + ".m3u";
                        music.readPlaylist(channel, member, temp, member.getVoiceState().getChannel().getName());
                        return true;
                    } catch (Exception e) {
                        try {
                            String temp;
                            temp = System.getProperty("user.dir") + "\\data\\" + channel.getGuild().getName().toLowerCase() + "\\playlists\\" + command[2] + ".m3u";
                            music.readPlaylist(channel, member, temp, "");
                            return true;
                        } catch (Exception e1) {
                            e.printStackTrace();
                            e1.printStackTrace();
                            return false;
                        }
                    }
                } else {
                    channel.sendMessage("WrongFormat: please use: `" + getCommandSymbol(guild) + "playlist load [name]`").queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return false;
            case "list":
                music.retrievePlaylistDir(channel);
                return true;
            case "get":
                if (command.length == 3) {
                    music.getPlaylist(channel, command[2]);
                    return true;
                } else {
                    channel.sendMessage("WrongFormat: please use: `" + getCommandSymbol(guild) + "playlist get [name]`").queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return false;
            case "remove":
                if (command.length == 3 && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                    music.removePlaylist(channel, command[2]);
                    return true;
                } else {
                    if (!member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                    } else
                        channel.sendMessage("WrongFormat: please use: `" + getCommandSymbol(guild) + "playlist remove [name]`").queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return false;
        }
        return false;
    }
}
