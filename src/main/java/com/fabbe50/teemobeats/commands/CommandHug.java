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
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.Main.giphy;

/**
 * Created by fabbe on 24/04/2018 - 7:12 PM.
 */
public class CommandHug implements Command {
    @Override
    public String getName() {
        return "hug";
    }

    @Override
    public String shortDesc() {
        return "Hugs to the people.";
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
        desc.add("Give hugs. Have hugs. Take hugs. All THE HUGS!");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[usertag] - Hug someone!");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        try {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
            SearchRandom giphyData = giphy.searchRandom("hug hug hug hug hug");
            builder.setImage(giphyData.getData().getImageOriginalUrl());
            builder.setFooter("Powered by Giphy", "https://media2.giphy.com/media/54Ya3l8S8y1ggAlzTA/giphy.gif");
            if (!message.getMentionedMembers().isEmpty()) {
                Member member1 = message.getMentionedMembers().get(0);
                builder.setColor(Color.MAGENTA);
                builder.setDescription(member.getAsMention() + " gave " + member1.getAsMention() + " a hug! :hugging:");
                channel.sendMessage(builder.build()).queue();
            } else if (message.mentionsEveryone()) {
                builder.setColor(Color.MAGENTA);
                builder.setDescription(member.getAsMention() + " gave @everyone a hug! :hugging:");
                channel.sendMessage(builder.build()).queue();
            } else {
                channel.sendMessage("You can't hug no one... :confused:").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
            }
        } catch (GiphyException e) {
            e.printStackTrace();
        }

        return true;
    }
}
