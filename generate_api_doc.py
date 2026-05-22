import os
import re

def generate_api_doc(directory):
    doc = "# SwapU 接口文档\n\n"
    
    for filename in os.listdir(directory):
        if not filename.endswith("Controller.java"):
            continue
            
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        # Extract class-level mapping
        class_mapping_match = re.search(r'@RequestMapping\(\s*(?:value\s*=\s*)?"([^"]*)"\s*\)', content)
        base_path = class_mapping_match.group(1) if class_mapping_match else ""
        
        doc += f"## {filename.replace('.java', '')}\n"
        doc += f"**基础路径**: `{base_path}`\n\n"
        
        # Split content by method using 'public Result' or 'public ' as rough separators
        # Or better, match lines with @XxxMapping and then find the following public method
        
        mapping_pattern = re.compile(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|RequestMapping)(?:\(\s*(?:value\s*=\s*)?"([^"]*)"\s*\))?')
        
        # we can iterate line by line
        lines = content.split('\n')
        current_mapping = None
        current_path = ""
        
        for i, line in enumerate(lines):
            mapping_match = mapping_pattern.search(line)
            if mapping_match and "class" not in line and "interface" not in line:
                # it's a method mapping if it's inside class
                # let's just grab it
                req_type = mapping_match.group(1).replace("Mapping", "").upper()
                if req_type == "REQUEST":
                    req_type = "ANY"
                current_path = mapping_match.group(2) or ""
                current_mapping = req_type
                
            elif current_mapping and "public " in line and "(" in line and ")" in line:
                # found the method signature
                method_line = line.strip()
                # extract return type and method name
                # e.g. public Result<List<Item>> list(Item item)
                sig_match = re.search(r'public\s+(.*?)\s+([A-Za-z0-9_]+)\s*\((.*)\)', method_line)
                if sig_match:
                    return_type = sig_match.group(1).strip()
                    method_name = sig_match.group(2).strip()
                    params_raw = sig_match.group(3).strip()
                    
                    full_path = f"{base_path}{current_path}".replace('//', '/')
                    
                    doc += f"### {method_name}\n"
                    doc += f"- **接口路径**: `{full_path}`\n"
                    doc += f"- **请求方式**: `{current_mapping}`\n"
                    doc += f"- **返回值**: `{return_type}`\n"
                    
                    # parse params
                    params = [p.strip() for p in params_raw.split(',') if p.strip()]
                    valid_params = []
                    for p in params:
                        if "HttpServletRequest" in p or "HttpServletResponse" in p:
                            continue
                        p = re.sub(r'@[A-Za-z0-9_]+(?:\([^)]*\))?\s*', '', p)
                        valid_params.append(p)
                        
                    if valid_params:
                        doc += "- **参数**:\n"
                        for p in valid_params:
                            doc += f"  - `{p}`\n"
                    else:
                        doc += "- **参数**: 无\n"
                    doc += "\n"
                    
                current_mapping = None # reset
                
    return doc

if __name__ == "__main__":
    controller_dir = r"d:\SwapU\backend\src\main\java\com\swapu\controller"
    markdown_doc = generate_api_doc(controller_dir)
    with open("API_DOC.md", "w", encoding="utf-8") as f:
        f.write(markdown_doc)
    print("API_DOC.md generated successfully.")
