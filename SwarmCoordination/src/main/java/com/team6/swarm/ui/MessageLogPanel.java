package com.team6.swarm.ui;

import com.team6.swarm.communication.Message;
import com.team6.swarm.communication.MessageType;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Week 3: Display recent message activity
 * Purpose: Users want to see what's happening in the network
 * Author: Anthony (UI Team)
 */
public class MessageLogPanel extends BorderPane {
    
    private final ListView<MessageEntry> messageListView;
    private final List<MessageEntry> messages;
    private final Map<MessageType, Boolean> typeFilters;
    
    private static final int MAX_MESSAGES = 500;
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // UI Components
    private CheckBox filterVotes;
    private CheckBox filterTasks;
    private CheckBox filterPositions;
    private CheckBox filterStatus;
    private Button clearButton;
    private Label messageCountLabel;
    
    /**
     * Constructor
     */
    public MessageLogPanel() {
        messages = new ArrayList<>();
        typeFilters = new HashMap<>();
        
        // Initialize all filters as enabled
        for (MessageType type : MessageType.values()) {
            typeFilters.put(type, true);
        }
        
        // Create list view
        messageListView = new ListView<>();
        messageListView.setCellFactory(param -> new MessageCell());
        
        // Create controls
        createControls();
        
        // Layout
        setCenter(messageListView);
        setTop(createTopBar());
        setBottom(createBottomBar());
        
        // Styling
        setPadding(new Insets(5));
        setStyle("-fx-background-color: #f0f0f0;");
    }
    
    /**
     * Create top bar with title and controls
     */
    private HBox createTopBar() {
        Label titleLabel = new Label("Message Log");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(5));
        topBar.getChildren().add(titleLabel);
        topBar.setStyle("-fx-background-color: #e0e0e0;");
        
        return topBar;
    }
    
    /**
     * Create controls for filtering and clearing
     */
    private void createControls() {
        filterVotes = new CheckBox("Votes");
        filterVotes.setSelected(true);
        filterVotes.setOnAction(e -> updateFilter(MessageType.VOTE_REQUEST, filterVotes.isSelected()));
        
        filterTasks = new CheckBox("Tasks");
        filterTasks.setSelected(true);
        filterTasks.setOnAction(e -> updateFilter(MessageType.TASK_ASSIGNMENT, filterTasks.isSelected()));
        
        filterPositions = new CheckBox("Positions");
        filterPositions.setSelected(true);
        filterPositions.setOnAction(e -> updateFilter(MessageType.POSITION_UPDATE, filterPositions.isSelected()));
        
        filterStatus = new CheckBox("Status");
        filterStatus.setSelected(true);
        filterStatus.setOnAction(e -> updateFilter(MessageType.STATUS_UPDATE, filterStatus.isSelected()));
        
        clearButton = new Button("Clear Log");
        clearButton.setOnAction(e -> clearMessages());
    }
    
    /**
     * Create bottom bar with filters and controls
     */
    private HBox createBottomBar() {
        messageCountLabel = new Label("Messages: 0");
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(5));
        filterBox.getChildren().addAll(
            filterLabel,
            filterVotes,
            filterTasks,
            filterPositions,
            filterStatus,
            clearButton,
            messageCountLabel
        );
        filterBox.setStyle("-fx-background-color: #e0e0e0;");
        
        return filterBox;
    }
    
    /**
     * Add a message to the log
     */
    public void addMessage(Message message) {
        if (message == null) {
            return;
        }
        
        MessageEntry entry = new MessageEntry(
            message,
            LocalTime.now(),
            formatMessage(message)
        );
        
        messages.add(entry);
        
        // Limit message history
        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
        
        // Update display
        refreshDisplay();
        
        // Auto-scroll to latest
        messageListView.scrollTo(messageListView.getItems().size() - 1);
        
        // Update count
        updateMessageCount();
    }
    
    /**
     * Format message for display
     */
    private String formatMessage(Message msg) {
        String sender = msg.getSenderId();
        String receiver = msg.getReceiverId();
        MessageType type = msg.getType();
        
        StringBuilder sb = new StringBuilder();
        
        // Sender
        sb.append(sender != null ? sender : "Unknown");
        sb.append(" → ");
        
        // Receiver
        if (receiver == null || receiver.equals("ALL") || receiver.equals("BROADCAST")) {
            sb.append("All");
        } else {
            sb.append(receiver);
        }
        
        sb.append(": ");
        
        // Type and content
        sb.append(formatMessageType(type));
        
        return sb.toString();
    }
    
    /**
     * Format message type for display
     */
    private String formatMessageType(MessageType type) {
        return switch (type) {
            case VOTE_REQUEST -> "Vote Proposal";
            case VOTE_RESPONSE -> "Vote Response";
            case TASK_ASSIGNMENT -> "Task Assignment";
            case POSITION_UPDATE -> "Position Update";
            case STATUS_UPDATE -> "Status Update";
            case EMERGENCY -> "⚠ EMERGENCY";
            case HEARTBEAT -> "Heartbeat";
            default -> type.toString();
        };
    }
    
    /**
     * Get color for message type
     */
    private Color getMessageColor(MessageType type) {
        return switch (type) {
            case VOTE_REQUEST, VOTE_RESPONSE -> Color.PURPLE;
            case TASK_ASSIGNMENT -> Color.ORANGE;
            case POSITION_UPDATE -> Color.BLUE;
            case STATUS_UPDATE -> Color.GREEN;
            case EMERGENCY -> Color.RED;
            case HEARTBEAT -> Color.GRAY;
            default -> Color.BLACK;
        };
    }
    
    /**
     * Update filter for message type
     */
    private void updateFilter(MessageType type, boolean enabled) {
        typeFilters.put(type, enabled);
        refreshDisplay();
    }
    
    /**
     * Refresh the display with filtered messages
     */
    private void refreshDisplay() {
        List<MessageEntry> filteredMessages = messages.stream()
            .filter(entry -> typeFilters.getOrDefault(entry.message.getType(), true))
            .toList();
        
        messageListView.getItems().clear();
        messageListView.getItems().addAll(filteredMessages);
    }
    
    /**
     * Clear all messages
     */
    public void clearMessages() {
        messages.clear();
        messageListView.getItems().clear();
        updateMessageCount();
        System.out.println("MessageLogPanel: Log cleared");
    }
    
    /**
     * Update message count label
     */
    private void updateMessageCount() {
        messageCountLabel.setText(String.format("Messages: %d / %d", 
            messageListView.getItems().size(), messages.size()));
    }
    
    /**
     * Set maximum message history
     */
    public void setMaxMessages(int max) {
        // Limit already enforced in addMessage
    }
    
    /**
     * Export messages to string
     */
    public String exportMessages() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message Log Export\n");
        sb.append("==================\n\n");
        
        for (MessageEntry entry : messages) {
            sb.append(String.format("[%s] %s\n", 
                entry.timestamp.format(TIME_FORMATTER), 
                entry.displayText));
        }
        
        return sb.toString();
    }
    
    /**
     * Message entry class
     */
    private static class MessageEntry {
        final Message message;
        final LocalTime timestamp;
        final String displayText;
        
        MessageEntry(Message message, LocalTime timestamp, String displayText) {
            this.message = message;
            this.timestamp = timestamp;
            this.displayText = displayText;
        }
    }
    
    /**
     * Custom cell for displaying messages
     */
    private class MessageCell extends ListCell<MessageEntry> {
        @Override
        protected void updateItem(MessageEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            
            if (empty || entry == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
            } else {
                String timeStr = entry.timestamp.format(TIME_FORMATTER);
                String fullText = String.format("[%s] %s", timeStr, entry.displayText);
                setText(fullText);
                
                // Color based on message type
                Color color = getMessageColor(entry.message.getType());
                setTextFill(color);
                
                // Bold for important messages
                if (entry.message.getType() == MessageType.EMERGENCY) {
                    setFont(Font.font("Arial", FontWeight.BOLD, 11));
                } else {
                    setFont(Font.font("Arial", 11));
                }
                
                // Tooltip with details
                setTooltip(createTooltip(entry.message));
            }
        }
        
        private Tooltip createTooltip(Message msg) {
            StringBuilder sb = new StringBuilder();
            sb.append("Type: ").append(msg.getType()).append("\n");
            sb.append("From: ").append(msg.getSenderId()).append("\n");
            sb.append("To: ").append(msg.getReceiverId()).append("\n");
            if (msg.getContent() != null) {
                sb.append("Content: ").append(msg.getContent()).append("\n");
            }
            
            return new Tooltip(sb.toString());
        }
    }
}
