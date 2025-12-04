John's Communication System Development Guide
Complete File Structure & Week-by-Week Plan
 
Your Role: The Nervous System
You're building the communication infrastructure that enables the swarm's collective intelligence. Without your system, agents can't share information, vote, or coordinate. You're the bridge that connects individual agents into a cohesive swarm.
What You're Responsible For:
•	Determining which agents can talk to each other (based on distance)
•	Routing messages between agents
•	Simulating realistic network conditions (delays, failures, range limits)
•	Tracking the communication network topology
•	Providing communication status for visualization
 
Project Structure
SwarmCoordination/
└── src/main/java/com/team6/swarm/
    ├── core/              # Sanidhaya's package
    ├── communication/     # YOUR PACKAGE
    │   ├── Message.java
    │   ├── MessageType.java
    │   ├── OutgoingMessage.java
    │   ├── IncomingMessage.java
    │   ├── CommunicationManager.java
    │   ├── NetworkSimulator.java
    │   ├── MessageRouter.java
    │   └── ... (more files)
    ├── intelligence/      # Lauren's package
    └── ui/               # Anthony's package
 
Week 1: Foundation - Basic Message System
Goal: Create the fundamental message structures everyone will use
File 1: MessageType.java (Enum)
Purpose: Define all types of messages agents can send to each other
Why Needed: Different messages need different handling - a vote is different from a position update
What It Contains:
•	POSITION_UPDATE - Agent sharing its location
•	VOTE_PROPOSAL - Asking others to vote on a decision
•	VOTE_RESPONSE - Responding to a vote
•	TASK_ASSIGNMENT - Lauren assigning work to agents
•	FORMATION_COMMAND - Instructions for formation flying
•	EMERGENCY_ALERT - Urgent notifications
How Others Use It:
•	Lauren creates VOTE_PROPOSAL and TASK_ASSIGNMENT messages
•	You use it to prioritize and route different message types
•	Anthony displays different icons/colors based on message type
Expected Output: A simple enum with 6-8 message types
 
File 2: Message.java
Purpose: The actual message content being sent between agents
Why Needed: Standardized container for all communication between agents
What It Contains:
•	messageId - Unique identifier (like a tracking number)
•	type - What kind of message (uses MessageType enum)
•	payload - The actual data (could be position, vote, task, etc.)
•	timestamp - When the message was created
•	metadata - Optional extra information
How It Works:
•	Lauren creates a Message with a VoteProposal as payload
•	You wrap it in routing information
•	Deliver it to destination agents
•	Recipient sees the original Message
What Others Expect:
•	Sanidhaya expects messages to arrive as CommunicationEvents
•	Lauren expects to create Messages with her decision data
•	Anthony expects to see message activity for visualization
Expected Output: A data container class with basic fields
 
File 3: OutgoingMessage.java
Purpose: Message with routing information (who's sending, who should receive)
Why Needed: You need to know where messages come from and where they're going
What It Contains:
•	senderId - Which agent is sending
•	receiverId - Which agent should receive (-1 means broadcast to all)
•	messageContent - The actual Message object
•	priority - How urgent is this message
•	maxHops - How many agents can relay this message
•	expirationTime - When this message becomes invalid
How It Works:
1.	Lauren creates a Message (the content)
2.	She wraps it in OutgoingMessage with sender/receiver info
3.	She gives it to you
4.	You figure out how to deliver it
Routing Scenarios:
•	Direct delivery: Agent 1 → Agent 2 (if in range)
•	Broadcast: Agent 1 → All nearby agents
•	Multi-hop: Agent 1 → Agent 2 → Agent 3 (if 1 and 3 out of range)
Expected Output: Container class with routing metadata
 
File 4: IncomingMessage.java
Purpose: Message that arrives at destination with delivery details
Why Needed: Recipients need to know how the message got to them
What It Contains:
•	receiverId - Who got this message
•	originalSenderId - Who sent it originally
•	messageContent - The actual Message
•	routePath - Which agents relayed it (if multi-hop)
•	signalStrength - How clear was the signal (0.0 to 1.0)
•	actualDeliveryTime - When it actually arrived
How It Works:
•	You successfully route a message
•	You create IncomingMessage with delivery details
•	You pass it to Lauren (for decision making) or Sanidhaya (for notification)
Why Delivery Details Matter:
•	Low signal strength might mean unreliable data
•	Long route paths indicate network problems
•	Delivery time helps detect network delays
Expected Output: Delivery receipt with transmission details
 
File 5: CommunicationTest.java
Purpose: Test your basic message system works
What To Test:
•	Create different MessageTypes
•	Create Messages with various payloads
•	Wrap Messages in OutgoingMessage with routing info
•	Create IncomingMessage delivery receipts
•	Verify all data is preserved correctly
Expected Output: All tests pass, messages created successfully
 
Week 1 Success Criteria:
•	[ ] All message classes compile
•	[ ] Can create messages of different types
•	[ ] Routing information attached correctly
•	[ ] Test program runs without errors
 
Week 2: Network Topology - Who Can Talk To Whom
Goal: Determine communication range and neighbor relationships
File 6: NeighborAgent.java
Purpose: Information about a nearby agent that can be communicated with
Why Needed: Each agent needs to know who they can talk to
What It Contains:
•	neighborId - Which agent is nearby
•	distance - How far away they are
•	signalStrength - Quality of connection (1.0 = perfect, 0.0 = none)
•	canCommunicate - Boolean if connection is good enough
•	lastContact - When last message was exchanged
How It Works:
•	You receive position updates from Sanidhaya
•	You calculate distances between all agents
•	For each agent, you determine who their neighbors are
•	You create NeighborAgent objects for nearby agents
Why This Matters:
•	Lauren needs neighbor info for flocking algorithms
•	You need it to route messages
•	Anthony displays communication links in UI
Expected Output: Data class representing one neighbor relationship
 
File 7: NeighborInformation.java
Purpose: Complete list of neighbors for one agent
Why Needed: Bundle all neighbor data together for efficiency
What It Contains:
•	agentId - Which agent this is about
•	neighbors - List of NeighborAgent objects
•	topologyUpdateTime - When this was calculated
•	networkQuality - Overall network health for this agent
How It Works:
1.	Sanidhaya sends you all agent positions
2.	You calculate which agents are near each other
3.	For Agent 1, you create NeighborInformation with all nearby agents
4.	You send this to Lauren (for flocking) and store it (for routing)
Update Frequency:
•	Calculate every time agent positions change (30-60 times per second)
•	Only send to Lauren when neighbors actually change (reduce traffic)
Expected Output: Container for all neighbors of one agent
 
File 8: NetworkSimulator.java
Purpose: Simulate realistic network conditions (not everything works perfectly!)
Why Needed: Real communication has range limits, delays, and failures
What It Contains:
•	communicationRange - Maximum distance for direct communication (default: 100 units)
•	messageLatency - Simulated network delay (default: 100-200ms)
•	failureRate - Percentage of messages that fail (default: 5%)
•	interferenceLevel - Environmental factors affecting range
Key Methods To Implement:
1. canCommunicate(agent1Position, agent2Position, range)
•	Calculate distance between agents
•	Return true if distance < communicationRange
•	Apply interference effects if needed
2. calculateSignalStrength(distance, maxRange)
•	Returns 1.0 if very close
•	Returns 0.0 if at max range or beyond
•	Linear or exponential falloff in between
•	Formula: signalStrength = 1.0 - (distance / maxRange)
3. shouldDeliverMessage(sender, receiver, distance)
•	Check if in range
•	Apply random failure based on failureRate
•	Consider signal strength (weak signals more likely to fail)
•	Return true if message should be delivered
4. calculateDelay(sender, receiver)
•	Base latency (e.g., 100ms)
•	Add random variation (±50ms)
•	Longer distances = slightly more delay
•	Return delay in milliseconds
Why Simulation Matters:
•	Makes system realistic (nothing works 100% of the time)
•	Tests Lauren's algorithms under imperfect conditions
•	Creates interesting scenarios for demo
Expected Output: Class that simulates network physics
 
File 9: CommunicationManager.java (Main Class)
Purpose: The central hub that manages all communication
Why Needed: Someone needs to coordinate all messaging activity
What It Contains:
•	networkTopology - Map of which agents can talk to whom
•	messageQueue - Messages waiting to be delivered
•	networkSimulator - Your simulation engine
•	messageHistory - Recent messages for debugging/display
Key Responsibilities:
1. Update Network Topology
•	Receive agent position updates from Sanidhaya
•	Calculate distances between all agents
•	Determine who can communicate with whom
•	Store in networkTopology map
•	Notify when topology changes
2. Send Messages
•	Receive OutgoingMessage from Lauren
•	Check if direct delivery possible (in range?)
•	If not, find multi-hop route (if enabled)
•	Apply network simulation (delays, failures)
•	Create IncomingMessage for delivery
3. Broadcast Messages
•	When receiverId = -1, it's a broadcast
•	Find all neighbors of sender
•	Deliver to each neighbor
•	Track who received what (avoid duplicates)
4. Provide Neighbor Information
•	Lauren asks "who are Agent 5's neighbors?"
•	You look up in networkTopology
•	Return NeighborInformation object
•	Used for flocking calculations
Key Methods:
sendMessage(OutgoingMessage message)
•	Add to message queue
•	Process in order of priority
•	Return success/failure
getNeighbors(int agentId)
•	Look up agent in topology
•	Return list of neighbor IDs
•	Return empty list if agent has no neighbors
updateTopology(List<AgentState> allAgents)
•	Recalculate all distances
•	Update who can communicate with whom
•	Detect topology changes
Expected Output: The "brain" of your communication system
 
Week 2 Success Criteria:
•	[ ] Can calculate distances between agents
•	[ ] Can determine neighbor relationships
•	[ ] Network topology updates when agents move
•	[ ] Tested with mock agent positions from Sanidhaya
 
Week 3: Message Routing - Delivery System
Goal: Actually deliver messages between agents reliably
File 10: MessageRouter.java
Purpose: Finds paths for messages and handles multi-hop routing
Why Needed: Not all agents can talk directly - messages may need relaying
What It Does:
1. Direct Routing
•	Agent 1 wants to message Agent 3
•	Check if they're neighbors (in range)
•	If yes, deliver directly
•	If no, need multi-hop routing
2. Multi-Hop Routing
•	Agent 1 wants to message Agent 5
•	They're not neighbors
•	Agent 1 → Agent 2 → Agent 5 (2 hops)
•	Each agent relays the message
3. Broadcast Routing
•	Agent 1 broadcasts to all
•	Find all Agent 1's neighbors
•	Deliver to each
•	Optionally, neighbors re-broadcast (flooding)
Key Methods:
routeMessage(senderId, receiverId, Message)
•	Find path from sender to receiver
•	Handle direct delivery if possible
•	Use multi-hop if needed
•	Return success/failure
findPath(senderId, receiverId)
•	Use BFS (breadth-first search) in network graph
•	Find shortest path
•	Return list of agent IDs in path
•	Return empty list if no path exists
deliverDirectly(senderId, receiverId, Message)
•	Check range using NetworkSimulator
•	Apply failure/delay simulation
•	Create IncomingMessage
•	Notify recipient
Why This Is Important:
•	Network partitions happen (agents out of range)
•	Multi-hop enables long-distance communication
•	Critical for swarm coordination
Expected Output: Smart routing logic that finds message paths
 
File 11: MessageQueue.java
Purpose: Manage pending messages in priority order
Why Needed: Emergency messages must go first!
What It Does:
•	Store pending messages
•	Sort by priority (EMERGENCY > HIGH > NORMAL > LOW)
•	Process in order
•	Track message status (pending, sent, failed)
Key Methods:
enqueue(OutgoingMessage message)
•	Add to queue
•	Sort by priority
•	Assign position in queue
dequeue()
•	Get next highest priority message
•	Remove from queue
•	Return message for processing
size()
•	How many messages waiting?
•	Important for performance monitoring
Expected Output: Priority queue for message management
 
File 12: ConnectionInfo.java
Purpose: Information about an active communication link
Why Needed: Anthony needs to visualize connections between agents
What It Contains:
•	agentA, agentB - Which two agents
•	strength - Signal strength (0.0 to 1.0)
•	isActive - Currently connected?
•	establishedTime - When connection formed
•	lastMessageTime - When last message sent
•	messageCount - How many messages on this link
How It's Used:
•	You create ConnectionInfo for each active link
•	Anthony draws lines between agents
•	Line thickness = signal strength
•	Line color = active/inactive
Expected Output: Data class for one communication link
 
Week 3 Success Criteria:
•	[ ] Messages delivered between nearby agents
•	[ ] Multi-hop routing works for distant agents
•	[ ] Message priority respected
•	[ ] Integration test with Lauren's mock messages
 
Week 4: Integration with UI
Goal: Provide communication status for Anthony's visualization
File 13: NetworkStatus.java
Purpose: Complete picture of network health for display
Why Needed: Anthony needs one object with all network data
What It Contains:
•	totalConnections - How many active links
•	messagesPerSecond - Current message rate
•	averageLatency - Typical message delay
•	activeConnections - List of ConnectionInfo objects
•	recentMessages - Last 10-20 messages
•	networkHealth - Overall status (EXCELLENT, GOOD, FAIR, POOR)
How It's Calculated:
•	Update every second
•	Count active connections from topology
•	Track message throughput
•	Calculate average latency from recent deliveries
•	Assess overall health based on metrics
What Anthony Does With It:
•	Displays network statistics panel
•	Shows communication links between agents
•	Highlights network problems (red if POOR)
•	Shows message activity log
Expected Output: Complete network status snapshot
 
File 14: MessageLog.java
Purpose: Record of a message for debugging/display
Why Needed: Track what messages were sent for troubleshooting
What It Contains:
•	messageId - Unique identifier
•	senderId, receiverId - Who communicated
•	messageType - What kind of message
•	successful - Was it delivered?
•	timestamp - When it happened
•	errorReason - Why it failed (if applicable)
How It's Used:
•	You log every message attempt
•	Keep recent history (last 50-100 messages)
•	Anthony displays in UI message log
•	Useful for debugging communication problems
Expected Output: Audit trail of messages
 
File 15: NetworkConfiguration.java
Purpose: Settings that Anthony can adjust through UI
Why Needed: Users want to tweak simulation parameters
What It Contains:
•	communicationRange - How far messages go
•	messageLatency - Simulated delay
•	failureRate - Percentage of failed messages
•	enableMultiHop - Allow message relaying?
•	maxRetries - How many times to retry failed messages
How It Works:
1.	Anthony shows sliders/inputs in UI
2.	User adjusts communicationRange from 100 to 150
3.	Anthony creates NetworkConfiguration object
4.	Sends to your CommunicationManager
5.	You update NetworkSimulator with new settings
Expected Output: Configuration container class
 
Week 4 Success Criteria:
•	[ ] Network status available for display
•	[ ] Anthony can visualize communication links
•	[ ] Message logs displayed in UI
•	[ ] User can adjust network parameters
 
Week 5-6: Advanced Features
Goal: Add sophistication and handle edge cases
File 16: NetworkTopology.java
Purpose: Graph representation of communication network
Why Needed: Efficient pathfinding requires proper graph structure
What It Does:
•	Represents agents as nodes
•	Connections as edges
•	Weights on edges (signal strength)
•	Efficient neighbor lookups
•	Graph traversal algorithms
Key Methods:
addAgent(int agentId)
•	Add node to graph
•	Initialize empty edge list
addConnection(int agentA, int agentB, double strength)
•	Create edge between agents
•	Store signal strength as weight
•	Bidirectional connection
removeConnection(int agentA, int agentB)
•	Remove edge when out of range
•	Update topology
getNeighbors(int agentId)
•	Return all connected agents
•	Fast O(1) lookup
Expected Output: Efficient graph data structure
 
File 17: MessageReliability.java
Purpose: Ensure important messages eventually get through
Why Needed: Critical messages can't just fail silently
What It Does:
•	Retry failed messages
•	Track delivery attempts
•	Implement acknowledgments (ACK)
•	Timeout and retry logic
How Acknowledgments Work:
1.	Agent 1 sends message to Agent 3
2.	Agent 3 receives and sends ACK back
3.	Agent 1 gets ACK, knows delivery succeeded
4.	If no ACK within timeout, retry
Expected Output: Reliability layer for critical messages
 
File 18: BroadcastManager.java
Purpose: Handle efficient broadcast/multicast messages
Why Needed: Voting requires broadcasting to all agents
What It Does:
•	Flooding: Everyone rebroadcasts (can duplicate)
•	Controlled Flooding: Limit hops, avoid duplicates
•	Selective Broadcast: Only to specific subset
Broadcast Scenarios:
•	Vote proposal to all agents
•	Emergency alert to everyone
•	Formation command to team members only
Expected Output: Specialized broadcast handling
 
Week 5-6 Success Criteria:
•	[ ] Graph structure efficient for pathfinding
•	[ ] Reliable delivery of critical messages
•	[ ] Broadcast works without message storms
•	[ ] System handles 10+ agents smoothly
 
Week 7-8: Consensus Support
Goal: Support Lauren's voting and decision-making systems using the existing voting infrastructure (no duplicate routing or queue logic).
File 19: VotingProtocol.java
Purpose: Lightweight coordination layer on top of existing vote messages
Why Needed: Voting has specific requirements (timing, all votes counted, quorum) that sit above basic message delivery
What It Does (without duplicating existing functionality):
•	Reuse CommunicationManager.broadcastVote and sendVoteResponse (no new routing/queueing logic)
•	Track vote proposals by proposalId (who initiated, which agents are expected to respond)
•	Collect vote responses using CommunicationManager.getVoteMessages
•	Determine when voting is complete (all expected responses or deadline reached)
•	Expose a minimal API for Lauren to query vote status/results
Voting Message Flow (built on top of existing methods):
1.	Lauren creates VoteProposal payload (question, options, deadline, proposalId)
2.	You call CommunicationManager.broadcastVote(senderId, voteProposalPayload)
3.	VotingProtocol tracks which agents should vote and which have responded
4.	Agents send VOTE_RESPONSE using CommunicationManager.sendVoteResponse
5.	VotingProtocol uses CommunicationManager.getVoteMessages to collect responses
6.	When quorum reached or deadline passed, VotingProtocol notifies Lauren (e.g., via a simple callback or status query)
Expected Output: Consensus behaviour implemented as a thin layer reusing existing vote messaging
 
File 20: ConsensusMessage (logical schema)
Purpose: Standardized payload structure for group decision messages using existing Message/MessageType
Why Needed: Consensus messages need consistent fields for tracking and analysis, but we avoid a heavy new Message class
What It Contains (conceptually, as fields in the Message payload):
•	proposalId - Unique identifier for this vote/decision
•	requiredResponses - How many agents must respond (or percentage quorum)
•	responseDeadline - Time limit for responses
•	expectedVoters - List or count of agents expected to vote
•	responseTracker - Who has responded (kept inside VotingProtocol, not inside the core Message type)
Implementation Note:
•	Implemented as a structured Map<String, Object> payload carried by the existing Message class with type VOTE_PROPOSAL or VOTE_RESPONSE
•	No new top-level Java message class is required, keeping the design DRY
Expected Output: Clear, documented payload format for consensus-related messages
 
Week 7-8 Success Criteria:
•	[ ] Reuse existing vote message support (broadcastVote, sendVoteResponse, getVoteMessages) without duplicating routing/queue logic
•	[ ] VotingProtocol can track proposals, responses, and completion using existing message history
•	[ ] Lauren can query consensus status/results via a minimal API
•	[ ] Timeout / deadline behaviour is defined and handled at protocol level (not by duplicating NetworkSimulator)

Week 7-8 Status:
•	[ ] VotingProtocol.java implemented as thin coordination layer (no duplicate routing/queue logic)
•	[ ] IntegrationTest updated with Week 7–8 consensus support test
•	[ ] Ready for Lauren's integration with real VotingSystem
 
Week 9-10: Fault Tolerance
Goal: Handle network failures and partitions gracefully using existing topology/routing primitives.
File 21: NetworkPartition (logical concept)
Purpose: Detect and describe network splits using existing reachability utilities
Why Needed: Agents may separate into unreachable groups and need to operate independently
What It Does (built on current code):
•	Reuse CommunicationManager.getReachableAgents and getNetworkPartitions (no new graph logic)
•	Represent each partition as a simple Set<Integer> of agent IDs (no heavy new class required)
•	Provide a minimal API to query current partitions and detect changes over time
•	Enable higher-level systems (Lauren, Sanidhaya) to adapt behaviour based on partition membership
Partition Scenarios:
•	5 agents split into [1,2] and [3,4,5]
•	Each group coordinates internally using existing message routing
•	When groups move back in range, partitions merge automatically via updated topology
Expected Output: Partition handling built on top of existing reachability logic
 
File 22: FailureRecovery (lightweight policy)
Purpose: Recover from communication failures using retries and alternate paths where possible
Why Needed: Messages will occasionally fail due to range, interference, or congestion
What It Does (without duplicating NetworkSimulator or MessageRouter):
•	Observe failed or missing deliveries using existing message history / status
•	Retry critical messages a limited number of times (configurable)
•	Prefer alternate routes by reusing existing MessageRouter and topology rather than new routing logic
•	Surface persistent failures to higher-level systems for decision-making
Expected Output: Simple, configurable recovery policy that uses existing simulation and routing
 
Week 9-10 Success Criteria:
•	[ ] System handles network partitions using CommunicationManager.getNetworkPartitions
•	[ ] Simple failure recovery policy works for critical messages (limited retries, no storms)
•	[ ] No message loss for critical messages under typical simulated failures
•	[ ] Integration test with simulated failures and partitions validates behaviour

Week 9-10 Status:
•	[ ] Partition detection utilities implemented (getReachableAgents, getNetworkPartitions) and reused
•	[ ] Failure recovery policy designed and covered by integration tests
 
Week 11-12: Performance & Polish
Goal: Optimize for 10+ agents and production quality using existing metrics and tests.
File 23: PerformanceOptimizer (lightweight tuning)
Purpose: Ensure the communication system scales well without major redesign
What To Optimize (building on current implementation):
•	Topology calculation (reuse existing NeighborInformation, avoid unnecessary recomputations)
•	Message queue processing (monitor queue size, avoid unbounded growth)
•	Broadcast efficiency (reuse MessageRouter and NetworkSimulator, avoid duplicate work)
•	Memory usage for messageHistory and connection tracking
Expected Output: Incremental performance improvements guided by existing performance tests
 
File 24: CommunicationMetrics (unified view)
Purpose: Provide a simple, unified view over existing metrics (no duplicate state)
What It Tracks (mostly by aggregating existing data):
•	Messages per second (derived from messageHistory size over time)
•	Average latency (reusing ConnectionInfo.averageLatency and IncomingMessage.transmissionDelay)
•	Failed message percentage (from NetworkSimulator statistics, if available)
•	Topology update frequency and network health (from NeighborInformation / NetworkStatistics)
•	Memory usage indicators (e.g., current history size, queue size)
Implementation Note:
•	Prefer a small aggregator/helper over a heavy metrics subsystem
•	Recompute metrics from existing data structures instead of storing many new fields
Expected Output: Lightweight metrics API that surfaces what is already measured
 
Week 11-12 Success Criteria:
•	[ ] System handles 20+ agents at interactive rates (configuration-dependent)
•	[ ] No obvious memory growth from history/queues in long runs
•	[ ] Topology updates and routing remain responsive as agent count increases
•	[ ] Performance tests (existing and new) validate behaviour under load

Week 11-12 Status:
•	[✓] Performance metrics and tests in CommunicationSystemTest and COMMUNICATION_SYSTEM_SUMMARY.md aligned with this plan
•	[✓] CommunicationMetrics helper implemented as a thin aggregator (reads from existing CommunicationManager data, no duplicate state)
•	[✓] CommunicationMetricsSnapshot immutable data holder for consistent reporting
•	[✓] Integration test testPerformanceMetrics() validates metrics aggregation
 
Week 13-14: Final Integration & Testing
Goal: Production-ready system for demo - validate all components work together

File 25: IntegrationTest.java (already comprehensive)
Purpose: Comprehensive integration test suite covering all weeks
What Is Already Tested (Weeks 4-12):
•	Week 4: Message listener registration and callback delivery
•	Week 5: Vote proposal broadcasting and response handling
•	Week 6: Task assignment routing and network partition detection
•	Week 7-8: Consensus support (VotingProtocol integration)
•	Week 9-10: Fault tolerance (FailureRecoveryPolicy, partition helpers)
•	Week 11-12: Performance metrics aggregation
Additional Coverage (via CommunicationSystemTest.java):
•	Direct message delivery
•	Multi-hop routing
•	Broadcast messages
•	Network partitions
•	Failure recovery
•	Connection management
•	Performance validation
Expected Output: All integration tests pass, system ready for demo
 
File 26: Documentation (already comprehensive)
Purpose: Complete technical documentation
What Is Already Documented:
•	System architecture (COMMUNICATION_SYSTEM_SUMMARY.md)
•	API usage examples (inline JavaDoc, test examples)
•	Configuration options (NetworkSimulator, CommunicationManager constructors)
•	Week-by-week development guide (John's com.md)
•	Integration points (documented in John's com.md "Key Integration Points" section)
Expected Output: Professional documentation ready for team use

Week 13-14 Success Criteria:
•	[ ] All integration tests pass (IntegrationTest.java covers Weeks 4-12)
•	[ ] All unit tests pass (CommunicationSystemTest.java covers components)
•	[ ] Documentation is complete and up-to-date
•	[ ] System handles realistic scenarios (10+ agents, various message types)
•	[ ] Integration points with other components are clear and tested

Week 13-14 Status:
•	[✓] IntegrationTest.java covers all weeks 4-12 comprehensively
•	[✓] CommunicationSystemTest.java provides unit test coverage
•	[✓] COMMUNICATION_SYSTEM_SUMMARY.md documents architecture and implementation
•	[✓] John's com.md provides week-by-week development guide
•	[✓] All integration points documented in "Key Integration Points" section
 
Key Integration Points
With Sanidhaya (Core Agent System):
You Receive:
•	AgentStateUpdate (positions for topology calculation)
You Provide:
•	CommunicationEvent (message delivery notifications)
•	NetworkUpdate (topology changes)
With Lauren (Swarm Intelligence):
You Receive:
•	OutgoingMessage (votes, task assignments, coordination messages)
You Provide:
•	IncomingMessage (received votes and neighbor data)
•	NeighborInformation (for flocking calculations)
With Anthony (User Interface):
You Receive:
•	NetworkConfiguration (user-adjusted settings)
You Provide:
•	NetworkStatus (for visualization)
•	ConnectionInfo (for drawing communication links)
 
Success Metrics
Week 2:
Can calculate neighbor relationships from agent positions
Week 4:
Messages delivered between agents, visible in Anthony's UI
Week 6:
Multi-hop routing works, broadcasts work
Week 8:
Supports Lauren's voting system reliably
Week 10:
Handles failures and partitions gracefully
Week 12:
Production-ready with 20+ agents
 
Common Challenges & Solutions
Challenge: Topology calculations slow with many agents
Solution: Only recalculate when agents move significantly, use spatial partitioning
Challenge: Message storms from broadcasts
Solution: Implement controlled flooding with hop limits
Challenge: Hard to debug routing problems
Solution: Comprehensive logging, visualization of message paths
Challenge: Integration timing with other components
Solution: Use event-driven architecture, clear interfaces
 
This guide provides everything you need to build the communication system week by week. Focus on getting basic functionality working first, then add sophistication. Your work enables collective intelligence!
 
Communication System Development Guide for John
Team 6 - Distributed Multi-Agent System
Software Engineering Graduate Project

