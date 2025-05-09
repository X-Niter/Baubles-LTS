# Baubles LTS Changelog

## Version 1.12.2-1.0.7.0-LTS (May 9, 2025)

### Performance Optimization Update

This Long-Term Support (LTS) update focuses on significantly improving performance while maintaining 100% backward compatibility with existing mods that depend on Baubles.

#### Memory Optimization
- **Object Pooling System**: Implemented a sophisticated object pooling mechanism for network packets that recycles packet instances instead of creating new ones. This substantially reduces garbage collection pressure during high-traffic inventory operations.
- **String Interning**: Added an optimized string interning system to reduce memory duplications in network operations. This improves memory efficiency for heavily-modded environments where many baubles are synced.
- **Type Caching**: Implemented a cache for BaubleType validation to eliminate redundant capability lookups, significantly improving performance during inventory operations.

#### Synchronization Improvements
- **Enhanced Packet Management**: Completely reworked the packet synchronization system with more efficient data structures and object reuse patterns.
- **Pre-computation Strategy**: Added smart pre-computation of frequently used values in inventory management code.
- **Optimized Update Logic**: Reduced redundant iterations and object creation in inventory synchronization code.

#### Code Efficiency
- **Early Exit Patterns**: Implemented strategic early exits in validation methods to avoid unnecessary processing.
- **Reduced Object Creation**: Eliminated multiple unnecessary object instantiations throughout the codebase.
- **Improved Collection Usage**: Replaced inefficient collection operations with more performant alternatives.

#### Diagnostics & Configuration
- **Debug Mode**: Added configuration option for performance statistics tracking through the `debug.enabled` setting.
- **Performance Monitoring**: Added the ability to monitor memory usage and object creation statistics when debug mode is enabled.

#### API & Compatibility
- **Full Backward Compatibility**: All optimizations have been carefully implemented to maintain 100% API compatibility with mods that depend on Baubles.
- **No Breaking Changes**: Zero changes to the public API surface - all mods using Baubles will continue to work without modification.
- **Drop-In Replacement**: This LTS version serves as a direct performance-focused replacement for previous Baubles versions.

#### Technical Details
- Object pools are self-limiting to prevent memory leaks
- String interning uses weak references for proper garbage collection
- Type caching uses enum-optimized maps for improved lookup speed
- All optimizations have been extensively tested for memory safety

#### Full Changelog History
See [changelog.txt](src/main/resources/changelog.txt) for the complete version history.