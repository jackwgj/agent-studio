package com.openjiuwen.studio.agent.space.app.util.md.converter;

import com.openjiuwen.studio.agent.space.app.util.md.MdConvertConstants;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTColor;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTUnderline;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STUnderline;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

/**
 * HTML to Word 转换器
 */
@Slf4j
public class HtmlToWordConverter {
    /**
     * 转换 HTML 到 Word
     *
     * @param htmlContent  HTML 内容
     * @param outputStream 输出流
     * @throws IOException 转换异常
     */
    public void convert(String htmlContent, OutputStream outputStream) throws IOException {
        log.info("Starting HTML to Word conversion");

        try (XWPFDocument document = new XWPFDocument()) {
            // 解析 HTML
            org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(htmlContent);
            Element body = doc.body();

            // 递归处理 HTML 节点
            parseHtmlToWord(body, document);

            // 写入输出流
            document.write(outputStream);
            log.info("HTML to Word conversion completed");
        } catch (Exception e) {
            log.error("[convert] HTML to Word conversion failed", e);
            throw e;
        }
    }

    /**
     * 递归处理所有节点
     */
    private void parseHtmlToWord(Element root, XWPFDocument doc) {
        List<Node> nodes = root.childNodes();
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text().trim();
                if (!text.isEmpty()) {
                    XWPFParagraph para = doc.createParagraph();
                    XWPFRun run = para.createRun();
                    run.setText(text);
                    run.setFontSize(MdConvertConstants.NORMAL_FONT_SIZE);
                }
            } else if (node instanceof Element elem) {
                switch (elem.tagName().toLowerCase(Locale.ROOT)) {
                    case "h1":
                    case "h2":
                    case "h3":
                    case "h4":
                    case "h5":
                    case "h6":
                        handleHeading(elem, doc);
                        break;
                    case "p":
                        handleParagraph(elem, doc);
                        break;
                    case "ul":
                        handleList(elem, doc, 1, false, "");
                        break;
                    case "ol":
                        handleList(elem, doc, 1, true, "");
                        break;
                    case "table":
                        handleTable(elem, doc);
                        break;
                    case "pre":
                        handlePre(elem, doc);
                        break;
                    case "br":
                        doc.createParagraph();
                        break;
                    case "hr":
                        createWordHorizontalLine(doc);
                        break;
                    default:
                        // 未匹配的元素当做行内元素
                        handleInlineElements(elem, doc);
                        break;
                }
            }
        }
    }

    /**
     * 处理标题
     */
    private void handleHeading(Element elem, XWPFDocument doc) {
        XWPFParagraph para = doc.createParagraph();
        XWPFRun run = para.createRun();

        String text = elem.text();
        run.setText(text);

        // 设置字体大小
        String tagName = elem.tagName().toLowerCase(Locale.ROOT);
        int level = Integer.parseInt(tagName.substring(1));
        int fontSize = MdConvertConstants.TITLE_BASE_FONT_SIZE - (level * 2);
        run.setFontSize(fontSize);
        run.setBold(true);
    }

    /**
     * 处理段落
     */
    private void handleParagraph(Element elem, XWPFDocument doc) {
        XWPFParagraph para = doc.createParagraph();
        parseInlineElements(elem, para);
    }

    /**
     * 处理列表
     *
     * @param listElem 列表元素
     * @param doc Word文档
     * @param level 列表层级（从0开始）
     * @param isOrdered 是否为有序列表
     * @param parentSymbol 父列表项符号（已废弃，保留参数兼容性）
     */
    private void handleList(Element listElem, XWPFDocument doc, int level, boolean isOrdered, String parentSymbol) {
        int listIndex = 0;
        for (Element li : listElem.children()) {
            listIndex++;

            XWPFParagraph para = doc.createParagraph();
            para.setIndentationFirstLine(level * MdConvertConstants.LIST_INDENT_STEP);
            XWPFRun symbolRun = para.createRun();
            String liSymbol = isOrdered ? parentSymbol + listIndex + ". " : "▪ ";
            symbolRun.setText(liSymbol + " ");
            parseLiContent(li, para);

            // 递归处理子列表，只找第一级子列表，递归时每次处理一级
            li.select("> ol").forEach(childOl -> handleList(childOl, doc, level + 1, true, liSymbol));
            li.select("> ul").forEach(childUl -> handleList(childUl, doc, level + 1, false, ""));
        }
    }

    /**
     * 解析列表项内容
     */
    private void parseLiContent(Element li, XWPFParagraph para) {
        Element liClone = li.clone();
        liClone.select("ul, ol").remove();
        parseInlineElements(liClone, para);
    }

    /**
     * 处理行内元素
     */
    private void parseInlineElements(Element elem, XWPFParagraph para) {
        List<Node> nodes = elem.childNodes();
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                String text = ((TextNode) node).text();
                if (!text.isEmpty()) {
                    XWPFRun run = para.createRun();
                    run.setText(text);
                    run.setFontSize(MdConvertConstants.NORMAL_FONT_SIZE);
                }
            } else if (node instanceof Element inlineElem) {
                XWPFRun run = para.createRun();
                run.setFontSize(MdConvertConstants.NORMAL_FONT_SIZE);

                switch (inlineElem.tagName().toLowerCase(Locale.ROOT)) {
                    case "strong":
                    case "b":
                        run.setBold(true);
                        break;
                    case "em":
                    case "i":
                        run.setItalic(true);
                        break;
                    case "del":
                    case "s":
                    case "strike":
                        run.setStrikeThrough(true);
                        break;
                    case "a":
                        addUrlText(para, inlineElem);
                        // 超链接已经在 addUrlText 中设置文本，跳过默认文本设置
                        continue;
                    case "br":
                        run.addBreak();
                        break;
                    default:
                        break;
                }

                String text = inlineElem.text().trim();
                if (!text.isEmpty()) {
                    run.setText(text);
                }
            }
        }
    }

    /**
     * 单独处理行内元素（处理行内加粗、斜体等）
     */
    private void handleInlineElements(Element elem, XWPFDocument doc) {
        XWPFParagraph para = doc.createParagraph();
        parseInlineElements(elem, para);
    }

    /**
     * 添加超链接
     */
    private void addUrlText(XWPFParagraph para, Element elem) {
        String linkText = elem.text();
        String linkUrl = elem.attr("href");

        String id = para.getDocument()
            .getPackagePart()
            .addExternalRelationship(linkUrl, XWPFRelation.HYPERLINK.getRelation())
            .getId();

        CTHyperlink cthyperLink = para.getCTP().addNewHyperlink();
        cthyperLink.setId(id);

        CTR ctr = cthyperLink.addNewR();
        CTText ctText = ctr.addNewT();
        ctText.setStringValue(linkText);

        // 设置链接样式
        CTRPr ctrPr = ctr.addNewRPr();
        CTColor ctColor = ctrPr.addNewColor();
        ctColor.setVal(MdConvertConstants.LINK_COLOR);
        CTUnderline underline = ctrPr.addNewU();
        underline.setVal(STUnderline.SINGLE);
    }

    /**
     * 处理表格
     */
    private void handleTable(Element elem, XWPFDocument doc) {
        Elements headerRows = elem.select("thead tr");
        Elements bodyRows = elem.select("tr:has(td):not(:has(th))").not("thead tr");

        // 如果没有明确的thead，则找到第一个包含th的行作为表头，其余视为正文
        if (headerRows.isEmpty()) {
            Elements potentialHeaderRows = elem.select("tr:has(th)");
            if (!potentialHeaderRows.isEmpty()) {
                headerRows.add(potentialHeaderRows.get(0));
                // 将其他包含th的行添加到bodyRows中
                for (int i = 1; i < potentialHeaderRows.size(); i++) {
                    bodyRows.add(potentialHeaderRows.get(i));
                }
            }
        } else {
            // 如果有thead，thead中的th作为表头，其他th的行作为正文处理
            Elements allThRows = elem.select("tr:has(th)").not("thead tr");
            bodyRows.addAll(allThRows);
        }

        // 1. 确定表格的最大列数
        int maxColumns = getMaxTableColumns(headerRows, bodyRows);

        // 2. 根据最大列数创建表格
        if (maxColumns <= 0 && !bodyRows.isEmpty()) {
            // 如果没有header但有body，默认创建2列
            maxColumns = 2;
        }

        XWPFTable table;
        if (maxColumns > 0) {
            int totalRows = headerRows.size() + bodyRows.size();
            if (totalRows > 0) {
                table = doc.createTable(totalRows, maxColumns);
            } else {
                table = doc.createTable(1, maxColumns);
            }
            table.setWidth("100%");
            table.getCTTbl().getTblPr().unsetTblBorders();
        } else {
            // 空表格
            table = doc.createTable(1, 1);
            table.setWidth("100%");
            table.getCTTbl().getTblPr().unsetTblBorders();
            return;
        }

        // 3. 处理header行
        for (int i = 0; i < headerRows.size(); i++) {
            Element tr = headerRows.get(i);
            Elements ths = tr.select("th");
            if (ths.isEmpty()) {
                continue;
            }

            XWPFTableRow headerRow = table.getRow(i);
            adjustTableCellCount(headerRow, maxColumns);

            for (int j = 0; j < ths.size() && j < maxColumns; j++) {
                XWPFTableCell cell = headerRow.getCell(j);
                cell.setText(ths.get(j).text().trim());
                setHeaderCellStyle(cell);
            }
        }

        // 4. 处理body行
        int bodyRowIndex = headerRows.size();
        for (Element tr : bodyRows) {
            Elements tds = tr.select("td");
            if (tds.isEmpty()) {
                continue;
            }

            XWPFTableRow bodyRow = table.getRow(bodyRowIndex);
            adjustTableCellCount(bodyRow, maxColumns);

            for (int j = 0; j < tds.size() && j < maxColumns; j++) {
                XWPFTableCell cell = bodyRow.getCell(j);
                cell.setText(tds.get(j).text().trim());
                setBodyCellStyle(cell);
            }
            bodyRowIndex++;
        }

        // 5. 如果没有header但有body，确保第一行使用body样式
        if (headerRows.isEmpty() && !bodyRows.isEmpty() && table.getNumberOfRows() > 0) {
            XWPFTableRow firstRow = table.getRow(0);
            if (!firstRow.getTableCells().isEmpty()) {
                for (XWPFTableCell cell : firstRow.getTableCells()) {
                    setBodyCellStyle(cell);
                }
            }
        }
    }

    /**
     * 获取表格的最大列数
     */
    private int getMaxTableColumns(Elements headerRows, Elements bodyRows) {
        int maxColumns = 0;

        // 检查所有header行的列数
        for (Element tr : headerRows) {
            Elements ths = tr.select("th");
            maxColumns = Math.max(maxColumns, ths.size());
        }

        // 检查所有body行的列数
        for (Element tr : bodyRows) {
            Elements tds = tr.select("td");
            maxColumns = Math.max(maxColumns, tds.size());
        }

        return maxColumns;
    }

    /**
     * 调整表格行的单元格数量
     */
    private void adjustTableCellCount(XWPFTableRow row, int targetCount) {
        int currentCount = row.getTableCells().size();

        if (currentCount < targetCount) {
            // 需要添加单元格
            for (int i = currentCount; i < targetCount; i++) {
                row.addNewTableCell();
            }
        } else if (currentCount > targetCount) {
            // 处理多余的单元格，清空文本而不是删除（避免数据丢失）
            for (int i = targetCount; i < currentCount; i++) {
                if (i < row.getTableCells().size()) {
                    XWPFTableCell cell = row.getCell(i);
                    if (cell != null) {
                        cell.setText("");
                    }
                }
            }
        }
    }

    /**
     * 处理代码块
     */
    private void handlePre(Element elem, XWPFDocument doc) {
        // 1. 创建代码块段落，核心：强制左对齐
        XWPFParagraph para = doc.createParagraph();
        para.setAlignment(ParagraphAlignment.LEFT); // 关键：设置左对齐（覆盖默认两端对齐）
        para.setIndentationFirstLine(0);
        para.setIndentationLeft(MdConvertConstants.CODE_BLOCK_LEFT_INDENT);

        // 2. 获取代码内容，保留换行/空行
        Element codeElem = elem.selectFirst("code");
        String codeContent = codeElem != null ? codeElem.text() : elem.text();
        String[] codeLines = codeContent.split("\\r?\\n", -1);

        // 3. 逐行渲染代码
        for (String codeLine : codeLines) {
            String line = codeLine.replace("\t", "    ");
            XWPFRun run = para.createRun();
            run.setFontSize(MdConvertConstants.CODE_BLOCK_FONT_SIZE);
            run.getCTR().addNewRPr().addNewNoProof();
            run.setText(line, 0);
            run.addBreak();
        }

        // 4. 代码块后添加空行，区分后续内容
        XWPFParagraph emptyPara = doc.createParagraph();
        emptyPara.createRun().setText("");
    }

    /**
     * 创建水平线
     */
    private void createWordHorizontalLine(XWPFDocument doc) {
        XWPFParagraph hrParagraph = doc.createParagraph();
        CTPPr ppr = hrParagraph.getCTP().getPPr();
        if (ppr == null) {
            ppr = hrParagraph.getCTP().addNewPPr();
        }

        CTPBdr pbDr = ppr.addNewPBdr();
        CTBorder bottomBorder = pbDr.addNewBottom();
        bottomBorder.setVal(STBorder.SINGLE);
        bottomBorder.setSz(BigInteger.TWO);
        bottomBorder.setColor(MdConvertConstants.TABLE_BORDER_COLOR);
        bottomBorder.setSpace(BigInteger.TWO);
    }

    /**
     * 设置表头单元格样式
     */
    private void setHeaderCellStyle(XWPFTableCell cell) {
        cell.setColor(MdConvertConstants.TABLE_HEADER_BG);
        XWPFParagraph para = cell.getParagraphs().get(0);
        XWPFRun run = para.getRuns().isEmpty() ? para.createRun() : para.getRuns().get(0);
        run.setBold(true);
        run.setFontSize(MdConvertConstants.NORMAL_FONT_SIZE);
        setCellBorder(cell);
    }

    /**
     * 设置表体单元格样式
     */
    private void setBodyCellStyle(XWPFTableCell cell) {
        cell.setColor(MdConvertConstants.TABLE_CELL_COLOR);
        XWPFParagraph para = cell.getParagraphs().get(0);
        XWPFRun run = para.getRuns().isEmpty() ? para.createRun() : para.getRuns().get(0);
        run.setFontSize(MdConvertConstants.NORMAL_FONT_SIZE);
        setCellBorder(cell);
    }

    /**
     * 设置单元格边框
     */
    private void setCellBorder(XWPFTableCell cell) {
        String borderColor = MdConvertConstants.TABLE_BORDER_COLOR;
        BigInteger borderWidth = BigInteger.ONE;

        // 获取或创建单元格边框属性
        CTTcPr tcPr = cell.getCTTc().isSetTcPr() ? cell.getCTTc().getTcPr() : cell.getCTTc().addNewTcPr();
        CTTcBorders tcBorders = tcPr.isSetTcBorders() ? tcPr.getTcBorders() : tcPr.addNewTcBorders();

        // 设置顶部边框
        CTBorder topBorder = tcBorders.isSetTop() ? tcBorders.getTop() : tcBorders.addNewTop();
        topBorder.setVal(STBorder.SINGLE);
        topBorder.setColor(borderColor);
        topBorder.setSz(borderWidth);

        // 设置底部边框
        CTBorder bottomBorder = tcBorders.isSetBottom() ? tcBorders.getBottom() : tcBorders.addNewBottom();
        bottomBorder.setVal(STBorder.SINGLE);
        bottomBorder.setColor(borderColor);
        bottomBorder.setSz(borderWidth);

        // 设置左侧边框
        CTBorder leftBorder = tcBorders.isSetLeft() ? tcBorders.getLeft() : tcBorders.addNewLeft();
        leftBorder.setVal(STBorder.SINGLE);
        leftBorder.setColor(borderColor);
        leftBorder.setSz(borderWidth);

        // 设置右侧边框
        CTBorder rightBorder = tcBorders.isSetRight() ? tcBorders.getRight() : tcBorders.addNewRight();
        rightBorder.setVal(STBorder.SINGLE);
        rightBorder.setColor(borderColor);
        rightBorder.setSz(borderWidth);
    }
}