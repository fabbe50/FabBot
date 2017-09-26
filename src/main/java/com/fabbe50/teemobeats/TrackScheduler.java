package com.fabbe50.teemobeats;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
    static JDA jda;

    TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    void queue(AudioTrack track) {
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
        jda.getPresence().setGame(Game.of(player.getPlayingTrack().getInfo().title, player.getPlayingTrack().getInfo().uri));
    }

    void nextTrack() {
        player.startTrack(queue.poll(), false);
        if (player.getPlayingTrack() != null)
            jda.getPresence().setGame(Game.of(player.getPlayingTrack().getInfo().title, player.getPlayingTrack().getInfo().uri));
    }

    void stop() {
        player.stopTrack();
        queue.clear();
        jda.getPresence().setGame(Game.of(".help"));
    }

    BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        jda.getPresence().setGame(Game.of(".help"));
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}
