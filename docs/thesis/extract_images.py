import sys
sys.path.insert(0, r'D:\SwapU\docs\thesis\pip_libs')
from docx import Document
from docx.oxml.ns import qn
import os

doc = Document(r'd:\SwapU\docs\thesis\SwapU毕业论文_终版v14.docx')

print("=" * 80)
print("ALL PARAGRAPHS WITH IMAGES")
print("=" * 80)

for i, p in enumerate(doc.paragraphs):
    text = p.text.strip()
    
    has_image = False
    image_names = []
    for run in p.runs:
        drawing_elements = run._element.findall(qn('w:drawing'))
        if drawing_elements:
            has_image = True
        inline_shapes = run._element.findall('.//' + qn('wp:inline'))
        for shape in inline_shapes:
            docPr = shape.find(qn('wp:docPr'))
            if docPr is not None:
                name = docPr.get('name', '')
                descr = docPr.get('descr', '')
                image_names.append(f"name={name} descr={descr}")
    
    if has_image or text:
        if has_image:
            print(f"P[{i}] [IMAGE: {image_names}] {text[:60] if text else ''}")
        else:
            print(f"P[{i}] {text[:120]}")

print("\n" + "=" * 80)
print("IMAGE FILES IN THESIS DIRECTORY")
print("=" * 80)
img_dir = r'd:\SwapU\docs\thesis\images'
if os.path.exists(img_dir):
    for f in sorted(os.listdir(img_dir)):
        if f.endswith(('.png', '.jpg', '.jpeg', '.gif')):
            print(f"  {f}")
