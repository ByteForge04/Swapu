import sys
sys.path.insert(0, r'D:\SwapU\docs\thesis\pip_libs')
from docx import Document
from docx.oxml.ns import qn
import os
import hashlib

doc = Document(r'd:\SwapU\docs\thesis\SwapU毕业论文_终版v14.docx')

output_dir = r'd:\SwapU\docs\thesis\thesis_images'
os.makedirs(output_dir, exist_ok=True)

image_map = {}

for rel in doc.part.rels.values():
    if "image" in rel.reltype:
        image = rel.target_part
        image_bytes = image.blob
        ext = os.path.splitext(image.partname)[1]
        if not ext:
            ext = '.png'
        
        h = hashlib.md5(image_bytes).hexdigest()[:8]
        filename = f"img_{h}{ext}"
        filepath = os.path.join(output_dir, filename)
        
        with open(filepath, 'wb') as f:
            f.write(image_bytes)
        
        image_map[rel.rId] = filename
        print(f"Extracted: {filename} ({len(image_bytes)} bytes, rId={rel.rId})")

print(f"\nTotal images extracted: {len(image_map)}")

print("\n=== Mapping paragraphs to images ===")
for i, p in enumerate(doc.paragraphs):
    for run in p.runs:
        drawing_elements = run._element.findall('.//' + qn('wp:inline'))
        for shape in drawing_elements:
            docPr = shape.find(qn('wp:docPr'))
            name = docPr.get('name', '') if docPr is not None else ''
            
            blip = shape.find('.//' + qn('a:blip'))
            if blip is not None:
                rId = blip.get(qn('r:embed'))
                if rId in image_map:
                    print(f"P[{i}] {name} -> {image_map[rId]} | caption: {p.text[:60]}")
            
            blip_link = shape.find('.//' + qn('a:blip'))
            rLink = blip_link.get(qn('r:link')) if blip_link is not None else None
            if rLink:
                print(f"P[{i}] {name} -> LINKED image rLink={rLink}")
