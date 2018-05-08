package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

import static com.fabbe50.teemobeats.Main.music;

/**
 * Created by fabbe on 25/04/2018 - 8:11 PM.
 */
public class CommandLyrics implements Command {
    @Override
    public String getName() {
        return "lyrics";
    }

    @Override
    public String shortDesc() {
        return "Have your own Sing-a-long!";
    }

    @Override
    public String group() {
        return "music";
    }

    @Override
    public int permission() {
        return 0;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Now you can sing along with the tracks you're playing.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Attempts getting lyrics for the current song.");
        usage.add("[artist - song] - Attempts getting the lyrics for specified song.");
        usage.add("[lyrics URL] - Gets lyrics from URL.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        if (command.length == 2) {
            if (command[1].contains("http://"))
                music.lyrics(channel, command[1]);
            else
                music.lyrics(channel, command[1], music.getGuildAudioPlayer(guild).player.getPlayingTrack());
            return true;
        } else {
            music.lyrics(channel);
            return true;
        }
    }
}
