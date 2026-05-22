const { execSync } = require('child_process');
const fs = require('fs');

// Use PowerShell COM to extract text from .doc
const script = `
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open("d:\SwapU\要求和模板\武汉理工大学毕业设计周记.doc")
$text = $doc.Content.Text
$doc.Close()
$word.Quit()
Write-Output $text
`;

fs.writeFileSync('d:\\SwapU\\docs\\thesis\\extract_doc.ps1', script, 'utf8');

try {
  const result = execSync('powershell -NoProfile -ExecutionPolicy Bypass -File "d:\\SwapU\\docs\\thesis\\extract_doc.ps1"', { encoding: 'utf8', timeout: 60000 });
  console.log(result.substring(0, 5000));
} catch(e) {
  console.log('Error:', e.message.substring(0, 500));
}
