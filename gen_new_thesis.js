const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  AlignmentType, HeadingLevel, BorderStyle, WidthType, ShadingType,
  Header, Footer, PageNumber, PageBreak, LevelFormat
} = require("docx");

// Font config for Chinese
const fontConfig = { ascii: "Times New Roman", hAnsi: "Times New Roman", eastAsia: "SimSun" };
const fontConfigBold = { ascii: "Times New Roman", hAnsi: "Times New Roman", eastAsia: "SimHei" };

// Helper to create paragraph
function createParagraph(text, options = {}) {
  const { bold = false, size = 24, alignment = AlignmentType.JUSTIFIED, spacing = { before: 0, after: 0, line: 360 }, firstLineIndent = 480 } = options;
  return new Paragraph({
    alignment,
    spacing,
    indent: firstLineIndent ? { firstLine: firstLineIndent } : undefined,
    children: [
      new TextRun({
        text,
        bold,
        size,
        font: bold ? fontConfigBold : fontConfig
      })
    ]
  });
}

// Helper for heading
function createHeading(text, level = 1) {
  const sizes = { 1: 32, 2: 28, 3: 24 };
  return new Paragraph({
    alignment: level === 1 ? AlignmentType.CENTER : AlignmentType.LEFT,
    spacing: { before: 240, after: 120, line: 360 },
    children: [
      new TextRun({
        text,
        bold: true,
        size: sizes[level] || 24,
        font: fontConfigBold
      })
    ]
  });
}

// Create document
const doc = new Document({
  styles: {
    default: {
      document: {
        run: {
          font: fontConfig,
          size: 24
        }
      }
    },
    paragraphStyles: [
      {
        id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 32, bold: true, font: fontConfigBold },
        paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 0, keepNext: false, keepLines: false }
      },
      {
        id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 28, bold: true, font: fontConfigBold },
        paragraph: { spacing: { before: 200, after: 100 }, outlineLevel: 1, keepNext: false, keepLines: false }
      },
      {
        id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: fontConfigBold },
        paragraph: { spacing: { before: 160, after: 80 }, outlineLevel: 2, keepNext: false, keepLines: false }
      }
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: { top: 1440, right: 1800, bottom: 1440, left: 1800 }
      }
    },
    headers: {
      default: new Header({
        children: [
          new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
              new TextRun({
                text: "\u6b66\u6c49\u7406\u5de5\u5927\u5b66\u6bd5\u4e1a\u8bbe\u8ba1\uff08\u8bba\u6587\uff09",
                size: 18,
                font: fontConfig,
                color: "666666"
              })
            ]
          })
        ]
      })
    },
    footers: {
      default: new Footer({
        children: [
          new Paragraph({
            alignment: AlignmentType.CENTER,
            children: [
              new TextRun({ children: [PageNumber.CURRENT], size: 20, font: fontConfig })
            ]
          })
        ]
      })
    },
    children: [
      // Title Page
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 600, after: 400 },
        children: [
          new TextRun({
            text: "\u6b66\u6c49\u7406\u5de5\u5927\u5b66\u6bd5\u4e1a\u8bbe\u8ba1\uff08\u8bba\u6587\uff09",
            bold: true,
            size: 44,
            font: { ascii: "Times New Roman", hAnsi: "Times New Roman", eastAsia: "STZhongsong" }
          })
        ]
      }),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        spacing: { before: 800, after: 600 },
        children: [
          new TextRun({
            text: "\u57fa\u4e8e Vue3+SpringBoot \u7684\u6821\u56ed\u95f2\u7f6e\u7269\u54c1\u5faa\u73af\u5229\u7528\u7cfb\u7edf\u8bbe\u8ba1\u4e0e\u5b9e\u73b0",
            bold: true,
            size: 36,
            font: fontConfigBold
          })
        ]
      }),
      new Paragraph({
        spacing: { before: 400, after: 200 },
        children: [
          new TextRun({ text: "\u5b66    \u9662\uff1a", size: 28, font: fontConfig }),
          new TextRun({ text: "\u8ba1\u7b97\u673a\u4e0e\u4eba\u5de5\u667a\u80fd\u5b66\u9662", size: 28, font: fontConfig })
        ]
      }),
      new Paragraph({
        spacing: { before: 200, after: 200 },
        children: [
          new TextRun({ text: "\u4e13\u4e1a\u73ed\u7ea7\uff1a", size: 28, font: fontConfig }),
          new TextRun({ text: "\u8f6f\u4ef6\u5de5\u7a0b 2201 \u73ed", size: 28, font: fontConfig })
        ]
      }),
      new Paragraph({
        spacing: { before: 200, after: 200 },
        children: [
          new TextRun({ text: "\u5b66\u751f\u59d3\u540d\uff1a", size: 28, font: fontConfig }),
          new TextRun({ text: "\u00d7\u00d7\u00d7", size: 28, font: fontConfig })
        ]
      }),
      new Paragraph({
        spacing: { before: 200, after: 400 },
        children: [
          new TextRun({ text: "\u6307\u5bfc\u6559\u5e08\uff1a", size: 28, font: fontConfig }),
          new TextRun({ text: "\u00d7\u00d7\u00d7", size: 28, font: fontConfig })
        ]
      }),
      new Paragraph({ children: [new PageBreak()] }),

      // Abstract
      createHeading("\u6458  \u8981", 1),
      createParagraph("\u9ad8\u6821\u95f2\u7f6e\u7269\u54c1\u7684\u5468\u671f\u6027\u79ef\u538b\u662f\u4e00\u4e2a\u957f\u671f\u5b58\u5728\u4f46\u7f3a\u5c11\u6709\u6548\u6280\u672f\u65b9\u6848\u7684\u95ee\u9898\u3002\u6bcf\u5b66\u671f\u672b\uff0c\u5927\u91cf\u6559\u6750\u3001\u7535\u5b50\u4ea7\u54c1\u548c\u751f\u6d3b\u7528\u54c1\u5728\u77ed\u6682\u4f7f\u7528\u540e\u5373\u88ab\u95f2\u7f6e\uff0c\u800c\u4f20\u7edf\u7efc\u5408\u7c7b\u4e8c\u624b\u5e73\u53f0\u5728\u6821\u56ed\u8fd9\u4e00\u5c01\u95ed\u573a\u666f\u4e2d\u59cb\u7ec8\u672a\u80fd\u63d0\u4f9b\u7cbe\u51c6\u7684\u6480\u5408\u673a\u5236\u3002\u672c\u6587\u8bbe\u8ba1\u5e76\u5b9e\u73b0\u4e86\u4e00\u6b3e\u9762\u5411\u6821\u56ed\u7684\u667a\u80fd\u5316\u4e8c\u624b\u4ea4\u6613\u5e73\u53f0SwapU\uff0c\u4ece\u7cfb\u7edf\u5de5\u7a0b\u548cAI\u5e94\u7528\u4e24\u4e2a\u7ef4\u5ea6\u56de\u5e94\u4e0a\u8ff0\u6311\u6218\u3002"),
      createParagraph("\u5e73\u53f0\u91c7\u7528\u524d\u540e\u7aef\u5206\u79bb\u67b6\u6784\uff1a\u524d\u7aef\u4ee5Vue 3\u914d\u5408Element Plus\u6784\u5efa\u54cd\u5e94\u5f0f\u754c\u9762\uff0c\u540e\u7aef\u57fa\u4e8eSpring Boot 3.2\u63d0\u4f9bRESTful API\uff0c\u6570\u636e\u5c42\u7531MySQL 8.0\u627f\u62c5\u4e8b\u52a1\u5b58\u50a8\u3001Redis\u8d1f\u8d23\u7f13\u5b58\u52a0\u901f\u3002\u4e1a\u52a1\u5c42\u9762\uff0c\u5e73\u53f0\u6253\u901a\u4e86\u4ece\u5546\u54c1\u53d1\u5e03\u3001\u8ba2\u5355\u6d41\u8f6c\u5230\u652f\u4ed8\u5b9d\u6c99\u7bb1\u652f\u4ed8\u7684\u5168\u94fe\u8def\u4ea4\u6613\u95ed\u73af\uff0c\u5e76\u4ee5WebSocket\u534f\u8bae\u652f\u6491\u4e70\u5356\u53cc\u65b9\u7684\u5b9e\u65f6\u901a\u8baf\u3002"),
      createParagraph("AI\u4fa7\u7684\u6574\u5408\u662fSwapU\u533a\u522b\u4e8e\u4e00\u822c\u4ea4\u6613\u7cfb\u7edf\u7684\u6838\u5fc3\u3002\u67b6\u6784\u4e0a\uff0cSpring AI\u6846\u67b6\u7edf\u4e00\u4e86LLM\u8c03\u7528\u5165\u53e3\uff0cElasticsearch\u63a5\u7ba1\u4e86\u5168\u6587\u68c0\u7d22\u3002\u5728\u5bfc\u8d2d\u73af\u8282\uff0c\u5e73\u53f0\u7528RAG\uff08\u68c0\u7d22\u589e\u5f3a\u751f\u6210\uff09\u6280\u672f\u642d\u914dPgVector\u5411\u91cf\u6570\u636e\u5e93\uff0c\u6784\u5efa\u4e86\u4e00\u4e2a\u5177\u5907\u4e0a\u4e0b\u6587\u7406\u89e3\u80fd\u529b\u7684\u667a\u80fd\u5bfc\u8d2d\u52a9\u624b\u3002\u5728\u53d1\u5e03\u73af\u8282\uff0c\u5927\u8bed\u8a00\u6a21\u578b\uff08DeepSeek/Ollama\uff09\u7684\u4e00\u952e\u6587\u6848\u6da6\u8272\u529f\u80fd\u8ba9\u5356\u5bb6\u53ea\u9700\u8f93\u5165\u5173\u952e\u4fe1\u606f\u5373\u53ef\u83b7\u5f97\u7ed3\u6784\u5316\u7684\u5546\u54c1\u63cf\u8ff0\u3002"),
      createParagraph("\u6d4b\u8bd5\u6570\u636e\u8868\u660e\uff0cSwapU\u5728500\u7ebf\u7a0b\u6301\u7eed\u65bd\u538b5\u5206\u949f\u7684\u6761\u4ef6\u4e0b\uff0c\u6838\u5fc3\u4ea4\u6613\u94fe\u8def\u6ca1\u6709\u51fa\u73b0\u72b6\u6001\u65ad\u70b9\u6216\u810f\u6570\u636e\u3002RocketMQ\u5ef6\u65f6\u961f\u5217\u4fdd\u969c\u4e86\u8ba2\u5355\u8d85\u65f6\u573a\u666f\u4e0b\u7684\u72b6\u6001\u4e00\u81f4\u6027\u4e0e\u5e93\u5b58\u56de\u6eda\u3002\u6574\u4f53\u7cfb\u7edf\u5c06\u591a\u7ec4\u4ef6\u534f\u540c\u67b6\u6784\u4e0eAI\u6280\u672f\u878d\u5408\u4e3a\u4e00\u4e2a\u5b8c\u6574\u53ef\u7528\u7684\u5de5\u7a0b\u65b9\u6848\uff0c\u4e3a\u5927\u578b\u8bed\u8a00\u6a21\u578b\u5728\u5782\u76f4\u7535\u5546\u9886\u57df\u7684\u843d\u5730\u63d0\u4f9b\u4e86\u5b9e\u8bc1\u53c2\u8003\u3002"),
      new Paragraph({
        spacing: { before: 200, after: 100 },
        children: [
          new TextRun({ text: "\u5173\u952e\u8bcd\uff1a", bold: true, size: 24, font: fontConfigBold }),
          new TextRun({ text: "\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\uff1bSpring Boot\uff1bVue 3\uff1b\u5927\u8bed\u8a00\u6a21\u578b\uff1bRAG\uff1bRocketMQ", size: 24, font: fontConfig })
        ]
      }),
      new Paragraph({ children: [new PageBreak()] }),

      // Abstract (English)
      createHeading("Abstract", 1),
      createParagraph("The cyclical accumulation of idle items on university campuses is a persistent structural problem. At the end of every semester, textbooks, electronics, and household goods---barely used---are discarded, yet general-purpose second-hand platforms remain ill-fitted for the campus context: search results lack geographic precision, listing creation demands too much effort, and communication tools lack scenario-specific design. This paper presents SwapU, a campus-oriented intelligent second-hand trading platform that addresses these gaps through a combination of system engineering and practical AI integration.", { firstLineIndent: 0 }),
      createParagraph("The platform adopts a decoupled frontend-backend architecture: Vue 3 with Element Plus on the frontend, Spring Boot 3.2 RESTful APIs on the backend, MySQL 8.0 for transactional storage, and Redis for caching. The core trading loop---item publishing, order processing, and Alipay Sandbox payment---is fully implemented, with WebSocket-based real-time messaging connecting buyers and sellers.", { firstLineIndent: 0 }),
      createParagraph("The AI layer is where SwapU departs from conventional second-hand systems. The Spring AI framework provides a unified abstraction over LLM providers, while Elasticsearch handles full-text search. On the search side, RAG (Retrieval-Augmented Generation) technology combined with the PgVector vector database powers an intelligent shopping assistant: user queries in natural language are first matched against real inventory via vector similarity search, and only then passed to the LLM for grounded recommendation---a design that directly suppresses hallucination. On the listing side, a one-click copywriting polish feature powered by LLMs (DeepSeek/Ollama) generates structured product descriptions from minimal keyword input.", { firstLineIndent: 0 }),
      createParagraph("Load testing at 500 concurrent threads for 5 minutes confirmed zero broken state transitions and zero dirty data across core trading APIs. RocketMQ delayed messages ensure consistent order timeout handling and inventory rollback. The system demonstrates that practical AI deployment in a vertical e-commerce domain is achievable through careful data pipeline engineering rather than model selection alone.", { firstLineIndent: 0 }),
      new Paragraph({
        spacing: { before: 200, after: 100 },
        children: [
          new TextRun({ text: "Keywords: ", bold: true, size: 24, font: { ascii: "Times New Roman", hAnsi: "Times New Roman", eastAsia: "SimHei" } }),
          new TextRun({ text: "Campus Second-hand Trading; Spring Boot; Vue 3; Large Language Model; RAG; RocketMQ", size: 24, font: { ascii: "Times New Roman", hAnsi: "Times New Roman", eastAsia: "SimSun" } })
        ]
      }),
      new Paragraph({ children: [new PageBreak()] }),

      // Chapter 1
      createHeading("\u7b2c1\u7ae0  \u7eea\u8bba", 1),
      createHeading("1.1  \u8bfe\u9898\u7814\u7a76\u80cc\u666f\u53ca\u610f\u4e49", 2),
      createParagraph("\u9ad8\u6821\u95f2\u7f6e\u7269\u54c1\u7684\u5468\u671f\u6027\u79ef\u538b\uff0c\u5e76\u4e0d\u662f\u4e00\u4e2a\u65b0\u73b0\u8c61\u3002\u6bcf\u5b66\u671f\u672b\uff0c\u5bbf\u820d\u697c\u4e0b\u6210\u645e\u7684\u8003\u7814\u4e66\u3001\u6ca1\u62c6\u5c01\u7684\u751f\u6d3b\u7528\u54c1\u3001\u7528\u4e86\u51e0\u4e2a\u6708\u7684\u7535\u5b50\u8bbe\u5907\u2014\u2014\u8fd9\u6279\u7269\u8d44\u88ab\u201c\u6dd8\u6c70\u201d\u7684\u539f\u56e0\u5e76\u975e\u4e27\u5931\u4e86\u4f7f\u7528\u4ef7\u503c\uff0c\u800c\u662f\u539f\u4e3b\u4eba\u7684\u5b66\u4e1a\u9636\u6bb5\u53d1\u751f\u4e86\u5207\u6362\u3002\u4e00\u9762\u662f\u5de8\u91cf\u95f2\u7f6e\u54c1\u7684\u6301\u7eed\u4ea7\u751f\uff0c\u53e6\u4e00\u9762\u662f\u65b0\u751f\u7fa4\u4f53\u5e74\u590d\u4e00\u5e74\u7684\u521a\u6027\u91c7\u8d2d\u9700\u6c42\u3002\u4e8c\u8005\u7684\u9519\u914d\u662f\u7ed3\u6784\u6027\u7684\u3002\u56e0\u6b64\uff0c\u6784\u5efa\u4e00\u4e2a\u9ad8\u6548\u4fbf\u6377\u7684\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u751f\u6001\uff0c\u5728\u8d44\u6e90\u5faa\u73af\u5229\u7528\u548c\u7eff\u8272\u6d88\u8d39\u4e24\u4e2a\u7ef4\u5ea6\u4e0a\u90fd\u5177\u6709\u5207\u5b9e\u7684\u73b0\u5b9e\u610f\u4e49\u3002"),
      createParagraph("\u4e00\u4e2a\u503c\u5f97\u8ffd\u95ee\u7684\u95ee\u9898\u662f\uff1a\u95f2\u9c7c\u548c\u8f6c\u8f6c\u4e3a\u4ec0\u4e48\u6ca1\u80fd\u6709\u6548\u8986\u76d6\u8fd9\u4e00\u9700\u6c42\uff1f\u4ece\u4f53\u91cf\u4e0a\u770b\uff0c\u95f2\u9c7c\u80cc\u9760\u652f\u4ed8\u5b9d\u7684\u4fe1\u7528\u4f53\u7cfb\uff0c\u8f6c\u8f6c\u57283C\u54c1\u7c7b\u7684\u8d28\u68c0\u6d41\u7a0b\u4e5f\u505a\u5f97\u624e\u5b9e\u3002\u4f46\u5b83\u4eec\u7684\u67b6\u6784\u57fa\u56e0\u662f\u201c\u5168\u56fd\u5927\u5e02\u573a\u201d\u2014\u2014\u7b97\u6cd5\u4f18\u5148\u63a8\u8350\u8de8\u533a\u57df\u4f18\u8d28\u5356\u5bb6\uff0c\u7269\u6d41\u4f53\u7cfb\u56f4\u7ed5\u5feb\u9012\u7f51\u7edc\u8bbe\u8ba1\uff0c\u5546\u54c1\u6c60\u5927\u5230\u65e0\u6cd5\u7528\u6821\u533a\u534a\u5f84\u5708\u5b9a\u3002\u8fd9\u5957\u903b\u8f91\u653e\u5728\u6821\u56ed\u573a\u666f\u91cc\uff0c\u51fa\u73b0\u4e86\u4e09\u65b9\u9762\u7684\u9519\u4f4d\u3002"),
      createParagraph("\u5176\u4e00\uff0c\u4fe1\u606f\u5339\u914d\u6548\u7387\u4e0d\u8db3\u3002\u641c\u7d22\u8005\u5173\u5fc3\u7684\u4e0d\u662f\u5168\u56fd\u8303\u56f4\u5185\u8c01\u7684\u4fe1\u7528\u5206\u6700\u9ad8\uff0c\u800c\u662f\u5bf9\u65b9\u662f\u5426\u5728\u76f8\u90bb\u5bbf\u820d\u697c\u3002\u4f20\u7edf\u57fa\u4e8e\u5012\u6392\u7d22\u5f15\u7684\u5173\u952e\u8bcd\u5339\u914d\u96be\u4ee5\u7406\u89e3\u201c\u4fbf\u5b9c\u4e14\u9002\u5408\u8003\u7814\u7528\u7684\u201d\u8fd9\u7c7b\u5e26\u6709\u9690\u542b\u610f\u56fe\u7684\u81ea\u7136\u8bed\u8a00\u67e5\u8be2\uff0c\u53ec\u56de\u7387\u4e0e\u51c6\u786e\u7387\u5747\u4e0d\u7406\u60f3\u3002\u5176\u4e8c\uff0c\u53d1\u5e03\u95e8\u69db\u504f\u9ad8\u3002\u64b0\u5199\u4e00\u6761\u5438\u5f15\u4eba\u7684\u5546\u54c1\u63cf\u8ff0\u9700\u8981\u4e00\u5b9a\u7684\u65f6\u95f4\u548c\u8868\u8fbe\u529b\u2014\u2014\u5927\u591a\u6570\u5b66\u751f\u4e0d\u613f\u6295\u5165\u592a\u591a\u7cbe\u529b\uff0c\u4f46\u8fc7\u4e8e\u7b80\u7565\u7684\u63cf\u8ff0\u53c8\u96be\u4ee5\u5728\u641c\u7d22\u4e2d\u83b7\u5f97\u66dd\u5149\u3002\u9648\u51b0\u5728\u5176\u7855\u58eb\u8bba\u6587\u4e2d\u6307\u51fa\uff0c\u5546\u54c1\u4fe1\u606f\u7684\u5448\u73b0\u8d28\u91cf\u76f4\u63a5\u5f71\u54cd\u7528\u6237\u7684\u4ea4\u6613\u51b3\u7b56\uff0c\u800c\u4e14\u57fa\u4e8eSpringBoot\u7684\u7cfb\u7edf\u8bbe\u8ba1\u80fd\u591f\u6709\u6548\u652f\u6491\u8fd9\u4e00\u9700\u6c42[1]\u3002\u5176\u4e09\uff0c\u6c9f\u901a\u7f3a\u4e4f\u573a\u666f\u5316\u8003\u91cf\u3002\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u4ee5\u5f53\u9762\u4ea4\u4ed8\u4e3a\u4e3b\uff0c\u804a\u5929\u4e2d\u9891\u7e41\u6d89\u53ca\u201c\u4ec0\u4e48\u65f6\u95f4\u5728\u54ea\u680b\u697c\u78b0\u5934\u201d\u8fd9\u7c7b\u672c\u5730\u5316\u4fe1\u606f\uff0c\u4f46\u73b0\u6709\u7efc\u5408\u5e73\u53f0\u7684\u5373\u65f6\u901a\u8baf\u529f\u80fd\u5728\u8bbe\u8ba1\u4e0a\u5e76\u672a\u5bf9\u6821\u56ed\u573a\u666f\u505a\u9488\u5bf9\u6027\u4f18\u5316\u3002"),
      createParagraph("SwapU\u7684\u8bbe\u8ba1\u6b63\u662f\u56f4\u7ed5\u4e0a\u8ff0\u4e09\u4e2a\u652f\u70b9\u5c55\u5f00\u7684\u3002\u5e73\u53f0\u57fa\u4e8eSpring Boot 3.2\u4e0eVue 3\u6784\u5efa\u4e86\u5b8c\u6574\u7684\u4ea4\u6613\u94fe\u8def\uff0c\u66f4\u5173\u952e\u7684\u662f\u5c06Spring AI\u6846\u67b6\u4e0e\u5927\u8bed\u8a00\u6a21\u578b\u5d4c\u5165\u5230\u4e86\u4e24\u4e2a\u6838\u5fc3\u4e1a\u52a1\u73af\u8282\u3002\u5728\u53d1\u5e03\u4fa7\uff0c\u5356\u5bb6\u53ea\u9700\u8f93\u5165\u5173\u952e\u4fe1\u606f\u5e76\u70b9\u51fb\u201cAI\u4e00\u952e\u6da6\u8272\u201d\uff0c\u7cfb\u7edf\u901a\u8fc7\u53cd\u590d\u8c03\u4f18\u7684System Prompt\u8c03\u7528\u5927\u6a21\u578b\u751f\u6210\u7ed3\u6784\u5316\u7684\u5546\u54c1\u63cf\u8ff0\u6587\u6848\uff0c\u663e\u8457\u964d\u4f4e\u4e86\u5546\u54c1\u4fe1\u606f\u7684\u5448\u73b0\u95e8\u69db\u3002\u5728\u5bfc\u8d2d\u4fa7\uff0c\u7528\u6237\u4ee5\u81ea\u7136\u8bed\u8a00\u5728\u5bf9\u8bdd\u6846\u4e2d\u63d0\u95ee\uff0c\u7cfb\u7edf\u5e76\u4e0d\u76f4\u63a5\u5c06\u95ee\u9898\u4ea4\u7ed9\u5927\u6a21\u578b\uff0c\u800c\u662f\u5148\u5728PgVector\u5411\u91cf\u6570\u636e\u5e93\u4e2d\u6267\u884c\u4f59\u5f26\u76f8\u4f3c\u5ea6\u68c0\u7d22\uff0c\u5c06\u5e73\u53f0\u4e0a\u771f\u5b9e\u5728\u552e\u7684Top 3\u5546\u54c1\u4fe1\u606f\u2014\u2014\u5305\u62ec\u6807\u9898\u3001\u4ef7\u683c\u548c\u63cf\u8ff0\u2014\u2014\u4f5c\u4e3a\u4e0a\u4e0b\u6587\u6ce8\u5165Prompt\uff0c\u518d\u7531\u6a21\u578b\u57fa\u4e8e\u8fd9\u4e9b\u771f\u5b9e\u6570\u636e\u751f\u6210\u63a8\u8350\u3002\u8fd9\u4e00\u201c\u5148\u68c0\u7d22\uff0c\u540e\u751f\u6210\u201d\u7684RAG\uff08\u68c0\u7d22\u589e\u5f3a\u751f\u6210\uff09\u67b6\u6784\uff0c\u662fSwapU\u5728\u5de5\u7a0b\u4e0a\u6700\u6838\u5fc3\u7684\u8bbe\u8ba1\u51b3\u7b56\uff1a\u5b83\u4ece\u6839\u672c\u4e0a\u6291\u5236\u4e86\u5927\u6a21\u578b\u5728\u5782\u76f4\u573a\u666f\u4e2d\u5e38\u89c1\u7684\u201c\u5e7b\u89c9\u201d\u95ee\u9898\u3002\u672c\u8bfe\u9898\u7684\u4ea7\u51fa\uff0c\u65e2\u662f\u4e00\u4e2a\u5b8c\u6574\u53ef\u8fd0\u884c\u7684\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u7cfb\u7edf\uff0c\u4e5f\u4e3a\u5927\u578b\u8bed\u8a00\u6a21\u578b\u5728\u5782\u76f4\u7535\u5546\u9886\u57df\u7684\u5de5\u7a0b\u5316\u843d\u5730\u63d0\u4f9b\u4e86\u4e00\u4efd\u5b9e\u8bc1\u53c2\u8003\u3002"),

      // 1.2 Research Status
      createHeading("1.2  \u56fd\u5185\u5916\u7814\u7a76\u73b0\u72b6", 2),
      createParagraph("\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u7cfb\u7edf\u7684\u6280\u672f\u5b9e\u73b0\u5df2\u6709\u8f83\u4e3a\u4e30\u5bcc\u7684\u5b66\u672f\u79ef\u7d2f\u3002\u5728\u7cfb\u7edf\u67b6\u6784\u5c42\u9762\uff0c\u9648\u51b0\u57fa\u4e8eSpringBoot\u6846\u67b6\u8bbe\u8ba1\u5e76\u5b9e\u73b0\u4e86\u6821\u56ed\u4e8c\u624b\u5546\u54c1\u4ea4\u6613\u7cfb\u7edf\uff0c\u8be5\u7cfb\u7edf\u91c7\u7528\u524d\u540e\u7aef\u5206\u79bb\u67b6\u6784\uff0c\u524d\u7aef\u4f7f\u7528Vue\u6846\u67b6\uff0c\u540e\u7aef\u91c7\u7528SpringBoot\u63d0\u4f9bRESTful API\u63a5\u53e3\uff0c\u6570\u636e\u5e93\u9009\u7528MySQL\uff0c\u5b9e\u73b0\u4e86\u5546\u54c1\u53d1\u5e03\u3001\u6d4f\u89c8\u3001\u641c\u7d22\u3001\u8ba2\u5355\u7ba1\u7406\u7b49\u6838\u5fc3\u529f\u80fd[1]\u3002\u8be5\u7814\u7a76\u4e0e\u672c\u9879\u76ee\u7684\u6280\u672f\u6808\u9009\u578b\u9ad8\u5ea6\u4e00\u81f4\uff0c\u4e3a\u672c\u7cfb\u7edf\u7684\u67b6\u6784\u8bbe\u8ba1\u63d0\u4f9b\u4e86\u91cd\u8981\u53c2\u8003\u3002"),
      createParagraph("\u5728\u63a8\u8350\u7b97\u6cd5\u5e94\u7528\u65b9\u9762\uff0c\u9648\u6625\u9f99\u7814\u7a76\u4e86\u57fa\u4e8e\u6df7\u5408\u63a8\u8350\u7684\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u7cfb\u7edf\uff0c\u8be5\u7cfb\u7edf\u878d\u5408\u4e86\u57fa\u4e8e\u5185\u5bb9\u7684\u63a8\u8350\u548c\u534f\u540c\u8fc7\u6ee4\u63a8\u8350\u7b97\u6cd5\uff0c\u901a\u8fc7\u591a\u7ef4\u5ea6\u7684\u63a8\u8350\u7b56\u7565\u63d0\u5347\u4e86\u5546\u54c1\u5339\u914d\u7684\u51c6\u786e\u6027\u548c\u7528\u6237\u6ee1\u610f\u5ea6[2]\u3002\u8d75\u58ee\u5219\u4ece\u63a8\u8350\u7b97\u6cd5\u5728\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u5e73\u53f0\u4e2d\u7684\u5e94\u7528\u89d2\u5ea6\u51fa\u53d1\uff0c\u63a2\u8ba8\u4e86\u57fa\u4e8e\u7528\u6237\u884c\u4e3a\u548c\u5546\u54c1\u7279\u5f81\u7684\u63a8\u8350\u7b56\u7565\uff0c\u4e3a\u672c\u9879\u76ee\u7684AI\u667a\u80fd\u5bfc\u8d2d\u529f\u80fd\u63d0\u4f9b\u4e86\u63a8\u8350\u7b97\u6cd5\u5c42\u9762\u7684\u53c2\u8003[3]\u3002"),
      createParagraph("\u5728\u7528\u6237\u4f53\u9a8c\u4e0e\u754c\u9762\u8bbe\u8ba1\u65b9\u9762\uff0c\u738b\u9759\u4ece\u7528\u6237\u4f53\u9a8c\u89d2\u5ea6\u7814\u7a76\u4e86\u6821\u56ed\u95f2\u7f6e\u7269\u54c1\u4ea4\u6613\u5c0f\u7a0b\u5e8f\u7684\u754c\u9762\u8bbe\u8ba1\uff0c\u63d0\u51fa\u4e86\u9488\u5bf9\u6821\u56ed\u573a\u666f\u7684\u4ea4\u4e92\u4f18\u5316\u65b9\u6848[4]\u3002\u8be5\u7814\u7a76\u5bf9\u672c\u9879\u76ee\u524d\u7aefUI/UX\u8bbe\u8ba1\u5177\u6709\u91cd\u8981\u7684\u542f\u793a\u610f\u4e49\u3002\u5468\u4f73\u654f\u5219\u4ece\u7528\u6237\u611f\u77e5\u4ef7\u503c\u89d2\u5ea6\u7814\u7a76\u4e86\u4e8c\u624b\u4ea4\u6613\u5e73\u53f0\u7528\u6237\u6ee1\u610f\u5ea6\u7684\u5f71\u54cd\u56e0\u7d20\uff0c\u4e3a\u672c\u9879\u76ee\u7684\u7528\u6237\u4f53\u9a8c\u4f18\u5316\u548c\u9700\u6c42\u5206\u6790\u63d0\u4f9b\u4e86\u7406\u8bba\u652f\u6491[10]\u3002"),
      createParagraph("\u5728\u4e91\u5e73\u53f0\u4e0e\u5fae\u670d\u52a1\u67b6\u6784\u65b9\u9762\uff0c\u767d\u5fe0\u519b\u8bbe\u8ba1\u4e86\u57fa\u4e8e\u4e91\u5e73\u53f0\u7684\u6821\u56ed\u4e8c\u624b\u5546\u54c1\u4ea4\u6613\u7cfb\u7edf\uff0c\u63a2\u8ba8\u4e86\u4e91\u8ba1\u7b97\u73af\u5883\u4e0b\u7684\u7cfb\u7edf\u90e8\u7f72\u4e0e\u67b6\u6784\u8bbe\u8ba1[5]\u3002\u8be5\u7814\u7a76\u4e0e\u672c\u9879\u76ee\u7684Docker\u5bb9\u5668\u5316\u90e8\u7f72\u65b9\u6848\u5f62\u6210\u5bf9\u7167\uff0c\u4e3a\u7cfb\u7edf\u7684\u53ef\u6269\u5c55\u6027\u8bbe\u8ba1\u63d0\u4f9b\u4e86\u53c2\u8003\u3002"),
      createParagraph("\u5728Web\u5e94\u7528\u67b6\u6784\u65b9\u9762\uff0c\u82cf\u7389\u6167\u8bbe\u8ba1\u4e86\u57fa\u4e8eB/S\u67b6\u6784\u7684\u9ad8\u6821\u4e8c\u624b\u7f51\u7edc\u4ea4\u6613\u5e73\u53f0\uff0c\u5b9e\u73b0\u4e86\u7528\u6237\u7ba1\u7406\u3001\u5546\u54c1\u53d1\u5e03\u3001\u4ea4\u6613\u6d41\u7a0b\u7b49\u529f\u80fd[6]\u3002\u7f2c\u5821\u5219\u91c7\u7528MVC\u8bbe\u8ba1\u6a21\u5f0f\u5b9e\u73b0\u4e86\u6821\u56ed\u4e8c\u624b\u5546\u54c1\u4ea4\u6613\u7cfb\u7edf\uff0c\u63a2\u8ba8\u4e86\u7cfb\u7edf\u67b6\u6784\u8bbe\u8ba1\u4e0e\u5b9e\u73b0\u65b9\u6cd5[7]\u3002\u4e8e\u8427\u8bbe\u8ba1\u4e86\u57fa\u4e8eJ2EE\u7684\u6821\u56ed\u4e8c\u624b\u7269\u54c1\u7f51\u7edc\u9884\u4ea4\u6613\u7cfb\u7edf\uff0c\u63a2\u8ba8\u4e86\u9884\u4ea4\u6613\u6a21\u5f0f\u7684\u5b9e\u73b0\u65b9\u6848[8]\u3002\u6768\u519b\u5219\u8bbe\u8ba1\u4e86\u57fa\u4e8eThinkphp\u6846\u67b6\u7684\u6821\u56ed\u4e8c\u624b\u56fe\u4e66\u4ea4\u6613\u7cfb\u7edf\uff0c\u5b9e\u73b0\u4e86\u56fe\u4e66\u53d1\u5e03\u3001\u641c\u7d22\u3001\u4ea4\u6613\u7b49\u529f\u80fd[9]\u3002"),
      createParagraph("\u7efc\u5408\u4e0a\u8ff0\u7814\u7a76\u73b0\u72b6\uff0c\u5f53\u524d\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u7cfb\u7edf\u7684\u7814\u7a76\u4e3b\u8981\u96c6\u4e2d\u5728\u4ee5\u4e0b\u51e0\u4e2a\u65b9\u5411\uff1a\u4e00\u662f\u57fa\u4e8eSpringBoot\u7b49\u73b0\u4ee3Web\u6846\u67b6\u7684\u7cfb\u7edf\u67b6\u6784\u8bbe\u8ba1\uff1b\u4e8c\u662f\u57fa\u4e8e\u534f\u540c\u8fc7\u6ee4\u3001\u5185\u5bb9\u63a8\u8350\u7b49\u7b97\u6cd5\u7684\u667a\u80fd\u63a8\u8350\u7cfb\u7edf\uff1b\u4e09\u662f\u57fa\u4e8e\u7528\u6237\u4f53\u9a8c\u7684\u754c\u9762\u8bbe\u8ba1\u4e0e\u4ea4\u4e92\u4f18\u5316\uff1b\u56db\u662f\u57fa\u4e8e\u4e91\u5e73\u53f0\u7684\u5fae\u670d\u52a1\u67b6\u6784\u90e8\u7f72\u3002\u7136\u800c\uff0c\u5c06\u5927\u8bed\u8a00\u6a21\u578b\u4e0eRAG\u6280\u672f\u6df1\u5ea6\u878d\u5165\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u573a\u666f\u7684\u7814\u7a76\u4ecd\u8f83\u4e3a\u7a00\u7f3a\u3002\u672c\u9879\u76ee\u5728\u5145\u5206\u5438\u6536\u73b0\u6709\u7814\u7a76\u6210\u679c\u7684\u57fa\u7840\u4e0a\uff0c\u521b\u65b0\u6027\u5730\u5c06Spring AI\u6846\u67b6\u3001RAG\u68c0\u7d22\u589e\u5f3a\u751f\u6210\u6280\u672f\u4e0e\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u573a\u666f\u76f8\u7ed3\u5408\uff0c\u6784\u5efa\u4e86\u4e00\u5957\u5177\u5907\u667a\u80fd\u5bfc\u8d2d\u548c\u4e00\u952e\u6587\u6848\u751f\u6210\u80fd\u529b\u7684\u667a\u80fd\u5316\u4ea4\u6613\u5e73\u53f0\uff0c\u586b\u8865\u4e86\u8be5\u9886\u57df\u7684\u7814\u7a76\u7a7a\u767d\u3002"),

      // References
      new Paragraph({ children: [new PageBreak()] }),
      createHeading("\u53c2\u8003\u6587\u732e", 1),
      new Paragraph({
        spacing: { before: 100, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[1] \u9648\u51b0. \u57fa\u4e8eSpringBoot\u7684\u6821\u56ed\u4e8c\u624b\u5546\u54c1\u4ea4\u6613\u7cfb\u7edf\u7684\u8bbe\u8ba1\u4e0e\u5b9e\u73b0[D]. \u534e\u4e2d\u5e08\u8303\u5927\u5b66, 2024.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[2] \u9648\u6625\u9f99. \u57fa\u4e8e\u6df7\u5408\u63a8\u8350\u7684\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u7cfb\u7edf\u7684\u7814\u7a76\u4e0e\u5b9e\u73b0[D]. \u8fbd\u5b81\u5927\u5b66, 2023.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[3] \u8d75\u58ee. \u63a8\u8350\u7b97\u6cd5\u5728\u6821\u56ed\u4e8c\u624b\u4ea4\u6613\u5e73\u53f0\u4e2d\u7684\u7814\u7a76\u4e0e\u5e94\u7528[D]. \u6b66\u6c49\u8f7b\u5de5\u5927\u5b66, 2024.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[4] \u738b\u9759. \u57fa\u4e8e\u7528\u6237\u4f53\u9a8c\u7684\u6821\u56ed\u95f2\u7f6e\u7269\u54c1\u4ea4\u6613\u5c0f\u7a0b\u5e8f\u754c\u9762\u8bbe\u8ba1\u7814\u7a76[D]. \u6c5f\u82cf\u5927\u5b66, 2024.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[5] \u767d\u5fe0\u519b. \u57fa\u4e8e\u4e91\u5e73\u53f0\u7684\u6821\u56ed\u4e8c\u624b\u5546\u54c1\u4ea4\u6613\u7cfb\u7edf\u7684\u8bbe\u8ba1\u4e0e\u5b9e\u73b0[D]. \u8d35\u5dde\u5927\u5b66, 2023.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[6] \u82cf\u7389\u6167. \u57fa\u4e8eB/S\u67b6\u6784\u7684\u9ad8\u6821\u4e8c\u624b\u7f51\u7edc\u4ea4\u6613\u5e73\u53f0\u7684\u8bbe\u8ba1\u4e0e\u5b9e\u73b0[D]. \u5357\u660c\u5927\u5b66, 2022.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[7] \u7f2c\u5821. \u57fa\u4e8eMVC\u7684\u6821\u56ed\u4e8c\u624b\u5546\u54c1\u4ea4\u6613\u7cfb\u7edf\u8bbe\u8ba1\u4e0e\u5b9e\u73b0[D]. \u4e1c\u5317\u5927\u5b66, 2022.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[8] \u4e8e\u8427. \u57fa\u4e8eJ2EE\u7684\u6821\u56ed\u4e8c\u624b\u7269\u54c1\u7f51\u7edc\u9884\u4ea4\u6613\u7cfb\u7edf\u7684\u8bbe\u8ba1\u4e0e\u5b9e\u73b0[D]. \u5c71\u4e1c\u5e08\u8303\u5927\u5b66, 2021.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[9] \u6768\u519b. \u57fa\u4e8eThinkphp\u6846\u67b6\u7684\u6821\u56ed\u4e8c\u624b\u56fe\u4e66\u4ea4\u6613\u7cfb\u7edf\u7684\u8bbe\u8ba1\u4e0e\u5b9e\u73b0[D]. \u5c71\u4e1c\u5e08\u8303\u5927\u5b66, 2020.", size: 21, font: fontConfig })]
      }),
      new Paragraph({
        spacing: { before: 60, after: 60 },
        indent: { left: 420, hanging: 420 },
        children: [new TextRun({ text: "[10] \u5468\u4f73\u654f. \u4e8c\u624b\u4ea4\u6613\u5e73\u53f0\u7528\u6237\u611f\u77e5\u4ef7\u503c\u5bf9\u6ee1\u610f\u5ea6\u7684\u5f71\u54cd\u7814\u7a76[D]. \u54c8\u5c14\u6ee8\u5546\u4e1a\u5927\u5b66, 2024.", size: 21, font: fontConfig })]
      })
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("d:\\SwapU\\docs\\thesis\\SwapU\\u6bd5\\u4e1a\\u8bba\\u6587_\\u65b0\\u7248\\u672c.docx", buffer);
  console.log("New thesis document generated successfully!");
});
