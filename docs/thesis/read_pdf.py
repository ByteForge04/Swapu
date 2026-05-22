import sys
sys.path.insert(0, r'D:\SwapU\docs\thesis\pip_libs')
import pdfplumber

with pdfplumber.open(r'd:\SwapU\要求和模板\附件4、武汉理工大学本科生毕业设计（论文）工作管理办法（试行）.pdf') as pdf:
    print(f"Total pages: {len(pdf.pages)}")
    for i, page in enumerate(pdf.pages):
        text = page.extract_text()
        if text:
            print(f"\n===== Page {i+1} =====")
            print(text)
