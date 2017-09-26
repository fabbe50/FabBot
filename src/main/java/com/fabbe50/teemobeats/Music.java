package com.fabbe50.teemobeats;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
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
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.managers.AudioManager;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

/**
 * Created by fabbe on 23/09/2017 - 11:36 AM.
 */
class Music {
    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    static String youtubeAPIKey;

    AudioPlayerManager playerManager;
    Map<Long, GuildMusicManager> musicManagers;

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.computeIfAbsent(guildId, k -> new GuildMusicManager(playerManager));

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    void search(final TextChannel channel, final String trackName, String channelName) throws IOException {
        try {
            YouTube youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest httpRequest) throws IOException {
                }
            }).setApplicationName("teemo-beats").build();

            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(youtubeAPIKey);
            search.setQ(trackName);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            long NUMBER_OF_VIDEOS_RETURNED = 25;
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();
            if (results != null) {
                loadAndPlay(channel, "https://www.youtube.com/watch?v=" + results.get(0).getId().getVideoId(), channelName);
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }

    void loadAndPlay(final TextChannel channel, final String trackUrl, String channelName) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue: " + track.getInfo().title).queue();

                play(channel.getGuild(), musicManager, track, channelName);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                    channel.sendMessage("Adding playlist to queue: " + playlist.getName()).queue();
                    play(channel.getGuild(), musicManager, firstTrack, channelName);
                    for (AudioTrack track : playlist.getTracks()) {
                        if (!track.equals(firstTrack))
                            play(channel.getGuild(), musicManager, track, channelName);
                    }
                } else {
                    channel.sendMessage("Adding to queue: " + firstTrack.getInfo().title + " (current track of playlist: " + playlist.getName() + ")").queue();
                    play(channel.getGuild(), musicManager, firstTrack, channelName);
                }
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, String channel) {
        connectToFirstVoiceChannel(guild.getAudioManager(), channel);

        musicManager.scheduler.queue(track);
    }

    void skipVote(TextChannel channel, int[] votes) {
        if (votes[0] > votes[1]) {
            skipTrack(channel);
        }
    }

    void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        if (!musicManager.scheduler.getQueue().isEmpty()) {
            musicManager.scheduler.nextTrack();
            channel.sendMessage("Skipped to next track.").queue();
        }
        else {
            channel.sendMessage("Nothing in queue!").queue();
        }
    }

    void stopVote(TextChannel channel, int[] votes) {
        if (votes[0] > votes[1]) {
            stop(channel);
        }
    }

    void stop(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        if (musicManager.player.getPlayingTrack() != null) {
            channel.sendMessage("Stopping music.").queue();
            musicManager.scheduler.stop();
            disconnectVoice(channel.getGuild().getAudioManager());
        }
        else if (channel.getGuild().getAudioManager().isConnected()) {
            channel.sendMessage("No music is on. Disconnecting!").queue();
            disconnectVoice(channel.getGuild().getAudioManager());
        }
        else {
            channel.sendMessage("Nothing is playing.").queue();
        }
    }

    void remove(TextChannel channel, int i) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        int i2 = musicManager.scheduler.getQueue().size();
        i--;
        int i1 = 0;
        if (musicManager.scheduler.getQueue().isEmpty()) {
            channel.sendMessage("Queue is empty!").queue();
        }
        for (AudioTrack track : musicManager.scheduler.getQueue()) {
            if (i1 == i) {
                musicManager.scheduler.getQueue().remove(track);
                channel.sendMessage(track.getInfo().title + " removed!").queue();
                break;
            }
            i1++;
        }
        if (i2 == musicManager.scheduler.getQueue().size() && !musicManager.scheduler.getQueue().isEmpty()) {
            i++;
            channel.sendMessage("No track found at: " + i).queue();
        }
    }

    void getQueue(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        String message = "";
        for (AudioTrack track : queue) {
            if (2000 < (message + track.getInfo().title + " by " + track.getInfo().author).length()) {
                channel.sendMessage(message).queue();
                message = "";
            }
            message = message + track.getInfo().title + " by " + track.getInfo().author + "\n";
        }
        if (!Objects.equals(message, "")) {
            channel.sendMessage("Current queue: ").queue();
            channel.sendMessage(message).queue();
        }
        else
            channel.sendMessage("Queue is empty!").queue();
    }

    void getQueue(TextChannel channel, int i) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();
        String message = "";
        int j = 0;
        for (AudioTrack track : queue) {
            if (j == i) break;
            message = message + track.getInfo().title + " by " + track.getInfo().author + "\n";
            j++;
        }
        if (!Objects.equals(message, "")) {
            channel.sendMessage("Current queue: ").queue();
            channel.sendMessage(message).queue();
        }
        else
            channel.sendMessage("Queue is empty!").queue();
    }

    void current(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (track != null)
            channel.sendMessage("`Currently playing: " + track.getInfo().author + " - " + track.getInfo().title + "`").queue();
        else
            channel.sendMessage("`Currently playing: nothing`").queue();
    }

    void lyrics(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AudioTrack track = musicManager.player.getPlayingTrack();

        if (track == null) {
            channel.sendMessage("Nothing playing right now!").queue();
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

            lyrics(channel, modifiedTrackName);
        }
    }

    void lyrics(TextChannel channel, String query) {
        String[] aQuery = query.split(" - ");

        String trackName = aQuery[0];
        String artistName = aQuery[1];

        try {
            List<String> lyrics = com.fabbe50.teemobeats.Lyrics.getSongLyrics(artistName, trackName);
            String lyricsSheet = "";
            for (String s : lyrics) {
                lyricsSheet = lyricsSheet + s;
            }
            String[] split = lyricsSheet.split("\n\n");
            for (String s : split) {
                channel.sendMessage("```" + s + "```").queue();
            }
        } catch (IOException e) {
            channel.sendMessage("Couldn't find lyrics for this song.").queue();
        }
    }

    private static void disconnectVoice(AudioManager audioManager) {
        if (audioManager.isConnected() || audioManager.isAttemptingToConnect()) {
            audioManager.closeAudioConnection();
        }
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager, String channel) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            for (VoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
                if (voiceChannel.getName().equalsIgnoreCase(channel) || channel.equals("")) {
                    audioManager.openAudioConnection(voiceChannel);
                    break;
                }
            }
        }
    }
}

