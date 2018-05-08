package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.Random;

import static com.fabbe50.teemobeats.handlers.DataHandler.WILLIAM;

/**
 * Created by fabbe on 24/04/2018 - 7:25 PM.
 */
public class CommandWilliam implements Command {
    @Override
    public String getName() {
        return "william";
    }

    @Override
    public String shortDesc() {
        return "Blame William.";
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
        desc.add("Blame William for something he either did or didn't do. Does it matter?");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        Random random = new Random();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setTitle("William");
        List<Member> members = channel.getGuild().getMembersByName("wexiity", true);
        if (!members.isEmpty()) {
            for (Member member1 : members) {
                if (member1.getUser().getDiscriminator().contains("6969")) {
                    builder.setDescription(member1.getAsMention() + "! What did you do...?");
                }
            }
        }
        builder.setImage(WILLIAM.get(random.nextInt(WILLIAM.size())));
        channel.sendMessage(builder.build()).queue();
        return true;
    }
}
