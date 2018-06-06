package com.fabbe50.teemobeats.music;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 23/09/2017 - 11:36 AM.
 */
public class Music {
    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static String youtubeAPIKey;

    public static AudioPlayerManager playerManager;
    public static Map<Long, GuildMusicManager> musicManagers;

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.computeIfAbsent(guildId, k -> new GuildMusicManager(playerManager));

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void readPlaylist(final TextChannel channel, Member member, final String playlistURL, String channelName) throws IOException {
        System.out.println(playlistURL);
        if (!new File(playlistURL).exists()) {
            channel.sendMessage("Playlist doesn't exist!").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            return;
        }
        Path path = FileSystems.getDefault().getPath(playlistURL);
        List<String> lines = Files.readAllLines(path);
        channel.sendMessage("Starting playlist: " + new File(playlistURL).getName().replace(".m3u", "")).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        for (String s : lines) {
            if (s.contains("https://"))
                loadAndPlay(channel, member, s, channelName, false);
        }
    }

    public void search(final TextChannel channel, Member member, final String trackName, String channelName) {
        try {
            YouTube youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, httpRequest -> {
            }).setApplicationName("teemo-beats").build();

            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(youtubeAPIKey);
            search.setQ(trackName);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            long NUMBER_OF_VIDEOS_RETURNED = 1;
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();
            if (results != null) {
                try {
                    loadAndPlay(channel, member, "https://www.youtube.com/watch?v=" + results.get(0).getId().getVideoId(), channelName);
                }
                catch (Exception e) {
                    channel.sendMessage("Couldn't find anything for: " + trackName).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
            }
            else {
                channel.sendMessage("Couldn't find anything for: " + trackName).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void loadAndPlay(final TextChannel channel, Member member, final String trackUrl, String channelName) {
        loadAndPlay(channel, member, trackUrl, channelName, true);
    }

    public void loadAndPlay(final TextChannel channel, Member member, final String trackUrl, String channelName, boolean logSong) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (isVoiceChatEmpty(channel.getGuild().getAudioManager(), channelName)) {
            channel.sendMessage("I refuse to be alone! Join a voice channel first.").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setColor(new Color(138, 43, 226));

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (logSong) {
                    builder.setTitle("Adding to queue");
                    builder.setDescription(track.getInfo().title);
                    String[] s = trackUrl.split("=", 2);
                    if (s.length == 2 && trackUrl.contains("youtu"))
                        builder.setThumbnail("https://img.youtube.com/vi/" + s[1] + "/sddefault.jpg");
                    channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }

                List<AudioTrack> temp = new ArrayList<>();
                if (Main.session.get(channel.getGuild()) != null)
                    if (!Main.session.get(channel.getGuild()).isEmpty())
                        temp = Main.session.get(channel.getGuild());
                temp.add(track);
                Main.session.put(channel.getGuild(), temp);

                play(channel.getGuild(), channel, musicManager, track, channelName);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                    if (logSong) {
                        builder.setTitle("Adding playlist to queue");
                        builder.setDescription("**Playlist name:** " + playlist.getName());
                        builder.appendDescription("\nContains " + playlist.getTracks().size() + " songs.");
                        channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                    List<AudioTrack> temp = new ArrayList<>();
                    if (Main.session.get(channel.getGuild()) != null)
                        if (!Main.session.get(channel.getGuild()).isEmpty())
                            temp = Main.session.get(channel.getGuild());
                    temp.add(firstTrack);
                    Main.session.put(channel.getGuild(), temp);
                    play(channel.getGuild(), channel, musicManager, firstTrack, channelName);
                    for (AudioTrack track : playlist.getTracks()) {
                        if (!track.equals(firstTrack)) {
                            List<AudioTrack> temp1 = new ArrayList<>();
                            if (Main.session.get(channel.getGuild()) != null)
                                if (!Main.session.get(channel.getGuild()).isEmpty())
                                    temp1 = Main.session.get(channel.getGuild());
                            temp1.add(track);
                            Main.session.put(channel.getGuild(), temp1);
                            play(channel.getGuild(), channel, musicManager, track, channelName);
                        }
                    }
                } else {
                    if (logSong) {
                        builder.setTitle("Adding playlist to queue");
                        builder.setDescription("**Playlist name:** " + playlist.getName());
                        builder.appendDescription("\n**Starting with:** " + firstTrack.getInfo().title);
                        builder.appendDescription("\nContains " + playlist.getTracks().size() + " songs.");
                        channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                    }
                    List<AudioTrack> temp = new ArrayList<>();
                    if (Main.session.get(channel.getGuild()) != null)
                        if (!Main.session.get(channel.getGuild()).isEmpty())
                            temp = Main.session.get(channel.getGuild());
                    temp.add(firstTrack);
                    Main.session.put(channel.getGuild(), temp);
                    play(channel.getGuild(), channel, musicManager, firstTrack, channelName);
                    for (AudioTrack track : playlist.getTracks()) {
                        if (!track.equals(firstTrack)) {
                            List<AudioTrack> temp1 = new ArrayList<>();
                            if (Main.session.get(channel.getGuild()) != null)
                                if (!Main.session.get(channel.getGuild()).isEmpty())
                                    temp1 = Main.session.get(channel.getGuild());
                            temp1.add(track);
                            Main.session.put(channel.getGuild(), temp1);
                            play(channel.getGuild(), channel, musicManager, track, channelName);
                        }
                    }
                }
            }

            @Override
            public void noMatches() {
                if (logSong) {
                    builder.setTitle("Adding song to queue");
                    builder.setDescription("Nothing found by " + trackUrl);
                    channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                if (logSong) {
                    builder.setTitle("Adding song to queue");
                    builder.setDescription("Could not play: " + exception.getMessage());
                    channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                }
            }
        });
    }

    private void play(Guild guild, TextChannel tchannel, GuildMusicManager musicManager, AudioTrack track, String channel) {
        connectToFirstVoiceChannel(guild.getAudioManager(), tchannel, channel);

        musicManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel, Member member) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setColor(new Color(138, 43, 226));
        if (!musicManager.scheduler.getQueue().isEmpty()) {
            musicManager.scheduler.nextTrack();
            builder.setTitle("Skip song");
            builder.setDescription("**Skipped to next track:** " + musicManager.player.getPlayingTrack().getInfo().title);
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        else {
            builder.setTitle("Skip song");
            builder.setDescription("**Nothing in queue!");
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    public void stop(TextChannel channel, Member member) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setColor(new Color(138, 43, 226));
        if (musicManager.player.getPlayingTrack() != null) {
            builder.setTitle("Stopping music");
            builder.setDescription("__**This session:**__\n");
            builder.appendDescription("**Songs played:** " + (Main.session.get(channel.getGuild()).size() - musicManager.scheduler.getQueue().size()));
            builder.appendDescription("\n**Songs left:** " + musicManager.scheduler.getQueue().size());
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            musicManager.scheduler.stop(channel.getGuild());
            disconnectVoice(channel.getGuild().getAudioManager());
        } else if (channel.getGuild().getAudioManager().isConnected()) {
            builder.setTitle("Disconnecting");
            builder.setDescription("__**This session:**__\n");
            builder.appendDescription("**Songs played:** " + (Main.session.get(channel.getGuild()).size() - musicManager.scheduler.getQueue().size()));
            builder.appendDescription("\n**Songs left:** " + musicManager.scheduler.getQueue().size());
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            disconnectVoice(channel.getGuild().getAudioManager());
        } else {
            channel.sendMessage("Nothing is playing.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    public void forceStop(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.stop(guild);
        disconnectVoice(guild.getAudioManager());
    }

    public void clear(TextChannel channel, Member member) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setColor(new Color(138, 43, 226));
        if (!Main.session.isEmpty()) {
            builder.setTitle("Clearing queue");
            builder.appendDescription("Cleared " + Main.session.size() + " songs.");
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            musicManager.scheduler.clear(channel.getGuild());
            if (channel.getGuild().getAudioManager().isConnected())
                disconnectVoice(channel.getGuild().getAudioManager());
        } else {
            channel.sendMessage("Nothing in queue.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    public void remove(TextChannel channel, int i) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        int i2 = musicManager.scheduler.getQueue().size();
        i--;
        int i1 = 0;
        if (musicManager.scheduler.getQueue().isEmpty()) {
            channel.sendMessage("Queue is empty!").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        for (AudioTrack track : musicManager.scheduler.getQueue()) {
            if (i1 == i) {
                musicManager.scheduler.getQueue().remove(track);
                Main.session.remove(track);
                channel.sendMessage(track.getInfo().title + " removed!").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                break;
            }
            i1++;
        }
        if (i2 == musicManager.scheduler.getQueue().size() && !musicManager.scheduler.getQueue().isEmpty()) {
            i++;
            channel.sendMessage("No track found at: " + i).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    public void getQueue(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        String message2 = "";
        message2 = message2 + "Current queue: \n";
        for (AudioTrack track : queue) {
            if (2000 < (message2 + track.getInfo().title + " by " + track.getInfo().author).length()) {
                channel.sendMessage("```" + message2 + "```").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
                message2 = "";
            }
            message2 = message2 + track.getInfo().title + " by " + track.getInfo().author + "\n";
        }
        if (!Objects.equals(message2.substring(16), "")) {
            channel.sendMessage("```" + message2 + "```").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        else
            channel.sendMessage("Queue is empty!").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
    }

    public void getQueue(TextChannel channel, int i) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        String message2 = "";
        message2 = message2 + "Current queue:\n";
        int j = 0;
        for (AudioTrack track : queue) {
            if (j == i) break;
            message2 = message2 + track.getInfo().title + " by " + track.getInfo().author + "\n";
            j++;
        }
        if (!Objects.equals(message2.substring(16), "")) {
            channel.sendMessage("```" + message2 + "```").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        else
            channel.sendMessage("Queue is empty!").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
    }

    public void current(TextChannel channel, Member member) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
        builder.setColor(new Color(138, 43, 226));
        builder.setTitle("Currently playing");

        if (track != null) {
            builder.setDescription("Title: " + track.getInfo().title);
            builder.appendDescription("\nAuthor: " + track.getInfo().author);
            builder.appendDescription("\nLength: " + Utils.getFormattedTimeInMinutes(track.getInfo().length));
            String[] s = track.getInfo().uri.split("=", 2);
            if (s.length == 2 && track.getInfo().uri.contains("youtu"))
                builder.setThumbnail("https://img.youtube.com/vi/" + s[1] + "/sddefault.jpg");
            builder.appendDescription("\nURL: " + track.getInfo().uri);
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(track.getInfo().length - track.getPosition(), TimeUnit.MILLISECONDS));
        } else {
            builder.setDescription("Nothing playing");
            channel.sendMessage(builder.build()).queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    public void lyrics(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (track == null) {
            channel.sendMessage("Nothing playing right now!").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
            return;
        }

        String trackName = track.getInfo().title;
        String artistName = track.getInfo().author;

        if (track.getInfo().title.split(" - ").length >= 2) {
            String modifiedTrackName = track.getInfo().title;

            while (true) {
                if (modifiedTrackName.contains("[")) {
                    modifiedTrackName = modifiedTrackName.replace(modifiedTrackName.substring(modifiedTrackName.indexOf("["), modifiedTrackName.indexOf("]")+1), "");
                }
                else
                    break;
            }

            while (true) {
                if (modifiedTrackName.contains("(")) {
                    modifiedTrackName = modifiedTrackName.replace(modifiedTrackName.substring(modifiedTrackName.indexOf("("), modifiedTrackName.indexOf(")")+1), "");
                }
                else
                    break;
            }

            if (modifiedTrackName.split(" - ").length == 3) {
                String[] temp = modifiedTrackName.split(" - ");
                if (Objects.equals(temp[0], "")) {
                    modifiedTrackName = temp[1] + " - " + temp[2];
                }
            }

            if (modifiedTrackName.contains(" & ")) {
                modifiedTrackName = modifiedTrackName.replace(" & ", " ");
            }

            if (modifiedTrackName.contains(" ft. ")) {
                modifiedTrackName = modifiedTrackName.replace(" ft. ", " feat ");
            }
            if (modifiedTrackName.contains(" feat. ")) {
                modifiedTrackName = modifiedTrackName.replace(" feat. ", " feat ");
            }
            if (modifiedTrackName.contains(" Ft. ")) {
                modifiedTrackName = modifiedTrackName.replace(" Ft. ", " feat ");
            }
            if (modifiedTrackName.contains(" Feat. ")) {
                modifiedTrackName = modifiedTrackName.replace(" Feat. ", " feat ");
            }

            lyrics(channel, modifiedTrackName, track);
        }
    }

    public void lyrics(TextChannel channel, String url) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (!url.contains("http://www.songlyrics.com")) {
            channel.sendMessage("Invalid link: source must be \"http://www.songlyrics.com/\"").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        lyrics(channel, url, track);
    }

    public void lyrics(TextChannel channel, String query, AudioTrack track) {
        String trackName = "";
        String artistName = "";

        if (!query.contains("http://")) {
            String[] aQuery = query.split(" - ");

            trackName = aQuery[0];
            artistName = aQuery[1];
        }

        try {
            List<String> lyrics;
            if (query.contains("http://www.songlyrics.com"))
                lyrics = com.fabbe50.teemobeats.music.Lyrics.getSongLyrics(query);
            else
                lyrics = com.fabbe50.teemobeats.music.Lyrics.getSongLyrics(artistName, trackName);
            String lyricsSheet = "";
            for (String s : lyrics) {
                lyricsSheet = lyricsSheet + s;
            }
            String[] split = lyricsSheet.split("\n\n");
            for (String s : split) {
                try {
                    if (track != null) {
                        channel.sendMessage("```" + s + "```").queue(message -> message.delete().queueAfter(track.getDuration() - (track.getPosition() - 10000), TimeUnit.MILLISECONDS));
                    } else {
                        channel.sendMessage("```" + s + "```").queue(message -> message.delete().queueAfter(5, TimeUnit.MINUTES));
                    }
                } catch (Exception e) {
                    channel.sendMessage("Exception: lyrics formatting wasn't possible.").queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
                    if (query.contains("http://"))
                        channel.sendMessage("Read lyrics in browser here: " + query).queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
                    else
                        channel.sendMessage("Read lyrics in browser here: http://www.songlyrics.com/" + artistName.replace(" ", "-").toLowerCase() + "/"+trackName.replace(" ", "-").toLowerCase() + "-lyrics/").queue(message -> message.delete().queueAfter(20, TimeUnit.SECONDS));
                }
            }
        } catch (IOException e) {
            channel.sendMessage("Couldn't find lyrics for this song.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
    }

    public void createPlaylist(TextChannel channel, String name, List<AudioTrack> tracks) {
        Guild guild = channel.getGuild();
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists").exists())
            new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists").mkdir();
        File list = new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists\\" + name + ".m3u");
        List<String> contents = new ArrayList<>();
        contents.add("#EXTM3U");
        for (AudioTrack track : tracks) {
            if (track == null)
                continue;
            StringBuffer songInfo = new StringBuffer("#EXTINF:");
            songInfo.append(track.getInfo().length / 1000);
            songInfo.append(", ");
            songInfo.append(track.getInfo().author);
            songInfo.append(" - ");
            songInfo.append(track.getInfo().title);
            contents.add(songInfo.toString());
            contents.add(track.getInfo().uri);
        }
        savePlaylist(contents, list);
        channel.sendMessage("Playlist " + name + " has been saved.").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
    }

    private void savePlaylist(List<String> contents, File list) {
        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(list));
            for (String content : contents) {
                output.write(content);
                output.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null)
                try {
                    output.flush();
                    output.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
        }
    }

    public void retrievePlaylistDir(TextChannel channel) {
        Guild guild = channel.getGuild();
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists").exists()) {
            return;
        }
        File folder = new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists");
        File[] list = folder.listFiles(pathname -> pathname.getName().contains(".m3u"));
        StringBuilder message2 = new StringBuilder("```Stored playlists: \n");
        if (list != null && list.length > 0) {
            for (File file : list) {
                message2.append(file.getName()).append("\n");
            }
            channel.sendMessage(message2 + "```").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
        }
        else
            channel.sendMessage("`No playlists are stored.`").queue(message -> message.delete().queueAfter(30, TimeUnit.SECONDS));
    }

    public void getPlaylist(TextChannel channel, String playlistName) {
        Guild guild = channel.getGuild();
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists").exists()) {
            return;
        }
        File folder = new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists");
        File[] list = folder.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.getName().equalsIgnoreCase(playlistName + ".m3u")) {
                    channel.sendFile(f, playlistName + ".m3u", null).queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                }
            }
        }
    }

    public void removePlaylist(TextChannel channel, String playlistName) {
        Guild guild = channel.getGuild();
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists").exists())
            return;
        if (!new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists\\backup").exists())
            new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists\\backup").mkdir();
        File folder = new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists");
        File[] list = folder.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.getName().equalsIgnoreCase(playlistName + ".m3u")) {
                    try {
                        FileUtils.copyFile(f, new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\playlists\\backup\\" + f.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    f.delete();
                    channel.sendMessage("Playlist `" + f.getName() + "` deleted.").queue(message -> message.delete().queueAfter(15, TimeUnit.SECONDS));
                }
            }
        }
    }

    public void setVolume(TextChannel channel, int volume) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.player.setVolume(volume);
    }

    private static void disconnectVoice(AudioManager audioManager) {
        if (audioManager.isConnected() || audioManager.isAttemptingToConnect()) {
            audioManager.closeAudioConnection();
        }
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager, TextChannel tchannel, String channel) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (voiceChannel.getName().equalsIgnoreCase(channel) || channel.equals("")) {
                    audioManager.openAudioConnection(voiceChannel);
                    break;
                }
            }
        }
    }

    private static boolean isVoiceChatEmpty(AudioManager audioManager, String channel) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (voiceChannel.getName().equalsIgnoreCase(channel) || channel.equals("")) {
                    if (voiceChannel.getMembers().isEmpty())
                        return true;
                }
            }
        }
        return false;
    }
}

