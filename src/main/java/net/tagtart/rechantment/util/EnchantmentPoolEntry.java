package net.tagtart.rechantment.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnchantmentPoolEntry {
    public final String enchantment;
    public final int weight;
    public final int minLevel;
    public final int maxLevel;

    public final ArrayList<Integer> levelWeights;
    public final ArrayList<Integer> potentialLevels;
    public final int levelWeightsSum;

    public EnchantmentPoolEntry(String enchantment, int weight, int minLevel, int maxLevel, ArrayList<Integer> levelWeights) {
        this.enchantment = enchantment;
        this.weight = weight;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.levelWeights = levelWeights;

        potentialLevels = new ArrayList<>();
        for (int i = minLevel; i <= maxLevel; i++) {
            potentialLevels.add(i);
        }

        int currentSum = 0;
        for (Integer levelWeight : levelWeights) {
            currentSum += levelWeight;
        }
        this.levelWeightsSum = currentSum;
    }

    public static EnchantmentPoolEntry fromString(String configString) {

        // Separate the enchantment by main parts using vertical | bars
        String[] parts = configString.split("\\|");
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Bad format (expected 4 parts): <enchantment>|<weight>|<level-range>|<level-weights>. Got "
                            + parts.length + " part(s): '" + configString + "'"
            );
        }

        String enchantment = parts[0];
        if (!enchantment.contains(":")) {
            throw new IllegalArgumentException("Enchantment id must look like 'namespace:path' (part 1). Got: '"
                    + enchantment + "' in: '" + configString + "'");
        }


        int weight = Integer.parseInt(parts[1]);

        String[] levelRange = parts[2].split("-");
        int minLevel = Integer.parseInt(levelRange[0]);
        int maxLevel = minLevel;
        if (levelRange.length > 1)
            maxLevel = Integer.parseInt(levelRange[1]);

        ArrayList<Integer> levelWeights = new ArrayList<>();
        String[] weightStrings = parts[3].split(",");
        for (String weightString : weightStrings) {
            levelWeights.add(Integer.parseInt(weightString));
        }


        // Enforce one weight per level in the range
        int expected = (maxLevel - minLevel) + 1;
        if (levelWeights.size() != expected) {
            throw new IllegalArgumentException(
                "Wrong number of level weights (part 4). Range is " + minLevel + "-" + maxLevel
                        + " so expected " + expected + " weight(s), got " + levelWeights.size()
                        + " in: '" + configString + "'"
             );
    }

        return new EnchantmentPoolEntry(enchantment, weight, minLevel, maxLevel, levelWeights);
    }

    public static List<EnchantmentPoolEntry> listFromString(List<? extends String> configStrings) {
        ArrayList<EnchantmentPoolEntry> buildPool = new ArrayList<>();

        for (String configString : configStrings) {
            buildPool.add(fromString(configString));
        }

        return buildPool;
    }

    public int getRandomEnchantLevelWeighted() {

        Random rand = new Random();
        int randVal = rand.nextInt(levelWeightsSum);

        int cumulativeSum = 0;
        for (int i = 0; i < levelWeights.size(); ++i) {
            cumulativeSum += levelWeights.get(i);
            if (randVal < cumulativeSum) {
                return potentialLevels.get(i);
            }
        }

        throw new IllegalStateException("Failed to select an enchantment level based on weight.\nEnsure Rechantment config is set up properly,");
    }
}
