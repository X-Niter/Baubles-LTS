#!/usr/bin/env python3
"""
API Documentation Generator for Baubles LTS

This script:
1. Parses Java code files in the API directory
2. Extracts class, method, and field information
3. Generates markdown documentation with examples
"""

import os
import sys
import re
import argparse
import glob
import json
import openai
from pathlib import Path

# Configure OpenAI API
openai.api_key = os.environ.get("OPENAI_API_KEY")

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(description="Generate API documentation from Java source files")
    parser.add_argument("--output-dir", required=True, help="Output directory for documentation files")
    parser.add_argument("--api-paths", required=True, help="Paths to API source directories, comma-separated")
    parser.add_argument("--format", choices=["markdown", "html"], default="markdown", help="Output format")
    return parser.parse_args()

def parse_java_file(file_path):
    """Parse a Java file to extract information about classes, methods, and fields."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Extract package name
    package_match = re.search(r'package\s+([\w.]+);', content)
    package = package_match.group(1) if package_match else None
    
    # Extract class/interface/enum name
    class_match = re.search(r'(public|private|protected)?\s+(class|interface|enum)\s+(\w+)', content)
    class_type = class_match.group(2) if class_match else None
    class_name = class_match.group(3) if class_match else None
    
    # Extract class JavaDoc comment
    class_javadoc = ""
    if class_match:
        javadoc_pattern = r'/\*\*([\s\S]*?)\*/\s*?(public|private|protected)?\s+(class|interface|enum)\s+' + re.escape(class_name)
        javadoc_match = re.search(javadoc_pattern, content)
        if javadoc_match:
            class_javadoc = javadoc_match.group(1).strip()
    
    # Extract methods
    method_pattern = r'(public|private|protected)\s+(?:static\s+)?(?:final\s+)?(?:<.*?>)?\s*(\w+(?:<.*?>)?(?:\[\])?)\s+(\w+)\s*\((.*?)\)(?:\s+throws\s+[\w,\s.]+)?(?:\s*\{|\s*;)'
    method_matches = re.finditer(method_pattern, content)
    
    methods = []
    for method_match in method_matches:
        method_info = {
            'access': method_match.group(1),
            'return_type': method_match.group(2),
            'name': method_match.group(3),
            'parameters': method_match.group(4).strip(),
            'javadoc': ''
        }
        
        # Extract method JavaDoc
        javadoc_pattern = r'/\*\*([\s\S]*?)\*/\s*?' + re.escape(method_info['access']) + r'\s+(?:static\s+)?(?:final\s+)?(?:<.*?>)?\s*' + re.escape(method_info['return_type']) + r'\s+' + re.escape(method_info['name']) + r'\s*\('
        javadoc_match = re.search(javadoc_pattern, content)
        if javadoc_match:
            method_info['javadoc'] = javadoc_match.group(1).strip()
        
        methods.append(method_info)
    
    # Extract fields
    field_pattern = r'(public|private|protected)\s+(?:static\s+)?(?:final\s+)?(\w+(?:<.*?>)?(?:\[\])?)\s+(\w+)\s*(?:=\s*[^;]+)?;'
    field_matches = re.finditer(field_pattern, content)
    
    fields = []
    for field_match in field_matches:
        field_info = {
            'access': field_match.group(1),
            'type': field_match.group(2),
            'name': field_match.group(3),
            'javadoc': ''
        }
        
        # Extract field JavaDoc
        javadoc_pattern = r'/\*\*([\s\S]*?)\*/\s*?' + re.escape(field_info['access']) + r'\s+(?:static\s+)?(?:final\s+)?' + re.escape(field_info['type']) + r'\s+' + re.escape(field_info['name'])
        javadoc_match = re.search(javadoc_pattern, content)
        if javadoc_match:
            field_info['javadoc'] = javadoc_match.group(1).strip()
        
        fields.append(field_info)
    
    return {
        'package': package,
        'class_type': class_type,
        'class_name': class_name,
        'class_javadoc': class_javadoc,
        'methods': methods,
        'fields': fields,
        'source_file': file_path
    }

def generate_markdown_docs(file_info, output_dir):
    """Generate markdown documentation for a Java file."""
    try:
        os.makedirs(output_dir, exist_ok=True)
        
        # Skip files with no class info
        if not file_info['class_name']:
            print(f"Skipping {file_info['source_file']} - could not extract class information")
            return
        
        output_file = os.path.join(output_dir, f"{file_info['class_name']}.md")
        
        with open(output_file, 'w', encoding='utf-8') as f:
            # Header
            f.write(f"# {file_info['class_name']}\n\n")
            
            # Package info
            f.write(f"**Package:** `{file_info['package']}`\n\n")
            
            # Type info
            f.write(f"**Type:** {file_info['class_type']}\n\n")
            
            # Class description
            if file_info['class_javadoc']:
                # Clean up JavaDoc
                javadoc = re.sub(r'@\w+\s+[^\n]*', '', file_info['class_javadoc'])
                javadoc = re.sub(r'\n\s*\*\s*', '\n', javadoc)
                javadoc = re.sub(r'\s+', ' ', javadoc)
                f.write(f"## Description\n\n{javadoc.strip()}\n\n")
            
            # Generate example usage with AI if API key is available
            if openai.api_key:
                f.write("## Example Usage\n\n")
                example = generate_example_with_ai(file_info)
                f.write(f"```java\n{example}\n```\n\n")
            
            # Fields
            if file_info['fields']:
                f.write("## Fields\n\n")
                
                for field in file_info['fields']:
                    # Only document public fields
                    if field['access'].lower() != 'public':
                        continue
                        
                    f.write(f"### {field['name']}\n\n")
                    f.write(f"**Type:** `{field['type']}`\n\n")
                    
                    if field['javadoc']:
                        # Clean up JavaDoc
                        javadoc = re.sub(r'@\w+\s+[^\n]*', '', field['javadoc'])
                        javadoc = re.sub(r'\n\s*\*\s*', '\n', javadoc)
                        javadoc = re.sub(r'\s+', ' ', javadoc)
                        f.write(f"{javadoc.strip()}\n\n")
            
            # Methods
            if file_info['methods']:
                f.write("## Methods\n\n")
                
                for method in file_info['methods']:
                    # Only document public methods
                    if method['access'].lower() != 'public':
                        continue
                        
                    f.write(f"### {method['name']}\n\n")
                    
                    # Format method signature
                    signature = f"{method['access']} {method['return_type']} {method['name']}({method['parameters']})"
                    f.write(f"```java\n{signature}\n```\n\n")
                    
                    if method['javadoc']:
                        # Extract parameter and return docs
                        param_docs = re.findall(r'@param\s+(\w+)\s+([^\n]*)', method['javadoc'])
                        return_doc = re.search(r'@return\s+([^\n]*)', method['javadoc'])
                        throws_docs = re.findall(r'@throws\s+(\w+)\s+([^\n]*)', method['javadoc'])
                        
                        # Clean up JavaDoc
                        main_doc = re.sub(r'@\w+\s+[^\n]*', '', method['javadoc'])
                        main_doc = re.sub(r'\n\s*\*\s*', '\n', main_doc)
                        main_doc = re.sub(r'\s+', ' ', main_doc)
                        
                        f.write(f"{main_doc.strip()}\n\n")
                        
                        # Parameters
                        if param_docs:
                            f.write("**Parameters:**\n\n")
                            for param_name, param_desc in param_docs:
                                f.write(f"- `{param_name}`: {param_desc.strip()}\n")
                            f.write("\n")
                        
                        # Return value
                        if return_doc and method['return_type'].lower() != 'void':
                            f.write(f"**Returns:** {return_doc.group(1).strip()}\n\n")
                        
                        # Exceptions
                        if throws_docs:
                            f.write("**Throws:**\n\n")
                            for exc_type, exc_desc in throws_docs:
                                f.write(f"- `{exc_type}`: {exc_desc.strip()}\n")
                            f.write("\n")
        
        print(f"Generated documentation for {file_info['class_name']} in {output_file}")
        return output_file
    
    except Exception as e:
        print(f"Error generating documentation for {file_info['source_file']}: {str(e)}")
        return None

def generate_example_with_ai(file_info):
    """Generate example usage for a class using OpenAI."""
    try:
        # Prepare class info for the prompt
        class_info = {
            'name': file_info['class_name'],
            'package': file_info['package'],
            'type': file_info['class_type'],
            'description': file_info['class_javadoc']
        }
        
        # Prepare methods info
        methods_info = []
        for method in file_info['methods']:
            if method['access'].lower() == 'public':
                methods_info.append({
                    'name': method['name'],
                    'return_type': method['return_type'],
                    'parameters': method['parameters'],
                    'description': method['javadoc']
                })
        
        # Prepare fields info
        fields_info = []
        for field in file_info['fields']:
            if field['access'].lower() == 'public':
                fields_info.append({
                    'name': field['name'],
                    'type': field['type'],
                    'description': field['javadoc']
                })
        
        # Create prompt
        prompt = f"""
        Generate a simple, clear example of how to use the following Java class from the Baubles LTS mod API:
        
        Class: {class_info['name']}
        Package: {class_info['package']}
        Type: {class_info['type']}
        
        Description:
        {class_info['description']}
        
        Public Methods:
        {json.dumps(methods_info, indent=2)}
        
        Public Fields:
        {json.dumps(fields_info, indent=2)}
        
        Please create a concise, practical example that demonstrates the primary purpose of this class.
        Return ONLY the Java code example with proper imports, no explanation or markdown formatting.
        The example should be 10-20 lines of code that would actually work in a Minecraft mod.
        """
        
        # Call OpenAI API
        completion = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are an expert Java programmer with extensive knowledge of Minecraft modding, particularly for Baubles API. You create concise, practical code examples."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=500
        )
        
        example = completion.choices[0].message.content.strip()
        
        # Remove markdown code block if present
        example = re.sub(r'^```java\n', '', example)
        example = re.sub(r'\n```$', '', example)
        
        return example
    
    except Exception as e:
        print(f"Error generating example for {file_info['class_name']}: {str(e)}")
        return "// Example could not be generated automatically\n// Please refer to the method documentation above"

def generate_api_index(docs_files, output_dir):
    """Generate an index file for all API documentation."""
    index_file = os.path.join(output_dir, "README.md")
    
    with open(index_file, 'w', encoding='utf-8') as f:
        f.write("# Baubles LTS API Documentation\n\n")
        f.write("This is the API documentation for Baubles LTS, a performance-optimized fork of the Baubles mod for Minecraft Forge 1.12.2.\n\n")
        f.write("## API Classes\n\n")
        
        # Organize docs by package
        docs_by_package = {}
        for doc_file in docs_files:
            if not doc_file:
                continue
                
            file_info = parse_java_file(doc_file.replace('.md', '.java'))
            package = file_info['package']
            class_name = file_info['class_name']
            
            if package not in docs_by_package:
                docs_by_package[package] = []
            
            docs_by_package[package].append({
                'name': class_name,
                'file': os.path.basename(doc_file)
            })
        
        # Generate links by package
        for package, classes in sorted(docs_by_package.items()):
            f.write(f"### {package}\n\n")
            
            for class_info in sorted(classes, key=lambda x: x['name']):
                f.write(f"- [{class_info['name']}]({class_info['file']})\n")
            
            f.write("\n")
        
        f.write("\n## Integration Guide\n\n")
        f.write("Baubles LTS is designed to be a drop-in replacement for the original Baubles mod. It maintains complete API compatibility while offering significantly improved performance.\n\n")
        f.write("To integrate with Baubles LTS, follow the same API patterns as with the original Baubles, but benefit from enhanced performance and memory usage.\n\n")
        f.write("Refer to the class documentation above for detailed API usage examples.\n")
    
    print(f"Generated API index at {index_file}")
    return index_file

def main():
    """Main function."""
    args = parse_args()
    
    # Create output directory
    os.makedirs(args.output_dir, exist_ok=True)
    
    # Get all Java files in API paths
    api_paths = args.api_paths.split(',')
    all_java_files = []
    for api_path in api_paths:
        all_java_files.extend(glob.glob(f"{api_path}/**/*.java", recursive=True))
    
    print(f"Found {len(all_java_files)} Java files to process")
    
    # Process each file
    doc_files = []
    for java_file in all_java_files:
        file_info = parse_java_file(java_file)
        if args.format == 'markdown':
            doc_file = generate_markdown_docs(file_info, args.output_dir)
            if doc_file:
                doc_files.append(doc_file)
    
    # Generate index
    if args.format == 'markdown':
        generate_api_index(doc_files, args.output_dir)
    
    print("Documentation generation complete")

if __name__ == "__main__":
    main()