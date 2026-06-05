package com.openjiuwen.studio.agent.space.app.util.md.converter;

import lombok.extern.slf4j.Slf4j;

import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;

/**
 * HTML to PDF 转换器
 */
@Slf4j
public class HtmlToPdfConverter {

    /**
     * 将 HTML 转换为 PDF
     *
     * @param htmlContent HTML 内容
     * @return PDF 字节数组
     */
    public byte[] htmlToPdf(String htmlContent) throws Exception {
        if (htmlContent == null) {
            throw new IllegalArgumentException("HTML content cannot be null");
        }
        if (htmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be empty");
        }
        log.info("Starting HTML to PDF conversion, content length: {} characters", htmlContent.length());

        try {
            byte[] pdfBytes = getPdfBytes(htmlContent);
            log.info("HTML to PDF conversion completed, PDF size: {} bytes", pdfBytes.length);
            return pdfBytes;
        } catch (Exception e) {
            log.error("[htmlToPdf] HTML to PDF conversion failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 将 HTML 转换为 PDF
     *
     * @param htmlContent HTML 内容
     * @return PDF 字节数组
     */
    private byte[] getPdfBytes(String htmlContent) throws IOException {
        String fullHtml = createHtmlTemplate(htmlContent);

        ITextRenderer renderer = new ITextRenderer();
        ITextFontResolver resolver = renderer.getFontResolver();


        resolver.addFont("/fonts/HarmonyOS_Sans_SC_Regular.ttf",
                com.lowagie.text.pdf.BaseFont.IDENTITY_H,
                com.lowagie.text.pdf.BaseFont.EMBEDDED);

        renderer.setDocumentFromString(fullHtml);
        renderer.layout();

        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        renderer.createPDF(out);
        return out.toByteArray();
    }

    /**
     * 创建 HTML 包裹模板
     */
    private String createHtmlTemplate(String content) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <style>
                        * {
                            font-family: 'HarmonyOS Sans SC', sans-serif !important;
                        }
                        table {
                            border-collapse: collapse !important;
                            table-layout: fixed !important;
                            margin: 20px 0 !important;
                            border: 1px solid #000 !important;
                        }
                        th, td {
                            border: 1px solid #000 !important;
                            padding: 8px 12px !important;
                            text-align: left !important;
                            vertical-align: middle !important;
                            word-wrap: break-word !important;
                        }
                        th {
                            background-color: #f5f5f5 !important;
                            font-weight: bold !important;
                        }
                        tr:nth-child(even) td {
                            background-color: #fafafa !important;
                        }
                        pre {
                            background-color: #f8f8f8 !important;
                            padding: 10px !important;
                            border-radius: 6px !important;
                            overflow-x: auto !important;
                            font-size: 13px !important;
                        }
                        pre code {
                            background: none !important;
                            padding: 0 !important;
                        }
                    </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(content);
    }
}