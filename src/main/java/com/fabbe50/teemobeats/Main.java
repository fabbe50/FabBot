package com.fabbe50.teemobeats;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    private String commandSymbol = ".";
    private Music music = new Music();
    static JDA jda;
    static String[] arguments;
    private static boolean startOnce = false;
    private static String runtimeToken;
    static String botUsername;

    public static void main(String[] args) throws Exception {
        if (args.length != 2) return;

        JDA jda = new JDABuilder(AccountType.BOT)
                .setToken(args[0])
                .buildAsync();

        Main.arguments = args;
        Main.jda = jda;
        jda.addEventListener(new Main());
    }

    private Main() {
        music.musicManagers = new HashMap<>();

        music.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(music.playerManager);
        AudioSourceManagers.registerLocalSource(music.playerManager);

        TrackScheduler.jda = jda;
        Music.youtubeAPIKey = arguments[1];
        jda.getPresence().setGame(Game.of(".help"));
    }

    @Override
    public void onReady(ReadyEvent event) {
        createRuntimeToken(false);
    }

    private void createRuntimeToken(boolean recheck) {
        if (recheck) {
            startOnce = false;
        }

        runtimeToken = Utils.getSaltString();
        System.out.println("Runtime Token: " + runtimeToken);

        try {
            jda.getTextChannels().get(0).sendMessage(runtimeToken).queue();
        } catch (Exception e) {
            e.printStackTrace();
            jda.shutdown();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] command;
        if (event.getMessage().getContent().contains(".vote"))
            command = event.getMessage().getContent().split(" ", 3);
        else
            command = event.getMessage().getContent().split(" ", 2);
        Guild guild = event.getGuild();
        String cs = commandSymbol;

        if (guild != null) {
            if (runtimeToken.equals(event.getMessage().getContent()) && !startOnce) {
                event.getMessage().delete().queue();
                botUsername = event.getMessage().getAuthor().getName();
                event.getTextChannel().sendMessage("Saved username: " + botUsername).queue();
                startOnce = true;
            } else if (("Saved username: " + botUsername).equals(event.getMessage().getContent())) {
                event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
            } else if (("-- Time's up!").equals(event.getMessage().getContent())) {
                if (!VotingSystem.topic.equals("")) {
                    event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
                    int[] votes = VotingSystem.endPoll(event.getTextChannel());
                    music.skipVote(event.getTextChannel(), votes);
                }
                else
                    event.getMessage().delete().queue();
            } else if (("- Time's up!").equals(event.getMessage().getContent())) {
                if (!VotingSystem.topic.equals("")) {
                    event.getMessage().delete().queueAfter(3, TimeUnit.SECONDS);
                    int[] votes = VotingSystem.endPoll(event.getTextChannel());
                    music.stopVote(event.getTextChannel(), votes);
                }
                else
                    event.getMessage().delete().queue();
            }
        }

        if (guild != null && !event.getAuthor().isBot()) {
            if ((cs + "play").equals(command[0]) && command.length >= 2) {
                event.getMessage().delete().queue();
                if (command[1].contains("http")) {
                    try {
                        music.loadAndPlay(event.getTextChannel(), command[1], event.getMember().getVoiceState().getChannel().getName());
                    } catch (Exception f) {
                        music.loadAndPlay(event.getTextChannel(), command[1], "");
                    }
                } else {
                    try {
                        music.search(event.getTextChannel(), command[1], event.getMember().getVoiceState().getChannel().getName());
                    }
                    catch (Exception f) {
                        try {
                            music.search(event.getTextChannel(), command[1], "");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if ((cs + "skip").equals(command[0]) || ((cs + "skipf").equals(command[0]) && (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)))) {
                event.getMessage().delete().queue();
                if (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER) && (cs + "skipf").equals(command[0])) {
                    music.skipTrack(event.getTextChannel());
                }
                else {
                    if (event.getGuild().getAudioManager().getConnectedChannel() != null) {
                        if (event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() > 0) {
                            int members = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size();
                            VotingSystem.createPoll(event.getTextChannel(), "Skip?", members, "Yes", "No");
                            event.getTextChannel().sendMessage("-- Time's up!").queueAfter(30, TimeUnit.SECONDS);
                        }
                    }
                }
            } else if ((cs + "stop").equals(command[0]) || ((cs + "stopf").equals(command[0]) && (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)))) {
                event.getMessage().delete().queue();
                if (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER) && (cs + "stopf").equals(command[0])) {
                    music.stop(event.getTextChannel());
                }
                else {
                    if (event.getGuild().getAudioManager().getConnectedChannel() != null) {
                        if (event.getGuild().getAudioManager().getConnectedChannel().getMembers().size() > 0) {
                            int members = event.getGuild().getAudioManager().getConnectedChannel().getMembers().size();
                            VotingSystem.createPoll(event.getTextChannel(), "Stop?", members, "Yes", "No");
                            event.getTextChannel().sendMessage("- Time's up!").queueAfter(30, TimeUnit.SECONDS);
                        }
                    }
                }
            } else if ((cs + "list").equals(command[0])) {
                event.getMessage().delete().queue();
                if (command.length == 2) {
                    try {
                        music.getQueue(event.getTextChannel(), Integer.parseInt(command[1]));
                    }
                    catch (Exception e) {
                        event.getTextChannel().sendMessage("Error: " + e.getLocalizedMessage()).queue();
                    }
                }
                else
                    music.getQueue(event.getTextChannel());
            } else if ((cs + "kill").equals(command[0]) && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                guild.getJDA().shutdown();
            } else if ((cs + "help").equals(command[0])) {
                event.getMessage().delete().queue();
                event.getTextChannel().sendMessage("```\n" +
                        cs + "help - Shows this list, duh!\n" +
                        cs + "clear - Clears the last 1000 of this bot's messages.\n" +
                        cs + "csymbol [symbol] - Changes the command symbol.\n" +
                        cs + "current - Display the current track.\n" +
                        cs + "feedback - Prints link to a page to send feedback.\n" +
                        cs + "kill - Kills the bot.\n" +
                        cs + "list (integer) - Shows the playlist (integer decides how many songs to show)\n" +
                        cs + "lyrics (\"artist - song name\") - Shows the lyrics for the current or specified song.\n" +
                        cs + "play [url | song name] - Adds song from url or song name to playlist and starts playback.\n" +
                        cs + "remove [integer] - Removes song with this position in the list.\n" +
                        cs + "skip - Starts skip voting.\n" +
                        cs + "skipf - Skips to the next song in the list.\n" +
                        cs + "stop - Stops playback and clears playlist.\n" +
                        cs + "vote [\"topic\" | \"answer\" | end] (\"answers....\") - Voting System for Discord." +
                        "```").queue();
            } else if ((cs + "csymbol").equals(command[0]) && command.length == 2 && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                commandSymbol = command[1];
                event.getTextChannel().sendMessage("Symbol changed to: `" + command[1] + "`").queue();
            } else if ((cs + "lyrics").equals(command[0])) {
                event.getMessage().delete().queue();
                if (command.length == 2) {
                    music.lyrics(event.getTextChannel(), command[1]);
                } else {
                    music.lyrics(event.getTextChannel());
                }
            } else if ((cs + "current").equals(command[0])) {
                event.getMessage().delete().queue();
                music.current(event.getTextChannel());
            } else if ((cs + "clear").equals(command[0])) {
                event.getMessage().delete().queue();
                event.getTextChannel().sendMessage("Clearing...").queue();
                event.getTextChannel().sendMessage("...").queue();
                event.getTextChannel().deleteMessages(ChatManagement.clear(event.getChannel())).queue();
            } else if ((cs + "remove").equals(command[0]) && command.length == 2 && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                try {
                    music.remove(event.getTextChannel(), Integer.parseInt(command[1]));
                }
                catch (Exception e) {
                    event.getTextChannel().sendMessage("Couldn't find track on that position.").queue();
                }
            } else if ((cs + "feedback").equals(command[0])) {
                event.getMessage().delete().queue();
                event.getTextChannel().sendMessage("https://goo.gl/forms/AD8Lk4rmSjHIxTrC2").queue();
            } else if ((cs + "vote").equals(command[0]) && command.length >= 2) {
                event.getMessage().delete().queue();
                if (VotingSystem.options != null) {
                    List<String> options = new ArrayList<>();
                    options.addAll(Arrays.asList(VotingSystem.options));
                    if (VotingSystem.topic.equals("Skip?") && options.contains(command[1])) {
                        music.skipVote(event.getTextChannel(), VotingSystem.putVote(event.getTextChannel(), options.indexOf(command[1]), event.getMember()));
                    } else if (VotingSystem.topic.equals("Skip?") && Utils.isInteger(command[1])) {
                        music.skipVote(event.getTextChannel(), VotingSystem.putVote(event.getTextChannel(), (Integer.parseInt(command[1]) - 1), event.getMember()));
                    } else if (VotingSystem.topic.equals("Stop?") && options.contains(command[1])) {
                        music.stopVote(event.getTextChannel(), VotingSystem.putVote(event.getTextChannel(), options.indexOf(command[1]), event.getMember()));
                    } else if (VotingSystem.topic.equals("Stop?") && Utils.isInteger(command[1])) {
                        music.stopVote(event.getTextChannel(), VotingSystem.putVote(event.getTextChannel(), (Integer.parseInt(command[1]) - 1), event.getMember()));
                    } else if (options.contains(command[1])) {
                        VotingSystem.putVote(event.getTextChannel(), options.indexOf(command[1]), event.getMember());
                    } else if (Utils.isInteger(command[1])) {
                        VotingSystem.putVote(event.getTextChannel(), (Integer.parseInt(command[1]) - 1), event.getMember());
                    } else if (command[1].equals("end")) {
                        VotingSystem.endPoll(event.getTextChannel());
                    }
                }
                else
                    VotingSystem.createPoll(event.getTextChannel(), command[1], command[2].split(" "));
            } else if ((cs + "rename").equals(command[0]) && command.length == 2) {
                event.getMessage().delete().queue();
            }
        }

        super.onMessageReceived(event);
    }
}
