import sys
sys.path.insert(0, r'D:\SwapU\docs\thesis\pip_libs')
from docx import Document
from docx.oxml.ns import qn

doc = Document(r'D:\SwapU\docs\thesis\SwapU毕业论文_终版v12.docx')

superscript_count = 0
for i, p in enumerate(doc.paragraphs):
    for run in p.runs:
        rPr = run._element.find(qn('w:rPr'))
        if rPr is not None:
            vertAlign = rPr.find(qn('w:vertAlign'))
            if vertAlign is not None and vertAlign.get(qn('w:val')) == 'superscript':
                superscript_count += 1
                print(f'P[{i}] superscript: "{run.text}" | paragraph[:40]: {p.text[:40]}')

print(f'\nTotal superscript citations: {superscript_count}')
