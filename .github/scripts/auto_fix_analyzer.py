#!/usr/bin/env python3
"""
Auto Fix Analyzer for Baubles LTS

This script:
1. Analyzes GitHub issues to determine if they can be automatically fixed
2. Identifies the type of fix needed
3. Outputs fix information for the workflow
"""

import os
import sys
import json
import argparse
import requests
import openai

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
    parser = argparse.ArgumentParser(description="Analyze GitHub issues for auto-fixing")
    parser.add_argument("--repo", required=True, help="Repository in format owner/repo")
    parser.add_argument("--issue-number", required=True, help="Issue number to analyze")
    return parser.parse_args()

def get_issue_details(repo, issue_number):
    """Fetch issue details from GitHub API."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching issue: {response.status_code}")
        print(response.text)
        sys.exit(1)
    
    return response.json()

def get_issue_comments(repo, issue_number):
    """Fetch comments for an issue from GitHub API."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/comments"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching comments: {response.status_code}")
        print(response.text)
        sys.exit(1)
    
    return response.json()

def get_repo_files(repo, path=""):
    """Fetch repository file structure."""
    url = f"https://api.github.com/repos/{repo}/contents/{path}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching repo files: {response.status_code}")
        print(response.text)
        return []
    
    return response.json()

def analyze_with_openai(issue_data, comments_data, repo_files):
    """Use OpenAI to analyze if the issue is automatically fixable."""
    try:
        # Prepare context for GPT
        issue_title = issue_data["title"]
        issue_body = issue_data["body"] or ""
        issue_labels = [label["name"] for label in issue_data["labels"]]
        
        # Format comments for context
        comments_text = ""
        if comments_data:
            for comment in comments_data:
                comments_text += f"\nComment by {comment['user']['login']}:\n{comment['body']}\n"
        
        # Simplify repo files for context
        repo_files_text = "\n".join([f.get("path", "") for f in repo_files if f.get("type") == "file"][:50])
        
        # Create prompt
        prompt = f"""
        You are an AI assistant for the Baubles LTS Minecraft mod. Analyze this GitHub issue to determine if it can be automatically fixed.
        
        ISSUE #{issue_data['number']}: {issue_title}
        LABELS: {', '.join(issue_labels)}
        
        ISSUE DESCRIPTION:
        {issue_body}
        
        COMMENTS:
        {comments_text}
        
        REPOSITORY FILE STRUCTURE (partial):
        {repo_files_text}
        
        Based on this information, please determine:
        
        1. Is this issue a bug that can be automatically fixed? Consider:
           - Simple syntax errors
           - Performance optimizations
           - Memory leaks
           - Configuration issues
           - Compatibility issues with clear solutions
           - Typo fixes
        
        2. What specific files would need to be modified to fix this issue?
        
        3. What would the fix involve at a high level?
        
        Answer these questions and then provide a final assessment: CAN_FIX=Yes or CAN_FIX=No.
        If Yes, also provide FILES_TO_MODIFY=[comma-separated list of file paths] and FIX_TYPE=[simple|complex].
        """
        
        # Call OpenAI API
        completion = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are a helpful AI assistant that specializes in Minecraft mod development, particularly for the Baubles API. You analyze GitHub issues to determine if they can be automatically fixed."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=1500
        )
        
        # Extract response
        analysis = completion.choices[0].message.content
        
        # Parse the response to get CAN_FIX, FILES_TO_MODIFY, FIX_TYPE
        can_fix = "No"
        files_to_modify = []
        fix_type = "complex"
        
        if "CAN_FIX=Yes" in analysis:
            can_fix = "Yes"
            
            # Extract files to modify
            import re
            files_match = re.search(r"FILES_TO_MODIFY=\[(.*?)\]", analysis)
            if files_match:
                files_list = files_match.group(1).strip()
                files_to_modify = [f.strip() for f in files_list.split(",")]
            
            # Extract fix type
            fix_type_match = re.search(r"FIX_TYPE=(\w+)", analysis)
            if fix_type_match:
                fix_type = fix_type_match.group(1).strip()
        
        return {
            "analysis": analysis,
            "can_fix": can_fix,
            "files_to_modify": files_to_modify,
            "fix_type": fix_type
        }
    
    except Exception as e:
        print(f"Error analyzing with OpenAI: {str(e)}")
        return {
            "analysis": "",
            "can_fix": "No",
            "files_to_modify": [],
            "fix_type": "complex"
        }

def set_output(name, value):
    """Set GitHub Actions output variable."""
    with open(os.environ.get("GITHUB_OUTPUT", ""), "a") as f:
        f.write(f"{name}={value}\n")

def main():
    """Main function."""
    args = parse_args()
    
    # Fetch data
    issue_data = get_issue_details(args.repo, args.issue_number)
    comments_data = get_issue_comments(args.repo, args.issue_number)
    repo_files = get_repo_files(args.repo)
    
    # Analyze the issue
    analysis_result = analyze_with_openai(issue_data, comments_data, repo_files)
    
    print(f"Analysis result: {analysis_result['analysis']}")
    print(f"Can fix: {analysis_result['can_fix']}")
    print(f"Files to modify: {', '.join(analysis_result['files_to_modify'])}")
    print(f"Fix type: {analysis_result['fix_type']}")
    
    # Set outputs for GitHub Actions
    set_output("can_fix", "true" if analysis_result['can_fix'] == "Yes" else "false")
    set_output("issue_number", args.issue_number)
    set_output("files_to_modify", ",".join(analysis_result['files_to_modify']))
    set_output("fix_type", analysis_result['fix_type'])
    
    # Post analysis as a comment
    url = f"https://api.github.com/repos/{args.repo}/issues/{args.issue_number}/comments"
    body = f"""
## Automated Fix Analysis

I've analyzed this issue to determine if it can be automatically fixed.

{analysis_result['analysis']}

**Automated Fix Status:** {"I'll attempt to fix this issue automatically." if analysis_result['can_fix'] == "Yes" else "This issue cannot be automatically fixed and will require manual intervention."}
"""
    
    response = requests.post(url, headers=HEADERS, json={"body": body})
    if response.status_code != 201:
        print(f"Error posting comment: {response.status_code}")
        print(response.text)

if __name__ == "__main__":
    main()