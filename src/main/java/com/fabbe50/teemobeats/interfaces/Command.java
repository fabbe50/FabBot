package com.fabbe50.teemobeats.interfaces;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;

/**
 * Created by fabbe on 24/04/2018 - 3:56 PM.
 */
public interface Command {
    String getName();

    String shortDesc();

    String group();

    int permission();

    List<String> addDescription(List<String> desc);

    List<String> addUsage(List<String> usage);

    boolean execute(TextChannel channel, Guild guild, Member member, Message message);
}
