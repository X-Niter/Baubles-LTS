#!/usr/bin/env python3
"""
README Updater for Baubles LTS

This script:
1. Analyzes the current project state
2. Generates an updated README.md using AI
3. Preserves important sections while updating others
"""

import os
import sys
import re
import subprocess
import openai
from pathlib import Path

# Configure OpenAI API
openai.api_key = os.environ.get("OPENAI_API_KEY")
if not openai.api_key:
    print("Error: OPENAI_API_KEY environment variable not set")
    sys.exit(1)

def get_current_version():
    """Extract current version from Baubles.java."""
    try:
        baubles_java_path = "src/main/java/baubles/common/Baubles.java"
        if not os.path.exists(baubles_java_path):
            return "unknown"
            
        with open(baubles_java_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        version_match = re.search(r'VERSION = "(.*?)"', content)
        if version_match:
            return version_match.group(1)
        return "unknown"
    except Exception as e:
        print(f"Error getting current version: {str(e)}")
        return "unknown"

def get_recent_changes():
    """Get recent changes from git log."""
    try:
        # Get last 10 commits
        result = subprocess.run(
            ["git", "log", "-n", "10", "--pretty=format:%s"],
            capture_output=True, text=True, check=True
        )
        return result.stdout
    except Exception as e:
        print(f"Error getting recent changes: {str(e)}")
        return ""

def get_project_stats():
    """Get project statistics."""
    stats = {}
    
    try:
        # Count Java files
        java_files = list(Path(".").glob("src/main/java/**/*.java"))
        stats["java_files"] = len(java_files)
        
        # Count API files
        api_files = list(Path(".").glob("src/main/java/baubles/api/**/*.java"))
        stats["api_files"] = len(api_files)
        
        # Count resource files
        resource_files = list(Path(".").glob("src/main/resources/**/*"))
        stats["resource_files"] = len(resource_files)
        
        # Get line counts
        result = subprocess.run(
            ["find", "src", "-name", "*.java", "-exec", "wc", "-l", "{}", ";"],
            capture_output=True, text=True, check=True
        )
        
        # Parse line count output
        total_lines = 0
        for line in result.stdout.splitlines():
            if line.strip():
                try:
                    count = int(line.strip().split()[0])
                    total_lines += count
                except (ValueError, IndexError):
                    pass
        
        stats["total_lines"] = total_lines
        
        return stats
    except Exception as e:
        print(f"Error getting project stats: {str(e)}")
        return {
            "java_files": 0,
            "api_files": 0,
            "resource_files": 0,
            "total_lines": 0
        }

def read_current_readme():
    """Read the current README.md if it exists."""
    try:
        readme_path = "README.md"
        if os.path.exists(readme_path):
            with open(readme_path, 'r', encoding='utf-8') as f:
                return f.read()
        return ""
    except Exception as e:
        print(f"Error reading README: {str(e)}")
        return ""

def extract_readme_sections(readme_content):
    """Extract sections from the README to preserve important ones."""
    sections = {}
    
    # Extract sections based on markdown headers
    section_pattern = r'^(#+)\s+(.*?)$'
    lines = readme_content.split('\n')
    
    current_section = None
    section_content = []
    
    for line in lines:
        header_match = re.match(section_pattern, line)
        
        if header_match:
            # Save previous section
            if current_section:
                sections[current_section[1]] = '\n'.join(section_content).strip()
            
            # Start new section
            current_section = (header_match.group(1), header_match.group(2))
            section_content = [line]
        elif current_section:
            section_content.append(line)
    
    # Save last section
    if current_section:
        sections[current_section[1]] = '\n'.join(section_content).strip()
    
    return sections

def generate_updated_readme(current_readme, version, recent_changes, stats):
    """Generate an updated README using OpenAI."""
    try:
        # Extract existing sections to preserve
        sections = extract_readme_sections(current_readme)
        
        # Prepare prompt
        prompt = f"""
        You are updating the README.md file for Baubles LTS, a performance-optimized fork of the Baubles mod for Minecraft Forge 1.12.2.
        
        Current version: {version}
        
        Project statistics:
        - Java files: {stats.get('java_files', 0)}
        - API files: {stats.get('api_files', 0)}
        - Resource files: {stats.get('resource_files', 0)}
        - Total lines of code: {stats.get('total_lines', 0)}
        
        Recent changes:
        {recent_changes}
        
        Current README content:
        ```
        {current_readme}
        ```
        
        Please generate an updated README.md that:
        1. Maintains the same overall structure but updates content for accuracy
        2. Improves clarity and organization where needed
        3. Highlights performance improvements and optimizations
        4. Keeps the same style and tone
        5. Preserves any custom sections or specific details from the original
        
        Do not invent features or changes that aren't mentioned. Focus on accurate representation of the current project state.
        The README should emphasize that this is a performance-optimized fork that maintains complete backward compatibility.
        """
        
        # Call OpenAI API
        completion = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are a technical documentation expert who specializes in creating clear, professional README files for open source projects."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=2000
        )
        
        updated_readme = completion.choices[0].message.content.strip()
        
        # Clean up any markdown formatting that might have been added around the content
        updated_readme = re.sub(r'^```markdown\n', '', updated_readme)
        updated_readme = re.sub(r'\n```$', '', updated_readme)
        
        return updated_readme
    
    except Exception as e:
        print(f"Error generating updated README: {str(e)}")
        return current_readme

def save_updated_readme(content):
    """Save the updated README."""
    try:
        with open("README.md", 'w', encoding='utf-8') as f:
            f.write(content)
        print("Successfully updated README.md")
        return True
    except Exception as e:
        print(f"Error saving README: {str(e)}")
        return False

def main():
    """Main function."""
    # Get current state
    current_version = get_current_version()
    recent_changes = get_recent_changes()
    project_stats = get_project_stats()
    current_readme = read_current_readme()
    
    print(f"Current version: {current_version}")
    print(f"Project stats: {project_stats}")
    
    # Generate updated README
    updated_readme = generate_updated_readme(
        current_readme, 
        current_version, 
        recent_changes, 
        project_stats
    )
    
    # Save updated README
    if updated_readme != current_readme:
        save_updated_readme(updated_readme)
    else:
        print("No significant changes needed for README.md")

if __name__ == "__main__":
    main()