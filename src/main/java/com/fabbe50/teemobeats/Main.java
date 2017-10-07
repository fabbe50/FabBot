package com.fabbe50.teemobeats;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    static String commandSymbol = ".";
    public static Music music = new Music();
    private String musicChannel = "";
    static JDA jda;
    static String[] arguments;
    private static boolean startOnce = false;
    private static String runtimeToken = "ffffffffffffffffff";
    static String botUsername;
    static List<AudioTrack> trackList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Launching from directory: " + System.getProperty("user.dir"));
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
        jda.getPresence().setGame(Game.of(commandSymbol + "help"));
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("READY!");
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
        if ((event.getMessage().getContent().contains(".vote") && VotingSystem.topic.equals("")) || event.getMessage().getContent().contains(".playlist") || event.getMessage().getContent().contains(".clear"))
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
                        music.loadAndPlay(event.getTextChannel(), command[1], musicChannel);
                    }
                } else {
                    try {
                        music.search(event.getTextChannel(), command[1], event.getMember().getVoiceState().getChannel().getName());
                    }
                    catch (Exception f) {
                        try {
                            music.search(event.getTextChannel(), command[1], musicChannel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if ((cs + "skip").equals(command[0]) || ((cs + "forceskip").equals(command[0]) && (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)))) {
                event.getMessage().delete().queue();
                if (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER) && (cs + "forceskip").equals(command[0])) {
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
            } else if ((cs + "stop").equals(command[0]) || ((cs + "forcestop").equals(command[0]) && (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)))) {
                event.getMessage().delete().queue();
                if (event.getMember().getPermissions().contains(Permission.MANAGE_SERVER) && (cs + "forcestop").equals(command[0])) {
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
                        event.getTextChannel().sendMessage("Error: " + e.getLocalizedMessage()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                }
                else
                    music.getQueue(event.getTextChannel());
            } else if ((cs + "kill").equals(command[0]) && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                event.getTextChannel().sendMessage("Okay... goodbye cruel world! :C").queue();
                guild.getJDA().shutdown();
            } else if ((cs + "help").equals(command[0])) {
                event.getMessage().delete().queue();
                HelpCommand.helpCommand(event.getTextChannel(), command, commandSymbol);
            } else if ((cs + "csymbol").equals(command[0]) && command.length == 2 && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                commandSymbol = command[1];
                event.getGuild().getJDA().getPresence().setGame(Game.of(commandSymbol + "help"));
                event.getTextChannel().sendMessage("Symbol changed to: `" + command[1] + "`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            } else if ((cs + "lyrics").equals(command[0])) {
                event.getMessage().delete().queue();
                if (command.length == 2) {
                    if (command[1].contains("http://"))
                        music.lyrics(event.getTextChannel(), command[1]);
                    else
                        music.lyrics(event.getTextChannel(), command[1], music.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack());
                } else {
                    music.lyrics(event.getTextChannel());
                }
            } else if ((cs + "current").equals(command[0])) {
                event.getMessage().delete().queue();
                music.current(event.getTextChannel());
            } else if ((cs + "clear").equals(command[0]) && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                if (command.length >= 2) {
                    if (Utils.isInteger(command[1])) {
                        event.getTextChannel().sendMessage("Clearing " + command[1] + " ...").queue();
                        event.getTextChannel().sendMessage("...").queue();
                        event.getTextChannel().deleteMessages(ChatManagement.clear(event.getChannel(), Integer.parseInt(command[1]) + 2)).queue();
                    } else if (command.length == 2) {
                        if (command[1].equalsIgnoreCase("me")) {
                            event.getTextChannel().sendMessage("Clearing " + event.getMember().getEffectiveName() + "'s messages...").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                            event.getTextChannel().deleteMessages(ChatManagement.clearWithName(event.getTextChannel(), event.getMember().getEffectiveName())).queue();
                        } else if (isMemberInGuild(event.getTextChannel(), command[1])) {
                            event.getTextChannel().sendMessage("Clearing " + command[1] + "'s messages...").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
                            event.getTextChannel().deleteMessages(ChatManagement.clearWithName(event.getTextChannel(), command[1])).queue();
                        } else {
                            event.getTextChannel().sendMessage(command[1] + " is not an integer nor a name.").queue();
                        }
                    }
                }
                else {
                    event.getTextChannel().sendMessage("Clearing...").queue();
                    event.getTextChannel().sendMessage("...").queue();
                    event.getTextChannel().deleteMessages(ChatManagement.clear(event.getChannel(), 500)).queue();
                }
            } else if ((cs + "remove").equals(command[0]) && command.length == 2 && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                try {
                    music.remove(event.getTextChannel(), Integer.parseInt(command[1]));
                } catch (Exception e) {
                    event.getTextChannel().sendMessage("Couldn't find track on that position.").queue();
                }
            } else if ((cs + "feedback").equals(command[0])) {
                event.getMessage().delete().queue();
                event.getTextChannel().sendMessage("https://goo.gl/forms/AD8Lk4rmSjHIxTrC2").queue();
            } else if ((cs + "vote").equals(command[0]) && command.length >= 2) {
                event.getMessage().delete().queue();
                VotingSystem.voteCommand(event.getTextChannel(), command, event.getMember());
            } else if ((cs + "channel").equals(command[0]) && command.length == 2 && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                event.getMessage().delete().queue();
                if (command[1].equals("clear")) {
                    musicChannel = "";
                    event.getTextChannel().sendMessage("Cleared default music channel.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                } else if (event.getGuild().getVoiceChannelsByName(command[1], true) != null) {
                    musicChannel = command[1];
                    event.getTextChannel().sendMessage("Set `" + command[1] + "` to music channel.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                } else {
                    event.getTextChannel().sendMessage("Couldn't find channel with that name.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
            } else if ((cs + "playlist").equals(command[0]) && command.length >= 2) {
                event.getMessage().delete().queue();
                switch (command[1]) {
                    case "save":
                        if (command.length == 3) {
                            music.createPlaylist(event.getTextChannel(), command[2], trackList);
                            trackList.clear();
                        } else {
                            event.getTextChannel().sendMessage("WrongFormat: please use: `"+ cs +"playlist save [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                        }
                        break;
                    case "load":
                        if (command.length == 3) {
                            try {
                                command[2] = System.getProperty("user.dir") + "\\playlists\\" + command[2] + ".m3u";
                                music.readPlaylist(event.getTextChannel(), command[2], event.getMember().getVoiceState().getChannel().getName());
                            } catch (Exception e) {
                                try {
                                    command[2] = System.getProperty("user.dir") + "\\playlists\\" + command[2] + ".m3u";
                                    music.readPlaylist(event.getTextChannel(), command[2], "");
                                } catch (Exception e1) {
                                    e.printStackTrace();
                                    e1.printStackTrace();
                                }
                            }
                        } else {
                            event.getTextChannel().sendMessage("WrongFormat: please use: `"+ cs +"playlist load [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                        }
                        break;
                    case "list":
                        music.retrievePlaylistDir(event.getTextChannel());
                        break;
                    case "get":
                        if (command.length == 3) {
                            music.getPlaylist(event.getTextChannel(), command[2]);
                        }
                        else {
                            event.getTextChannel().sendMessage("WrongFormat: please use: `"+ cs +"playlist get [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                        }
                        break;
                    case "remove":
                        if (command.length == 3 && event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {
                            music.removePlaylist(event.getTextChannel(), command[2]);
                        }
                        else {
                            if (!event.getMember().getPermissions().contains(Permission.MANAGE_SERVER)) {}
                            else
                                event.getTextChannel().sendMessage("WrongFormat: please use: `"+ cs +"playlist remove [name]`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                        }
                        break;
                }
            } else if ((cs + "cookie").equals(command[0])) {
                event.getMessage().delete().queue();
                String message = ":cookie:";
                if (command.length == 2) {
                    if (Utils.isInteger(command[1])) {
                        if (Integer.parseInt(command[1]) <= 200) {
                            for (int i = 1; i < Integer.parseInt(command[1]); i++) {
                                message = message + " :cookie:";
                            }
                        }
                        else {
                            event.getTextChannel().sendMessage("CookieOverflowException: You tried to take too many cookies, now the cookie monster is sad. :(").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
                        }
                    }
                }
                event.getTextChannel().sendMessage(message).queue(message1 -> message1.delete().queueAfter(20, TimeUnit.SECONDS));
            }
        }

        super.onMessageReceived(event);
    }

    boolean isMemberInGuild(TextChannel channel, String name) {
        for (Member member : channel.getGuild().getMembers()) {
            if (member.getEffectiveName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
