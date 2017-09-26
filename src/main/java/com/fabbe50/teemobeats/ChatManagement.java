package com.fabbe50.teemobeats;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by fabbe on 24/09/2017 - 12:13 PM.
 */
public class ChatManagement {
    static Collection<Message> clear (MessageChannel channel) {
        List<Message> messages = new ArrayList<>(1000);
        int i = 1000;
        for (Message message : channel.getIterableHistory().cache(false)) {
            if (message.getAuthor().getName().equals(Main.botUsername))
                messages.add(message);
            if (--i <= 0) break;
        }
        return messages;
    }
}
