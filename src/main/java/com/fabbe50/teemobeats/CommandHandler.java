package com.fabbe50.teemobeats;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.fabbe50.teemobeats.Main.commandSymbol;
import static com.fabbe50.teemobeats.Main.music;
import static com.fabbe50.teemobeats.Main.trackList;
import static com.fabbe50.teemobeats.Utils.isMemberInGuild;

/**
 * Created by fabbe on 07/10/2017 - 10:43 PM.
 */
class CommandHandler {
    static boolean handleCommand(MessageReceivedEvent event) {
        TextChannel channel = event.getTextChannel();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        Message message = event.getMessage();
        String[] commandBase = message.getContent().split(" ", 2);
        String cs = Main.commandSymbol;
        commandBase[0] = commandBase[0].replace(cs, "");

        message.delete().queue();
        switch (commandBase[0]) {
            case "help":
                HelpCommand.helpCommand(event.getTextChannel(), commandBase, cs);
                return true;
            case "play":
                return executePlay(channel, member, commandBase, Main.musicChannel);
            case "skip":
                return executeSkip(channel, member, commandBase, guild);
            case "stop":
                return executeStop(channel, member, commandBase, guild);
            case "list":
                return executeList(channel, commandBase);
            case "kill":
                return executeKill(channel, member, guild);
            case "playlist":
                return executePlaylist(channel, member, message.getContent().split(" ", 3));
            case "vote":
                VotingSystem.voteCommand(event.getTextChannel(), message.getContent().split(" ", 3), event.getMember());
                return true;
            case "cookie":
                return executeCookies(channel, commandBase);
            case "channel":
                return executeChannel(channel, member, guild, commandBase);
            case "current":
                music.current(event.getTextChannel());
                return true;
            case "feedback":
                event.getTextChannel().sendMessage("https://goo.gl/forms/AD8Lk4rmSjHIxTrC2").queue();
                return true;
            case "csymbol":
                return executeSymbol(channel, member, guild, commandBase);
            case "clear":
                return executeClear(channel, member, commandBase);
            case "lyrics":
                return executeLyrics(channel, guild, commandBase);
            case "remove":
                return executeRemove(channel, member, commandBase);
        }


        return false;
    }

    private static String[] subCommandSplitter(String[] commandBase, String regex, int splits) {
        return commandBase[1].split(regex, splits);
    }

    private static boolean executePlay(TextChannel channel, Member member, String[] command, String musicChannel) {
        if (command[1].contains("http")) {
            try {
                music.loadAndPlay(channel, command[1], member.getVoiceState().getChannel().getName());
                return true;
            } catch (Exception f) {
                music.loadAndPlay(channel, command[1], musicChannel);
                return true;
            }
        } else {
            try {
                music.search(channel, command[1], member.getVoiceState().getChannel().getName());
                return true;
            } catch (Exception f) {
                try {
                    music.search(channel, command[1], musicChannel);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
    }

    private static boolean executeSkip(TextChannel channel, Member member, String[] command, Guild guild) {
        if (command.length == 2) {
            if (command[1].equals("-f") && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                music.skipTrack(channel);
                return true;
            } else if (command[1].equals("-f") && !member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                channel.sendMessage("You don't have permission to use this command! \"" + member.getNickname() + "\"").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                return false;
            }
        } else {
            if (guild.getAudioManager().getConnectedChannel() != null) {
                if (guild.getAudioManager().getConnectedChannel().getMembers().size() > 0) {
                    int members = guild.getAudioManager().getConnectedChannel().getMembers().size();
                    VotingSystem.createPoll(channel, "Skip?", members, "Yes", "No");
                    channel.sendMessage("-- Time's up!").queueAfter(30, TimeUnit.SECONDS);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean executeStop(TextChannel channel, Member member, String[] command, Guild guild) {
        if (command.length == 2) {
            if (command[1].equals("-f") && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                music.stop(channel);
                return true;
            } else if (command[1].equals("-f") && !member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                channel.sendMessage("You don't have permission to use this command! \"" + member.getNickname() + "\"").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                return false;
            }
        } else {
            if (guild.getAudioManager().getConnectedChannel() != null) {
                if (guild.getAudioManager().getConnectedChannel().getMembers().size() > 0) {
                    int members = guild.getAudioManager().getConnectedChannel().getMembers().size();
                    VotingSystem.createPoll(channel, "Stop?", members, "Yes", "No");
                    channel.sendMessage("- Time's up!").queueAfter(30, TimeUnit.SECONDS);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean executeList(TextChannel channel, String[] command) {
        if (command.length == 2) {
            try {
                music.getQueue(channel, Integer.parseInt(command[1]));
                return true;
            } catch (Exception e) {
                channel.sendMessage("Error: " + e.getLocalizedMessage()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                return false;
            }
        } else {
            music.getQueue(channel);
            return true;
        }
    }

    private static boolean executeKill(TextChannel channel, Member member, Guild guild) {
        if (member.getPermissions().contains(Permission.MANAGE_SERVER)) {
            channel.sendMessage("Okay... goodbye cruel world! :C").queue();
            guild.getJDA().shutdown();
            return true;
        } else {
            channel.sendMessage("You don't have permission to use this command! \"" + member.getNickname() + "\"").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
            return false;
        }
    }

    private static boolean executePlaylist(TextChannel channel, Member member, String[] command) {
        switch (command[1]) {
            case "save":
                if (command.length == 3) {
                    if (!trackList.isEmpty()) {
                        music.removePlaylist(channel, command[2]);
                        music.createPlaylist(channel, command[2], trackList);
                        trackList.clear();
                    } else {
                        channel.sendMessage("Tracklist empty, please build up a queue before saving.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                    }
                } else {
                    channel.sendMessage("WrongFormat: please use: `" + commandSymbol + "playlist save [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return true;
            case "load":
                if (command.length == 3) {
                    try {
                        String temp = "";
                        temp = System.getProperty("user.dir") + "\\playlists\\" + command[2] + ".m3u";
                        music.readPlaylist(channel, temp, member.getVoiceState().getChannel().getName());
                        return true;
                    } catch (Exception e) {
                        try {
                            String temp = "";
                            temp = System.getProperty("user.dir") + "\\playlists\\" + command[2] + ".m3u";
                            music.readPlaylist(channel, temp, "");
                            return true;
                        } catch (Exception e1) {
                            e.printStackTrace();
                            e1.printStackTrace();
                            return false;
                        }
                    }
                } else {
                    channel.sendMessage("WrongFormat: please use: `" + commandSymbol + "playlist load [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return false;
            case "list":
                music.retrievePlaylistDir(channel);
                return true;
            case "get":
                if (command.length == 3) {
                    music.getPlaylist(channel, command[2]);
                    return true;
                } else {
                    channel.sendMessage("WrongFormat: please use: `" + commandSymbol + "playlist get [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return false;
            case "remove":
                if (command.length == 3 && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                    music.removePlaylist(channel, command[2]);
                    return true;
                } else {
                    if (!member.getPermissions().contains(Permission.MANAGE_SERVER)) {
                    } else
                        channel.sendMessage("WrongFormat: please use: `" + commandSymbol + "playlist remove [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
                return false;
        }
        return false;
    }

    private static boolean executeCookies(TextChannel channel, String[] command) {
        String message = ":cookie:";
        if (command.length == 2) {
            if (Utils.isInteger(command[1])) {
                if (Integer.parseInt(command[1]) <= 200) {
                    for (int i = 1; i < Integer.parseInt(command[1]); i++) {
                        message = message + " :cookie:";
                    }
                } else {
                    channel.sendMessage("CookieOverflowException: You tried to take too many cookies, now the cookie monster is sad. :(").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                    return false;
                }
            }
        }
        channel.sendMessage(message).queue(message1 -> message1.delete().queueAfter(20, TimeUnit.SECONDS));
        return true;
    }

    private static boolean executeChannel(TextChannel channel, Member member, Guild guild, String[] command) {
        if (member.getPermissions().contains(Permission.MANAGE_SERVER)) {
            if (command[1].equals("clear")) {
                Main.musicChannel = "";
                channel.sendMessage("Cleared default music channel.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                return true;
            } else if (guild.getVoiceChannelsByName(command[1], true) != null) {
                Main.musicChannel = command[1];
                channel.sendMessage("Set `" + command[1] + "` to music channel.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                return true;
            } else {
                channel.sendMessage("Couldn't find channel with that name.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                return false;
            }
        } else
            return false;
    }

    private static boolean executeSymbol(TextChannel channel, Member member, Guild guild, String[] command) {
        if (member.getPermissions().contains(Permission.MANAGE_SERVER)) {
            commandSymbol = command[1];
            guild.getJDA().getPresence().setGame(Game.of(commandSymbol + "help"));
            channel.sendMessage("Symbol changed to: `" + command[1] + "`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            return true;
        } else
            return false;
    }

    private static boolean executeClear(TextChannel channel, Member member, String[] command) {
        if (member.getPermissions().contains(Permission.MANAGE_SERVER)) {
            if (command.length >= 2) {
                if (Utils.isInteger(command[1])) {
                    channel.sendMessage("Clearing " + command[1] + " ...").queue();
                    channel.sendMessage("...").queue();
                    channel.deleteMessages(ChatManagement.clear(channel, Integer.parseInt(command[1]) + 2)).queue();
                    return true;
                } else if (command.length == 2) {
                    if (command[1].equalsIgnoreCase("me")) {
                        channel.sendMessage("Clearing " + member.getEffectiveName() + "'s messages...").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                        channel.deleteMessages(ChatManagement.clearWithName(channel, member.getEffectiveName())).queue();
                        return true;
                    } else if (isMemberInGuild(channel, command[1])) {
                        channel.sendMessage("Clearing " + command[1] + "'s messages...").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                        channel.deleteMessages(ChatManagement.clearWithName(channel, command[1])).queue();
                        return true;
                    } else {
                        channel.sendMessage(command[1] + " is not an integer nor a name.").queue();
                        return false;
                    }
                }
                return false;
            } else {
                channel.sendMessage("Clearing...").queue();
                channel.sendMessage("...").queue();
                channel.deleteMessages(ChatManagement.clear(channel, 500)).queue();
                return true;
            }
        }
        else
            return false;
    }

    private static boolean executeLyrics(TextChannel channel, Guild guild, String[] command) {
        if (command.length == 2) {
            if (command[1].contains("http://"))
                music.lyrics(channel, command[1]);
            else
                music.lyrics(channel, command[1], music.getGuildAudioPlayer(guild).player.getPlayingTrack());
            return true;
        } else {
            music.lyrics(channel);
            return true;
        }
    }

    private static boolean executeRemove(TextChannel channel, Member member, String[] command) {
        if (member.getPermissions().contains(Permission.MANAGE_SERVER)) {
            try {
                music.remove(channel, Integer.parseInt(command[1]));
                return true;
            } catch (Exception e) {
                channel.sendMessage("Couldn't find track on that position.").queue();
                return false;
            }
        }
        else
            return false;
    }
}
