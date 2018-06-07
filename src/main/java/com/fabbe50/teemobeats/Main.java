package com.fabbe50.teemobeats;

import at.mukprojects.giphy4j.Giphy;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fabbe50.teemobeats.handlers.*;
import com.fabbe50.teemobeats.music.Music;
import com.fabbe50.teemobeats.music.TrackScheduler;
import com.fabbe50.teemobeats.registry.CommandRegistry;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.update.GuildUpdateNameEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    //TODO: Fix this shit
    public static String musicChannel = "";
    private static String runtimeToken = "ffffffffffffffffff";

    //Stuff that works
    private static String STATUS = "Beta";
    private static int BUILD = 159;
    private static String VERSION = "1.6.5" + STATUS + BUILD;
    public static Music music = new Music();
    private static String sessionID;
    private static JDA jda;
    private static Console console;
    public static Giphy giphy;
    public static String[] arguments;
    private static String defaultCommandSymbol = ".";
    private static Map<Guild, String> commandSymbols = new HashMap<>();
    public static Map<Guild, List<AudioTrack>> session = new HashMap<>();
    public static boolean discordActive = false;
    public static boolean youtubeActive = false;
    public static boolean giphyActive = false;
    public static boolean aiActive = false;

    public static void main(String[] args) throws Exception {
        console = new Console();
        System.out.println("Launching from directory: " + System.getProperty("user.dir"));

        if (args.length > 4) return;

        arguments = args;
        List<String> argu = new ArrayList<>(Arrays.asList(args));
        for (String a : argu) {
            if (a.contains("--discordKey")) {
                String[] arg = a.split("=");
                if (arg.length == 2) {
                    arguments[0] = arg[1];
                    discordActive = true;
                }
            } else if (a.contains("--youtubeKey")) {
                String[] arg = a.split("=");
                if (arg.length == 2) {
                    arguments[1] = arg[1];
                    youtubeActive = true;
                }
            } else if (a.contains("--giphyKey")) {
                String[] arg = a.split("=");
                if (arg.length == 2) {
                    arguments[2] = arg[1];
                    giphyActive = true;
                }
            } else if (a.contains("--aiKey")) {
                String[] arg = a.split("=");
                if (arg.length == 2) {
                    arguments[3] = arg[1];
                    aiActive = true;
                }
            }
        }


        if (!discordActive || !youtubeActive || !giphyActive) {
            System.out.println("[Error]: Missing Arguments.");
            return;
        }
        if (!aiActive) {
            System.out.println("[Info]: AI disabled.");
        } else {
            System.out.println("[Info]: AI enabled.");
            aiActive = false;
            System.out.println("[Error]: AI Unreachable. Disabling.");
        }
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        sessionID = UUID.randomUUID().toString();

        jda = new JDABuilder(AccountType.BOT)
                .setToken(args[0])
                .buildAsync();

        jda.addEventListener(new Main());
        jda.addEventListener(new VoiceChatTimeoutHandler());
    }

    private Main() {
        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Game.playing("Initialization..."));
        music.musicManagers = new HashMap<>();

        CommandRegistry.init();

        music.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(music.playerManager);
        AudioSourceManagers.registerLocalSource(music.playerManager);

        TrackScheduler.jda = jda;
        Music.youtubeAPIKey = arguments[1];
        Main.giphy = new Giphy(Main.arguments[2]);
    }

    @Override
    public void onReady(ReadyEvent event) {
        DataHandler.init();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor("Fab Bot [" + STATUS + "]", "http://tsdeluxe.com", "http://tsdeluxe.com/hostedmedia/gifs/fabbe50face512.png");
        builder.setTitle("I'm awake!");
        builder.setColor(new Color(153, 255, 102));

        for (Guild guild : event.getJDA().getGuilds()) {
            System.out.println("Connected to guild: " + guild.getName());
            createRuntimeFiles(guild);
            try {
                if (DataHandler.getFileContents(guild, "prefix.cfg").isEmpty()) {
                    setCommandSymbol(guild, ".");
                    DataHandler.overrideTextFile(guild, "prefix.cfg", ".");
                } else {
                    setCommandSymbol(guild, DataHandler.getFileContents(guild, "prefix.cfg").get(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            builder.setDescription("I blacked out there for a moment, \nbut I'm back and ready for action. \n");
            builder.appendDescription("If you need **help** to figure out how I work, \nyou can start by **running the `" + getCommandSymbol(guild) + "help` command.**");
            boolean foundBotChannel = false;
            for (TextChannel channel : guild.getTextChannels()) {
                if (channel.getName().contains("bot")) {
                    channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                    foundBotChannel = true;
                    break;
                }
            }
            if (!foundBotChannel) {
                for (TextChannel channel : guild.getTextChannels()) {
                    if (channel.getName().contains("general")) {
                        channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                        break;
                    }
                }
            }
        }

        new ConsoleHandler();
        new YTAnnounceHandler();
        Runnable statusHandler = () -> {
            Timer timer = new Timer(true);
            timer.schedule(new StatusHandler(), 0, 60000);
        };
        new Thread(statusHandler).start();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Guild guild = event.getGuild();
        Message message = event.getMessage();
        Member member = event.getMember();
        TextChannel channel = event.getTextChannel();
        String cs = getCommandSymbol(guild);
        try {
            console.addMessageToConsole(member.getEffectiveName(), channel.getName(), guild.getName(), message.getContentRaw(), member.getColor());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        try {
            if (!DataHandler.getFileContents(guild, "blacklist.cfg").contains(member.getUser().getName() + "#" + member.getUser().getDiscriminator())) {
                if (event.getMessage().getContentRaw().length() > 1 && event.getMessage().getContentRaw().substring(0, cs.length()).equalsIgnoreCase(cs)) {
                    if (Character.isLetter(event.getMessage().getContentRaw().charAt(cs.length() + 1))) {
                        String messageContentRaw = event.getMessage().getContentRaw().toLowerCase();
                        if (guild != null && !event.getAuthor().isBot() && (messageContentRaw.substring(0, cs.length()).equals(cs) || messageContentRaw.substring(0, 1).equals("?"))) {
                            CommandHandlerOld.handleCommand(event);
                        }
                        if (guild != null && !event.getAuthor().isBot() && message.getContentRaw().substring(0, cs.length()).equals(cs)) {
                            String[] command = message.getContentRaw().replace(cs, "").split(" ", 2);
                            if (command[0].equalsIgnoreCase("help")) {
                                channel.sendMessage(CommandHandler.getHelp(event.getMessage()).build()).queue(message1 -> message1.delete().queueAfter(5, TimeUnit.MINUTES));
                                message.delete().queueAfter(2, TimeUnit.SECONDS);
                            } else {
                                boolean executed = CommandHandler.runCommand(message.getContentRaw().replaceFirst(cs, "").toLowerCase().split(" ", 2)[0], channel, guild, member, message);
                                if (executed)
                                    message.delete().queueAfter(2, TimeUnit.SECONDS);
                            }
                        }
                    }
                } else if (event.getMessage().getMentionedMembers().contains(guild.getMember(jda.getSelfUser())) && aiActive) {
                    String[] strings = event.getMessage().getContentRaw().split(" ", 2);
                    if (strings.length == 2) {
                        GoogleCredentials credentials = ComputeEngineCredentials.create();
                        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
                        try (SessionsClient sessionsClient = SessionsClient.create()) {
                            SessionName session = SessionName.of("small-talk-d6632", sessionID);
                            TextInput.Builder textInput = TextInput.newBuilder().setText(strings[1]).setLanguageCode("en-US");
                            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
                            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);
                            QueryResult queryResult = response.getQueryResult();
                            channel.sendMessage(queryResult.getFulfillmentText()).queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (event.getMessage().getMentionedMembers().contains(guild.getMember(jda.getSelfUser())) && !aiActive) {
                    String[] strings = event.getMessage().getContentRaw().split(" ", 2);
                    if (strings.length == 2) {
                        
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onMessageReceived(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (event.getGuild() != null) {
            Guild guild = event.getGuild();
            createRuntimeFiles(event.getGuild());
            System.out.println("Connected to guild: " + event.getGuild().getName());
            for (TextChannel channel : event.getGuild().getTextChannels()) {
                if (channel.getName().contains("general") || channel.getName().contains("bot")) {
                    channel.sendMessage("Thank you for inviting me to your server! I hope you enjoy my company!").queue();
                    break;
                }
            }
            try {
                if (DataHandler.getFileContents(guild, "prefix.cfg").isEmpty()) {
                    setCommandSymbol(guild, ".");
                    DataHandler.addChannelToTextFile(guild, "prefix.cfg", ".");
                } else {
                    setCommandSymbol(guild, DataHandler.getFileContents(guild, "prefix.cfg").get(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGuildUpdateName(GuildUpdateNameEvent event) {
        File guildDir = new File(System.getProperty("user.dir") + "\\data\\" + event.getOldName());
        if (guildDir.exists()) {
            guildDir.renameTo(new File(System.getProperty("user.dir") + "\\data\\" + event.getNewName()));
        }
    }

    //Methods of stuff
    private void createRuntimeFiles(Guild guild) {
        if (!new File(System.getProperty("user.dir") + "\\data\\").exists()) {
            new File(System.getProperty("user.dir") + "\\data\\").mkdir();
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\").exists()) {
            new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\").mkdir();
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\roles.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\roles.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\config.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\config.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\quotes.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\quotes.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\memory.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\memory.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\ytchannels.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\ytchannels.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\announcementchannel.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\announcementchannel.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\announcemessage.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\announcemessage.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\prefix.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\prefix.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\blacklist.cfg").exists()) {
            try {
                new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\blacklist.cfg").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getDefaultCommandSymbol() {
        return defaultCommandSymbol;
    }

    public static String getCommandSymbol(Guild guild) {
        return commandSymbols.get(guild);
    }

    public static void setCommandSymbol(Guild guild, String prefix) {
        commandSymbols.put(guild, prefix);
        try {
            DataHandler.addChannelToTextFile(guild, "prefix.cfg", prefix);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Guild> getAllGuilds() {
        return jda.getGuilds();
    }

    public static JDA getJda() {
        return jda;
    }

    public static String getVersion() {
        return VERSION;
    }

    public static int usersServed(List<Guild> guilds) {
        int users = 0;
        List<Long> members = new ArrayList<>();
        for (Guild guild : guilds) {
            for (Member member : guild.getMembers()) {
                if (!member.getUser().isBot() && !members.contains(member.getUser().getIdLong())) {
                    members.add(member.getUser().getIdLong());
                    users++;
                }
            }
        }
        return users;
    }
}
