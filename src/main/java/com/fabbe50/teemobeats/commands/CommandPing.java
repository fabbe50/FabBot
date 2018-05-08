package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Utils;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.List;

/**
 * Created by fabbe on 30/04/2018 - 5:39 PM.
 */
public class CommandPing implements Command {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String shortDesc() {
        return "Pong!";
    }

    @Override
    public String group() {
        return "tools";
    }

    @Override
    public int permission() {
        return 0;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Check the ping the bot has to the server.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Checks the ping.");
        return null;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        EmbedBuilder builder = new EmbedBuilder();
        long ping = guild.getJDA().getPing();
        int ping8 = Utils.clampTo8Bit((int)ping / 2);
        builder.setColor(new Color(ping8, 255 - ping8, 0));
        builder.setTitle("Pong!");
        builder.setDescription("Bot to server response time: " + ping + "ms");
        channel.sendMessage(builder.build()).queue();
        return true;
    }
}
