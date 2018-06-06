package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.handlers.DataHandler;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 05/06/2018 - 2:37 AM.
 */
public class CommandPrefix implements Command {
    @Override
    public String getName() {
        return "prefix";
    }

    @Override
    public String shortDesc() {
        return "Change the prefix.";
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
        desc.add("Change the prefix to your hearts delight!");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[prefix] - Changes prefix to specified prefix.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ");
        if (command.length == 2) {
            try {
                DataHandler.overrideTextFile(guild, "prefix.cfg", command[1]);
                Main.setCommandSymbol(guild, command[1]);
                channel.sendMessage("Prefix set to `" + command[1] + "`.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            channel.sendMessage("You must specify your prefix in one sequence.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
        }
        return true;
    }
}
