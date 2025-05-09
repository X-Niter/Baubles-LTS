#!/usr/bin/env python3
"""
Issue Analyzer for Baubles LTS

This script analyzes GitHub issues using OpenAI to automatically:
1. Understand the issue content and context
2. Generate appropriate responses
3. Suggest code changes when relevant
4. Recommend appropriate actions for maintenance
"""

import os
import sys
import json
import argparse
import requests
import re
import openai
from datetime import datetime

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
    parser = argparse.ArgumentParser(description="Analyze GitHub issues")
    parser.add_argument("--repo", required=True, help="Repository in format owner/repo")
    parser.add_argument("--issue-number", required=True, help="Issue number to analyze")
    parser.add_argument("--trigger-event", required=True, choices=["issues", "issue_comment", "workflow_dispatch", "schedule"], help="Event that triggered this workflow")
    parser.add_argument("--event-action", required=True, help="Action of the event (opened, edited, etc.)")
    parser.add_argument("--comment-id", help="ID of the comment to respond to (for issue_comment event)")
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

def get_repo_details(repo):
    """Fetch repository details and metadata."""
    # Get repository information
    url = f"https://api.github.com/repos/{repo}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching repository details: {response.status_code}")
        print(response.text)
        sys.exit(1)
    
    repo_info = response.json()
    
    # Get latest release
    url = f"https://api.github.com/repos/{repo}/releases/latest"
    response = requests.get(url, headers=HEADERS)
    
    latest_release = None
    if response.status_code == 200:
        latest_release = response.json()
    
    # Get top contributors
    url = f"https://api.github.com/repos/{repo}/contributors"
    response = requests.get(url, headers=HEADERS, params={"per_page": 5})
    
    contributors = []
    if response.status_code == 200:
        contributors = response.json()
    
    # Get recent commits
    url = f"https://api.github.com/repos/{repo}/commits"
    response = requests.get(url, headers=HEADERS, params={"per_page": 5})
    
    recent_commits = []
    if response.status_code == 200:
        recent_commits = response.json()
    
    return {
        "repository": repo_info,
        "latest_release": latest_release,
        "contributors": contributors,
        "recent_commits": recent_commits
    }

def analyze_with_openai(issue_data, comments_data, repo_data):
    """Use OpenAI to analyze the issue and generate response."""
    try:
        # Extract issue information
        issue_title = issue_data["title"]
        issue_body = issue_data["body"] or ""
        issue_author = issue_data["user"]["login"]
        issue_labels = [label["name"] for label in issue_data["labels"]]
        issue_state = issue_data["state"]
        issue_created_at = issue_data["created_at"]
        
        # Format comments for context
        comments_text = ""
        if comments_data:
            for comment in comments_data:
                comments_text += f"\nComment by {comment['user']['login']} on {comment['created_at']}:\n{comment['body']}\n"
        
        # Format repository info for context
        repo_name = repo_data["repository"]["full_name"]
        repo_description = repo_data["repository"]["description"] or ""
        
        latest_version = "unknown"
        if repo_data["latest_release"]:
            latest_version = repo_data["latest_release"]["tag_name"]
        
        # Create prompt
        prompt = f"""
        You are an assistant for the Baubles LTS project, which is a performance-optimized fork of the Baubles mod for Minecraft Forge 1.12.2.
        
        REPOSITORY: {repo_name}
        DESCRIPTION: {repo_description}
        LATEST VERSION: {latest_version}
        
        ISSUE #{issue_data['number']}: {issue_title}
        AUTHOR: {issue_author}
        CREATED: {issue_created_at}
        STATE: {issue_state}
        LABELS: {', '.join(issue_labels)}
        
        ISSUE DESCRIPTION:
        {issue_body}
        
        COMMENTS:
        {comments_text}
        
        Please analyze this issue and provide:
        
        1. A summary of the issue in 1-2 sentences
        2. Categorization of the issue type (bug, feature request, question, etc.)
        3. An assessment of the priority (critical, high, medium, low)
        4. A detailed response addressing the issue
        5. Suggested labels that should be applied
        6. Recommended next actions for maintainers
        
        Your response should be formatted in Markdown and be helpful, informative, and constructive.
        
        For bug reports, try to identify potential causes and solutions.
        For feature requests, assess compatibility with the project goals.
        For questions, provide clear and accurate answers based on your knowledge of the project.
        """
        
        # Call OpenAI API
        completion = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are a helpful assistant specializing in Minecraft modding, especially the Baubles API and its performance-optimized fork, Baubles LTS. You have deep knowledge of Java, Minecraft Forge 1.12.2, and common modding patterns."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=2000
        )
        
        response = completion.choices[0].message.content.strip()
        
        # Extract recommended labels and actions using regex
        labels_match = re.search(r"(?:Suggested|Recommended) Labels:(.+?)(?:\n\n|\n#|\Z)", response, re.DOTALL)
        suggested_labels = []
        if labels_match:
            labels_text = labels_match.group(1).strip()
            # Extract labels, which could be in various formats
            label_matches = re.findall(r'`([^`]+)`|\*\*([^*]+)\*\*|"([^"]+)"|\'([^\']+)\'|(\w+)', labels_text)
            for match in label_matches:
                # Each match is a tuple, one of the groups will have the label
                label = next((s for s in match if s), None)
                if label and not re.match(r'^\s*$', label) and label.lower() not in ['none', 'n/a']:
                    suggested_labels.append(label.strip())
        
        actions_match = re.search(r"(?:Recommended|Next) Actions:(.+?)(?:\n\n|\n#|\Z)", response, re.DOTALL)
        recommended_actions = ""
        if actions_match:
            recommended_actions = actions_match.group(1).strip()
        
        return {
            "analysis": response,
            "suggested_labels": suggested_labels,
            "recommended_actions": recommended_actions
        }
    
    except Exception as e:
        print(f"Error analyzing with OpenAI: {str(e)}")
        return {
            "analysis": f"I encountered an error while analyzing this issue: {str(e)}",
            "suggested_labels": [],
            "recommended_actions": "Error analyzing issue, manual review required."
        }

def post_comment(repo, issue_number, body):
    """Post a comment on the GitHub issue."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/comments"
    payload = {"body": body}
    
    response = requests.post(url, headers=HEADERS, json=payload)
    
    if response.status_code != 201:
        print(f"Error posting comment: {response.status_code}")
        print(response.text)
        return False
    
    return True

def add_labels(repo, issue_number, labels):
    """Add labels to the GitHub issue."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/labels"
    
    # First check if labels exist
    existing_labels_url = f"https://api.github.com/repos/{repo}/labels"
    response = requests.get(existing_labels_url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching existing labels: {response.status_code}")
        print(response.text)
        return False
    
    existing_labels = [label["name"] for label in response.json()]
    
    # Filter out labels that don't exist
    valid_labels = [label for label in labels if label in existing_labels]
    
    if not valid_labels:
        print("No valid labels found to add")
        return False
    
    # Add valid labels
    payload = {"labels": valid_labels}
    response = requests.post(url, headers=HEADERS, json=payload)
    
    if response.status_code != 200:
        print(f"Error adding labels: {response.status_code}")
        print(response.text)
        return False
    
    return True

def format_ai_response(ai_analysis):
    """Format the AI analysis for posting as a comment."""
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S UTC')
    
    formatted_response = f"""
# AI Assistant Analysis

*Generated at: {timestamp}*

{ai_analysis}

---
This response was automatically generated by the Baubles LTS issue management system.
If you have further questions, please let us know.
"""
    return formatted_response

def should_respond_to_issue(issue_data, comments_data, trigger_event, event_action):
    """Determine if we should respond to this issue."""
    # Always respond to newly opened issues
    if trigger_event == "issues" and event_action == "opened":
        return True
    
    # Always respond if manually triggered
    if trigger_event == "workflow_dispatch":
        return True
    
    # Always respond for scheduled updates on stale issues
    if trigger_event == "schedule" and event_action == "stale":
        return True
    
    # For reopened issues, check if we've already responded
    if trigger_event == "issues" and event_action == "reopened":
        # Check if the bot has already commented
        bot_comments = [comment for comment in comments_data if comment["user"]["login"] == "github-actions[bot]"]
        return len(bot_comments) == 0
    
    # By default, don't respond
    return False

def main():
    """Main function."""
    args = parse_args()
    
    # Get issue details
    issue_data = get_issue_details(args.repo, args.issue_number)
    comments_data = get_issue_comments(args.repo, args.issue_number)
    repo_data = get_repo_details(args.repo)
    
    # Determine if we should respond
    should_respond = should_respond_to_issue(issue_data, comments_data, args.trigger_event, args.event_action)
    
    # For issue_comment event with comment_id, we're specifically responding to that comment
    if args.trigger_event == "issue_comment" and args.comment_id:
        should_respond = True
    
    if not should_respond:
        print("Criteria for responding not met, skipping")
        return
    
    # Analyze the issue
    analysis_result = analyze_with_openai(issue_data, comments_data, repo_data)
    
    # Format response
    response = format_ai_response(analysis_result["analysis"])
    
    # Post comment
    success = post_comment(args.repo, args.issue_number, response)
    
    if success:
        print("Successfully posted analysis comment")
        
        # Add suggested labels if any
        if analysis_result["suggested_labels"]:
            print(f"Suggested labels: {analysis_result['suggested_labels']}")
            add_labels(args.repo, args.issue_number, analysis_result["suggested_labels"])
    else:
        print("Failed to post analysis comment")

if __name__ == "__main__":
    main()