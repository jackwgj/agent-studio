package com.openjiuwen.studio.agent.space.app.util.md.parser;

import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceErrorCodes;
import com.openjiuwen.studio.agent.space.common.exception.AgentSpaceException;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Safelist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * Markdown 解析器
 * 将 Markdown 解析为 HTML
 */
@Slf4j
public class MarkdownParser {
    private static final Parser PARSER;

    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS,
            List.of(TablesExtension.create(), TaskListExtension.create(), StrikethroughExtension.create(),
                EmojiExtension.create(), AutolinkExtension.create(), AbbreviationExtension.create(),
                DefinitionExtension.create(), TocExtension.create(), FootnoteExtension.create(),
                SuperscriptExtension.create(), InsExtension.create(), TypographicExtension.create()));
        options.set(Parser.HEADING_NO_ATX_SPACE, false);
        options.set(TablesExtension.COLUMN_SPANS, true);
        options.set(TablesExtension.MIN_HEADER_ROWS, 1);
        options.set(HtmlRenderer.PERCENT_ENCODE_URLS, true);

        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    /**
     * 将 Markdown 转换为 HTML
     *
     * @param inputStream Markdown 输入流
     * @return HTML 内容
     * @throws IOException 读取异常
     */
    public String markdownToHtml(InputStream inputStream, String targetFormat) throws IOException {
        String markdown = readMarkdownFromStream(inputStream);
        Document document = PARSER.parse(markdown);
        // 禁用不必要的html标签和属性
        Safelist safelist = (new Safelist()).addTags("a", "b", "blockquote", "br", "caption", "cite", "code", "col",
                "colgroup", "dd", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6", "i", "li", "ol", "p", "pre",
                "q", "small", "span", "strike", "strong", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https", "mailto");

        if (Objects.equals(targetFormat, "pdf")) {
            return cleanAsXhtml(RENDERER.render(document), safelist);
        } else if (Objects.equals(targetFormat, "docx")) {
            return Jsoup.clean(RENDERER.render(document), safelist);
        } else {
            throw new AgentSpaceException(AgentSpaceErrorCodes.AGENT_BUILDER_FILE_NOT_SUPPORT_ERROR);
        }
    }

    public String cleanAsXhtml(String html, Safelist safelist) {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        String cleaned = Jsoup.clean(doc.body().html(), safelist);
        doc.body().html(cleaned);
        doc.outputSettings()
            .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
            .escapeMode(Entities.EscapeMode.xhtml)
            .prettyPrint(false);
        return doc.body().html();
    }

    /**
     * 将 Markdown 输入流转为文本
     *
     * @param inputStream Markdown 输入流
     * @return Markdown 内容
     * @throws IOException 读取异常
     */
    private String readMarkdownFromStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return content.toString();
        }
    }
}