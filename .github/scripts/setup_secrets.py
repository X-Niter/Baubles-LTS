#!/usr/bin/env python3
"""
Setup Secrets for GitHub Actions

This script helps the user set up the necessary secrets for GitHub Actions workflows.
"""

import os
import sys
import subprocess
import json
import getpass

def check_gh_cli():
    """Check if GitHub CLI is installed."""
    try:
        subprocess.run(["gh", "--version"], capture_output=True, check=True)
        return True
    except (subprocess.SubprocessError, FileNotFoundError):
        return False

def check_authentication():
    """Check if authenticated with GitHub CLI."""
    try:
        result = subprocess.run(["gh", "auth", "status"], capture_output=True, text=True)
        return result.returncode == 0
    except subprocess.SubprocessError:
        return False

def get_repo_info():
    """Get repository owner and name."""
    try:
        result = subprocess.run(
            ["git", "config", "--get", "remote.origin.url"],
            capture_output=True, text=True, check=True
        )
        
        remote_url = result.stdout.strip()
        
        # Handle different remote URL formats
        if remote_url.startswith("https://github.com/"):
            # https://github.com/owner/repo.git
            parts = remote_url.split("/")
            owner = parts[-2]
            repo = parts[-1].replace(".git", "")
        elif remote_url.startswith("git@github.com:"):
            # git@github.com:owner/repo.git
            parts = remote_url.split(":")
            owner_repo = parts[-1].replace(".git", "")
            owner, repo = owner_repo.split("/")
        else:
            raise ValueError(f"Unsupported remote URL format: {remote_url}")
        
        return owner, repo
    except Exception as e:
        print(f"Error getting repo info: {str(e)}")
        owner = input("Enter repository owner: ")
        repo = input("Enter repository name: ")
        return owner, repo

def list_existing_secrets(owner, repo):
    """List existing GitHub secrets."""
    try:
        result = subprocess.run(
            ["gh", "secret", "list", "-R", f"{owner}/{repo}"],
            capture_output=True, text=True
        )
        
        if result.returncode == 0:
            print("\nExisting secrets:")
            print(result.stdout)
            
            # Extract secret names
            secrets = []
            for line in result.stdout.strip().split("\n")[1:]:  # Skip header
                if line.strip():
                    secrets.append(line.split()[0])
            
            return secrets
        return []
    except Exception as e:
        print(f"Error listing secrets: {str(e)}")
        return []

def set_secret(owner, repo, name, value):
    """Set a GitHub secret."""
    try:
        proc = subprocess.Popen(
            ["gh", "secret", "set", name, "-R", f"{owner}/{repo}"],
            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            text=True
        )
        
        stdout, stderr = proc.communicate(input=value)
        
        if proc.returncode == 0:
            print(f"✅ Successfully set secret: {name}")
            return True
        else:
            print(f"❌ Failed to set secret {name}: {stderr}")
            return False
    except Exception as e:
        print(f"Error setting secret {name}: {str(e)}")
        return False

def main():
    """Main function."""
    print("=== GitHub Actions Secrets Setup ===")
    
    # Check for GitHub CLI
    if not check_gh_cli():
        print("❌ GitHub CLI (gh) not found. Please install it:")
        print("- macOS: brew install gh")
        print("- Windows: winget install -e --id GitHub.cli")
        print("- Linux: https://github.com/cli/cli/blob/trunk/docs/install_linux.md")
        sys.exit(1)
    
    # Check authentication
    if not check_authentication():
        print("❌ Not authenticated with GitHub CLI. Please run:")
        print("gh auth login")
        sys.exit(1)
    
    # Get repository info
    owner, repo = get_repo_info()
    print(f"\nRepository: {owner}/{repo}")
    
    # List existing secrets
    existing_secrets = list_existing_secrets(owner, repo)
    
    # Define required secrets
    required_secrets = [
        {
            "name": "OPENAI_API_KEY",
            "description": "OpenAI API key for AI-assisted automation",
            "sensitive": True
        }
    ]
    
    # Process each required secret
    for secret_info in required_secrets:
        name = secret_info["name"]
        
        if name in existing_secrets:
            update = input(f"\nSecret '{name}' already exists. Update it? (y/n): ").lower() == 'y'
            if not update:
                continue
        
        print(f"\n{secret_info['description']}")
        if secret_info.get("sensitive", False):
            value = getpass.getpass(f"Enter value for {name}: ")
        else:
            value = input(f"Enter value for {name}: ")
        
        set_secret(owner, repo, name, value)
    
    print("\n✅ Secrets setup complete!")
    print("The GitHub Actions workflows should now be able to run properly.")

if __name__ == "__main__":
    main()