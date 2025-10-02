# Schedulizer

A Minecraft plugin for scheduling automated tasks with advanced conditional execution.

## Features

- **Multiple schedule types**: One-time, daily, and repeating tasks
- **Conditional execution**: Execute tasks based on player count and Minecraft time of day
- **Easy configuration**: YAML-based configuration with in-game commands
- **Time zone support**: Configure your preferred timezone
- **Flexible commands**: Execute multiple commands per task

## Schedule Types

### Once
Execute a task at a specific date and time (only once).
```yaml
schedules:
  one_time_event:
    enabled: true
    type: "once"
    time: "2025-02-08 15:30:00"
    command:
      - "say This will run once!"
```

### Daily
Execute a task every day at a specific time.
```yaml
schedules:
  daily_reset:
    enabled: true
    type: "daily"
    time: "12:00:00"
    command:
      - "say Daily event!"
```

### Repeat
Execute a task repeatedly at a fixed interval (in minutes).
```yaml
schedules:
  repeating_event:
    enabled: true
    type: "repeat"
    interval: 5  # Every 5 minutes
    command:
      - "say Repeating event!"
```

## Conditional Execution

Tasks can be configured with conditions that must be met before execution:

### Player Count Conditions
- **min-players**: Minimum number of online players required
- **max-players**: Maximum number of online players allowed

### Time of Day Condition
- **time-of-day**: Execute only during Minecraft day or night
  - `day`: 0-12000 ticks (daytime)
  - `night`: 12000-24000 ticks (nighttime)

### Example with Conditions
```yaml
schedules:
  daytime_event:
    enabled: true
    type: "repeat"
    interval: 10
    conditions:
      min-players: 2
      time-of-day: "day"
    command:
      - "say Daytime event with at least 2 players!"
  
  nighttime_event:
    enabled: true
    type: "daily"
    time: "20:00:00"
    conditions:
      max-players: 5
      time-of-day: "night"
    command:
      - "say Nighttime event with few players!"
```

## Commands

All commands start with `/Schedulizer` or the configured alias.

### Basic Commands
- `/Schedulizer help` - Show all available commands
- `/Schedulizer list` - List all tasks
- `/Schedulizer reload` - Reload configuration files

### Task Management
- `/Schedulizer add <name> <time> <type> <command>` - Add a new task
  - Example: `/Schedulizer add mytask 10:00:00 daily say Hello!`
- `/Schedulizer remove <name>` - Remove a task
- `/Schedulizer time <name> <time>` - Update task time
- `/Schedulizer status <name> <true|false>` - Enable/disable a task
- `/Schedulizer cmd <name> <command>; <command>` - Update task commands (separate multiple commands with `;`)

### Condition Management
- `/Schedulizer condition <name> minplayers:<value> maxplayers:<value> timeofday:<day|night>` - Set task conditions
  - Example: `/Schedulizer condition mytask minplayers:2 timeofday:day`
  - You can specify one or more conditions in a single command
- `/Schedulizer clearcondition <name>` - Remove all conditions from a task

## Configuration

### config.yml
```yaml
# The number of ticks per scheduler run
# 20 ticks = 1 second, recommended 300 ticks = 15 seconds
tick: 300

# Your timezone
timezone: "Asia/Ho_Chi_Minh"

# Date/time format for one-time tasks
datetime-format: "dd/MM/yyyy HH:mm"
```

### schedule.yml
All tasks are defined in `schedule.yml`. See examples above.

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Edit `config.yml` and `schedule.yml` in the `plugins/Schedulizer` folder
5. Use `/Schedulizer reload` to reload changes

## Use Cases

### Event Management
- Schedule automatic events during peak hours (with player count conditions)
- Run nighttime-specific events (with time-of-day condition)

### Server Maintenance
- Daily backups at specific times
- Periodic announcements (with appropriate player count)

### Gameplay Mechanics
- Day/night cycle events
- Population-based rewards
- Timed competitions

## License

See LICENSE file for details.
