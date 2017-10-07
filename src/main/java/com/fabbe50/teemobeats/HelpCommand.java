package com.fabbe50.teemobeats;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 30/09/2017 - 10:01 PM.
 */
public class HelpCommand {
    private static List<String> commands = new ArrayList<>();
    private static List<String> descriptions = new ArrayList<>();
    private static List<String> permissions = new ArrayList<>();

    public static void init() {
        commands.clear();
        descriptions.clear();
        permissions.clear();

        createCommandListEntry("help", "Shows this list, duh!", "No");
        createCommandListEntry("clear", "Cleans after itself.", "Yes");
        createCommandListEntry("csymbol", "Change symbol.", "Yes");
        createCommandListEntry("current", "Display Current track.", "No");
        createCommandListEntry("feedback", "Prints link feedback.", "No");
        createCommandListEntry("kill", "Kills the bot.", "Yes");
        createCommandListEntry("list", "Shows the playlist.", "No");
        createCommandListEntry("lyrics", "Shows the lyrics.", "No");
        createCommandListEntry("play", "Queue and play songs.", "No");
        createCommandListEntry("playlist", "Playlist management.", "Partly");
        createCommandListEntry("remove", "Removes song.", "No");
        createCommandListEntry("channel", "Sets music channel.", "Yes");
        createCommandListEntry("skip", "Starts skip voting.", "No");
        createCommandListEntry("forceskip", "Skips song.", "Yes");
        createCommandListEntry("stop", "Starts stop voting.", "No");
        createCommandListEntry("forcestop", "Stops playback.", "Yes");
        createCommandListEntry("vote", "Voting System.", "No");
    }

    public static String getCommands() {
        String message = "";

        for (String s : commands) {
            message = message + Main.commandSymbol + s + "\n";
        }

        return message;
    }

    public static String getDescriptions() {
        String message = "";

        for (String s : descriptions) {
            message = message + s + "\n";
        }

        return message;
    }

    public static String getPermissions() {
        String message = "";

        for (String s : permissions) {
            message = message + s + "\n";
        }

        return message;
    }

    private static void createCommandListEntry(String command, String description, String permission) {
        commands.add(command);
        descriptions.add(description);
        permissions.add(permission);
    }

    static void helpCommand(TextChannel channel, String[] command, String cs) {
        HelpCommand.init();

        EmbedBuilder builder = new EmbedBuilder();

        if (command.length == 2) {
            switch (command[1]) {
                case "clear":
                    builder.setTitle("Help - clear");
                    builder.addField("Description: ", Utils.addLinebreaks("It exists to clean up the mess that people may or may not leave behind.", 60), false);
                    builder.addField("Usages: ",
                            cs + "clear - Clears the last 500 messages.\n" +
                            cs + "clear [integer] - Clears specified amount of messages.\n" +
                            cs + "clear [name] - Clears messages from specific member.",
                            false);
                    break;
                case "csymbol":
                    builder.setTitle("Help - csymbol");
                    builder.addField("Description: ", Utils.addLinebreaks("Changes the symbol used when typing a command, to reduce conflict between different bots.", 60), false);
                    builder.addField("Usages: ",
                            cs + "csymbol [symbol] - Changes the symbol.",
                            false);
                    break;
                case "current":
                    builder.setTitle("Help - current");
                    builder.addField("Description: ", Utils.addLinebreaks("Gets information about the current track.", 60), false);
                    builder.addField("Usages: ",
                            cs + "current - Displays information about the playing track.",
                            false);
                    break;
                case "feedback":
                    builder.setTitle("Help - feedback");
                    builder.addField("Description: ", Utils.addLinebreaks("Something wrong? A suggestion? Maybe want to write me an essay about how shitty my bot is? Be my guest!", 60), false);
                    builder.addField("Usages: ",
                            cs + "feedback - Prints link to a feedback form.",
                            false);
                    break;
                case "kill":
                    builder.setTitle("Help - kill");
                    builder.addField("Description: ", Utils.addLinebreaks("Harshly telling my bot to commit suicide. You cruel bastard!", 60), false);
                    builder.addField("Usages: ",
                            cs + "kill - Shuts down the bot.",
                            false);
                    break;
                case "list":
                    builder.setTitle("Help - list");
                    builder.addField("Description: ", Utils.addLinebreaks("Displays the upcoming tracks.", 60), false);
                    builder.addField("Usages: ",
                            cs + "list - Displays the full list of upcoming tracks.\n" +
                            cs + "list [number] - Displays a specified amount of upcoming tracks.",
                            false);
                    break;
                case "lyrics":
                    builder.setTitle("Help - lyrics");
                    builder.addField("Description: ", Utils.addLinebreaks("Let's have a fucking sing-a-long, because we have nothing better to do.", 60), false);
                    builder.addField("Usages: ",
                            cs + "lyrics - Searches for and displays lyrics from the current song.\n" +
                            cs + "lyrics [\"Artist - Song Name\"] - Searches for the specified song and displays lyrics.\n" +
                            cs + "lyrics [songlyrics.com url] - Manually display lyrics from url.",
                            false);
                    break;
                case "play":
                    builder.setTitle("Help - play");
                    builder.addField("Description: ", Utils.addLinebreaks("Let's listen to some music, eh?", 60), false);
                    builder.addField("Usages: ",
                            cs + "play [Song name] - Searches on YouTube after the song, queues it and start playback.\n" +
                            cs + "play [TrackURL] - Paste a url to a specific song on YouTube, Vimeo, SoundCloud, Twitch, Bandcamp or a hosted track on a server.\n" +
                            cs + "play [PlaylistURL] - Paste a url to a playlist on YouTube to add the whole playlist.",
                            false);
                    break;
                case "playlist":
                    builder.setTitle("Help - playlist");
                    builder.addField("Description: ", Utils.addLinebreaks("Playlist management.", 60), false);
                    builder.addField("Usages: ",
                            cs + "playlist [list] - Lists all available playlists.\n" +
                            cs + "playlist [load] [name] - Adds specified playlist to queue.\n" +
                            cs + "playlist [save] [name] - Save current session to playlist.\n" +
                            cs + "playlist [remove] [name] - Removes playlist. [*]",
                            false);
                    break;
                case "remove":
                    builder.setTitle("Help - remove");
                    builder.addField("Description: ", Utils.addLinebreaks("Removes song from queue", 60), false);
                    builder.addField("Usages: ",
                            cs + "remove [number] - Removes specified track from queue.",
                            false);
                    break;
                case "channel":
                    builder.setTitle("Help - channel");
                    builder.addField("Description: ", Utils.addLinebreaks("You don't want music anywhere and everywhere?", 60), false);
                    builder.addField("Usages: ",
                            cs + "channel [channel name] - Specifies a default channel for the bot to connect to.",
                            false);
                    break;
                case "skip":
                    builder.setTitle("Help - skip");
                    builder.addField("Description: ", Utils.addLinebreaks("Agree to skip?", 60), false);
                    builder.addField("Usages: ",
                            cs + "skip - Starts skip voting.",
                            false);
                    break;
                case "forceskip":
                    builder.setTitle("Help - forceskip");
                    builder.addField("Description: ", Utils.addLinebreaks("Admins are OP.", 60), false);
                    builder.addField("Usages: ",
                            cs + "forceskip - Skips to the next song without voting.",
                            false);
                    break;
                case "stop":
                    builder.setTitle("Help - stop");
                    builder.addField("Description: ", Utils.addLinebreaks("Agree to be party-poopers?", 60), false);
                    builder.addField("Usages: ",
                            cs + "stop - Starts stop voting.",
                            false);
                    break;
                case "forcestop":
                    builder.setTitle("Help - forcestop");
                    builder.addField("Description: ", Utils.addLinebreaks("Admins are also party-poopers.", 60), false);
                    builder.addField("Usages: ",
                            cs + "forceskip - Stops music playback and disconnects bot from voice.",
                            false);
                    break;
                case "vote":
                    builder.setTitle("Help - vote");
                    builder.addField("Description: ", Utils.addLinebreaks("Poll? Vote? Call it whatever...", 60), false);
                    builder.addField("Usages: ",
                            cs + "vote [topic] [answer 1, answer 2, answer 3...] - Creates a Pol... vo.. whatever.\n" +
                            cs + "vote [response] - Place your vote.\n" +
                            cs + "vote [end] - Ends the thing.",
                            false);
                    break;
            }
        } else {
            builder.setTitle("Help");
            builder.addField("Command Syntax", HelpCommand.getCommands(), true);
            builder.addField("Command Description", HelpCommand.getDescriptions(), true);
            builder.addField("Admin?", HelpCommand.getPermissions(), true);

            builder.addField("Description: ", Utils.addLinebreaks(cs + "help is used to list the commands available and to get special help with certain commands.", 60), false);
            builder.addField("Usages: ",
                    cs + "help - to list all available commands.\n" +
                    cs + "help [command] - to get help with the specified command.",
                    false);
        }

        builder.setColor(Color.CYAN);

        channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
    }
}
