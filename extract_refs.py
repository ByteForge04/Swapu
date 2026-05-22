import os
import glob
import fitz

docs_dir = r"d:\SwapU\docs\参考文献"
pdf_files = glob.glob(os.path.join(docs_dir, "*.pdf"))

all_texts = {}

for pdf_file in pdf_files:
    try:
        doc = fitz.open(pdf_file)
        # Extract first 3 pages as abstract/introduction for context
        text = ""
        for i in range(min(3, len(doc))):
            text += doc[i].get_text()
        all_texts[os.path.basename(pdf_file)] = text[:1500] # Limit size per file
    except Exception as e:
        all_texts[os.path.basename(pdf_file)] = f"Error: {str(e)}"

with open(r"d:\SwapU\temp_refs_summary.txt", "w", encoding="utf-8") as f:
    for name, text in all_texts.items():
        f.write(f"--- {name} ---\n")
        # Remove excessive newlines
        clean_text = " ".join(text.split())
        f.write(clean_text + "\n\n")

print(f"Extracted {len(pdf_files)} PDFs.")
