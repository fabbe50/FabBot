package com.fabbe50.teemobeats.poll;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fabbe on 21/04/2018 - 2:52 AM.
 */
public class ObjectPoll {
    private String question;
    private List<String> answers;

    public ObjectPoll(String question, List<String> answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }
}
