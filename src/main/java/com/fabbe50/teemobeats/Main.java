package com.fabbe50.teemobeats;

import at.mukprojects.giphy4j.Giphy;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.fabbe50.teemobeats.handlers.*;
import com.fabbe50.teemobeats.music.Music;
import com.fabbe50.teemobeats.music.TrackScheduler;
import com.fabbe50.teemobeats.registry.CommandRegistry;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {
    //TODO: Fix this shit
    public static String musicChannel = "";
    private static String runtimeToken = "ffffffffffffffffff";

    //Stuff that works
    private static String STATUS = "Beta";
    private static int BUILD = 135;
    private static String VERSION = "1.6.4" + STATUS + BUILD;
    public static Music music = new Music();
    private static JDA jda;
    public static Giphy giphy;
    public static String[] arguments;
    private static String defaultCommandSymbol = ".";
    private static Map<Guild, String> commandSymbols = new HashMap<>();
    public static Map<Guild, List<AudioTrack>> session = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Launching from directory: " + System.getProperty("user.dir"));
        if (args.length != 3) return;
        Logger logger = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);

        jda = new JDABuilder(AccountType.BOT)
                .setToken(args[0])
                .buildAsync();

        Main.arguments = args;
        jda.addEventListener(new Main());
        jda.addEventListener(new VoiceChatTimeoutHandler());
    }

    private Main() {
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
        builder.setDescription("I blacked out there for a moment, \nbut I'm back and ready for action. \n");
        builder.appendDescription("If you need **help** to figure out how I work, \nyou can start by **running the `" + defaultCommandSymbol + "help` command.**");
        builder.setColor(new Color(153, 255, 102));

        for (Guild guild : event.getJDA().getGuilds()) {
            System.out.println("Connected to guild: " + guild.getName());
            createRuntimeFiles(guild);
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
        //new LoggingHandler();
        jda.getPresence().setPresence(OnlineStatus.IDLE, Game.watching(jda.getGuilds().size() + " guilds and " + usersServed(jda.getGuilds()) + " users."));
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        jda.getPresence().setPresence(OnlineStatus.IDLE, Game.watching(jda.getGuilds().size() + " guilds and " + usersServed(jda.getGuilds()) + " users."));
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        jda.getPresence().setPresence(OnlineStatus.IDLE, Game.watching(jda.getGuilds().size() + " guilds and " + usersServed(jda.getGuilds()) + " users."));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //LoggingHandler.addLogEntry(event.getMessage());
        Guild guild = event.getGuild();
        Message message = event.getMessage();
        Member member = event.getMember();
        TextChannel channel = event.getTextChannel();
        String cs = getCommandSymbol(guild);
        System.out.println("DiscordMessage: ['" + message.getMember().getEffectiveName() + "' in '" + message.getTextChannel().getName() + "` in guild `" + message.getGuild().getName() + "'] " + message.getContentRaw());

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
        }
        super.onMessageReceived(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        if (event.getGuild() != null) {
            createRuntimeFiles(event.getGuild());
            System.out.println("Connected to guild: " + event.getGuild().getName());
            for (TextChannel channel : event.getGuild().getTextChannels()) {
                if (channel.getName().contains("general") || channel.getName().contains("bot")) {
                    channel.sendMessage("Thank you for inviting me to your server! I hope you enjoy my company!").queue();
                    break;
                }
            }
            jda.getPresence().setPresence(OnlineStatus.IDLE, Game.watching(jda.getGuilds().size() + " guilds and " + usersServed(jda.getGuilds()) + " users."));
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

    private int usersServed(List<Guild> guilds) {
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
