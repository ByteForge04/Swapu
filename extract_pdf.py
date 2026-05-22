import fitz

def extract_pdf(pdf_path):
    doc = fitz.open(pdf_path)
    text = ""
    for page in doc:
        text += page.get_text()
    
    with open(r"d:\SwapU\docs\thesis\format_rules.txt", "w", encoding="utf-8") as f:
        f.write(text)
    print("PDF extracted")

if __name__ == "__main__":
    extract_pdf(r"d:\SwapU\要求和模板\附件4、武汉理工大学本科生毕业设计（论文）工作管理办法（试行）.pdf")
