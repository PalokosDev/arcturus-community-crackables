package com.palokos.communitycrackable;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.users.UserLoginEvent;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.palokos.communitycrackable.utils.DatabaseManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommunityCrackablePlugin extends HabboPlugin implements EventListener {
    public static CommunityCrackablePlugin INSTANCE = null;
    private static Map<Integer, CommunityCrackable> activeCrackables;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        INSTANCE = this;
        activeCrackables = new ConcurrentHashMap<>();
        databaseManager = new DatabaseManager();
        
        // Register events
        Emulator.getPluginManager().registerEvents(this);
        
        // Register commands
        CommandHandler.addCommand(new CreateCommunityCrackableCommand("cmd_createcrackable"));
        
        // Create database tables
        databaseManager.createTables();
        
        // Load active crackables
        activeCrackables.putAll(databaseManager.loadAllActiveCrackables().stream()
            .collect(java.util.stream.Collectors.toMap(
                CommunityCrackable::getId,
                crackable -> crackable
            )));
        
        Emulator.getLogging().logStart("Community Crackable Plugin -> Loaded!");
    }

    @Override
    public void onDisable() {
        // Cleanup active crackables
        for (CommunityCrackable crackable : activeCrackables.values()) {
            crackable.stopTimer();
        }
        activeCrackables.clear();
        
        INSTANCE = null;
        Emulator.getLogging().logShutdown("Community Crackable Plugin -> Disabled!");
    }

    @Override
    public boolean hasPermission(Habbo habbo, String s) {
        return habbo.hasPermission("cmd_createcrackable");
    }

    public void addCrackable(int id, CommunityCrackable crackable) {
        activeCrackables.put(id, crackable);
    }

    public CommunityCrackable getCrackable(int id) {
        return activeCrackables.get(id);
    }

    public void removeCrackable(int id) {
        CommunityCrackable crackable = activeCrackables.remove(id);
        if (crackable != null) {
            crackable.stopTimer();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
