const fs = require('fs');
const xml = fs.readFileSync('d:\\SwapU\\docs\\thesis\\template_document.xml', 'utf8');

const paras = xml.match(/<w:p[\s>][\s\S]*?<\/w:p>/g) || [];
for (let i = 0; i < paras.length; i++) {
  const texts = [];
  const matches = paras[i].matchAll(/<w:t[^>]*>([^<]*)<\/w:t>/g);
  for (const m of matches) texts.push(m[1]);
  const text = texts.join('');
  
  const pStyleMatch = paras[i].match(/<w:pStyle w:val="([^"]+)"/);
  const pStyle = pStyleMatch ? pStyleMatch[1] : 'none';
  
  const szMatch = paras[i].match(/<w:sz w:val="(\d+)"/);
  const sz = szMatch ? szMatch[1] : 'none';
  
  const jcMatch = paras[i].match(/<w:jc w:val="([^"]+)"/);
  const jc = jcMatch ? jcMatch[1] : 'none';
  
  const hasB = paras[i].includes('<w:b/>') || paras[i].includes('<w:b ');
  
  if (text.trim()) {
    console.log('P' + (i+1) + ' [style=' + pStyle + ' sz=' + sz + ' jc=' + jc + ' bold=' + hasB + ']: ' + text.substring(0, 80));
  } else {
    console.log('P' + (i+1) + ' [style=' + pStyle + ' sz=' + sz + '] (empty)');
  }
}
