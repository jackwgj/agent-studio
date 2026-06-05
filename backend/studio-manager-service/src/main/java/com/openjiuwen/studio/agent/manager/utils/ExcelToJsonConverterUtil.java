/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
 */

package com.openjiuwen.studio.agent.manager.utils;

import com.alibaba.fastjson2.JSON;
import com.openjiuwen.studio.agent.common.enums.StudioError;
import com.openjiuwen.studio.agent.common.exception.AgentStudioException;
import com.openjiuwen.studio.agent.manager.constant.CommonConstant;
import com.openjiuwen.studio.agent.manager.dto.StructMessage;

import lombok.extern.slf4j.Slf4j;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

/**
 * Excel 文件安全限制常量
 */
interface ExcelSecurityConstants {
    /** 最大文件大小 (10MB) */
    long MAX_FILE_SIZE = 10L * 1024 * 1024;

    /** 最大行数限制 */
    int MAX_ROW_COUNT = 201;

    /** SXSSF 行访问窗口大小 */
    int SXSSF_WINDOW_SIZE = 100;

    /** 大文件 SXSSF 行访问窗口大小 */
    int LARGE_FILE_SXSSF_WINDOW_SIZE = 1000;
}

@Slf4j
public class ExcelToJsonConverterUtil {

    // 列名映射配置
    private static final Map<String, String> COLUMN_MAPPING = createColumnMapping();

    private static final Map<String, String> VISIBILITY_MAP = createVisibilityMap();

    public static final Map<String, String> REVERSE_VISIBILITY_MAP = createReverseVisibilityMap();

    private static Map<String, String> createColumnMapping() {
        Map<String, String> map = new HashMap<>();
        map.put("消息名称", "name");
        map.put("消息体", "content");
        map.put("消息分类", "category");
        map.put("可见范围", "visibility");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, String> createVisibilityMap() {
        Map<String, String> map = new HashMap<>();
        map.put("仅个人", "personal");
        map.put("租户内", "tenant");
        map.put("当前空间内", "workspace");
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, String> createReverseVisibilityMap() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, String> entry : VISIBILITY_MAP.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * 处理上传的Excel文件
     */
    public static List<Map<String, Object>> processUploadedExcel(MultipartFile file) throws IOException {
        // 使用try-with-resources确保Workbook资源被正确释放
        try (Workbook workbook = createWorkbook(file)) {
            return processWorkbook(workbook);
        }
    }

    /**
     * 预检查Excel文件的安全性
     *
     * @param file 上传的文件
     * @throws IOException 文件读取异常
     * @throws AgentStudioException 文件格式或大小异常
     */
    public static void preCheckExcelFile(MultipartFile file) throws IOException, AgentStudioException {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new AgentStudioException(StudioError.ILLEGAL_FILE);
        }

        // 检查文件类型
        if (!isExcelFile(file.getContentType(), file.getOriginalFilename())) {
            throw new AgentStudioException(StudioError.EXCEL_FILE_FORMAT_INVALID);
        }

        // 使用try-with-resources进行预检查，确保资源被正确释放
        try (Workbook workbook = createWorkbook(file)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 预检查行数限制 - 严格限制为200行，防止DOS攻击
            int totalRows = sheet.getLastRowNum() + 1; // 包括标题行
            if (totalRows > CommonConstant.EXCEL_MAX_ROWS_PRECHECK) {
                throw new AgentStudioException(StudioError.EXCEL_ROW_COUNT_EXCEEDED);
            }

            // 预检查列数限制
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                int maxColumns = 0;
                for (Cell cell : headerRow) {
                    if (cell != null) {
                        maxColumns = Math.max(maxColumns, cell.getColumnIndex() + 1);
                    }
                }
                if (maxColumns > CommonConstant.EXCEL_MAX_COLUMNS) {
                    throw new AgentStudioException(StudioError.EXCEL_COLUMN_COUNT_EXCEEDED);
                }
            }
        }
    }

    /**
     * 处理Workbook数据
     */
    private static List<Map<String, Object>> processWorkbook(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);

        // 创建列索引到字段名的映射
        Map<Integer, String> columnMapping = createColumnMapping(headerRow);

        // 处理数据行
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row dataRow = sheet.getRow(i);
            if (dataRow == null || isEmptyRow(dataRow)) {
                continue;
            }

            Map<String, Object> rowData = parseRowData(dataRow, columnMapping);
            result.add(rowData);
        }

        return result;
    }

    /**
     * 验证Excel文件的安全性
     */
    private static void validateExcelFile(MultipartFile file) throws IOException {
        // 验证文件大小
        if (file.getSize() > ExcelSecurityConstants.MAX_FILE_SIZE) {
            throw new AgentStudioException(StudioError.MESSAGE_TEMPLATE_SIZE_LIMIT);
        }

        // 对于.xlsx文件，使用事件模型检查行数限制，防止DOS攻击
        if (isXlsxFile(file)) {
            validateExcelRowCountWithEventModel(file);
        }
    }

    /**
     * 使用事件模型验证Excel行数限制
     * 这种方式内存占用极低，适合处理大文件，防止DOS攻击
     */
    private static void validateExcelRowCountWithEventModel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            // 使用OPCPackage直接读取Excel文件，避免全量加载到内存
            try (OPCPackage pkg = OPCPackage.open(inputStream)) {
                XSSFReader reader = new XSSFReader(pkg);
                SAXParserFactory saxFactory = SAXParserFactory.newInstance();
                saxFactory.setNamespaceAware(true);
                saxFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                saxFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                saxFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                saxFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                XMLReader parser = saxFactory.newSAXParser().getXMLReader();

                // 设置行计数器和最大允许行数
                final int[] rowCount = {0};
                final int maxAllowedRows = ExcelSecurityConstants.MAX_ROW_COUNT;
                final boolean[] shouldStop = {false};
                // 自定义事件处理器
                parser.setContentHandler(new DefaultHandler() {
                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) {
                        // 检查是否需要停止解析
                        if (shouldStop[0]) {
                            return;
                        }

                        // 遇到行标签时计数
                        if ("row".equals(qName)) {
                            rowCount[0]++;

                            if (rowCount[0] > maxAllowedRows) {
                                shouldStop[0] = true;
                                throw new AgentStudioException(StudioError.MESSAGE_TEMPLATE_SIZE_LIMIT);
                            }
                        }
                    }
                });
                // 解析第一个sheet的数据
                try (InputStream sheetStream = reader.getSheetsData().next()) {
                    InputSource source = new InputSource(sheetStream);
                    parser.parse(source);
                }
            }
        } catch (Exception e) {
            // 如果是因为行数超限导致的异常，重新抛出为AgentStudioException
            throw new AgentStudioException(StudioError.MESSAGE_TEMPLATE_SIZE_LIMIT);
        }
    }

    /**
     * 判断是否为.xlsx文件
     */
    private static boolean isXlsxFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        return (filename != null && filename.endsWith(".xlsx"))
            || ("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType));
    }

    /**
     * 根据MultipartFile创建Workbook
     */
    private static Workbook createWorkbook(MultipartFile file) throws IOException {
        validateExcelFile(file);
        String filename = file.getOriginalFilename();

        if (filename != null && filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(file.getInputStream());
        } else if (filename != null && filename.endsWith(".xls")) {
            return new HSSFWorkbook(file.getInputStream());
        } else {

            if (file.getContentType() != null && file.getContentType().equals(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                // 对于导入操作，使用SXSSFWorkbook避免内存溢出
                return new SXSSFWorkbook(new XSSFWorkbook(file.getInputStream()),
                    ExcelSecurityConstants.SXSSF_WINDOW_SIZE);
            } else {
                return new HSSFWorkbook(file.getInputStream());
            }
        }
    }

    /**
     * 创建列映射
     */
    private static Map<Integer, String> createColumnMapping(Row headerRow) {
        Map<Integer, String> mapping = new HashMap<>();

        for (Cell cell : headerRow) {
            String headerValue = getCellValueAsString(cell);
            if (COLUMN_MAPPING.containsKey(headerValue)) {
                mapping.put(cell.getColumnIndex(), COLUMN_MAPPING.get(headerValue));
            }
        }

        return mapping;
    }

    /**
     * 解析单行数据
     */
    private static Map<String, Object> parseRowData(Row dataRow, Map<Integer, String> columnMapping) {
        Map<String, Object> rowData = new LinkedHashMap<>();

        for (Map.Entry<Integer, String> entry : columnMapping.entrySet()) {
            int columnIndex = entry.getKey();
            String fieldName = entry.getValue();
            Cell cell = dataRow.getCell(columnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Object cellValue = getCellValue(cell);
            // 如果是 visibility 字段，进行映射转换
            if ("visibility".equals(fieldName)) {
                cellValue = mapVisibility((String) cellValue);
            }

            rowData.put(fieldName, cellValue);
        }

        return rowData;
    }

    /**
     * 检查是否为Excel文件
     */
    public static boolean isExcelFile(String contentType, String filename) {
        if (filename == null) {
            return false;
        }
        return filename.endsWith(".xls") || filename.endsWith(".xlsx") || "application/vnd.ms-excel".equals(contentType)
            || "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType);
    }

    /**
     * 检查是否为空行
     */
    private static boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取单元格值
     */
    private static Object getCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == Math.floor(value)) {
                        return (long) value;
                    } else {
                        return value;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return cell.getNumericCellValue();
                }
            default:
                return "";
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private static String getCellValueAsString(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString() : "";
    }

    // 映射 visibility 字符串到枚举字符串
    private static String mapVisibility(String visibility) {
        return VISIBILITY_MAP.getOrDefault(visibility, visibility);
    }

    /**
     * 导出Excel文件并返回Resource
     */
    public static Resource exportMessagesToExcel(List<StructMessage> messages) throws IOException {
        // 验证导出数据量
        validateExportData(messages);
        // 创建Workbook
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // 创建Sheet
            Sheet sheet = workbook.createSheet("消息数据");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            createHeaderRow(sheet, headerStyle);

            // 填充数据
            fillDataRows(sheet, dataStyle, messages);

            // 自动调整列宽
            autoSizeColumns(sheet);

            // 写入字节数组输出流
            workbook.write(outputStream);

        } finally {
            workbook.close();
        }

        // 创建ByteArrayResource，确保UTF-8编码
        byte[] excelBytes = outputStream.toByteArray();
        return new ByteArrayResource(excelBytes) {
            @Override
            public String getFilename() {
                // 设置文件名，确保中文正确编码
                return System.currentTimeMillis() + ".xlsx";
            }
        };
    }

    /**
     * 验证导出数据的安全性
     */
    private static void validateExportData(List<StructMessage> messages) {
        if (messages == null) {
            log.error("Exported data cannot be null.");
            throw new AgentStudioException(StudioError.EXPORT_STRUCTURED_MESSAGES_FAILED);
        }

        if (messages.size() > ExcelSecurityConstants.MAX_ROW_COUNT) {
            log.error("Exported data is too large: {}. Please reduce the amount of data exported.", messages.size());
            throw new AgentStudioException(StudioError.EXPORT_STRUCTURED_MESSAGES_FAILED);
        }
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        // 设置字体（重要：使用支持中文的字体）
        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);

        // 设置样式
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    /**
     * 创建数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setFontName("宋体");
        font.setFontHeightInPoints((short) 10);

        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置文本格式防止CSV注入
        DataFormat textFormat = workbook.createDataFormat();
        style.setDataFormat(textFormat.getFormat("@"));

        return style;
    }

    /**
     * 创建表头行
     */
    private static void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        String[] headers = {"消息名称", "消息体", "消息分类", "可见范围"};

        // 获取工作簿并创建文本格式样式
        Workbook workbook = sheet.getWorkbook();
        CellStyle textStyle = workbook.createCellStyle();
        DataFormat textFormat = workbook.createDataFormat();
        textStyle.setDataFormat(textFormat.getFormat("@"));  // 设置文本格式
        textStyle.cloneStyleFrom(headerStyle);  // 继承原有样式属性

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            String safeValue = headers[i];
            if (!safeValue.isEmpty() && ("=@+-".indexOf(safeValue.charAt(0)) >= 0)) {
                safeValue = "'" + safeValue;
            }
            cell.setCellValue(safeValue);
            cell.setCellStyle(textStyle);  // 应用文本样式
        }
    }

    /**
     * 填充数据行
     */
    private static void fillDataRows(Sheet sheet, CellStyle dataStyle, List<StructMessage> messages) {

        for (int i = 0; i < messages.size(); i++) {
            StructMessage message = messages.get(i);
            Row row = sheet.createRow(i + 1);

            // 消息名称
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(message.getName());
            cell0.setCellStyle(dataStyle);

            // 消息体
            Cell cell1 = row.createCell(1);
            cell1.setCellValue(JSON.toJSONString(message.getContent()));
            cell1.setCellStyle(dataStyle);

            // 消息分类
            Cell cell2 = row.createCell(2);
            cell2.setCellValue(message.getCategory());
            cell2.setCellStyle(dataStyle);

            // 可见范围
            Cell cell3 = row.createCell(3);
            String visibility = message.getVisibility();
            String chineseVisibility = REVERSE_VISIBILITY_MAP.getOrDefault(visibility, visibility);
            cell3.setCellValue(chineseVisibility);
            cell3.setCellStyle(dataStyle);
        }
    }

    /**
     * 自动调整列宽
     */
    private static void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < COLUMN_MAPPING.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static Resource exportStructuredMessagesTemplate(String projectId, String workspaceId) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); // 使用SXSSFWorkbook，设置行访问窗口为100
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 创建文本格式样式（防止CSV注入）
            CellStyle textStyle = workbook.createCellStyle();
            DataFormat textFormat = workbook.createDataFormat();
            textStyle.setDataFormat(textFormat.getFormat("@")); // 设置单元格为文本格式

            // 为文本样式添加边框，保持与数据样式一致
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            // Sheet1: 模板数据
            Sheet dataSheet = workbook.createSheet("sheet1");
            Row headerRow = dataSheet.createRow(0);
            String[] headers = {"消息名称", "消息体", "消息分类", "可见范围", "说明"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellStyle(textStyle); // 应用文本格式
                cell.setCellValue(headers[i]);
            }

            Row exampleRow = dataSheet.createRow(1);
            // 消息名称
            Cell nameCell = exampleRow.createCell(0);
            nameCell.setCellStyle(textStyle);
            nameCell.setCellValue("无权限");

            // 消息体（JSON）
            Cell jsonCell = exampleRow.createCell(1);
            jsonCell.setCellStyle(textStyle);
            jsonCell.setCellValue("{\n  \"code\": \"ERR-1001\",\n  \"message\": \"数据库连接失败\"\n}");

            // 消息分类
            Cell categoryCell = exampleRow.createCell(2);
            categoryCell.setCellStyle(textStyle);
            categoryCell.setCellValue("异常");

            // 可见范围
            Cell scopeCell = exampleRow.createCell(3);
            scopeCell.setCellStyle(textStyle);
            scopeCell.setCellValue("仅个人");

            // 说明
            Cell descCell = exampleRow.createCell(4);
            descCell.setCellStyle(textStyle);
            descCell.setCellValue("此为示例模板，请按实际情况填写");

            dataSheet.setColumnWidth(0, 5000);
            dataSheet.setColumnWidth(1, 15000);
            dataSheet.setColumnWidth(2, 5000);
            dataSheet.setColumnWidth(3, 5000);
            dataSheet.setColumnWidth(4, 8000);

            // Sheet2: 填写说明
            Sheet instructionSheet = workbook.createSheet("填写说明");
            String[] instructions = {"1.如果消息为异常码，请在消息分类处填写：异常", "2.可见范围分为：仅个人、租户内、当前空间内，填写其他字段无效",
                "3.消息体请用json格式进行书写，其他格式无法解析"};

            for (int i = 0; i < instructions.length; i++) {
                Row row = instructionSheet.createRow(i);
                Cell cell = row.createCell(0);
                cell.setCellStyle(textStyle); // 应用文本格式
                cell.setCellValue(instructions[i]);
            }
            instructionSheet.setColumnWidth(0, 8000);

            // 写入输出流
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray()) {
                @Override
                public String getFilename() {
                    return "template.xlsx";
                }
            };
        } catch (Exception e) {
            log.error("template failed.", e);
            throw new AgentStudioException(StudioError.MESSAGE_TEMPLATE_SIZE_LIMIT);
        }
    }

}
