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
    public static String musicChannel = "";
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
        System.out.println("DiscordMessage: ['" + event.getMessage().getMember().getEffectiveName() + "' in '" + event.getTextChannel().getName() + "'] " + event.getMessage().getContent());
        String[] command;
        if ((event.getMessage().getContent().contains(".vote") && VotingSystem.topic.equals("")) || event.getMessage().getContent().contains(".playlist") || event.getMessage().getContent().contains(".clear"))
            command = event.getMessage().getContent().split(" ", 3);
        else
            command = event.getMessage().getContent().split(" ", 2);
        Guild guild = event.getGuild();
        String cs = commandSymbol;

        if (guild != null && event.getAuthor().isBot()) {
            if (runtimeToken.equals(event.getMessage().getContent()) && !startOnce) {
                event.getMessage().delete().queue();
                botUsername = event.getMessage().getAuthor().getName();
                event.getTextChannel().sendMessage("Saved username: " + botUsername).queue(message -> message.delete().queueAfter(3, TimeUnit.SECONDS));
                startOnce = true;
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

        boolean commandSuccess = false;
        if (guild != null && !event.getAuthor().isBot() && event.getMessage().getContent().substring(0, 1).equals(cs)) {
             commandSuccess = CommandHandler.handleCommand(event);
        }

        super.onMessageReceived(event);
    }


}
