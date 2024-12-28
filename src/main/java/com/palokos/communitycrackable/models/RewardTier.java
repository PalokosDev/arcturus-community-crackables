package com.palokos.communitycrackable.models;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.items.ItemManager;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import java.util.HashMap;

public class RewardTier {
    private final int minHits;
    private final String rewardType;
    private final int rewardId;
    private final int rewardAmount;
    
    public RewardTier(int minHits, String rewardType, int rewardId, int rewardAmount) {
        this.minHits = minHits;
        this.rewardType = rewardType;
        this.rewardId = rewardId;
        this.rewardAmount = rewardAmount;
    }
    
    public void distributeReward(Habbo habbo) {
        String rewardMessage = buildRewardMessage();
        boolean success = true;
        
        try {
            switch (rewardType.toLowerCase()) {
                case "credits":
                    habbo.giveCredits(rewardAmount);
                    break;
                case "duckets":
                    habbo.givePixels(rewardAmount);
                    break;
                case "diamonds":
                    habbo.giveDiamonds(rewardAmount);
                    break;
                case "badge":
                    if (rewardId > 0) {
                        habbo.addBadge(String.valueOf(rewardId));
                    }
                    break;
                case "furniture":
                    if (rewardId > 0) {
                        ItemManager itemManager = Emulator.getGameEnvironment().getItemManager();
                        for (int i = 0; i < rewardAmount; i++) {
                            itemManager.createItem(
                                habbo.getHabboInfo().getId(),
                                rewardId,
                                0,
                                0,
                                "{}"
                            );
                        }
                    }
                    break;
                default:
                    success = false;
                    break;
            }

            // Send reward notification
            if (success) {
                HashMap<String, String> keys = new HashMap<>();
                keys.put("display", "BUBBLE");
                keys.put("message", "Du hast eine Belohnung erhalten:\n" + rewardMessage);
                habbo.getClient().sendResponse(new BubbleAlertComposer(
                    BubbleAlertKeys.RECEIVED_BADGE.key, 
                    keys
                ));
            }
        } catch (Exception e) {
            Emulator.getLogging().logErrorLine("Error distributing reward: " + e.getMessage());
            HashMap<String, String> keys = new HashMap<>();
            keys.put("display", "BUBBLE");
            keys.put("message", "Ein Fehler ist beim Verteilen der Belohnung aufgetreten!");
            habbo.getClient().sendResponse(new BubbleAlertComposer(
                BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, 
                keys
            ));
        }
    }
    
    private String buildRewardMessage() {
        StringBuilder message = new StringBuilder();
        switch (rewardType.toLowerCase()) {
            case "credits":
                message.append(rewardAmount).append(" Taler");
                break;
            case "duckets":
                message.append(rewardAmount).append(" Duckets");
                break;
            case "diamonds":
                message.append(rewardAmount).append(" Diamanten");
                break;
            case "badge":
                message.append("Badge ").append(rewardId);
                break;
            case "furniture":
                message.append(rewardAmount).append("x Möbelstück #").append(rewardId);
                break;
        }
        return message.toString();
    }
    
    public int getMinHits() {
        return minHits;
    }

    public String getRewardType() {
        return rewardType;
    }

    public int getRewardId() {
        return rewardId;
    }

    public int getRewardAmount() {
        return rewardAmount;
    }

    @Override
    public String toString() {
        return "RewardTier{" +
            "minHits=" + minHits +
            ", type='" + rewardType + '\'' +
            ", reward=" + buildRewardMessage() +
            '}';
    }
}
