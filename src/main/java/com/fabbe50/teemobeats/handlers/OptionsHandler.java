package com.fabbe50.teemobeats.handlers;

import net.dv8tion.jda.core.entities.Guild;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 06/04/2018 - 5:02 AM.
 */
public class OptionsHandler {
    public static void handleOptionsCommand() {

    }

    private static List<String> readConfig(Guild guild) throws IOException {
        return Files.readAllLines(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\config.cfg").toPath());
    }

    private static void writeConfig(Guild guild, List<String> lines) throws IOException {
        Files.write(new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\config.cfg").toPath(), lines, StandardOpenOption.WRITE);
    }

    private static void createConfig(Guild guild) {
        if (new File(System.getProperty("user.dir") + "\\data\\" + guild.getName().toLowerCase() + "\\config.cfg").exists()) {
            try {
                List<String> lines = readConfig(guild);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static List<String> createSettingsEntry(List<String> lines, String setting, String defaultSetting) {


        return lines;
    }
}
