import re

with open(r'd:\SwapU\generate_ppt_pptxgen.js', 'r', encoding='utf-8') as f:
    content = f.read()

# Dictionary of replacements: old_char -> new_char
replacements = {
    '“': '「',  # left double quote → 「
    '”': '」',  # right double quote → 」
    '‘': '『',  # left single quote → 『
    '’': '』',  # right single quote → 』
    '＂': '「',  # fullwidth quote → 「
}

for old, new in replacements.items():
    count = content.count(old)
    if count > 0:
        content = content.replace(old, new)
        print(f'Replaced {count} occurrences of U+{ord(old):04X}')

with open(r'd:\SwapU\generate_ppt_pptxgen.js', 'w', encoding='utf-8') as f:
    f.write(content)
print('Done')
