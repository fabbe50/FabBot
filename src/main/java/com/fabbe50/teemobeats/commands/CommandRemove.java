package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

import static com.fabbe50.teemobeats.Main.music;

/**
 * Created by fabbe on 25/04/2018 - 8:40 PM.
 */
public class CommandRemove implements Command {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String shortDesc() {
        return "Remove song from queue.";
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
        desc.add("Remove a song from the queue.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[name] - Remove song from queue.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        if (member.getPermissions().contains(Permission.MANAGE_SERVER)) {
            try {
                music.remove(channel, Integer.parseInt(command[1]));
                return true;
            } catch (Exception e) {
                channel.sendMessage("Couldn't find track on that position.").queue();
                return false;
            }
        }
        else
            return false;
    }
}
