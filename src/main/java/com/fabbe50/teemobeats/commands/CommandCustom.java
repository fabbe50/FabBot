package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * Created by fabbe on 01/05/2018 - 3:12 PM.
 */
public class CommandCustom implements Command {
    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public String shortDesc() {
        return "Manage Custom Commands";
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
        desc.add("Add your own custom commands that allow");
        desc.add("you to print some text in chat with the");
        desc.add("snap of a finger, not really, but trust");
        desc.add("me it's easy to use.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("add [command] [text] - ");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        return false;
    }
}
