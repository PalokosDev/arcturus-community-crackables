package com.palokos.communitycrackable;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.palokos.communitycrackable.models.RewardTier;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class CreateCommunityCrackableCommand extends Command {
    public CreateCommunityCrackableCommand(String permission) {
        super(permission, "createcrackable");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length < 5) {
            HashMap<String, String> keys = new HashMap<>();
            keys.put("display", "BUBBLE");
            keys.put("message", "Syntax: :createcrackable <furniture_id> <sprite_id> <totalHits> <timeInMinutes> <reward1MinHits>:<type>:<id>:<amount> ...\n" +
                    "Beispiel: :createcrackable 1234 5678 100000 120 1:credits:0:100 100:furniture:9876:1");
            gameClient.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, keys));
            return true;
        }

        try {
            // Parse basic parameters
            int furnitureId = Integer.parseInt(params[1]);
            int spriteId = Integer.parseInt(params[2]);
            int totalHits = Integer.parseInt(params[3]);
            int timeInMinutes = Integer.parseInt(params[4]);

            // Validate basic values
            if (totalHits <= 0 || timeInMinutes <= 0) {
                throw new IllegalArgumentException("Hits und Zeit müssen größer als 0 sein!");
            }

            if (furnitureId <= 0 || spriteId <= 0) {
                throw new IllegalArgumentException("Furniture ID und Sprite ID müssen gültig sein!");
            }

            // Create the crackable
            CommunityCrackable crackable = CommunityCrackablePlugin.INSTANCE
                .getDatabaseManager()
                .createCommunityCrackable(furnitureId, spriteId, totalHits, timeInMinutes);

            // Parse and add rewards starting from params[5]
            for (int i = 5; i < params.length; i++) {
                String[] rewardParts = params[i].split(":");
                if (rewardParts.length == 4) {
                    try {
                        int minHits = Integer.parseInt(rewardParts[0]);
                        String type = validateRewardType(rewardParts[1]);
                        int rewardId = Integer.parseInt(rewardParts[2]);
                        int amount = Integer.parseInt(rewardParts[3]);

                        // Validate reward values
                        if (minHits <= 0 || amount <= 0) {
                            throw new IllegalArgumentException("Mindest-Hits und Belohnungsmenge müssen größer als 0 sein!");
                        }

                        if (minHits > totalHits) {
                            throw new IllegalArgumentException("Mindest-Hits können nicht größer als Gesamt-Hits sein!");
                        }

                        CommunityCrackablePlugin.INSTANCE
                            .getDatabaseManager()
                            .addCrackableReward(crackable.getId(), minHits, type, rewardId, amount);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Ungültige Zahlen im Reward-Format!");
                    }
                } else {
                    throw new IllegalArgumentException("Ungültiges Reward-Format! Nutze: minHits:type:id:amount");
                }
            }

            // Add crackable to active crackables
            CommunityCrackablePlugin.INSTANCE.addCrackable(crackable.getId(), crackable);
            
            // Success message with details
            StringBuilder rewardsInfo = new StringBuilder();
            rewardsInfo.append("Community Crackable erstellt!\n");
            rewardsInfo.append("ID: ").append(crackable.getId()).append("\n");
            rewardsInfo.append("Benötigte Hits: ").append(totalHits).append("\n");
            rewardsInfo.append("Zeitlimit: ").append(timeInMinutes).append(" Minuten\n");
            rewardsInfo.append("Möbel ID: ").append(furnitureId).append("\n");
            rewardsInfo.append("Sprite ID: ").append(spriteId);

            HashMap<String, String> keys = new HashMap<>();
            keys.put("display", "BUBBLE");
            keys.put("message", rewardsInfo.toString());
            gameClient.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACED.key, keys));
            
        } catch (IllegalArgumentException e) {
            HashMap<String, String> keys = new HashMap<>();
            keys.put("display", "BUBBLE");
            keys.put("message", "Fehler: " + e.getMessage());
            gameClient.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, keys));
        } catch (Exception e) {
            e.printStackTrace();
            HashMap<String, String> keys = new HashMap<>();
            keys.put("display", "BUBBLE");
            keys.put("message", "Ein unerwarteter Fehler ist aufgetreten!");
            gameClient.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, keys));
        }

        return true;
    }

    private String validateRewardType(String type) throws IllegalArgumentException {
        String lowercaseType = type.toLowerCase();
        switch (lowercaseType) {
            case "credits":
            case "duckets":
            case "diamonds":
            case "badge":
            case "furniture":
                return lowercaseType;
            default:
                throw new IllegalArgumentException("Ungültiger Belohnungstyp: " + type + 
                    "\nErlaubte Typen: credits, duckets, diamonds, badge, furniture");
        }
    }
}
