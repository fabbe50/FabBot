package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Created by fabbe on 30/04/2018 - 8:40 PM.
 */
public class VoiceChatTimeoutHandler extends ListenerAdapter {
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        System.out.println("[" + this.getClass().getSimpleName() + "]: " + event.getMember().getEffectiveName() + " left voice channel: " + event.getChannelLeft().getName());
        if (event.getGuild().getAudioManager().isConnected()) {
            System.out.println("[" + this.getClass().getSimpleName() + "]: " + "I'm connected to voice.");
            if (event.getChannelLeft() == event.getGuild().getAudioManager().getConnectedChannel()) {
                System.out.println("[" + this.getClass().getSimpleName() + "]: " + "I belong to voice channel: " + event.getChannelLeft().getName());
                if (event.getChannelLeft().getMembers().size() == 1) {
                    System.out.println("[" + this.getClass().getSimpleName() + "]: " + "I'm alone.");
                    if (Main.music.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        System.out.println("[" + this.getClass().getSimpleName() + "]: " + "Stopping music and quitting.");
                        Main.music.forceStop(event.getGuild());
                    } else {
                        System.out.println("[" + this.getClass().getSimpleName() + "]: " + "No music is playing, quitting.");
                        event.getGuild().getAudioManager().closeAudioConnection();
                    }
                }
            }
        }
    }
}
