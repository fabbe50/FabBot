package com.fabbe50.teemobeats.handlers;

import com.fabbe50.teemobeats.Main;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;

import java.util.TimerTask;

/**
 * Created by fabbe on 31/05/2018 - 8:07 AM.
 */
public class StatusHandler extends TimerTask {
    int index = 0;
    @Override
    public void run() {
        if (Main.getJda().getPresence().getGame() != null) {
            switch (index) {
                case 0:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.listening("the sound of pain and agony."));
                    break;
                case 1:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.watching(Main.getJda().getGuilds().size() + " guilds."));
                    break;
                case 2:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.watching(Main.usersServed(Main.getJda().getGuilds()) + " users."));
                    break;
                case 3:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.playing("with hamsters."));
                    break;
                case 4:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.playing("the blame William game."));
                    break;
                case 5:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.watching("the world burn."));
                    break;
                case 6:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.listening("serious screeching."));
                    break;
                case 7:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.playing("with quotes."));
                    break;
                case 8:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.streaming("bits and bytes.", "https://twitch.tv/fabbe50"));
                    break;
                case 9:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.playing("the be rude af game."));
                    break;
                case 10:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.watching("my creator."));
                    break;
                case 11:
                    Main.getJda().getPresence().setPresence(OnlineStatus.IDLE, Game.playing("the waiting game."));
                    break;
            }
            index++;
            if (index > 11)
                index = 0;
        }
    }
}
