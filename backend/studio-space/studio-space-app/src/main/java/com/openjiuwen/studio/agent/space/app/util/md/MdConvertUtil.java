package com.openjiuwen.studio.agent.space.app.util.md;

import com.openjiuwen.studio.agent.space.app.util.md.converter.HtmlToPdfConverter;
import com.openjiuwen.studio.agent.space.app.util.md.converter.HtmlToWordConverter;
import com.openjiuwen.studio.agent.space.app.util.md.parser.MarkdownParser;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Markdown 格式转换类，支持将Markdown输入流转为pdf/docx输入流
 */
@Slf4j
public class MdConvertUtil {
    private static final MdConvertUtil INSTANCE = new MdConvertUtil();

    private final MarkdownParser markdownParser = new MarkdownParser();

    private final HtmlToWordConverter htmlToWordConverter = new HtmlToWordConverter();

    private final HtmlToPdfConverter htmlToPdfConverter = new HtmlToPdfConverter();

    /**
     * 获取单例实例
     */
    public static MdConvertUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 将 Markdown 文件流转为 PDF 文件流
     *
     * @param inputStream Markdown 文件流
     * @return 转换后的 PDF 文件流
     * @throws IOException 转换异常
     */
    public byte[] markdownToPdf(InputStream inputStream, String targetFormat) throws IOException {
        log.info("Starting Markdown to PDF conversion");

        try {
            // 1. Markdown 转 HTML 并处理 LaTeX 公式
            String processedHtml = markdownParser.markdownToHtml(inputStream, targetFormat);
            // 输出转换后的 HTML 内容
            log.info("Converted HTML content: {}", processedHtml);
            // 2. HTML 转 PDF
            byte[] pdfBytes = htmlToPdfConverter.htmlToPdf(processedHtml);
            log.info("Markdown to PDF conversion completed");
            return pdfBytes;
        } catch (Exception e) {
            log.error("[markdownToPdf] Markdown to PDF conversion failed", e);
            throw new IOException("Markdown to PDF conversion failed", e);
        }
    }

    /**
     * 将 Markdown 文件流转为 DOCX 文件流
     *
     * @param inputStream Markdown 文件流
     * @return 转换后的 DOCX 文件流
     * @throws IOException 转换异常
     */
    public byte[] markdownToWord(InputStream inputStream, String targetFormat) throws IOException {
        log.info("Starting Markdown to Word conversion");

        try {
            // 1. Markdown 转 HTML
            String processedHtml = markdownParser.markdownToHtml(inputStream, targetFormat);

            // 2. HTML 转 Word
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                htmlToWordConverter.convert(processedHtml, outputStream);
                byte[] wordBytes = outputStream.toByteArray();
                log.info("Markdown to Word conversion completed");
                return wordBytes;
            }
        } catch (Exception e) {
            log.error("[markdownToWord] Markdown to Word conversion failed", e);
            throw new IOException("Markdown to Word conversion failed", e);
        }
    }
}