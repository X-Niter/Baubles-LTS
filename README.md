# SevenToDie Baubles Plugin

A high-performance Minecraft plugin adding a baubles equipment system for Paper/Spigot servers. This plugin adds 7 bauble slots to players' inventory:
- Amulet slot
- Belt slot
- Head slot
- Body slot
- Charm slot
- Two ring slots

This plugin is inspired by the original Baubles mod for Forge but rebuilt from scratch for modern Paper servers.

## Features

- **Bauble Slot System**: Adds 7 special equipment slots for magical items
- **API for Developers**: Simple API for creating custom bauble items with special effects
- **Performance Optimized**: Built with efficiency in mind to minimize server impact
- **Command System**: Easy-to-use `/baubles` command to open the bauble inventory
- **Event System**: Comprehensive events for bauble equip/unequip actions

## Performance Optimizations

The following optimizations have been implemented to ensure maximum server performance:

1. **Storage optimizations**:
   - Efficient item management with optimized data structures
   - Enhanced slot access with fast lookup algorithms
   - Reduced redundant object creation during inventory interactions

2. **Memory management**:
   - Object pooling for frequently used network packets
   - String interning for common text values
   - Type caching for faster validation checks

3. **Event handling improvements**:
   - Optimized event dispatching to reduce overhead
   - Smart event filtering to prevent unnecessary processing
   - Efficient listener registration and management

4. **Thread safety**:
   - Proper concurrency handling for player data
   - Safe shared resource access
   - Optimized synchronization strategies

## Development

### Repository Maintenance

This repository includes maintenance scripts to keep the codebase clean:

- **clean-repo.sh**: Run this script before committing to remove temporary files and build artifacts
  ```
  ./clean-repo.sh
  ```

- **setup-hooks.sh**: Run this script after cloning to set up Git hooks
  ```
  ./setup-hooks.sh
  ```

- **Pre-commit Hook**: The repository includes a Git pre-commit hook that automatically runs the cleaning script before each commit

### Building

The project can be built using Maven:

```
mvn clean package
```

The built JAR will be in the `target` directory.

## Installation

1. Place the JAR file in your server's `plugins` directory
2. Restart your server
3. Configure the plugin in the `plugins/SevenToDie/config.yml` file if needed

## License

SevenToDie is distributed under the Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) license.

## Credits
- Original concept inspired by [Baubles mod By:Azanor13](https://www.curseforge.com/minecraft/mc-mods/baubles)
- Developed by the SevenToDie team
