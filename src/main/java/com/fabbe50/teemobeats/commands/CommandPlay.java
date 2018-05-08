package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

import static com.fabbe50.teemobeats.Main.music;
import static com.fabbe50.teemobeats.Main.musicChannel;

/**
 * Created by fabbe on 24/04/2018 - 7:38 PM.
 */
public class CommandPlay implements Command {
    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String shortDesc() {
        return "Queue and play songs";
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
        desc.add("Let's listen to some music, eh?");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[Song Name] - Searches on YouTube after the song, queues it and start playback.");
        usage.add("[TrackURL] - Paste a url to a specific song on YouTube, Vimeo, SoundCloud, Twitch, Bandcamp or a hosted track on a server.");
        usage.add("[PlaylistURL] - Paste a url to a playlist on YouTube to add the whole playlist.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().replace(Main.getCommandSymbol(guild), "").toLowerCase().split(" ", 2);
        if (command.length == 2) {
            if (command[1].contains("http")) {
                try {
                    music.loadAndPlay(channel, member, command[1], member.getVoiceState().getChannel().getName());
                    return true;
                } catch (Exception f) {
                    music.loadAndPlay(channel, member, command[1], musicChannel);
                    return true;
                }
            } else {
                try {
                    music.search(channel, member, command[1], member.getVoiceState().getChannel().getName());
                    return true;
                } catch (Exception f) {
                    music.search(channel, member, command[1], musicChannel);
                    return true;
                }
            }
        } else {
            return true;
        }
    }
}
