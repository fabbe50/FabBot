package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 02/05/2018 - 2:16 AM.
 */
public class CommandInfo implements Command {
    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String shortDesc() {
        return "Information";
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
        desc.add("Just information.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- displays information.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        JDA jda = Main.getJda();
        SelfUser selfUser = jda.getSelfUser();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(selfUser.getName(), selfUser.getAvatarUrl(), selfUser.getAvatarUrl());
        builder.setTitle("Information");
        builder.setColor(new Color(153, 255, 51));
        StringBuilder botInfo = new StringBuilder();
        botInfo.append("ID: ").append(selfUser.getId()).append("\n");
        botInfo.append("Name: ").append(selfUser.getName()).append("\n");
        botInfo.append("Version: ").append(Main.getVersion()).append("\n");
        botInfo.append("Guilds: ").append(jda.getGuilds().size()).append("\n");
        botInfo.append("Members: ").append(usersServed(jda.getGuilds())).append("\n");
        botInfo.append("Ping: ").append(jda.getPing()).append("\n");
        StringBuilder userInfo = new StringBuilder();
        userInfo.append("ID: ").append(member.getUser().getId()).append("\n");
        userInfo.append("Name: ").append(member.getUser().getName()).append("\n");
        userInfo.append("Discriminator: ").append("#").append(member.getUser().getDiscriminator()).append("\n");
        userInfo.append("Nickname: ").append(member.getNickname()).append("\n");
        userInfo.append("Status: ").append(member.getOnlineStatus()).append("\n");
        if (member.getGame() != null)
            userInfo.append("Playing: ").append(member.getGame().getName()).append("\n");
        StringBuilder guildInfo = new StringBuilder();
        guildInfo.append("ID: ").append(guild.getId()).append("\n");
        guildInfo.append("Name: ").append(guild.getName()).append("\n");
        guildInfo.append("Members: ").append(membersNoBot(guild)).append("\n");
        guildInfo.append("Text Channels: ").append(guild.getTextChannels().size()).append("\n");
        guildInfo.append("Voice Channels: ").append(guild.getVoiceChannels().size()).append("\n");
        guildInfo.append("Emotes: ").append(guild.getEmotes().size()).append("\n");
        guildInfo.append("Region: ").append(guild.getRegion().getName()).append("\n");

        builder.addField("Bot Info", botInfo.toString(), false);
        builder.addField("User Info", userInfo.toString(), false);
        builder.addField("Guild Info", guildInfo.toString(), false);

        channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(5, TimeUnit.MINUTES));

        return true;
    }

    private int usersServed(List<Guild> guilds) {
        int users = 0;
        List<Long> members = new ArrayList<>();
        for (Guild guild : guilds) {
            for (Member member : guild.getMembers()) {
                if (!member.getUser().isBot() && !members.contains(member.getUser().getIdLong())) {
                    members.add(member.getUser().getIdLong());
                    users++;
                }
            }
        }
        return users;
    }

    private int membersNoBot(Guild guild) {
        int users = 0;
        for (Member member : guild.getMembers()) {
            if (!member.getUser().isBot())
                users++;
        }
        return users;
    }
}
