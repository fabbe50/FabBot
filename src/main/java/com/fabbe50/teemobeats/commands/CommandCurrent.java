package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.Main.music;

/**
 * Created by fabbe on 25/04/2018 - 8:01 PM.
 */
public class CommandCurrent implements Command {
    @Override
    public String getName() {
        return "current";
    }

    @Override
    public String shortDesc() {
        return "Displays the current track.";
    }

    @Override
    public String group() {
        return "music";
    }

    @Override
    public int permission() {
        return 0;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Shows current track.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Shows current track.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        music.current(channel, member);
        return true;
    }
}
