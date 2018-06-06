package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 03/06/2018 - 11:30 PM.
 */
public class CommandVolume implements Command {
    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String shortDesc() {
        return "Turn it up to 11!";
    }

    @Override
    public String group() {
        return "music";
    }

    @Override
    public int permission() {
        return 3;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Change the volume of the music player in the bot.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[number] - Changes the volume to the set number.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] strings = message.getContentRaw().split(" ", 2);
        if (strings.length == 2) {
            if (Utils.isInteger(strings[1])) {
                Main.music.setVolume(channel, Integer.parseInt(strings[1]));
                channel.sendMessage("Set volume to " + strings[1] + ".").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
            } else {
                channel.sendMessage(strings[1] + " is not a number. Please input numbers only.").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage("This command needs an argument. Please input a number after the command.").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
        }

        return true;
    }
}
