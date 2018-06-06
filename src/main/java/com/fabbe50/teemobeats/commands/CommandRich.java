package com.fabbe50.teemobeats.commands;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.Utils;
import com.fabbe50.teemobeats.interfaces.Command;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 08/05/2018 - 6:01 PM.
 */
public class CommandRich implements Command {
    @Override
    public String getName() {
        return "rich";
    }

    @Override
    public String shortDesc() {
        return "Check your presence!";
    }

    @Override
    public String group() {
        return "tools";
    }

    @Override
    public int permission() {
        return 0;
    }

    @Override
    public List<String> addDescription(List<String> desc) {
        desc.add("Get information about your rich presence.");
        return desc;
    }

    @Override
    public List<String> addUsage(List<String> usage) {
        usage.add("- Get presence.");
        return usage;
    }

    @Override
    public boolean execute(TextChannel channel, Guild guild, Member member, Message message) {
        if (message.getMentionedMembers().size() <= 1) {
            Member tagged = member;
            if (message.getMentionedMembers().size() == 1)
                tagged = message.getMentionedMembers().get(0);
            if (tagged.getGame() != null) {
                if (tagged.getGame().isRich()) {
                    EmbedBuilder builder;
                    RichPresence presence = tagged.getGame().asRichPresence();
                    if (presence.getName().equalsIgnoreCase("spotify")) {
                        builder = richSpotify(presence);
                    } else if (presence.getName().equalsIgnoreCase("league of legends")) {
                        builder = richLeague(presence);
                    } else {
                        builder = richDebug(presence);
                    }
                    if (builder != null) {
                        builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
                        channel.sendMessage(builder.build()).queue(message1 -> message1.delete().queueAfter(5, TimeUnit.MINUTES));
                    }
                }
                else
                    channel.sendMessage(tagged.getEffectiveName() + " is playing: " + tagged.getGame().getName()).queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
            } else {
                channel.sendMessage(tagged.getEffectiveName() + " isn't playing anything.").queue(message1 -> message1.delete().queueAfter(30, TimeUnit.SECONDS));
            }
        } else {
            channel.sendMessage("You can't tag more than one member.").queue(message1 -> message1.delete().queueAfter(15, TimeUnit.SECONDS));
        }
        return true;
    }

    private EmbedBuilder richSpotify(RichPresence presence) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Rich Presence - " + presence.getName());
        builder.setColor(new Color(132, 189, 0));
        String album = "unknown";
        if (presence.getLargeImage() != null) {
            builder.setThumbnail(presence.getLargeImage().getUrl());
            album = presence.getLargeImage().getText();
        }
        builder.addField("Details: ", "**Title:** " + presence.getDetails() + "\n**Artist:** " + presence.getState() + "\n**Album:** " + album, false);
        builder.addField("Links: ",
                "YouTube: " + getYouTubeLink(presence.getState() + " - " + presence.getDetails()) + "\n" +
                "Genius: " + getGeniusLink(presence.getState() + " - " + presence.getDetails())
                , false);

        return builder;
    }

    private EmbedBuilder richLeague(RichPresence presence) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Rich Presence - " + presence.getName());
        builder.setColor(new Color(191, 132, 38));
        String champ = "";
        if (presence.getLargeImage() != null) {
            builder.setThumbnail(presence.getLargeImage().getUrl());
            if (presence.getLargeImage().getText() != null)
                champ = "\n**Champion:** " + presence.getLargeImage().getText();
        }
        String time = "";
        if (presence.getTimestamps() != null)
            time = "\n**Elapsed Time:** " + Utils.getFormattedTimeInMinutes(presence.getTimestamps().getElapsedTime(ChronoUnit.MILLIS));
        builder.addField("Details: ", "**Type:** " + presence.getDetails() + "\n**Status:** " + presence.getState() + champ + time, false);
        return builder;
    }

    private EmbedBuilder richDebug(RichPresence presence) {
        EmbedBuilder builder = new EmbedBuilder();
        StringBuilder sbuilder = new StringBuilder();
        builder.setColor(new Color(204, 102, 153));
        builder.setTitle("Rich Presence - " + presence.getName());
        sbuilder.append("AppID: ").append(presence.getApplicationIdLong()).append("\n");
        sbuilder.append("State: ").append(presence.getState()).append("\n");
        sbuilder.append("Flags: ").append(presence.getFlags()).append("\n");
        sbuilder.append("SessionID: ").append(presence.getSessionId()).append("\n");
        if (presence.getTimestamps() != null) {
            sbuilder.append("Start Time: ").append(Utils.getFormattedTimeInDays(presence.getTimestamps().getStart())).append("\n");
            sbuilder.append("End Time: ").append(Utils.getFormattedTimeInMinutes(presence.getTimestamps().getEnd())).append("\n");
            sbuilder.append("Elapsed Time: ").append(Utils.getFormattedTimeInMinutes(presence.getTimestamps().getElapsedTime(ChronoUnit.MILLIS))).append("\n");
            sbuilder.append("Remaining Time: ").append(Utils.getFormattedTimeInMinutes(presence.getTimestamps().getRemainingTime(ChronoUnit.MILLIS))).append("\n");
        }
        builder.addField("Data: ", sbuilder.toString(), false);
        if (presence.getSmallImage() != null)
            builder.setThumbnail(presence.getSmallImage().getUrl());
        if (presence.getLargeImage() != null)
            builder.setImage(presence.getLargeImage().getUrl());
        if (presence.getParty() != null) {
            String partyBuilder = "ID: " + presence.getParty().getId() + "\n" + "Size: " + presence.getParty().getSize() + "/" + presence.getParty().getMax() + "\n";
            builder.addField("Party", partyBuilder, false);
        }
        builder.setDescription("Details: " + presence.getDetails());
        return builder;
    }

    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public String getYouTubeLink(final String trackName) {
        try {
            YouTube youtube = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, httpRequest -> {
            }).setApplicationName("teemo-beats").build();

            YouTube.Search.List search = youtube.search().list("id,snippet");

            search.setKey(Main.arguments[1]);
            search.setQ(trackName);
            search.setType("video");
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            long NUMBER_OF_VIDEOS_RETURNED = 1;
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse response = search.execute();
            List<SearchResult> results = response.getItems();
            if (results != null) {
                try {
                   return "http://youtu.be/" + results.get(0).getId().getVideoId();
                }
                catch (Exception e) {
                    return "null";
                }
            }
            else {
                return "null";
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
            return "null";
        }
    }

    public String getGeniusLink(String query) {
        String[] querySplit = query.split(" - ", 2);
        String[] artists = querySplit[0].split(";");

        return "https://genius.com/search?q=" + artists[0].replace(" ", "%20") + "%20" + querySplit[1].replace(" ", "%20");
    }
}
