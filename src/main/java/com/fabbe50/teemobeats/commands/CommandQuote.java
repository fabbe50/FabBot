package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.handlers.DataHandler;
import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.handlers.DataHandler.getFileContents;
import static com.fabbe50.teemobeats.handlers.DataHandler.writeTextToFile;

/**
 * Created by fabbe on 24/04/2018 - 7:27 PM.
 */
public class CommandQuote implements Command{
    @Override
    public String getName() {
        return "quote";
    }

    @Override
    public String shortDesc() {
        return "Got quotes?";
    }

    @Override
    public String group() {
        return "fun";
    }

    @Override
    public int permission() {
        return 1;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Are people funny or saying something that you wanna remember? Save it with a quote.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Gets a random quote and posts it in chat.");
        usage.add("add [quote] - Saves quote for later use.");
        usage.add("remove [quote] - Removes quote from storage.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] command = message.getContentRaw().split(" ", 3);
        if (command.length == 3) {
            if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
                if (command[1].contains("add")) {
                    try {
                        writeTextToFile(channel.getGuild(), "quotes.cfg", command[2], true, true);
                        int index = getFileContents(channel.getGuild(), "quotes.cfg").size();
                        channel.sendMessage("Saved quote #" + index + ": `" + command[2] + "`.").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (command[1].contains("remove")) {
                    if (!Utils.isInteger(command[2])) {
                        channel.sendMessage("Can't remove quote `" + command[2] + "`, input a number instead.").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                    } else {
                        try {
                            int index = Integer.parseInt(command[2]) - 1;
                            List<String> lines = getFileContents(channel.getGuild(), "quotes.cfg");
                            String s = lines.get(index);
                            writeTextToFile(channel.getGuild(), "quotes.cfg", "" + index, false, true);
                            channel.sendMessage("Removed quote #" + command[2] + ": `" + s + "`.").queue(message1 -> message1.delete().queueAfter(5, TimeUnit.SECONDS));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (command.length >= 1) {
            Random random = new Random(new Random().nextLong());
            int overrideIndex = 0;
            if (command.length == 2)
                if (Utils.isInteger(command[1]))
                    overrideIndex = Integer.parseInt(command[1]);
            try {
                List<String> lines = DataHandler.getFileContents(channel.getGuild(), "quotes.cfg");
                int index = random.nextInt(lines.size());
                if (overrideIndex != 0 && overrideIndex <= lines.size())
                    index = overrideIndex - 1;
                EmbedBuilder builder = new EmbedBuilder();
                builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
                if ((overrideIndex <= 0 || overrideIndex > lines.size()) && command.length == 2) {
                    builder.setColor(Color.RED);
                    builder.setTitle("Error 404");
                    builder.setDescription("**Quote not found.**");
                    builder.appendDescription("\nThere's no quote #" + overrideIndex);
                    builder.appendDescription("\nCurrently storing " + lines.size() + " quotes.");
                } else {
                    String s = lines.get(index);
                    int indexOf = s.lastIndexOf("-");
                    String[] st = {s.substring(0, indexOf), s.substring(indexOf + 1)};
                    if (s.contains("~")) {
                        int indexOf2 = s.lastIndexOf("-");
                        st = new String[]{s.substring(0, indexOf2), s.substring(indexOf2 + 1)};
                    }
                    builder.setColor(Color.GREEN);
                    builder.setTitle("Quote #" + (index + 1));
                    builder.setDescription("**" + st[0] + "**");
                    if (st.length == 2) {
                        if (st[1].substring(0, 1).equals(" "))
                            st[1] = st[1].substring(1);
                        builder.appendDescription("\n-" + st[1]);
                    }
                }
                channel.sendMessage(builder.build()).queue();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
