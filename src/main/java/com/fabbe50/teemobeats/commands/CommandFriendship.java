package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static com.fabbe50.teemobeats.handlers.DataHandler.FRIENDSHIP;

/**
 * Created by fabbe on 24/04/2018 - 7:22 PM.
 */
public class CommandFriendship implements Command {
    @Override
    public String getName() {
        return "friendship";
    }

    @Override
    public String shortDesc() {
        return "Give your friends some love.";
    }

    @Override
    public String group() {
        return "fun";
    }

    @Override
    public int permission() {
        return 0;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Give your friends some lovely friendship.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[usertag] - Gives friendship.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        Random random = new Random();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setColor(Color.MAGENTA);
        builder.setTitle("Friendship");
        if (!message.getMentionedMembers().isEmpty() && message.getMentionedMembers().size() == 1) {
            builder.setDescription(member.getAsMention() + " expresses their friendship for " + message.getMentionedMembers().get(0).getAsMention());
            builder.setImage(FRIENDSHIP.get(random.nextInt(FRIENDSHIP.size())));
        } else {
            builder.setDescription("You need to mention __one__ user.");
        }
        channel.sendMessage(builder.build()).queue();
        return true;
    }
}
