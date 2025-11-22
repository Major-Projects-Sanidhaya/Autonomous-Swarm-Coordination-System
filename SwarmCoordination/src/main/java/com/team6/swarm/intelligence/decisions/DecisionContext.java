package com.team6.swarm.intelligence.decisions;
import java.util.ArrayList;
import java.util.List;

/**
 * DECISIONCONTEXT CLASS - Multi-Criteria Decision Setup
 */
public class DecisionContext {
    public String decisionId;
    public String description;
    public List<String> options;
    public List<DecisionCriterion> criteria;
    public long createdTime;
    public DecisionStatus status;
    
    public DecisionContext(String decisionId, String description, List<String> options) {
        this.decisionId = decisionId;
        this.description = description;
        this.options = new ArrayList<>(options);
        this.criteria = new ArrayList<>();
        this.createdTime = System.currentTimeMillis();
        this.status = DecisionStatus.PENDING;
    }
    
    /**
     * Add criterion to decision
     */
    public void addCriterion(String name, CriterionType type, double weight) {
        criteria.add(new DecisionCriterion(name, type, weight));
    }
    
    @Override
    public String toString() {
        return String.format("DecisionContext[%s: %s | Options: %d | Criteria: %d]",
            decisionId, description, options.size(), criteria.size());
    }
}
