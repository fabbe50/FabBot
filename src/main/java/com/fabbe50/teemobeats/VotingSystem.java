package com.fabbe50.teemobeats;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by fabbe on 24/09/2017 - 6:23 PM.
 */
class VotingSystem {
    static String topic = "";
    private static int[] responses;
    static String[] options;
    static List<Member> voted = new ArrayList<>();
    private static int voteMax;
    private static Message messageQuery;

    static void createPoll(TextChannel channel, String topic, String... resp) {
        createPoll(channel, topic, 0, resp);
    }

    static void createPoll(TextChannel channel, String topic, int voteMax, String... resp) {
        responses = new int[resp.length];
        options = resp.clone();
        VotingSystem.topic = topic;
        VotingSystem.voted.clear();
        VotingSystem.voteMax = voteMax - 1;

        String composedQuestion = "Topic: " + VotingSystem.topic + "\n";

        int index = 1;
        for (String s : resp) {
            composedQuestion = composedQuestion + index + ". " + s + "\n";
            index++;
        }

        channel.sendMessage("```" + composedQuestion + "```").queue(VotingSystem::saveMessage);
    }

    private static void saveMessage(Message message) {
        messageQuery = message;
    }

    static int[] putVote(TextChannel channel, int i, Member member) {
        if (voteMax > voted.size() || voteMax == -1) {
            if (voted.isEmpty()) {
                if (responses.length >= i) {
                    channel.sendMessage(member.getEffectiveName() + " has voted!").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                    responses[i]++;
                    voted.add(member);
                }
            } else {
                if (responses.length >= i && !voted.contains(member)) {
                    channel.sendMessage(member.getEffectiveName() + " has voted!").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                    responses[i]++;
                    voted.add(member);
                } else {
                    channel.sendMessage("@" + member.getEffectiveName() + ", You've already voted!").queue(message -> message.delete().queueAfter(5, TimeUnit.SECONDS));
                }
            }
        }
        if (voteMax == voted.size()) {
            return endPoll(channel);
        }
        return new int[0];
    }

    static int[] endPoll(TextChannel channel) {
        String composedResults = "Results for topic: " + topic + "\n" + voted.size() + " people voted!\n";

        int index = 0;
        for (String s : options) {
            composedResults = composedResults + s + ": " + responses[index] + "\n";
            index++;
        }

        channel.sendMessage("```" +
                composedResults +
                "```").queue(message -> message.delete().queueAfter(5, TimeUnit.MINUTES));


        int[] temp;
        temp = responses.clone();
        responses = null;
        options = null;
        topic = "";
        voted.clear();
        messageQuery.delete().queue();

        return temp;
    }

    static void voteCommand(TextChannel channel, String[] command, Member member) {
        if (VotingSystem.options != null) {
            Music music = Main.music;
            List<String> options = new ArrayList<>();
            options.addAll(Arrays.asList(VotingSystem.options));
            if (VotingSystem.topic.equals("Skip?") && options.contains(command[1])) {
                music.skipVote(channel, VotingSystem.putVote(channel, options.indexOf(command[1]), member));
            } else if (VotingSystem.topic.equals("Skip?") && Utils.isInteger(command[1])) {
                music.skipVote(channel, VotingSystem.putVote(channel, (Integer.parseInt(command[1]) - 1), member));
            } else if (VotingSystem.topic.equals("Stop?") && options.contains(command[1])) {
                music.stopVote(channel, VotingSystem.putVote(channel, options.indexOf(command[1]), member));
            } else if (VotingSystem.topic.equals("Stop?") && Utils.isInteger(command[1])) {
                music.stopVote(channel, VotingSystem.putVote(channel, (Integer.parseInt(command[1]) - 1), member));
            } else if (options.contains(command[1])) {
                VotingSystem.putVote(channel, options.indexOf(command[1]), member);
            } else if (Utils.isInteger(command[1])) {
                VotingSystem.putVote(channel, (Integer.parseInt(command[1]) - 1), member);
            } else if (command[1].equals("end")) {
                VotingSystem.endPoll(channel);
            }
        }
        else
            VotingSystem.createPoll(channel, command[1], command[2].split(","));
    }
}
