package com.team6.swarm.intelligence.decisions;
import java.util.*;

/**
 * MULTICRITERIARESULT CLASS - Multi-Criteria Analysis Outcome
 */
public class MultiCriteriaResult {
    private final String decisionId;
    private final String bestOption;
    private final double bestScore;
    private final Map<String, Double> optionScores;          // option -> total score
    private final Map<String, Map<String, Double>> detailedScores;  // option -> criterion -> score
    private final long timestamp;

    public String getDecisionId() {
      return decisionId;
    }

    public String getBestOption() {
      return bestOption;
    }

    public double getBestScore() {
      return bestScore;
    }

    public Map<String, Double> getOptionScores() {
      return Collections.unmodifiableMap(optionScores);
    }

    /**
     * Returns an unmodifiable deep copy of the detailed scores map.
     */
    public Map<String, Map<String, Double>> getAllDetailedScores() {
      Map<String, Map<String, Double>> copy = new HashMap<>();
      for (Map.Entry<String, Map<String, Double>> entry : detailedScores.entrySet()) {
        copy.put(entry.getKey(), Collections.unmodifiableMap(new HashMap<>(entry.getValue())));
      }
      return Collections.unmodifiableMap(copy);
    }

    public long getTimestamp() {
      return timestamp;
    }
    
    public MultiCriteriaResult(String decisionId, String bestOption, double bestScore,
                              Map<String, Double> optionScores,
                              Map<String, Map<String, Double>> detailedScores) {
        this.decisionId = decisionId;
        this.bestOption = bestOption;
        this.bestScore = bestScore;
        this.optionScores = new HashMap<>(optionScores);
        this.detailedScores = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : detailedScores.entrySet()) {
            this.detailedScores.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Get score for specific option
     */
    public double getScoreForOption(String option) {
        return optionScores.getOrDefault(option, 0.0);
    }
    
    /**
     * Get detailed breakdown for option
     */
    public Map<String, Double> getDetailedScores(String option) {
        return detailedScores.getOrDefault(option, new HashMap<>());
    }
    
    /**
     * Get score difference between best and second best
     */
    public double getScoreMargin() {
        List<Double> scores = new ArrayList<>(optionScores.values());
        scores.sort(Collections.reverseOrder());
        
        if (scores.size() < 2) return bestScore;
        return scores.get(0) - scores.get(1);
    }
    
    @Override
    public String toString() {
        return String.format(
            "MultiCriteriaResult[%s: %s wins | Score: %.2f | Margin: %.2f]",
            decisionId, bestOption, bestScore, getScoreMargin()
        );
    }
}
