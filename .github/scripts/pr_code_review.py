#!/usr/bin/env python3
"""
PR Code Review Script for Baubles LTS

This script:
1. Gets the changed files in a pull request
2. Analyzes the changes using OpenAI
3. Posts a detailed code review as a comment
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
    parser = argparse.ArgumentParser(description="Analyze pull request code changes")
    parser.add_argument("--repo", required=True, help="Repository in format owner/repo")
    parser.add_argument("--pr", required=True, help="Pull request number to analyze")
    return parser.parse_args()

def get_pr_details(repo, pr_number):
    """Fetch pull request details from GitHub API."""
    url = f"https://api.github.com/repos/{repo}/pulls/{pr_number}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching PR details: {response.status_code}")
        print(response.text)
        sys.exit(1)
    
    return response.json()

def get_pr_files(repo, pr_number):
    """Fetch files changed in the pull request."""
    url = f"https://api.github.com/repos/{repo}/pulls/{pr_number}/files"
    files = []
    page = 1
    
    while True:
        params = {"page": page, "per_page": 100}
        response = requests.get(url, headers=HEADERS, params=params)
        
        if response.status_code != 200:
            print(f"Error fetching PR files: {response.status_code}")
            print(response.text)
            sys.exit(1)
        
        page_files = response.json()
        files.extend(page_files)
        
        # Check if we've fetched all files
        if len(page_files) < 100:
            break
        
        page += 1
    
    return files

def get_file_content(repo, file_path, ref):
    """Get content of a file at a specific commit."""
    url = f"https://api.github.com/repos/{repo}/contents/{file_path}?ref={ref}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching file content: {response.status_code}")
        return None
    
    import base64
    content = response.json().get("content", "")
    if content:
        try:
            return base64.b64decode(content).decode('utf-8')
        except:
            return None
    return None

def get_file_diff(repo, base_sha, head_sha, file_path):
    """Get git diff for a specific file between two commits."""
    url = f"https://api.github.com/repos/{repo}/compare/{base_sha}...{head_sha}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching diff: {response.status_code}")
        print(response.text)
        return None
    
    files = response.json().get("files", [])
    for file in files:
        if file.get("filename") == file_path:
            return file.get("patch", "")
    
    return None

def analyze_code_with_openai(pr_details, files_data, max_files=10):
    """Use OpenAI to analyze code changes and generate review."""
    try:
        # Prepare PR context
        pr_title = pr_details["title"]
        pr_body = pr_details["body"] or ""
        pr_author = pr_details["user"]["login"]
        
        # Limit the number of files to analyze to avoid token limits
        if len(files_data) > max_files:
            print(f"Limiting analysis to {max_files} files out of {len(files_data)}")
            # Prioritize API files
            api_files = [f for f in files_data if "api" in f["path"].lower()]
            non_api_files = [f for f in files_data if "api" not in f["path"].lower()]
            
            # Select API files first, then other files
            selected_files = api_files[:max_files]
            if len(selected_files) < max_files:
                selected_files += non_api_files[:max_files - len(selected_files)]
                
            files_data = selected_files
        
        # Prepare file context
        file_context = ""
        for file in files_data:
            file_path = file["path"]
            file_type = file.get("status", "modified")
            patch = file.get("diff", "")
            
            # Truncate very large diffs to avoid token limits
            if len(patch) > 2000:
                patch = patch[:1000] + "\n... [diff truncated] ...\n" + patch[-1000:]
            
            file_context += f"File: {file_path} ({file_type})\n"
            file_context += f"Diff:\n{patch}\n\n"
        
        # Create prompt
        prompt = f"""
        You are an expert code reviewer for the Baubles LTS Minecraft mod, which is a performance-optimized fork of the Baubles mod for Minecraft Forge 1.12.2.
        
        Please review this pull request:
        
        PR Title: {pr_title}
        PR Author: {pr_author}
        PR Description:
        {pr_body}
        
        Changed files:
        {file_context}
        
        Please provide a detailed code review focusing on:
        
        1. Code quality and best practices
        2. Performance implications
        3. Backward compatibility (crucial for this project)
        4. Potential bugs or edge cases
        5. Security concerns if applicable
        
        For each issue found, please suggest specific improvements. Be thorough but constructive, and highlight positive aspects of the changes as well.
        
        If you notice any potential improvements to performance, please specifically mention them as this is a performance-optimized fork.
        
        Format your review with Markdown using sections, code blocks, and bullet points for clarity.
        """
        
        # Call OpenAI API
        completion = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are an expert Java and Minecraft modding code reviewer who specializes in performance optimization and maintaining backward compatibility. You provide thorough but constructive feedback, with specific code suggestions where appropriate."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=2500
        )
        
        review = completion.choices[0].message.content.strip()
        return review
    
    except Exception as e:
        print(f"Error analyzing code with OpenAI: {str(e)}")
        return f"""
## Automated Code Review

I attempted to analyze this PR but encountered an error.

Please review these changes manually, paying special attention to:
- Performance impacts
- Backward compatibility
- Code quality and best practices

Error details: {str(e)}
"""

def post_review_comment(repo, pr_number, body):
    """Post a review comment on the pull request."""
    url = f"https://api.github.com/repos/{repo}/issues/{pr_number}/comments"
    payload = {"body": body}
    
    response = requests.post(url, headers=HEADERS, json=payload)
    
    if response.status_code != 201:
        print(f"Error posting review: {response.status_code}")
        print(response.text)
        return False
    
    return True

def format_review(review_text):
    """Format the AI review for GitHub comment display."""
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S UTC')
    
    formatted_review = f"""
# Automated Code Review

*Generated at: {timestamp}*

{review_text}

---
This review was automatically generated by the Baubles LTS PR review system. 
If you have questions or need clarification on any points, please reply to this comment.
"""
    return formatted_review

def main():
    """Main function."""
    args = parse_args()
    
    # Get PR details
    pr_details = get_pr_details(args.repo, args.pr)
    base_sha = pr_details["base"]["sha"]
    head_sha = pr_details["head"]["sha"]
    
    # Get files changed in PR
    pr_files = get_pr_files(args.repo, args.pr)
    
    print(f"Found {len(pr_files)} files changed in PR #{args.pr}")
    
    # Process each file to get content and diff
    files_data = []
    for file in pr_files:
        file_path = file["filename"]
        file_status = file["status"]
        
        print(f"Processing {file_path} ({file_status})")
        
        # Skip binary files
        if file.get("binary", False):
            print(f"Skipping binary file: {file_path}")
            continue
        
        # Get file diff
        diff = file.get("patch") or get_file_diff(args.repo, base_sha, head_sha, file_path)
        
        files_data.append({
            "path": file_path,
            "status": file_status,
            "diff": diff
        })
    
    # Analyze code with OpenAI
    review = analyze_code_with_openai(pr_details, files_data)
    
    # Format review
    formatted_review = format_review(review)
    
    # Post review as a comment
    success = post_review_comment(args.repo, args.pr, formatted_review)
    
    if success:
        print("Successfully posted code review")
    else:
        print("Failed to post code review")

if __name__ == "__main__":
    main()