package com.fabbe50.teemobeats;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 26/09/2017 - 6:24 PM.
 */
public class Utils {
    public static boolean isInteger(String str) {
        int size = str.length();

        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }

        return size > 0;
    }

    public static String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public static String addLinebreaks(String input, int maxLineLength) {
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken() + " ";

            if (lineLen + word.length() > maxLineLength) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word);
            lineLen += word.length();
        }
        return output.toString();
    }

    public static boolean isMemberInGuild(TextChannel channel, String name) {
        for (Member member : channel.getGuild().getMembers()) {
            if (member.getEffectiveName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static String getFormattedTimeInMinutes(long time) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time);
        seconds = seconds - (minutes * 60);
        return minutes + ":" + seconds;
    }

    public static int clampTo8Bit(int v) {
        if ((v & ~0xFF) != 0) {
            v = ((~v) >> 31) & 0xFF;
        }
        return v;
    }

    public static int RGBAclamped(int r, int g, int b, int a) {
        if (((r | g | b | a) & ~0xFF) != 0) {
            r = clampTo8Bit(r);
            g = clampTo8Bit(g);
            b = clampTo8Bit(b);
            a = clampTo8Bit(a);
        }
        return (a << 24) + (r << 16) + (g << 8) + (b << 0);
    }
}
