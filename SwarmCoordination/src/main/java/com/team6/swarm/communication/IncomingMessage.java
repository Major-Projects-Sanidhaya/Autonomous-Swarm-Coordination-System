/**
 * Message with delivery details and routing information
 */
package com.team6.swarm.communication;

import java.util.List;
import java.util.ArrayList;

public class IncomingMessage {
    public final int receiverId;
    public final int originalSenderId;
    public final Message messageContent;
    public final List<Integer> routePath;
    public final double signalStrength;
    public final long actualDeliveryTime;
    public final long transmissionDelay;
    
    public IncomingMessage(int receiverId, int originalSenderId, Message messageContent, 
                          List<Integer> routePath, double signalStrength) {
        this.receiverId = receiverId;
        this.originalSenderId = originalSenderId;
        this.messageContent = messageContent;
        this.routePath = new ArrayList<>(routePath);
        this.signalStrength = signalStrength;
        this.actualDeliveryTime = System.currentTimeMillis();
        this.transmissionDelay = actualDeliveryTime - messageContent.timestamp;
    }
    
    public boolean isDirectDelivery() {
        return routePath.isEmpty();
    }
    
    public int getHopCount() {
        return routePath.size();
    }
    
    public boolean isReliable() {
        return signalStrength > 0.7;
    }
    
    @Override
    public String toString() {
        return String.format("IncomingMessage{receiver=%d, sender=%d, type=%s, hops=%d, signal=%.2f, delay=%dms}", 
                           receiverId, originalSenderId, messageContent.type, getHopCount(), 
                           signalStrength, transmissionDelay);
    }
}
