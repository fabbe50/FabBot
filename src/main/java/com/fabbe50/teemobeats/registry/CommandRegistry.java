package com.fabbe50.teemobeats.registry;

import com.fabbe50.teemobeats.commands.*;
import com.fabbe50.teemobeats.handlers.CommandHandler;

/**
 * Created by fabbe on 24/04/2018 - 4:02 PM.
 */
public class CommandRegistry {
    public static void init() {
        CommandHandler handler = new CommandHandler();
        handler.registerCommand("blacklist", CommandBlacklist.class);
        handler.registerCommand("cat", CommandCat.class);
        handler.registerCommand("cookie", CommandCookies.class);
        handler.registerCommand("current", CommandCurrent.class);
        handler.registerCommand("feedback", CommandFeedback.class);
        handler.registerCommand("friendship", CommandFriendship.class);
        handler.registerCommand("hug", CommandHug.class);
        handler.registerCommand("gif", CommandImageSearch.class);
        handler.registerCommand("info", CommandInfo.class);
        handler.registerCommand("lyrics", CommandLyrics.class);
        handler.registerCommand("ping", CommandPing.class);
        handler.registerCommand("play", CommandPlay.class);
        handler.registerCommand("playlist", CommandPlaylist.class);
        handler.registerCommand("prefix", CommandPrefix.class);
        handler.registerCommand("queue", CommandQueue.class);
        handler.registerCommand("quote", CommandQuote.class);
        handler.registerCommand("remove", CommandRemove.class);
        handler.registerCommand("rich", CommandRich.class);
        handler.registerCommand("skip", CommandSkip.class);
        handler.registerCommand("slap", CommandSlap.class);
        handler.registerCommand("stop", CommandStop.class);
        handler.registerCommand("volume", CommandVolume.class);
        handler.registerCommand("william", CommandWilliam.class);
        handler.registerCommand("yt-live", CommandYTLive.class);
    }
}
