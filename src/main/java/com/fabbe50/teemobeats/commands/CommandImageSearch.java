package com.fabbe50.teemobeats.commands;

import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.entity.search.SearchRandom;
import at.mukprojects.giphy4j.exception.GiphyException;
import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static com.fabbe50.teemobeats.Main.giphy;

/**
 * Created by fabbe on 24/04/2018 - 9:28 PM.
 */
public class CommandImageSearch implements Command {
    @Override
    public String getName() {
        return "gif";
    }

    @Override
    public String shortDesc() {
        return "Searches for gifs on Giphy.";
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
        desc.add("Find any gif on Giphy.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("[query] - Find a picture based on the query.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        if (command.length == 2) {
            try {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.MAGENTA);
                builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
                SearchRandom giphyData = giphy.searchRandom(command[1]);
                builder.setImage(giphyData.getData().getImageOriginalUrl());
                builder.setTitle("Giphy - " + command[1]);
                builder.setFooter("Powered by Giphy", "https://media2.giphy.com/media/54Ya3l8S8y1ggAlzTA/giphy.gif");
                channel.sendMessage(builder.build()).queue();
            } catch (GiphyException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
