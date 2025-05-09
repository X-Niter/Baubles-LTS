#!/usr/bin/env python3
"""
Find Fixable Issues Script for Baubles LTS

This script:
1. Searches for open issues that might be automatically fixable
2. Analyzes them using AI to determine if they can be fixed
3. Triggers the auto-fix process for eligible issues
"""

import os
import sys
import json
import argparse
import requests
import openai
import time
from datetime import datetime, timedelta

# Configure OpenAI API
openai.api_key = os.environ.get("OPENAI_API_KEY")
if not openai.api_key:
    print("Error: OPENAI_API_KEY environment variable not set")
    sys.exit(1)

# GitHub API configuration
GITHUB_TOKEN = os.environ.get("GITHUB_TOKEN")
if not GITHUB_TOKEN:
    print("Error: GITHUB_TOKEN environment variable not set")
    sys.exit(1)

HEADERS = {
    "Authorization": f"token {GITHUB_TOKEN}",
    "Accept": "application/vnd.github.v3+json"
}

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description="Find fixable issues in GitHub repository")
    parser.add_argument("--repo", required=True, help="Repository in format owner/repo")
    parser.add_argument("--limit", type=int, default=5, help="Maximum number of issues to process")
    parser.add_argument("--days", type=int, default=30, help="Consider issues from the last N days")
    return parser.parse_args()

def get_open_issues(repo, days_ago, limit):
    """Fetch open issues from GitHub API."""
    # Calculate date from N days ago
    since_date = (datetime.now() - timedelta(days=days_ago)).strftime('%Y-%m-%dT%H:%M:%SZ')
    
    url = f"https://api.github.com/repos/{repo}/issues"
    params = {
        "state": "open",
        "sort": "created",
        "direction": "desc",
        "since": since_date,
        "per_page": limit
    }
    
    response = requests.get(url, headers=HEADERS, params=params)
    
    if response.status_code != 200:
        print(f"Error fetching issues: {response.status_code}")
        print(response.text)
        sys.exit(1)
    
    # Filter out pull requests (they're also considered issues in GitHub API)
    issues = [issue for issue in response.json() if "pull_request" not in issue]
    
    return issues

def evaluate_issue_fixability(issue):
    """Use OpenAI to evaluate if an issue is fixable automatically."""
    try:
        issue_title = issue["title"]
        issue_body = issue["body"] or ""
        issue_number = issue["number"]
        issue_labels = [label["name"] for label in issue.get("labels", [])]
        
        # Skip issues already labeled as "auto-fix" or "needs-human-review"
        if "auto-fix" in issue_labels or "needs-human-review" in issue_labels:
            return False
        
        # Create prompt for fixability analysis
        prompt = f"""
        You are an AI assistant for the Baubles LTS Minecraft mod. Analyze this GitHub issue to determine if it can be automatically fixed.
        
        ISSUE #{issue_number}: {issue_title}
        ISSUE DESCRIPTION:
        {issue_body}
        
        Based on this information, determine if this issue meets these criteria:
        1. Is it a clear bug report or performance issue?
        2. Is the issue well-described with enough detail to understand the problem?
        3. Does it seem like a simple fix that could be implemented with just a few lines of code change?
        4. Is it a specific technical issue rather than a feature request or user confusion?
        5. Has the issue been reproduced or verified by multiple users?
        
        Answer ONLY YES or NO, with no additional explanation.
        """
        
        # Call OpenAI API
        completion = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are a helpful AI assistant that specializes in Minecraft mod development. You evaluate if issues can be automatically fixed."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=5
        )
        
        # Extract response
        response = completion.choices[0].message.content.strip().upper()
        return response == "YES"
    
    except Exception as e:
        print(f"Error evaluating issue #{issue['number']}: {str(e)}")
        return False

def add_auto_fix_label(repo, issue_number):
    """Add auto-fix label to the issue."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/labels"
    payload = {
        "labels": ["auto-fix"]
    }
    
    response = requests.post(url, headers=HEADERS, json=payload)
    
    if response.status_code != 200:
        print(f"Error adding label: {response.status_code}")
        print(response.text)
        return False
    
    return True

def post_fixability_comment(repo, issue_number, is_fixable):
    """Post a comment about issue fixability."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/comments"
    
    if is_fixable:
        body = """
## Automated Fix Eligibility

I've analyzed this issue and determined that it may be eligible for an automated fix.
I'm adding the `auto-fix` label to this issue, and our automation system will attempt to generate a fix.

If a fix can be generated, it will be submitted as a pull request for review.
"""
    else:
        body = """
## Automated Fix Eligibility

I've analyzed this issue and determined that it likely requires manual intervention.
The issue appears to be too complex or lacks sufficient details for an automated fix.

A human developer will need to review this issue. Thank you for your patience.
"""
        # Also add needs-human-review label
        add_label_url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/labels"
        requests.post(add_label_url, headers=HEADERS, json={"labels": ["needs-human-review"]})
    
    response = requests.post(url, headers=HEADERS, json={"body": body})
    
    if response.status_code != 201:
        print(f"Error posting comment: {response.status_code}")
        print(response.text)
        return False
    
    return True

def trigger_auto_fix_workflow(repo, issue_number):
    """Trigger auto-fix workflow for an issue."""
    url = f"https://api.github.com/repos/{repo}/actions/workflows/auto-fix.yml/dispatches"
    payload = {
        "ref": "master",
        "inputs": {
            "issue_number": str(issue_number)
        }
    }
    
    response = requests.post(url, headers=HEADERS, json=payload)
    
    if response.status_code != 204:
        print(f"Error triggering workflow: {response.status_code}")
        print(response.text)
        return False
    
    return True

def main():
    """Main function."""
    args = parse_args()
    
    # Get open issues
    issues = get_open_issues(args.repo, args.days, args.limit)
    print(f"Found {len(issues)} open issues to evaluate")
    
    # Process each issue
    for issue in issues:
        issue_number = issue["number"]
        print(f"Evaluating issue #{issue_number}: {issue['title']}")
        
        # Respect rate limits
        time.sleep(1)
        
        # Evaluate if the issue is fixable
        is_fixable = evaluate_issue_fixability(issue)
        print(f"Issue #{issue_number} fixability: {is_fixable}")
        
        if is_fixable:
            # Add auto-fix label
            if add_auto_fix_label(args.repo, issue_number):
                print(f"Added auto-fix label to issue #{issue_number}")
            
            # Post comment
            if post_fixability_comment(args.repo, issue_number, True):
                print(f"Posted fixability comment to issue #{issue_number}")
            
            # Trigger auto-fix workflow
            if trigger_auto_fix_workflow(args.repo, issue_number):
                print(f"Triggered auto-fix workflow for issue #{issue_number}")
        else:
            # Post not fixable comment
            if post_fixability_comment(args.repo, issue_number, False):
                print(f"Posted not fixable comment to issue #{issue_number}")
        
        # Respect rate limits
        time.sleep(2)

if __name__ == "__main__":
    main()