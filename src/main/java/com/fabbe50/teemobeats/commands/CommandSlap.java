package com.fabbe50.teemobeats.commands;

import at.mukprojects.giphy4j.entity.search.SearchRandom;
import at.mukprojects.giphy4j.exception.GiphyException;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.List;

import static com.fabbe50.teemobeats.Main.giphy;

/**
 * Created by fabbe on 24/04/2018 - 7:18 PM.
 */
public class CommandSlap implements Command {
    @Override
    public String getName() {
        return "slap";
    }

    @Override
    public String shortDesc() {
        return "Slap people";
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
        desc.add("Slap people, because they fucking deserve it.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[usertag] - Slap someone!");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
            builder.setColor(Color.MAGENTA);
            builder.setTitle("Slapping");
            if (!message.getMentionedMembers().isEmpty() && message.getMentionedMembers().size() == 1) {
                if (message.getMentionedMembers().get(0).equals(member)) {
                    builder.setDescription(member.getAsMention() + " slapped themselves. :joy:");
                    builder.setImage("https://media.giphy.com/media/irU9BlmqEwZwc/giphy.gif");
                } else {
                    builder.setDescription(member.getAsMention() + " gave " + message.getMentionedMembers().get(0).getAsMention() + " a deserved slap.");
                    SearchRandom giphyData = giphy.searchRandom("slap");
                    builder.setImage(giphyData.getData().getImageOriginalUrl());
                    builder.setFooter("Powered by Giphy", "https://media2.giphy.com/media/54Ya3l8S8y1ggAlzTA/giphy.gif");
                }
            } else {
                builder.setDescription("You need to mention __one__ user.");
            }
            channel.sendMessage(builder.build()).queue();
        } catch (GiphyException e) {
            e.printStackTrace();
        }
        return true;
    }
}
