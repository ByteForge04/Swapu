const fs = require('fs');
const xml = fs.readFileSync('d:\\SwapU\\docs\\thesis\\template_document.xml', 'utf8');

const paras = xml.match(/<w:p[\s>][\s\S]*?<\/w:p>/g) || [];

// Print full XML for key paragraphs
const keyParas = [7, 14, 35, 36, 37, 38, 97, 132, 134, 135];
for (const idx of keyParas) {
  const i = idx - 1;
  const texts = [];
  const matches = paras[i].matchAll(/<w:t[^>]*>([^<]*)<\/w:t>/g);
  for (const m of matches) texts.push(m[1]);
  const text = texts.join('');
  console.log('=== P' + idx + ' (text: ' + text.substring(0, 50) + ') ===');
  console.log(paras[i]);
  console.log('');
}
