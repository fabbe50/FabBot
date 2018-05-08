package com.fabbe50.teemobeats.handlers;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 07/05/2018 - 10:00 PM.
 */
public class LoggingHandler {
    private static List<Message> logEntries = new ArrayList<>();
    public LoggingHandler() {
        Runnable logger = () -> {
            while (true) {
                if (!logEntries.isEmpty()) {
                    for (Message message : logEntries) {
                        System.out.println("DiscordMessage: ['" + message.getMember().getEffectiveName() + "' in '" + message.getTextChannel().getName() + "` in guild `" + message.getGuild().getName() + "'] " + message.getContentRaw());
                        if (!message.getEmbeds().isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            for (MessageEmbed embed : message.getEmbeds()) {
                                if (embed.getAuthor() != null) {
                                    builder.append("Author: [").append(embed.getAuthor().getName()).append(", ").append(embed.getAuthor().getUrl()).append(", ").append(embed.getAuthor().getIconUrl()).append("]\n");
                                }
                                if (embed.getColor() != null) {
                                    builder.append("Color: ").append(embed.getColor().getRGB()).append("\n");
                                }
                                if (embed.getTitle() != null) {
                                    builder.append("Title: ").append(embed.getTitle()).append("\n");
                                }
                                if (embed.getDescription() != null) {
                                    builder.append("Description: ").append(embed.getDescription()).append("\n");
                                }
                                if (embed.getImage() != null) {
                                    builder.append("Image: ").append(embed.getImage().getUrl()).append("\n");
                                }
                                if (embed.getThumbnail() != null) {
                                    builder.append("Thumbnail: ").append(embed.getThumbnail().getUrl()).append("\n");
                                }
                                if (!embed.getFields().isEmpty()) {
                                    for (MessageEmbed.Field field : embed.getFields()) {
                                        builder.append("Field: ").append(field.getName()).append("\n");
                                    }
                                }
                                if (embed.getFooter() != null) {
                                    builder.append("Footer: [").append(embed.getFooter().getIconUrl()).append(", ").append(embed.getFooter().getText()).append("]");
                                }
                            }
                            System.out.println("DiscordEmbed: ['" + message.getMember().getEffectiveName() + "' in '" + message.getTextChannel().getName() + "` in guild `" + message.getGuild().getName() + "']: {\n" + builder + "\n}");
                        }
                    }
                    logEntries.clear();
                }
            }
        };
        new Thread(logger).start();
    }

    public static void addLogEntry(Message message) {
        logEntries.add(message);
    }
}
