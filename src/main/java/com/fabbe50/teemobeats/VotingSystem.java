package com.fabbe50.teemobeats;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 24/09/2017 - 6:23 PM.
 */
class VotingSystem {
    static String topic = "";
    private static int[] responses;
    static String[] options;
    static List<Member> voted = new ArrayList<>();
    private static int voteMax;

    static void createPoll(TextChannel channel, String topic, String... resp) {
        createPoll(channel, topic, -1, resp);
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

        channel.sendMessage("```" + composedQuestion + "```").queue();
    }

    static int[] putVote(TextChannel channel, int i, Member member) {
        if (voteMax > voted.size() || voteMax == -1) {
            if (voted.isEmpty()) {
                if (responses.length >= i) {
                    channel.sendMessage(member.getEffectiveName() + " has voted!").queue();
                    //channel.sendMessage("Voted for " + options[i] + "!").queue();
                    responses[i]++;
                    voted.add(member);
                }
            } else {
                if (responses.length >= i && !voted.contains(member)) {
                    channel.sendMessage(member.getEffectiveName() + " has voted!").queue();
                    //channel.sendMessage("Voted for " + options[i] + "!").queue();
                    responses[i]++;
                    voted.add(member);
                } else {
                    channel.sendMessage("You've already voted!").queue();
                }
            }
        }
        System.out.println(voteMax);
        if (voteMax == voted.size()) {
            return endPoll(channel);
        }
        return new int[0];
    }

    static int[] endPoll(TextChannel channel) {
        String composedResults = "Results for topic: " + topic + ".\n" + voted.size() + " people voted!\n";

        int index = 0;
        for (String s : options) {
            composedResults = composedResults + s + ": " + responses[index] + "\n";
            index++;
        }

        channel.sendMessage("```" +
                composedResults +
                "```").queue();


        int[] temp;
        temp = responses.clone();
        responses = null;
        options = null;
        topic = "";
        voted.clear();

        return temp;
    }
}
