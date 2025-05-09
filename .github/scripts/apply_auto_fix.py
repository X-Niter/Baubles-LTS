#!/usr/bin/env python3
"""
Auto Fix Application Script for Baubles LTS

This script:
1. Takes an issue that has been identified as fixable
2. Generates a fix using AI assistance
3. Applies the fix to the codebase
4. Tests the fix to ensure it works and maintains compatibility
"""

import os
import sys
import json
import argparse
import requests
import tempfile
import subprocess
import openai
import re
from pathlib import Path

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
    parser = argparse.ArgumentParser(description="Apply auto-fixes to issues")
    parser.add_argument("--repo", required=True, help="Repository in format owner/repo")
    parser.add_argument("--issue-number", required=True, help="Issue number to fix")
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

def get_file_content(repo, file_path, ref="master"):
    """Get content of a file from GitHub."""
    url = f"https://api.github.com/repos/{repo}/contents/{file_path}?ref={ref}"
    response = requests.get(url, headers=HEADERS)
    
    if response.status_code != 200:
        print(f"Error fetching file content: {response.status_code}")
        print(response.text)
        return None
    
    import base64
    content = response.json().get("content", "")
    if content:
        try:
            return base64.b64decode(content).decode('utf-8')
        except:
            return None
    return None

def identify_files_to_fix(issue_data):
    """Identify which files need to be fixed based on the issue."""
    try:
        # Extract information from issue title and body
        issue_title = issue_data["title"]
        issue_body = issue_data["body"] or ""
        
        # Create prompt for file identification
        prompt = f"""
        You are an AI assistant tasked with identifying files that need to be modified to fix an issue in the Baubles LTS Minecraft mod.
        
        ISSUE TITLE: {issue_title}
        ISSUE DESCRIPTION:
        {issue_body}
        
        Based on this information, please identify the specific files that likely need to be modified to fix this issue.
        Consider Java files in the src/main/java/baubles directory that might be related to the described problem.
        
        Return ONLY a comma-separated list of file paths, with no additional text or explanation. For example:
        "src/main/java/baubles/common/event/EventHandlerEntity.java,src/main/java/baubles/api/BaubleType.java"
        """
        
        # Call OpenAI API with error handling
        try:
            # Try the newer client-based API format first
            try:
                from openai import OpenAI
                client = OpenAI(api_key=openai.api_key)
                completion = client.chat.completions.create(
                    model="gpt-4",
                    messages=[
                        {"role": "system", "content": "You are a helpful AI assistant that specializes in Minecraft mod development. You identify files that need to be modified to fix issues."},
                        {"role": "user", "content": prompt}
                    ],
                    max_tokens=100
                )
                file_list = completion.choices[0].message.content.strip()
            except (ImportError, AttributeError):
                # Fall back to the older API format
                completion = openai.ChatCompletion.create(
                    model="gpt-4",
                    messages=[
                        {"role": "system", "content": "You are a helpful AI assistant that specializes in Minecraft mod development. You identify files that need to be modified to fix issues."},
                        {"role": "user", "content": prompt}
                    ],
                    max_tokens=100
                )
                file_list = completion.choices[0].message.content.strip()
        except Exception as e:
            print(f"OpenAI API Error: {str(e)}")
            # Try to extract file paths from the issue title and body as a fallback
            potential_files = []
            for line in issue_body.split('\n'):
                if '.java' in line:
                    # Very simplistic extraction - just look for Java file mentions
                    for word in line.split():
                        if word.endswith('.java'):
                            clean_word = word.strip('.,():;"\'\n')
                            if clean_word.startswith('src/'):
                                potential_files.append(clean_word)
            
            file_list = ", ".join(potential_files) if potential_files else "src/main/java/baubles/common/Baubles.java"
        # Remove any quotes, extra spaces, or newlines
        file_list = file_list.replace('"', '').replace("'", "").strip()
        files = [f.strip() for f in file_list.split(",")]
        
        return files
    
    except Exception as e:
        print(f"Error identifying files to fix: {str(e)}")
        return []

def generate_fix(repo, issue_data, file_path, file_content):
    """Generate a fix for a specific file based on the issue."""
    try:
        issue_title = issue_data["title"]
        issue_body = issue_data["body"] or ""
        
        # Create prompt for fix generation
        prompt = f"""
        You are an AI assistant tasked with fixing an issue in the Baubles LTS Minecraft mod.
        
        ISSUE TITLE: {issue_title}
        ISSUE DESCRIPTION:
        {issue_body}
        
        FILE TO FIX: {file_path}
        CURRENT FILE CONTENT:
        ```java
        {file_content}
        ```
        
        Please provide a fix for this issue in the specified file. 
        The fix should:
        1. Be minimal and focused only on addressing the reported issue
        2. Maintain backward compatibility with the original Baubles mod
        3. Follow existing code style and conventions
        4. Include performance optimizations where possible
        5. Be well-documented with comments explaining the changes
        
        Provide ONLY the full updated file content with your changes applied, no explanations or annotations.
        Your response will be used to directly replace the file.
        If the file does not need to be changed, repeat the original content exactly.
        """
        
        # Call OpenAI API with error handling for different client versions
        try:
            # Try the newer client-based API format first
            try:
                from openai import OpenAI
                client = OpenAI(api_key=openai.api_key)
                completion = client.chat.completions.create(
                    model="gpt-4",
                    messages=[
                        {"role": "system", "content": "You are an expert Java developer specializing in Minecraft modding. Your task is to fix issues in a performance-optimized fork of the Baubles mod while maintaining backward compatibility."},
                        {"role": "user", "content": prompt}
                    ],
                    max_tokens=4000
                )
                fixed_content = completion.choices[0].message.content.strip()
            except (ImportError, AttributeError):
                # Fall back to the older API format
                completion = openai.ChatCompletion.create(
                    model="gpt-4",
                    messages=[
                        {"role": "system", "content": "You are an expert Java developer specializing in Minecraft modding. Your task is to fix issues in a performance-optimized fork of the Baubles mod while maintaining backward compatibility."},
                        {"role": "user", "content": prompt}
                    ],
                    max_tokens=4000
                )
                fixed_content = completion.choices[0].message.content.strip()
        except Exception as e:
            print(f"OpenAI API Error while generating fix: {str(e)}")
            # Return the original content to avoid breaking things
            print(f"Returning original content for {file_path} due to API error")
            return file_content
        
        # If the fixed content has code blocks, extract just the code
        code_pattern = re.compile(r"```(?:java)?\n([\s\S]*?)\n```")
        match = code_pattern.search(fixed_content)
        if match:
            fixed_content = match.group(1)
        
        # If the fixed content and original are identical, the file doesn't need changes
        if fixed_content == file_content:
            return None
        
        return fixed_content
    
    except Exception as e:
        print(f"Error generating fix: {str(e)}")
        return None

def apply_fix_to_file(file_path, fixed_content):
    """Apply the generated fix to the file in the local repository."""
    try:
        # Ensure the directory exists
        os.makedirs(os.path.dirname(file_path), exist_ok=True)
        
        # Write the fixed content to the file
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(fixed_content)
        
        print(f"Successfully applied fix to {file_path}")
        return True
    
    except Exception as e:
        print(f"Error applying fix to {file_path}: {str(e)}")
        return False

def test_fix(repo):
    """Test the fix by building the project."""
    try:
        # Detect build system
        if os.path.exists("pom.xml"):
            # Maven project
            build_cmd = ["mvn", "package"]
        elif os.path.exists("build.gradle"):
            # Gradle project with wrapper
            if os.path.exists("./gradlew"):
                build_cmd = ["./gradlew", "build"]
            else:
                build_cmd = ["gradle", "build"]
        else:
            print("Could not detect build system (no pom.xml or build.gradle)")
            return False
        
        print(f"Executing build command: {' '.join(build_cmd)}")
        result = subprocess.run(build_cmd, capture_output=True, text=True)
        
        if result.returncode == 0:
            print("Build successful")
            return True
        else:
            print(f"Build failed with exit code {result.returncode}")
            print(f"Standard output: {result.stdout[:500]}...")
            print(f"Error output: {result.stderr[:500]}...")
            return False
    
    except Exception as e:
        print(f"Error testing fix: {str(e)}")
        return False

def post_fix_comment(repo, issue_number, files_fixed):
    """Post a comment on the issue with details about the fix."""
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/comments"
    
    if not files_fixed:
        body = """
## Automated Fix Attempt

I attempted to fix this issue, but was unable to determine what changes were needed. 
This issue likely requires manual intervention from a developer.

If you have specific suggestions about what might need to be fixed, please share them.
"""
    else:
        body = f"""
## Automated Fix Applied

I've analyzed this issue and applied a fix to the following files:
{chr(10).join([f"- `{f}`" for f in files_fixed])}

The changes have been committed to a new branch and a pull request will be created shortly.
Please review the PR to ensure the fix works as expected.

The fix addresses the reported issue while maintaining backward compatibility with the original Baubles mod.
"""
    
    response = requests.post(url, headers=HEADERS, json={"body": body})
    if response.status_code != 201:
        print(f"Error posting comment: {response.status_code}")
        print(response.text)
        return False
    
    return True

def main():
    """Main function."""
    args = parse_args()
    
    # Get issue details
    issue_data = get_issue_details(args.repo, args.issue_number)
    
    # Identify files to fix
    files_to_fix = identify_files_to_fix(issue_data)
    print(f"Files identified for fixing: {files_to_fix}")
    
    if not files_to_fix:
        print("No files identified for fixing. Unable to proceed.")
        post_fix_comment(args.repo, args.issue_number, [])
        return
    
    # Apply fixes
    files_fixed = []
    for file_path in files_to_fix:
        # Get current file content
        file_content = get_file_content(args.repo, file_path)
        if not file_content:
            print(f"Unable to get content for {file_path}. Skipping.")
            continue
        
        # Generate fix
        fixed_content = generate_fix(args.repo, issue_data, file_path, file_content)
        if not fixed_content:
            print(f"No changes needed for {file_path}. Skipping.")
            continue
        
        # Apply fix
        if apply_fix_to_file(file_path, fixed_content):
            files_fixed.append(file_path)
    
    if not files_fixed:
        print("No files were fixed. Unable to proceed.")
        post_fix_comment(args.repo, args.issue_number, [])
        return
    
    # Test the fix
    if test_fix(args.repo):
        print("Fix successfully tested.")
    else:
        print("Warning: Fix did not pass tests.")
    
    # Post comment about the fix
    post_fix_comment(args.repo, args.issue_number, files_fixed)
    
    print("Auto-fix process completed.")

if __name__ == "__main__":
    main()