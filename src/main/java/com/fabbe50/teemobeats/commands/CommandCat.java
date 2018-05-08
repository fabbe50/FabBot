package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by fabbe on 24/04/2018 - 6:54 PM.
 */
public class CommandCat implements Command {
    @Override
    public String getName() {
        return "cat";
    }

    @Override
    public String shortDesc() {
        return "Cats!";
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
        desc.add("Cats! Cats! Meow! Meow!");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("");
        usage.add("-gif");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(java.awt.Color.MAGENTA);
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setTitle(member.getEffectiveName() + " called upon the catgods for adorableness!");
        builder.setFooter("Powered by TheCatAPI", "http://thecatapi.com/ico/favicon.ico");
        try {
            StringBuilder builder1 = new StringBuilder();
            builder1.append("http://thecatapi.com/api/images/get?api_key=Mjk5MDk0&format=src");
            if (command.length > 1) {
                if (command[1].contains("-gif")) {
                    builder1.append("&type=gif");
                }
            }

            URL url = new URL(builder1.toString());
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
            connection.setReadTimeout(5000);
            boolean redirect = false;
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    redirect = true;
                }
            }
            if (redirect) {
                String newURL = connection.getHeaderField("Location");
                connection = (HttpURLConnection)new URL(newURL).openConnection();
            }
            builder.setImage(connection.getURL().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        channel.sendMessage(builder.build()).queue();
        return true;
    }
}
