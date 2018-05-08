package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.Main.music;

/**
 * Created by fabbe on 25/04/2018 - 7:11 PM.
 */
public class CommandSkip implements Command {
    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String shortDesc() {
        return "Skip songs";
    }

    @Override
    public String group() {
        return "music";
    }

    @Override
    public int permission() {
        return 1;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Don't wanna listen to the current song? I gotcha fam!");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Skips to the next song.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        if (command.length == 2) {
            if (command[1].equals("-f") && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                music.skipTrack(channel, member);
                return true;
            } else if (command[1].equals("-f") && !member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
                builder.setDescription("**You don't have permission to use this command! \"" + member.getEffectiveName() + "\"**");
                channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                return true;
            }
        } else {
            if (guild.getAudioManager().getConnectedChannel() != null) {
                if (guild.getAudioManager().getConnectedChannel().getMembers().size() > 0) {
                    //int members = guild.getAudioManager().getConnectedChannel().getMembers().size();
                    music.skipTrack(channel, member);
                    //VotingSystem.createPoll(channel, "Skip?", members, "Yes", "No");
                    //channel.sendMessage("-- Time's up!").queueAfter(30, TimeUnit.SECONDS);
                    return true;
                }
            }
        }
        return false;
    }
}
