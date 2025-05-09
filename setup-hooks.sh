#!/bin/bash
# Script to set up Git hooks

# Terminal colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Setting up Git hooks...${NC}"

# Create hooks directory if it doesn't exist
mkdir -p .git/hooks

# Create pre-commit hook
cat > .git/hooks/pre-commit << 'EOL'
#!/bin/bash
# Pre-commit hook to clean repo before committing

# Execute cleanup script
./clean-repo.sh

# Stage the changes to .gitignore files
git add .gitignore
git add */logs/.gitkeep

exit 0
EOL

# Make pre-commit hook executable
chmod +x .git/hooks/pre-commit

echo -e "${GREEN}Git hooks set up successfully!${NC}"
echo -e "Pre-commit hook will automatically run ${YELLOW}./clean-repo.sh${NC} before each commit."