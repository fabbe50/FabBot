package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * Created by fabbe on 25/04/2018 - 8:05 PM.
 */
public class CommandFeedback implements Command {
    @Override
    public String getName() {
        return "feedback";
    }

    @Override
    public String shortDesc() {
        return "Provide feedback.";
    }

    @Override
    public String group() {
        return "general";
    }

    @Override
    public int permission() {
        return 0;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Report bugs, complain, suggest a new feature.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Prints link to feedback form in chat.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        channel.sendMessage("https://goo.gl/forms/AD8Lk4rmSjHIxTrC2").queue();
        return true;
    }
}
