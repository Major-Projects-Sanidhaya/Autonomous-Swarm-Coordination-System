/**
 * TASK COMPLETION REPORT - Task Status Notification Data
 *
 * PURPOSE:
 * - Notifies system when agent completes/fails assigned task
 * - Enables task reassignment and progress tracking
 * - Supports multi-agent coordination and task allocation
 *
 * MAIN COMPONENTS:
 * 1. Task Identification - Which task was completed
 * 2. Agent Identification - Which agent did the work
 * 3. Completion Status - Success/failure/partial
 * 4. Results Data - Outcome information
 * 5. Timestamp - When task completed
 *
 * COMPLETION STATUSES:
 * - SUCCESS: Task completed successfully
 * - FAILED: Agent unable to complete task
 * - PARTIAL: Partial completion (e.g., reached waypoint but battery low)
 * - CANCELLED: Task aborted due to higher priority command
 * - TIMEOUT: Task took too long to complete
 *
 * CORE FIELDS:
 * - taskId: Identifier of the task
 * - agentId: Agent that performed the task
 * - status: Completion outcome
 * - resultData: Task-specific output (Map<String, Object>)
 * - completionTime: When task finished
 * - duration: How long task took
 *
 * USAGE PATTERN:
 * 1. Agent finishes task execution
 * 2. Creates TaskCompletionReport with results
 * 3. Publishes to EventBus
 * 4. Task coordinator receives report
 * 5. Updates task list and assigns new tasks if needed
 *
 * RESULT DATA EXAMPLES:
 * For navigation task:
 *   resultData: {
 *     "finalPosition": Point2D(150, 200),
 *     "pathLength": 250.5,
 *     "batteryUsed": 0.15
 *   }
 *
 * For formation task:
 *   resultData: {
 *     "formationAccuracy": 0.95,
 *     "maintainedDuration": 45.2
 *   }
 *
 * INTEGRATION POINTS:
 * - Created by: Agents when tasks complete
 * - Published to: EventBus
 * - Consumed by: Task coordination system, SystemController
 * - Triggers: New task assignment, metric updates
 *
 * EXPECTED OUTPUTS:
 * - Console: "Agent 5 completed task MOVE_TO_TARGET (SUCCESS)"
 * - Metrics: Task completion rate, average duration
 * - Events: May trigger new task assignments
 */
package com.team6.swarm.core;

public class TaskCompletionReport {

}
