package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.Main.music;

/**
 * Created by fabbe on 25/04/2018 - 7:32 PM.
 */
public class CommandQueue implements Command {
    @Override
    public String getName() {
        return "queue";
    }

    @Override
    public String shortDesc() {
        return "Displays the current queue.";
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
        desc.add("Up next: a bunch of awesome tracks you people have added. Let the beats pump.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- shows the full queue.");
        usage.add("[number] - shows the specified number of songs in the queue.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        if (command.length == 2) {
            try {
                music.getQueue(channel, Integer.parseInt(command[1]));
                return true;
            } catch (Exception e) {
                channel.sendMessage("Error: " + e.getLocalizedMessage()).queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
                return true;
            }
        } else {
            music.getQueue(channel);
            return true;
        }
    }
}
