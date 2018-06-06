package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.handlers.DataHandler;
import com.fabbe50.teemobeats.interfaces.Command;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 05/06/2018 - 9:02 PM.
 */
public class CommandBlacklist implements Command {
    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public String shortDesc() {
        return "I'll ignore them all!!!";
    }

    @Override
    public String group() {
        return "tools";
    }

    @Override
    public int permission() {
        return 4;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Add people to this list and I'll ignore them.");
        desc.add("I'm okay with it anyways, since I don't like people.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("add [username] - Adds user to blacklist.");
        usage.add("remove [username] - Removes user from blacklist.");
        usage.add("list - Lists blacklisted users.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        String[] commands = message.getContentRaw().split(" ", 3);
        if (commands.length == 3) {
            if (commands[1].equals("add")) {
                String[] memberInfo = commands[2].split("#", 2);
                List<Member> members = guild.getMembersByEffectiveName(commands[2], false);
                if (memberInfo.length == 2) {
                    for (Member m : members) {
                        if (m.getUser().getDiscriminator().contains(memberInfo[1])) {
                            members = new ArrayList<>();
                            members.add(m);
                        }
                    }
                }
                if (members.size() == 1) {
                    Member member1 = members.get(0);
                    try {
                        DataHandler.writeTextToFile(guild, "blacklist.cfg", member1.getUser().getName() + "#" + member1.getUser().getDiscriminator(), true, false);
                        channel.sendMessage("Added " + member1.getUser().getName() + "#" + member1.getUser().getDiscriminator() + " to blacklist.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (members.size() == 2) {
                        channel.sendMessage("There's more than one person with that name, please type out the name like: `username#1234`.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                    } else {
                        channel.sendMessage("You need to specify a member to blacklist them.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                    }
                }
            } else if (commands[1].equals("remove")) {
                String[] memberInfo = commands[2].split("#", 2);
                List<Member> members = guild.getMembersByEffectiveName(commands[2], false);
                if (memberInfo.length == 2) {
                    for (Member m : members) {
                        if (m.getUser().getDiscriminator().contains(memberInfo[1])) {
                            members = new ArrayList<>();
                            members.add(m);
                        }
                    }
                }
                if (members.size() == 1) {
                    Member member1 = members.get(0);
                    try {
                        if (DataHandler.getFileContents(guild, "blacklist.cfg").contains(member1.getUser().getName() + "#" + member1.getUser().getDiscriminator())) {
                            DataHandler.writeTextToFile(guild, "blacklist.cfg", member1.getUser().getName() + "#" + member1.getUser().getDiscriminator(), false, false);
                            channel.sendMessage("Removed " + member1.getUser().getName() + "#" + member1.getUser().getDiscriminator() + " to blacklist.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                        } else {
                            channel.sendMessage("Couldn't find user on blacklist.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (members.size() == 2) {
                        channel.sendMessage("There's more than one person with that name, please type out the name like: `username#1234`.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                    } else {
                        channel.sendMessage("You need to specify a member to blacklist them.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                    }
                }
            }
        } else if (commands.length == 2) {
            if (commands[1].equals("list")) {
                try {
                    List<String> lines = DataHandler.getFileContents(guild, "blacklist.cfg");
                    StringBuilder builder = new StringBuilder();
                    builder.append("```Blacklisted Users: \n");
                    if (!lines.isEmpty()) {
                        for (String s : lines) {
                            builder.append(s).append("\n");
                        }
                    }
                    builder.append("```");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return true;
    }
}
