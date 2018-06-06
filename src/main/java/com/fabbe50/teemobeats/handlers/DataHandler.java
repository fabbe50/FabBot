package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fabbe on 09/04/2018 - 4:51 AM.
 */
public class DataHandler {
    public static List<String> getFileContents(Guild guild, String filename) throws IOException {
        return Files.readAllLines(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\" + filename).toPath());
    }

    public static void writeTextToFile(Guild guild, String filename, String text, boolean add, boolean allowDupes) throws IOException {
        List<String> lines = Files.readAllLines(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\" + filename).toPath());
        if (add) {
            if (!lines.contains(text) || allowDupes) {
                lines.add(text);
            }
        } else if (!add && Utils.isInteger(text)) {
            lines.remove(Integer.parseInt(text));
        } else {
            if (lines.contains(text)) {
                lines.remove(text);
            }
        }
        Files.write(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\" + filename).toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void addChannelToTextFile(Guild guild, String filename, String text) throws IOException {
        List<String> lines = new ArrayList<>();
        for (TextChannel channel : guild.getTextChannels())
            if (channel.getName().equalsIgnoreCase(text)) {
                lines.add(text);
                Files.write(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\" + filename).toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
                break;
            }
    }

    public static void overrideTextFile(Guild guild, String filename, String text) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(text);
        Files.write(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\" + filename).toPath(), lines, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static Map<Guild, String> ytmessage = new HashMap<>();
    public static void updateYTMessage() {
        try {
            ytmessage.clear();
            for (Guild guild : Main.getAllGuilds()) {
                if (!getFileContents(guild, "announcemessage.cfg").isEmpty())
                    ytmessage.put(guild, getFileContents(guild, "announcemessage.cfg").get(0));
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static Map<Guild, String> ytchannels = new HashMap<>();
    public static void updateytchannels() {
        try {
            ytchannels.clear();
            for (Guild guild : Main.getAllGuilds()) {
                if (!getFileContents(guild, "announcementchannel.cfg").isEmpty())
                    ytchannels.put(guild, getFileContents(guild, "announcementchannel.cfg").get(0));
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public static Map<Guild, List<String>> ytannouncements = new HashMap<>();
    public static void updateYouTubeAnnouncements() {
        try {
            ytannouncements.clear();
            for (Guild guild : Main.getAllGuilds()) {
                ytannouncements.put(guild, getFileContents(guild, "ytchannels.cfg"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        initHugs();
        initWilliam();
        initFriendship();
        initSlap();
        updateYouTubeAnnouncements();
        updateytchannels();
        updateYTMessage();
    }

    //Hug gif storage
    public static List<String> HUGS = new ArrayList<>();
    private static void initHugs() {
        HUGS.add("https://media.giphy.com/media/3M4NpbLCTxBqU/giphy.gif");
        HUGS.add("https://media.giphy.com/media/jMGxhWR7rtTNu/giphy.gif");
        HUGS.add("https://media1.giphy.com/media/Bj9k1U69GZ8Iw/giphy.gif");
        HUGS.add("https://media.giphy.com/media/EvYHHSntaIl5m/giphy.gif");
        HUGS.add("http://tsdeluxe.com/hostedmedia/gifs/hugs/d9DQ6_s-200x150.gif");
        HUGS.add("http://tsdeluxe.com/hostedmedia/gifs/hugs/giphy.gif");
        HUGS.add("http://tsdeluxe.com/hostedmedia/gifs/hugs/giphy_1.gif");
        HUGS.add("http://tsdeluxe.com/hostedmedia/gifs/hugs/gw3hsqe.gif");
        HUGS.add("http://tsdeluxe.com/hostedmedia/gifs/hugs/pLIqhgD.gif");
        HUGS.add("http://tsdeluxe.com/hostedmedia/gifs/hugs/spiritedaway1.gif");
        HUGS.add("https://media0.giphy.com/media/42YlR8u9gV5Cw/giphy.gif");
        HUGS.add("https://media.giphy.com/media/GXFDStd2CP1ba/giphy.gif");
        HUGS.add("https://media.giphy.com/media/11QQfJOKlh739e/giphy.gif");
        HUGS.add("https://media.giphy.com/media/x90dwDUuUx9Ys/giphy.gif");
        HUGS.add("https://media.giphy.com/media/juG2hdQUFo06Y/giphy.gif");
        HUGS.add("https://media.giphy.com/media/gl8ymnpv4Sqha/giphy.gif");
        HUGS.add("https://media.giphy.com/media/f6y4qvdxwEDx6/giphy.gif");
        HUGS.add("https://media1.giphy.com/media/QbkL9WuorOlgI/giphy.gif");
        HUGS.add("https://media.giphy.com/media/gnXG2hODaCOru/giphy.gif");
        HUGS.add("https://media.giphy.com/media/WQ9l85vIyhRV6/giphy.gif");
    }

    //William gif storage
    public static List<String> WILLIAM = new ArrayList<>();
    private static void initWilliam() {
        WILLIAM.add("http://tsdeluxe.com/hostedmedia/gifs/william/WilliamG.gif");
        WILLIAM.add("https://media.giphy.com/media/VZ5gRT17YNkn6/giphy.gif");
        WILLIAM.add("https://media.giphy.com/media/yr7n0u3qzO9nG/giphy.gif");
        WILLIAM.add("http://tsdeluxe.com/hostedmedia/gifs/william/2155a2020dfb87dc35601cadbb5675f5.gif");
        WILLIAM.add("http://tsdeluxe.com/hostedmedia/gifs/william/giphy%20(2).gif");
        WILLIAM.add("http://tsdeluxe.com/hostedmedia/gifs/william/giphy(1).gif");
        WILLIAM.add("http://tsdeluxe.com/hostedmedia/gifs/william/tumblr_m62y2aCEk11qkn7p6o1_500.gif");
        WILLIAM.add("https://media0.giphy.com/media/3o6UBpHgaXFDNAuttm/giphy.gif");
        WILLIAM.add("https://media1.giphy.com/media/l2QEgWxqxI2WJCXpC/giphy.gif");
        WILLIAM.add("https://media3.giphy.com/media/l378m8mNCUdjA3Euk/giphy.gif");
        WILLIAM.add("https://media.giphy.com/media/1JSmbDdelsWvC/giphy.gif");
        WILLIAM.add("https://media.giphy.com/media/15a78dCc2ESIw/giphy.gif");
        WILLIAM.add("https://media.giphy.com/media/134vVkHV9wQtaw/giphy.gif");
        WILLIAM.add("https://media.giphy.com/media/sBXHjZjmi8VrO/giphy.gif");
    }

    //Friendship gif storage
    public static List<String> FRIENDSHIP = new ArrayList<>();
    private static void initFriendship() {
        FRIENDSHIP.add("http://tsdeluxe.com/hostedmedia/gifs/friendship/boo.gif");
        FRIENDSHIP.add("http://tsdeluxe.com/hostedmedia/gifs/friendship/boo2.gif");
        FRIENDSHIP.add("http://tsdeluxe.com/hostedmedia/gifs/friendship/boo3.gif");
    }

    public static List<String> SLAP = new ArrayList<>();
    private static void initSlap() {
        SLAP.add("https://media.giphy.com/media/uqSU9IEYEKAbS/giphy.gif");
        SLAP.add("https://media.giphy.com/media/KhVMxKaA0TENW/giphy.gif");
        SLAP.add("https://media.giphy.com/media/l3q2w9gMriB1I6Hrq/giphy.gif");
        SLAP.add("https://media.giphy.com/media/sn0HmkBFe2Juo/giphy.gif");
        SLAP.add("https://media.giphy.com/media/3XlEk2RxPS1m8/giphy.gif");
        SLAP.add("https://media.giphy.com/media/3o7TKKcqcp1iESzeak/giphy.gif");
        SLAP.add("https://media.giphy.com/media/gSIz6gGLhguOY/giphy.gif");
        SLAP.add("https://media.giphy.com/media/iIPI1tpT9HcUE/giphy.gif");
        SLAP.add("https://media.giphy.com/media/s5zXKfeXaa6ZO/giphy.gif");
        SLAP.add("https://media.giphy.com/media/q8AiNhQJVyDoQ/giphy.gif");
        SLAP.add("https://media.giphy.com/media/gBW8Qgfaa2ije/giphy.gif");
        SLAP.add("https://media.giphy.com/media/So9h5kwOmq3Uk/giphy.gif");
        SLAP.add("https://media.giphy.com/media/pzTUvy9yDInn2/giphy.gif");
        SLAP.add("https://media.giphy.com/media/ewHSMEx2TtEo8/giphy.gif");
        SLAP.add("https://media.giphy.com/media/v0tBeMcKMdpUQ/giphy.gif");
        SLAP.add("https://media.giphy.com/media/fNdolDfnVPKNi/giphy.gif");
    }
}
