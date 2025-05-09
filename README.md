Baubles LTS
===========

A performance-optimized fork of the Baubles mod API for Minecraft Forge 1.12.2, which adds 7 bauble slots to players' inventory:
- Amulet slot
- Belt slot
- Head slot
- Body slot
- Charm slot
- Two ring slots

This fork maintains complete backward compatibility with the original Baubles mod while providing significant performance improvements. It serves as a drop-in replacement for other mods that depend on Baubles.

## Performance Improvements

The following optimizations have been implemented while maintaining exact API compatibility:

1. **Storage optimizations**:
   - Improved item addition/removal with more efficient array operations
   - Enhanced slot management with optimized resizing algorithms
   - Reduced redundant object creation during inventory modifications

2. **Memory management**:
   - Optimized buffer handling to reduce memory allocations
   - Improved reference management to prevent memory leaks
   - More efficient network packet serialization/deserialization

3. **Capability system improvements**:
   - Faster capability validation with optimized type checking
   - More efficient capability serialization and copying
   - Fixed critical bugs in storage capability system

4. **Network optimizations**:
   - Enhanced packet buffer handling with proper resource management
   - Reduced unnecessary buffer copying during network operations
   - Optimized client/server synchronization

The mod has no content of its own - it serves as an API foundation for other mods to build upon.

## License
Baubles LTS is distributed under the Attribution-NonCommercial-ShareAlike 3.0 Unported (CC BY-NC-SA 3.0) license.

## Credits
- [Original Baubles mod By:Azanor13](https://www.curseforge.com/minecraft/mc-mods/baubles)
- Performance optimizations by SevenToDie team
