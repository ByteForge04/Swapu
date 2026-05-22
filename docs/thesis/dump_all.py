import sys
sys.path.insert(0, r'D:\SwapU\docs\thesis\pip_libs')
from docx import Document
from docx.oxml.ns import qn

doc = Document(r'd:\SwapU\docs\thesis\SwapU毕业论文_终版v14.docx')

for i, p in enumerate(doc.paragraphs):
    text = p.text.strip()
    if text:
        print(f"P[{i}]: {text}")
