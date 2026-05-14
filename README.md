# Schedulizer

**Schedulizer** is a Minecraft Paper/Purpur/Folia plugin that allows you to schedule and automatically execute in-game commands with flexible scheduling options.

## Features

- ✅ **Once**: Execute once at a specified time
- ✅ **Daily**: Execute daily at a specified time
- ✅ **Repeat**: Execute repeatedly at intervals (minutes)
- ✅ **Cron**: Support for flexible UNIX cron expressions
- ✅ Easy configuration via YAML files
- ✅ Custom timezone support
- ✅ Complete command management

## Requirements

- **Server**: Paper/Purpur/Folia 1.21+
- **Java**: 21+

Note: Scheduled commands are dispatched through the server console. On Folia, Schedulizer uses the global region scheduler; the commands or plugins you run still need to be safe for the target server platform.

## Installation

1. Download the `.jar` file from [Releases](https://github.com/virusker/Schedulizer/releases)
2. Place the file in your server's `plugins` folder
3. Start the server to let the plugin create configuration files
4. Edit `plugins/Schedulizer/schedule.yml` to add tasks
5. Run `/schedulizer reload` to apply changes

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/schedulizer help` | Show help information | `schedulizer.command.help` |
| `/schedulizer list` | List all tasks | `schedulizer.command.list` |
| `/schedulizer reload` | Reload configuration | `schedulizer.command.reload` |
| `/schedulizer add <name> <type> <time...> <command>` | Add a new task (overwrite if exists) | `schedulizer.command.add` |
| `/schedulizer remove <name>` | Remove a task | `schedulizer.command.remove` |
| `/schedulizer status <name> <true/false>` | Enable/disable task | `schedulizer.command.status` |
| `/schedulizer cmd <name> <command>` | Update task command | `schedulizer.command.cmd` |
| `/schedulizer info <name>` | View task details | `schedulizer.command.info` |
| `/schedulizer execute <name>` | Execute task immediately | `schedulizer.command.execute` |

Notes:

- `once`: time can include a space (e.g. `18/03/2026 20:00`), so it is provided as multiple arguments.
- `cron`: provide 5 arguments: `minute hour day month weekday` (e.g. `0 0 * * *`).

**Aliases**: `/schedulizer`, `/sched`

## Configuration

### config.yml

```yaml
# Number of ticks between each check (20 ticks = 1 second)
# 300 ticks = 15 seconds (default)
tick: 300

# Timezone (see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)
timezone: "Asia/Ho_Chi_Minh"

# Date time format
datetime-format: "dd/MM/yyyy HH:mm"
```

### schedule.yml

```yaml
schedules:
  # One-time task
  first_time_event:
    enabled: true
    type: "once"
    time: "08/02/2025 15:30" # dd/MM/yyyy HH:mm
    command:
    - "say One time event!"

  # Daily task
  daily_reset:
    enabled: true
    type: "daily"
    time: "12:00" # HH:mm
    command:
    - "say New daily!"

  # Repeating task
  repeating_event:
    enabled: true
    type: "repeat"
    interval: 5  # minute
    command:
    - "say Repeating!"

  # Cron task
  cron_event:
    enabled: true
    type: "cron"
    cron: "0 0 * * *"  # Every day at 00:00
    command:
    - "say Cron event!"
```

## Time Formats by Task Type

| Type | Format | Example |
|------|--------|---------|
| `once` | `dd/MM/yyyy HH:mm` | `25/12/2025 20:00` |
| `daily` | `HH:mm` | `14:30` |
| `repeat` | Minutes (integer) | `5`, `60`, `1440` |
| `cron` | UNIX cron expression | `0 0 * * *` |

## Cron Expressions

Uses standard UNIX cron format: `minute hour day month weekday`

Examples:

| Cron | Description |
|------|-------------|
| `0 0 * * *` | Every day at 00:00 |
| `*/15 * * * *` | Every 15 minutes |
| `0 8 * * 1-5` | 8:00 AM Monday to Friday |
| `0 0 1 * *` | 1st day of every month |

👉 Cron generator tool: [crontab.guru](https://crontab.guru/)

## Permissions

```yaml
schedulizer.use              # Access to all commands
schedulizer.command.help     # Help command
schedulizer.command.list     # List tasks
schedulizer.command.reload   # Reload config
schedulizer.command.add      # Add tasks
schedulizer.command.remove   # Remove tasks
schedulizer.command.status   # Enable/disable
schedulizer.command.cmd      # Update command
schedulizer.command.info     # View info
schedulizer.command.execute  # Execute immediately
```

## Building from Source

```bash
git clone https://github.com/virusker/Schedulizer.git
cd Schedulizer
./gradlew build
```

The `.jar` file will be in `build/libs/`

## License

This project is licensed under the [MIT License](LICENSE)


## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request on GitHub.

---

## ☕ Support

If you find this plugin helpful and want to support its development, consider buying me a coffee!

[![Buy Me A Coffee](https://img.shields.io/badge/Buy_Me_A_Coffee-FFDD00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/kh4n)
