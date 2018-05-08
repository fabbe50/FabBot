package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;
import com.google.gson.JsonObject;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 27/04/2018 - 8:46 PM.
 */
public class YTAnnounceHandler {
    private static List<String> activeStreams = new ArrayList<>();
    public YTAnnounceHandler() {
        Runnable announcer = () -> {
            while (true) {
                try {
                    System.out.println("[" + this.getClass().getSimpleName() + "]: Checking for live channels.");
                    for (Guild guild : Main.getAllGuilds()) {
                        System.out.println("[" + this.getClass().getSimpleName() + "]: Checking for live channels in guild: " + guild.getName() + ".");
                        List<String> strings = DataHandler.ytannouncements.get(guild);
                        if (strings != null && !strings.isEmpty()) {
                            for (String s : strings) {
                                System.out.println("[" + this.getClass().getSimpleName() + "]: Checking Channel: " + s);
                                StringBuilder builder = new StringBuilder();
                                builder.append("https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=").append(s).append("&order=date&type=video&eventType=live&key=").append(Main.arguments[1]);

                                String data;
                                BufferedReader reader = null;
                                try {
                                    URL url = new URL(builder.toString());
                                    reader = new BufferedReader(new InputStreamReader(url.openStream()));
                                    StringBuilder buffer = new StringBuilder();
                                    int read;
                                    char[] chars = new char[1024];
                                    while ((read = reader.read(chars)) != -1)
                                        buffer.append(chars, 0, read);
                                    data = buffer.toString();
                                } finally {
                                    if (reader != null)
                                        reader.close();
                                }
                                JSONObject json = new JSONObject(data);
                                if (json.getJSONObject("pageInfo").get("totalResults").equals(1) && !activeStreams.contains(s)) {
                                    activeStreams.add(s);
                                    EmbedBuilder builder1 = new EmbedBuilder();
                                    builder1.setColor(Color.RED);
                                    builder1.setTitle(json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").get("channelTitle").toString() + " is live!", "https://www.youtube.com/watch?v=" + json.getJSONArray("items").getJSONObject(0).getJSONObject("id").get("videoId"));
                                    builder1.setDescription("@everyone");
                                    if (DataHandler.ytmessage.get(guild) != null)
                                        builder1.appendDescription("\n" + DataHandler.ytmessage.get(guild));
                                    builder1.addField(json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").get("title").toString(), json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").get("description").toString(), false);
                                    builder1.setThumbnail(json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("default").get("url").toString());
                                    //builder1.setImage("https://www.youtube.com/watch?v=" + json.getJSONArray("items").getJSONObject(0).getJSONObject("id").get("videoId"));
                                    boolean foundChannel = false;
                                    for (TextChannel channel : guild.getTextChannels()) {
                                        if (channel.getName().equalsIgnoreCase(DataHandler.ytchannels.get(guild))) {
                                            channel.sendMessage("@everyone").queue(message -> message.delete().queue());
                                            channel.sendMessage(builder1.build()).queue();
                                            foundChannel = true;
                                            break;
                                        }
                                    }
                                    if (!foundChannel) {
                                        guild.getTextChannels().get(0).sendMessage("@everyone").queue(message -> message.delete().queue());
                                        guild.getTextChannels().get(0).sendMessage(builder1.build()).queue();
                                    }
                                } else if (json.getJSONObject("pageInfo").get("totalResults").equals(0) && activeStreams.contains(s)) {
                                    System.out.println("[" + this.getClass().getSimpleName() + "]: Channel: " + s + " is not live.");
                                    activeStreams.remove(s);
                                }
                            }
                        } else {
                            System.out.println("[" + this.getClass().getSimpleName() + "]: No live channels monitored.");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(announcer).start();
    }

    public static void clearActiveChannels() {
        activeStreams.clear();
    }
}
