#!/bin/bash
# Script to clean temporary and unnecessary files before committing to Git

# Terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Cleaning repository before commit...${NC}"

# Remove build and compilation outputs
echo -e "${GREEN}Removing build artifacts...${NC}"
rm -rf target/
rm -rf build/
rm -rf .gradle/

# Clean up server files
echo -e "${GREEN}Cleaning server temporary files...${NC}"
rm -rf server/logs/*.log*
rm -rf server/crash-reports/
rm -rf server/debug/
rm -rf server/cache/
rm -rf server/world/session.lock
rm -rf server/world_nether/session.lock
rm -rf server/world_the_end/session.lock

# Make sure log directory exists with .gitkeep
mkdir -p server/logs
touch server/logs/.gitkeep

# Remove any IDE specific temporary files
echo -e "${GREEN}Removing IDE temporary files...${NC}"
find . -name "*.swp" -type f -delete
find . -name "*~" -type f -delete
find . -name ".DS_Store" -type f -delete

# Remove any temporary clones
echo -e "${GREEN}Removing temp directories...${NC}"
rm -rf temp_clone/

echo -e "${GREEN}Done! Repository cleaned.${NC}"