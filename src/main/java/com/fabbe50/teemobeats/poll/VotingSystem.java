package com.fabbe50.teemobeats.poll;

import com.fabbe50.teemobeats.Main;
import com.fabbe50.teemobeats.poll.ObjectPoll;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.restaction.MessageAction;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 24/09/2017 - 6:23 PM.
 */
class VotingSystem {
    static List<ObjectPoll> polls = new ArrayList<>();
    static String[] numbers = new String[] {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":zero:"};
    static long[] uNumbers = new long[] {437419588569923626L, 437419883668439060L, 437419898818527243L, 437419915943739393L, 437419927188799519L, 437419937707982849L, 437419949439451151L, 437419962613628931L, 437419976685781004L, 437419994880540682L};

    public static boolean createPoll(TextChannel channel, Guild guild, Member member) {
        //Main.getJda().addEventListener(new reader(channel, guild, member));
        return true;
    }

    public static void savePoll(String question, List<String> answers) {
        polls.add(new ObjectPoll(question, answers));
    }

    public static List<ObjectPoll> getPolls() {
        return polls;
    }

    private static class reader extends ListenerAdapter {
        TextChannel channel;
        Guild guild;
        Member member;

        EmbedBuilder builder;
        Message message;
        boolean finish = false;

        String question = "";
        List<String> answers = new ArrayList<>();

        reader(TextChannel channel, Guild guild, Member member) {
            this.channel = channel;
            this.guild = guild;
            this.member = member;

            builder = new EmbedBuilder();
            builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
            builder.setDescription("What's your question?");
            builder.setColor(new Color(0, 200, 174));
            MessageAction message1 = channel.sendMessage(builder.build());
            message = message1.complete();
            message1.queue();
        }

        /*@Override
        public void onReady(ReadyEvent event) {
            builder = new EmbedBuilder();
            builder.setAuthor(member.getEffectiveName(), member.getUser().getAvatarUrl(), member.getUser().getAvatarUrl());
            builder.setDescription("What's your question?");
            builder.setColor(new Color(0, 200, 174));
            MessageAction message1 = channel.sendMessage(builder.build());
            message = message1.complete();
            message1.queue();
        }*/

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getGuild() == guild) {
                if (event.getMember() == member) {
                    if (!finish) {
                        if (event.getMessage().getContentRaw().equalsIgnoreCase("stop")) {
                            message.delete().queue();
                            //Main.jda.removeEventListener(this);
                        } else if (event.getMessage().getContentRaw().equalsIgnoreCase("done")) {
                            if (!question.equals("") && answers.size() >= 2) {
                                finish = true;
                                createQuestion();
                            } else {
                                builder.setDescription("You need to provide a question and give at least two answers.");
                                message.editMessage(builder.build()).queue();
                            }
                        } else if (question.equals("")) {
                            question = event.getMessage().getContentRaw();
                            builder.setDescription("Provide your 1st answer.");
                            message.editMessage(builder.build()).queue();
                        } else if (answers.size() < 10) {
                            answers.add(event.getMessage().getContentRaw());
                            String suffix = answers.size() == 1 ? "nd" : answers.size() == 2 ? "rd" : "th";
                            builder.setDescription("Provide your " + (answers.size() + 1) + suffix + " answer.");
                            message.editMessage(builder.build()).queue();
                        }
                    }
                }
            }
        }

        private void createQuestion() {
            StringBuilder builder1 = new StringBuilder();
            int index = 0;
            for (String s : answers) {
                builder1.append(numbers[index]).append(" | ").append(s).append("\n");
                index++;
            }
            builder.setTitle(question);
            builder.setDescription(builder1);
            message.editMessage(builder.build()).queue();
            for (long s : uNumbers) {
                //message.addReaction(Main.jda.getEmoteById(s)).queue();
                if (answers.size() == message.getReactions().size())
                    break;
            }
        }
    }
}
