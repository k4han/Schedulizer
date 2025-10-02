# Testing Guide for Schedulizer Conditions

This guide will help you test the new conditional task execution features.

## Test Scenario 1: Player Count Condition

### Setup
Edit your `schedule.yml`:
```yaml
schedules:
  player_count_test:
    enabled: true
    type: "repeat"
    interval: 1  # Every minute
    conditions:
      min-players: 2
    command:
      - "say At least 2 players online!"
```

### Test Steps
1. Start your Minecraft server with the plugin installed
2. Join the server alone - the message should NOT appear
3. Have a second player join - the message should now appear every minute
4. Have one player leave - the message should stop appearing

### Expected Result
The task only executes when 2 or more players are online.

## Test Scenario 2: Time of Day Condition

### Setup
Edit your `schedule.yml`:
```yaml
schedules:
  daytime_test:
    enabled: true
    type: "repeat"
    interval: 1
    conditions:
      time-of-day: "day"
    command:
      - "say It's daytime in Minecraft!"
  
  nighttime_test:
    enabled: true
    type: "repeat"
    interval: 1
    conditions:
      time-of-day: "night"
    command:
      - "say It's nighttime in Minecraft!"
```

### Test Steps
1. Use `/time set day` (or `/time set 0`) in Minecraft
2. Wait and observe - only the daytime message should appear
3. Use `/time set night` (or `/time set 13000`)
4. Wait and observe - only the nighttime message should appear

### Expected Result
Tasks respect the Minecraft day/night cycle:
- Day: 0-12000 ticks
- Night: 12000-24000 ticks

## Test Scenario 3: Combined Conditions

### Setup
Edit your `schedule.yml`:
```yaml
schedules:
  combined_test:
    enabled: true
    type: "repeat"
    interval: 1
    conditions:
      min-players: 2
      max-players: 5
      time-of-day: "night"
    command:
      - "say 2-5 players during nighttime!"
```

### Test Steps
1. Set time to night: `/time set night`
2. Join with 1 player - message should NOT appear
3. Have a 2nd player join - message should appear
4. Have players 3, 4, 5 join - message should continue
5. Have a 6th player join - message should STOP appearing
6. Set time to day: `/time set day` - message should STOP appearing (even with 2-5 players)

### Expected Result
Task only executes when ALL conditions are met:
- Between 2 and 5 players online
- AND it's nighttime in Minecraft

## Test Scenario 4: In-Game Commands

### Test Adding Conditions
```
/Schedulizer add testcmd 5 repeat say Hello
/Schedulizer condition testcmd minplayers:3
/Schedulizer condition testcmd timeofday:day
```

### Test Clearing Conditions
```
/Schedulizer clearcondition testcmd
```

### Test Multiple Conditions at Once
```
/Schedulizer condition testcmd minplayers:2 maxplayers:10 timeofday:night
```

### Expected Result
Commands should update the conditions in the configuration file and reload tasks automatically.

## Debugging Tips

1. Check the current Minecraft time:
   ```
   /time query daytime
   ```

2. Check online player count:
   ```
   /list
   ```

3. Enable task by name:
   ```
   /Schedulizer status <taskname> true
   ```

4. View all tasks:
   ```
   /Schedulizer list
   ```

5. Reload configuration after manual edits:
   ```
   /Schedulizer reload
   ```

## Common Issues

### Task not executing
- Verify the task is enabled: check `enabled: true` in schedule.yml
- Check that conditions are met (player count, time of day)
- Ensure the interval/time is correct
- Check server logs for any errors

### Minecraft time condition not working
- Verify you're checking the time in the main world (world index 0)
- Remember: day = 0-12000, night = 12000-24000
- Time wraps around at 24000 back to 0

### Player count condition not working
- Ensure you're testing with the exact player counts specified
- Remember: min-players is inclusive (≥), max-players is inclusive (≤)

## Validation Checklist

- [ ] Tasks execute only when min-players condition is met
- [ ] Tasks execute only when max-players condition is met
- [ ] Tasks execute only during specified time-of-day
- [ ] Combined conditions work correctly (all must be true)
- [ ] Tasks without conditions still work normally
- [ ] Commands can add/modify conditions
- [ ] Commands can clear conditions
- [ ] Configuration reload preserves conditions
- [ ] Invalid condition values are rejected

## Performance Notes

The condition checking happens every scheduler tick (configurable in config.yml, default 300 ticks = 15 seconds). This is very lightweight:
- Player count check: O(1) operation
- Time of day check: O(1) operation
- Total overhead per task: negligible

No performance impact expected even with dozens of conditional tasks.
