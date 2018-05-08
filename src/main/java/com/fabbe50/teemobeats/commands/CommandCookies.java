package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Utils;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 25/04/2018 - 7:54 PM.
 */
public class CommandCookies implements Command {
    @Override
    public String getName() {
        return "cookie";
    }

    @Override
    public String shortDesc() {
        return "Cause cookies.";
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
        desc.add("Cookies! Cause who doesn't want cookies?");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Send a cookie.");
        usage.add("[number] - Send more cookies.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 2);
        StringBuilder message1 = new StringBuilder(":cookie:");
        if (command.length == 2) {
            if (Utils.isInteger(command[1])) {
                if (Integer.parseInt(command[1]) <= 200) {
                    for (int i = 1; i < Integer.parseInt(command[1]); i++) {
                        message1.append(" :cookie:");
                    }
                } else {
                    channel.sendMessage("CookieOverflowException: You tried to take too many cookies, now the cookie monster is sad. :(").queue(message2 -> message2.delete().queueAfter(1, TimeUnit.MINUTES));
                    return true;
                }
            }
        }
        channel.sendMessage(message1.toString()).queue(message2 -> message2.delete().queueAfter(5, TimeUnit.MINUTES));
        return true;
    }
}
