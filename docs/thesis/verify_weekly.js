const JSZip = require('jszip');
const fs = require('fs');

async function main() {
  const buf = fs.readFileSync('d:\\SwapU\\docs\\thesis\\毕业设计周记.docx');
  const zip = await JSZip.loadAsync(buf);
  const xml = await zip.file('word/document.xml').async('string');
  
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
    
    const hasB = paras[i].includes('<w:b/>') || paras[i].includes('<w:b ');
    
    if (text.trim()) {
      const preview = text.length > 70 ? text.substring(0, 70) + '...' : text;
      console.log('P' + (i+1) + ' [style=' + pStyle + ' sz=' + sz + ' bold=' + hasB + ']: ' + preview);
    } else {
      console.log('P' + (i+1) + ' [style=' + pStyle + ' sz=' + sz + '] (empty)');
    }
  }
  
  // Check styles.xml exists
  const styles = await zip.file('word/styles.xml').async('string');
  console.log('\nstyles.xml exists: ' + (styles.length > 0));
  
  // Check numbering.xml exists
  const numbering = zip.file('word/numbering.xml');
  console.log('numbering.xml exists: ' + (numbering !== null));
  
  // Check fontTable.xml exists
  const fontTable = zip.file('word/fontTable.xml');
  console.log('fontTable.xml exists: ' + (fontTable !== null));
  
  // Check settings.xml exists
  const settings = zip.file('word/settings.xml');
  console.log('settings.xml exists: ' + (settings !== null));
}
main().catch(console.error);
