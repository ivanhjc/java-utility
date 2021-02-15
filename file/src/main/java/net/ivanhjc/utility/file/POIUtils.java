package net.ivanhjc.utility.file;

import net.ivanhjc.utility.data.ListUtils;
import net.ivanhjc.utility.data.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

// TODO: 2017/3/15 Add new fields to existing field table and automatically put these fields to their corresponding locations in the exported sheet

/**
 * Apache POI (Poor Obfuscation Implementation) utility class. It aims to provide methods to process common office
 * documents (MS Word, Excel, etc.) as fast and intuitive as possible. Generally you would firstly create an instance
 * of this class and wrap a workbook in it as the working copy by using {@link #open()} or {@link #open(String, String)
 * and then you can continue other operations on the workbook.
 *
 * @author Ivan Huang 2017/2/28 13:41
 */
public class POIUtils {
    private static final Logger log = LogManager.getLogger();
    private static final String FIELD_DELIMITER = ".";
    private static final String DEFAULT_DATE_FORMAT = "yyyy/m/d";
    private static final String DEFAULT_FILE_NAME = "output.xlsx";
    private static final SimpleDateFormat COMMON_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /**
     * The workbook to work on
     */
    private Workbook workbook;
    /**
     * The sheet to work on
     */
    private Sheet sheet;
    private List<Header> headers;
    /**
     * The first row of the table used by {@link #createTable(LinkedHashMap, List)}
     */
    private int tableStartRow = 0;
    /**
     * The first column of the table used by {@link #createTable(LinkedHashMap, List)}
     */
    private int tableStartCol = 0;
    /**
     * the index of the last row of header area
     */
    private int headerEndRow;
    /**
     * the index of the last column of header area
     */
    private int headerEndCol;
    /**
     * the number of rows of data area
     */
    private int dataRows = 1;
    private List<Header> allHeaders; // All headers from left to right and from bottom to top
    private String saveDir;
    private String fileName;

    public POIUtils() {
    }

    /**
     * Create a new workbook with the default name "output.xlsx" and a new sheet in it named "Sheet1". After you're done
     * you should call {@link #saveAs()} to save the workbook.
     */
    public POIUtils open() {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet("Sheet1");
        return this;
    }

    /**
     * Open a workbook and sheet for processing. If the workbook or sheet doesn't exist create one.
     *
     * @param workbook path of the workbook, relative or absolute
     * @param sheet    name of the sheet
     */
    public POIUtils open(String workbook, String sheet) throws IOException {
        this.workbook = new XSSFWorkbook(workbook);
        this.sheet = this.workbook.getSheet(sheet);
        if (this.sheet == null) {
            this.sheet = this.workbook.createSheet(sheet);
        }
        return this;
    }

    /**
     * Save the workbook to the default location "classpath/target/output.xlsx".
     *
     * @return the saved file
     */
    public File saveAs() throws IOException {
        return saveAs("target", DEFAULT_FILE_NAME);
    }

    /**
     * Saves the workbook to a specified location
     *
     * @param dir  the directory where the workbook is saved. It could be an absolute path such as "/home/user" or "D:\data", or a relative path
     *             to the classpath such as "target" being absolutely "{classpath}/target".
     * @param name file name
     * @return the saved file
     */
    public File saveAs(String dir, String name) throws IOException {
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File file = new File(dir + "/" + name);
        if (file.exists()) {
            int suffixIdx = name.lastIndexOf('.');
            file = new File(dir + "/" + name.substring(0, suffixIdx) + "_" + date + name.substring(suffixIdx));
        }
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        log.info("File saved -> " + file.getAbsolutePath());
        return file;
    }

    /**
     * Closes the workbook and saves its content to the current file.
     */
    public void closeToSave() {
        try {
            if (this.workbook != null)
                workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the workbook without saving the changes
     */
    public void closeToDiscard() {
        ((XSSFWorkbook) this.workbook).getPackage().revert();
    }

    //======================Sheet level operations======================

    /**
     * Renames a sheet
     */
    public void setSheetName(String oldName, String newName) {
        setSheetName(workbook.getSheetIndex(oldName), newName);
    }

    /**
     * Rename a sheet by specifying the index of the sheet and the new name for it.
     *
     * @param sheetIndex
     * @param newName
     */
    public void setSheetName(int sheetIndex, String newName) {
        workbook.setSheetName(sheetIndex, newName);
    }

    public void resetDataRows() {
        dataRows = 1;
    }

    /**
     * Set the first row of the table created by {@link #createTable(LinkedHashMap, List)}
     *
     * @param rowIdx row index, 0-based
     */
    public POIUtils setTableStartRow(int rowIdx) {
        this.tableStartRow = rowIdx;
        return this;
    }

    /**
     * Set the first column of the table created by {@link #createTable(LinkedHashMap, List)}
     *
     * @param colIdx column index, 0-based
     */
    public POIUtils setTableStartCol(int colIdx) {
        this.tableStartCol = colIdx;
        return this;
    }

    /* -----------------------------------------------------------------Operations on a sheet*/

    /**
     * Create a table with the supplied header specifier (fieldMap) and the corresponding list of data.
     *
     * @param headerMap formatted as {@code Name:name,Gender:sex,...,Role:role,Role.Name:role.name,...} Each pair
     *                  delimited by comma represents a mapping of the header name and the class field from which it
     *                  obtains values. See {@link #createTable(LinkedHashMap, List)} for more details.
     */
    public <T> POIUtils createTable(String headerMap, List<T> data) throws Exception {
        return createTable(getFieldMap(headerMap), data);
    }

    /**
     * Create a table with the supplied header specifier (fieldMap) and the corresponding list of data. A use case is
     * given as below:<p><br>
     *
     * FieldMap:
     * <pre>
     * {@code
     * LinkedHashMap<String, String> map = new LinkedHashMap<>();
     * map.put("ID", "id");
     * map.put("Name", "name");
     * map.put("Role", "role");
     * map.put("Role.ID", "role.id");
     * map.put("Role.Name", "role.name");
     * }
     * </pre>
     * Data:
     * <pre>
     * {@code
     * List<User> users = new ArrayList<>();
     * users.add(new User(1, "John", new Role(1, "Admin")));
     * users.add(new User(2, "Alex", new Role(1, "Admin")));
     * }
     * </pre>
     * Result:<br>
     * <pre>
     * {@code
     * |    |      |   Role     |
     * | ID | Name --------------
     * |    |      | ID | Name  |
     * --------------------------
     * | 1  | John | 1  | Admin |
     * | 2  | Alex | 1  | Admin |
     * }
     * </pre>
     *
     * @param headerMap an ordered mapping of headers and their corresponding class fields from which they obtain values.
     *                  A header is the name of a column put in the first cell. A field is the field residing in the
     *                  component class of the supplied list of data. The mapping between them denotes where to obtain
     *                  the values for the column under a header. For example, a table in the simplest form is like this:
     *                  fieldMap is [ID:id,Name:name] and data is a list of User[id,name] objects.
     *                  <p><br>
     *                  A header may have sub-headers or child headers that can represent a nested group of data, and the
     *                  child headers can have their child headers, and so on. Child headers and child fields are marked
     *                  with a period ".". For example, if the above mentioned User class has a Role field, which has
     *                  its own id and name, like this, User[id,name,Role[id,name]], then the fieldMap should be given as
     *                  [ID:id,Name:name,Role:role,Role.ID:role.id,Role.Name:role.name].
     *                  <p><br>
     *                  The Order of the mappings is important. Specifically, child headers that belong to the same class
     *                  should be supplied in sequence. When a header from another class is supplied it starts creating
     *                  another group of headers.<br><br>
     *
     * @param data     data to write to the table. <p><br>
     *
     */
    public <T> POIUtils createTable(LinkedHashMap<String, String> headerMap, List<T> data) throws Exception {
        List<String> names = new ArrayList<>(headerMap.keySet());
        List<String> properties = new ArrayList<>(headerMap.values());
        this.headerEndRow = tableStartRow;
        this.headers = createHeaders(getOrCreateRow(tableStartRow), tableStartCol, names, properties, data.get(0).getClass());
        setParameters();
        resetDataRows();
        createDataRows(data);
        setHeaderStyle(tableStartRow, tableStartCol, headerEndRow, headerEndCol);
        return this;
    }

    /**
     * Create a table with the supplied headers and the corresponding columns of data. This is a reverse version of
     * {@link #getColumns}.
     *
     * @param headers comma-separated header names of each column, with a number denoting the column index prepending
     *                each name before a colon, e.g. "2:Name,3:Date of marriage,5:gender".
     * @param columns a map with the column index as the key denoting the column of each list of values. If there are
     *                more columns than headers then only the columns of the specified headers will be created.
     */
    public POIUtils createTable(String headers, Map<Integer, List<String>> columns, int tableStartRow) {
        String[] headers2 = headers.split(",");
        List<Integer> colIds = new ArrayList<>();
        for (String header2 : headers2) {
            String[] headers3 = header2.split(":");
            int col = Integer.parseInt(headers3[0]);
            writeValueToCell(headers3[1], tableStartRow, col);
            colIds.add(col);
        }
        colIds.sort(Integer::compareTo);
        List<Integer> sizes = columns.values().stream().map(List::size).sorted(Integer::compareTo).collect(Collectors.toList());
        int rowId = tableStartRow + 1;
        int maxRowId = sizes.get(sizes.size() - 1) + tableStartRow;
        while (rowId <= maxRowId) {
            int i = rowId - tableStartRow - 1;
            for (Integer colId : colIds) {
                if (i < columns.get(colId).size()) {
                    writeValueToCell(columns.get(colId).get(i), rowId, colId);
                }
            }
            rowId++;
        }
        setHeaderStyle(tableStartRow, colIds.get(0), tableStartRow, colIds.get(colIds.size() - 1));
        return this;
    }

    /**
     * Compare two columns and output the venn result. See {@link ListUtils#venn(List, List, Comparator)} for what is a
     * venn result.
     *
     * @param headerRow  which row the headers of the columns to compare is
     * @param startRow   which row the values of the columns to compare should start with
     * @param col1       the first column
     * @param col2       the second column
     * @param ignoreCase whether to ignore case while comparing
     */
    public POIUtils venn(int headerRow, int startRow, int col1, int col2, boolean ignoreCase) {
        Map<Integer, List<String>> columns = getColumns(startRow, col1, col2);
        List<List<String>> lists = ListUtils.venn(columns.get(col1), columns.get(col2), ignoreCase ? String::compareToIgnoreCase : null);
        columns.put(col2 + 1, lists.get(0));
        columns.put(col2 + 2, lists.get(1));
        columns.put(col2 + 3, lists.get(2));
        String header1 = getCellValue(headerRow, col1);
        String header2 = getCellValue(headerRow, col2);
        return createTable(String.format("%d:Only in %s,%d:Common,%d:Only in %s",
                col2 + 1, header1 == null ? "left" : header1, col2 + 2, col2 + 3, header2 == null ? "right" : header2),
                columns, startRow == 0 ? startRow : startRow - 1);
    }

    /**
     * Insert a sum row at the end of the table on the working sheet.
     * It sums all cells of every column from the column specified to the last column of the table, and then write the header string
     * to the merged cells before startCol.
     *
     * @param startCol the column from which the evaluation begins
     */
    public void insertSumRow(String header, int startCol) {
        int sumRowIdx = headerEndRow + dataRows;
        insertSumRow(sumRowIdx, headerEndRow + 1, sumRowIdx - 1, startCol, headerEndCol);
        writeValueToCell(header, sumRowIdx, tableStartCol);
        mergeAndCenter(sumRowIdx, sumRowIdx, tableStartCol, startCol - 1);
    }

    /**
     * Insert a sum row of the specified data area at the specified position on the working sheet.
     *
     * @param rowIdx       the position of the sum row
     * @param firstDataRow first row of the data area, zero-based
     * @param lastDataRow  last row of the data area, zero-based
     * @param firstDataCol first column of the data area, zero-based
     * @param lastDataCol  last column of the data area, zero-based
     */
    public void insertSumRow(int rowIdx, int firstDataRow, int lastDataRow, int firstDataCol, int lastDataCol) {
        Row row = sheet.getRow(rowIdx);
        if (row == null)
            row = sheet.createRow(rowIdx);
        for (int i = firstDataCol; i <= lastDataCol; i++) {
            String topCell = new CellReference(firstDataRow, i).formatAsString();
            String botCell = new CellReference(lastDataRow, i).formatAsString();
            Cell cell = row.getCell(i);
            if (cell == null)
                cell = row.createCell(i);
            String formula = String.format("SUM(%s:%s)", topCell, botCell);
            cell.setCellFormula(formula);
        }
    }

    /**
     * Merge cells horizontally, that is, merge cells that compose a father header of each row which has sub-headers
     * on next row in the specified range
     *
     * @param firstRow zero-based, inclusive
     * @param lastRow  zero-based, exclusive
     * @param firstCol zero-based, inclusive
     * @param lastCol  zero-based, inclusive
     */
    public void mergeRowCells(int firstRow, int lastRow, int firstCol, int lastCol) {
        for (int i = firstRow; i < lastRow; i++) {
            Row row = sheet.getRow(i);
            int k = firstCol;
            for (int j = firstCol; j <= lastCol; j++) {
                Cell cell = row.getCell(j);
                if (isEmpty(cell) && j < lastCol)
                    continue;
                if (!isEmpty(row.getCell(k)) && isEmpty(row.getCell(k + 1))) {
                    if (j < lastCol || !isEmpty(row.getCell(j)))
                        mergeAndCenter(i, i, k, j - 1);
                    else
                        mergeAndCenter(i, i, k, j);
                }
                k = j;
            }
        }
    }

    /**
     * Merge cells vertically, that is, merge cells that match one non-empty cell followed by multiple empty cells in each column
     *
     * @param firstRow zero-based, inclusive
     * @param lastRow  zero-based, inclusive
     * @param firstCol zero-based, inclusive
     * @param lastCol  zero-based, inclusive
     */
    public void mergeColCells(int firstRow, int lastRow, int firstCol, int lastCol) {
        for (int i = firstCol; i <= lastCol; i++) {
            int k = firstRow;
            for (int j = firstRow + 1; j <= lastRow; j++) {
                Cell cell = sheet.getRow(j).getCell(i);
                if (isEmpty(cell) && j < lastRow)
                    continue;

                if (!isEmpty(sheet.getRow(k).getCell(i)) && isEmpty(sheet.getRow(k + 1).getCell(i))) {
                    if (j < lastRow)
                        mergeAndCenter(k, j - 1, i, i);
                    else
                        mergeAndCenter(k, j, i, i);
                }
                k = j;
            }
        }
    }

    /**
     * Check if a cell is null or blank
     *
     * @param cell
     * @return
     */
    public boolean isEmpty(Cell cell) {
        return cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK;
    }

    /**
     * Write the value to the cell specified by the given row and column indices on the working sheet
     *
     * @param value  can be any type, including primitive types
     * @param rowIdx zero-based
     * @param colIdx zero-based
     */
    public void writeValueToCell(Object value, int rowIdx, int colIdx) {
        Row row = sheet.getRow(rowIdx);
        Cell cell;
        if (row == null) {
            cell = sheet.createRow(rowIdx).createCell(colIdx);
        } else {
            cell = row.getCell(colIdx);
            if (cell == null)
                cell = row.createCell(colIdx);
        }
        writeValueToCell(value, cell);
    }

    public void mergeAndCenter(int firstRow, int lastRow, int firstCol, int lastCol) {
        mergeAndCenter(sheet.getRow(firstRow).getCell(firstCol), new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    public void appendValuesToSheet(Object[][] values) {
        int initRow = sheet.getLastRowNum() + 1;
        int initCol = 0;
        for (int i = 0; i < values.length; i++) {
            Object[] rowValues = values[i];
            for (int j = 0; j < rowValues.length; j++) {
                Object value = rowValues[j];
                writeValueToCell(value, initRow + i, initCol + j);
            }
        }
    }

    /**
     * Return data of the specified columns on working sheet. Empty cells wont' be included in the result.
     *
     * @param rowId  which row to start with
     * @param colIds indexes of columns
     * @return a map whose keys are the indexes of the columns
     */
    public Map<Integer, List<String>> getColumns(int rowId, int... colIds) {
        Map<Integer, List<String>> cols = new HashMap<>();
        for (int i = rowId; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            for (int colId : colIds) {
                Cell cell = row.getCell(colId);
                if (!isEmpty(cell)) {
                    cols.computeIfAbsent(colId, k -> new ArrayList<>()).add(getCellValue(cell));
                }
            }
        }
        return cols;
    }

    /* -----------------------------------------------------------------Getters */
    public int getHeaderEndRow() {
        return headerEndRow;
    }

    public int getHeaderEndCol() {
        return headerEndCol;
    }

    public int getTableEndRow() {
        return headerEndRow + dataRows;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public List<Header> getAllHeaders() {
        return allHeaders;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public Sheet getSheet() {
        return sheet;
    }

    /* -----------------------------------------------------------------Utilities */

    /**
     * Creating a new sheet in the workbook.
     *
     * @param sheetName
     * @return
     */
    public Sheet newSheet(String sheetName) {
        return workbook.createSheet(sheetName);
    }

    public void printHeaders() {
        for (Header header : headers) {
            println(header);
        }
    }

    private int count;

    public void println(Header header) {
        System.out.println(count++ + "\t" + header);
        if (header.getSubHeaders() != null) {
            for (Header header1 : header.getSubHeaders())
                println(header1);
        }
    }

    public Cell getCell(int rowId, int colId) {
        return Optional.of(sheet).map(s -> s.getRow(rowId)).map(r -> r.getCell(colId)).orElse(null);
    }

    public String getCellValue(int rowId, int colId) {
        return Optional.of(sheet).map(s -> s.getRow(rowId)).map(r -> r.getCell(colId)).map(POIUtils::getCellValue).orElse(null);
    }

    public static String getCellValue(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return COMMON_DATE_FORMATTER.format(cell.getDateCellValue());
                }
                return String.valueOf(cell.getNumericCellValue());
            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "Unknown";
        }
    }

    public static XSSFWorkbook decrypt(File file, String password) throws IOException, GeneralSecurityException {
        POIFSFileSystem fs = new POIFSFileSystem(file);
        EncryptionInfo info = new EncryptionInfo(fs);
        Decryptor decryptor = Decryptor.getInstance(info);
        if (!decryptor.verifyPassword(password)) {
            throw new RuntimeException("Password is incorrect!");
        }
        InputStream is = decryptor.getDataStream(fs);
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        return workbook;
    }

    //--------------------------------------------------------------------------------------Private members
    /**
     * Used to store subHeaderNum of each row. A subHeaderNum indicates the number of headers that have subHeaders on this row,
     * as well as those below this row.
     */
    private int[] subHeaderNums = new int[10];

    /**
     * Create headers of the table according to the header data provided, i.e. headerProperties
     *
     * @param row        the row on which headers are to be created
     * @param startCol   the column from which the headers are to be created
     * @param names      the list of header names
     * @param properties the list of header properties, all of which are in the form "class.property"
     * @return the list of headers that have been created
     */
    private List<Header> createHeaders(Row row, final int startCol, List<String> names, List<String> properties, Class<?> cls) throws NoSuchFieldException {
        List<Header> headers = new ArrayList<>();
        int rowNum = row.getRowNum();
        for (int i = 0; i < names.size(); i++) {
            Header header = new Header();
            String name = names.get(i);
            String field = properties.get(i);

            if (!name.contains(FIELD_DELIMITER)) {
                int colIdx = startCol + i - subHeaderNums[rowNum];
                Cell cell = row.createCell(colIdx);
                cell.setCellValue(name);
                header.setCell(cell);
                if (!field.isEmpty()) {
//                    Method method = getMethod(classMap, field);
//                    header.setMethod(method);
                    header.setField(cls.getDeclaredField(field));
                    header.getField().setAccessible(true);
                }
                headers.add(header);

            } else {
                Row nextRow = getNextRow(row);
                if (nextRow == null) {
                    nextRow = createRowBelow(row);
                    headerEndRow++;
                }

                List<String> nextNames = new ArrayList<>();
                List<String> nextProperties = new ArrayList<>();
                int subHeaderNum = 0;
                while (name.contains(FIELD_DELIMITER)) {
                    String nextName = Objects.requireNonNull(StringUtils.split(name, StringUtils.escape(FIELD_DELIMITER)))[1];
                    String nextField = Objects.requireNonNull(StringUtils.split(field, StringUtils.escape(FIELD_DELIMITER)))[1];
                    nextNames.add(nextName);
                    nextProperties.add(nextField);

                    if (nextNames.size() >= 2 && !nextNames.get(nextNames.size() - 2).contains(FIELD_DELIMITER) && nextName.contains(FIELD_DELIMITER)) {
                        subHeaderNum++;
                    }

                    if (++i == names.size())
                        break;
                    name = names.get(i);
                    field = properties.get(i);
                }

                i--; //The next name and property after breaking the while loop won't be processed if not this since they have been taken out in the loop.

                Header lastHeader = headers.get(headers.size() - 1);
                int newStartCol = lastHeader.getCell().getColumnIndex();
//                int newEndCol = newStartCol + nextNames.size() - 1 - subHeaderNum;

                List<Header> subHeaders = createHeaders(nextRow, newStartCol, nextNames, nextProperties, lastHeader.getField().getType());
                lastHeader.setSubHeaders(subHeaders);


                // Keep track of the number of headers that have subheaders for each row/cycle/call stack
                for (int j = 0; j <= rowNum; j++) {
                    subHeaderNums[j]++;
                }
//                int newEndCol = i - subHeaderNums[rowNum];
//                mergeAndCenter(row.getCell(newStartCol), new CellRangeAddress(rowNum, rowNum, newStartCol, newEndCol));
            }
        }

        // When this cycle is done, set the number of subheaders for this row to zero for the subheaders of the next header of
        // the previous call stack need not subtract the number of subheaders of this call stack, albeit they are on the same row.
        subHeaderNums[rowNum] = 0;

        return headers;
    }

    /**
     * Convert a method indicating string to its corresponding method
     *
     * @param property should be like "ClassName.property"
     * @return
     */
    private Method getMethod(Map<String, Class<?>> classMap, String property) throws NoSuchMethodException {
        String[] splits = property.split("\\.");
        String propertyName = splits[1];
        String methodName;
        if (Character.isLowerCase(propertyName.charAt(1))) {
            methodName = "get".concat(StringUtils.capitalize(propertyName));
        } else {
            methodName = "get".concat(propertyName); // For xFoo type of properties
        }
//        Method method = Class.forName(className).getMethod(methodName);
        Method method = classMap.get(splits[0]).getMethod(methodName);
        return method;
    }

    private Row getNextRow(Row row) {
        return sheet.getRow(row.getRowNum() + 1);
    }

    private Row createRowBelow(Row row) {
        return sheet.createRow(row.getRowNum() + 1);
    }

    /**
     * Merge and center the cells specified by range
     *
     * @param startCell the first cell in the cells to be merged
     * @param range     the range of the cells to be merged
     */
    private void mergeAndCenter(Cell startCell, CellRangeAddress range) {
        startCell.getSheet().addMergedRegion(range);
        CellStyle style = startCell.getSheet().getWorkbook().createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        startCell.setCellStyle(style);
    }

    private <T> void createDataRows(List<T> data) throws InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < data.size(); i++) {
            T rowData = data.get(i);
            int temp = dataRows;
            createDataRow(getOrCreateRow(headerEndRow + dataRows), headers, rowData);
            if (dataRows == temp) // dataRows may not be incremented if all list values of this rowData are 0 length.
                dataRows++;
        }
    }

    private void createDataRow(Row row, List<Header> headers, Object data) throws InvocationTargetException, IllegalAccessException {
        for (Header header : headers) {
//            System.out.println(header + ", " + data);
            if (header.getField() == null) { //If a header has no associated method, it's either (1) a header merely used to group a list of subheaders, or (2) a header that temporarily has no value.
                if (header.getSubHeaders() != null) //It's (1)
                    createDataRow(row, header.getSubHeaders(), data);
                continue;
            }
            Object value = header.getField().get(data);
            if (header.getSubHeaders() != null) {
                if (value instanceof List) {
                    Row nextRow = row;
                    for (Object obj : (List) value) {
                        createDataRow(nextRow, header.getSubHeaders(), obj);
                        nextRow = getNextDataRow(nextRow, obj);
                    }
                } else if (value != null) {
                    createDataRow(row, header.getSubHeaders(), value);
                }
            } else {
                Cell cell = row.createCell(header.getCell().getColumnIndex());
                writeValueToCell(value, cell);
            }
        }
    }

    /**
     * Writing the given value to the given cell according to the value type.
     *
     * @param value
     * @param cell
     */
    private void writeValueToCell(Object value, Cell cell) {
        if (value == null)
            cell.setCellType(Cell.CELL_TYPE_BLANK);
        else if (value instanceof String)
            cell.setCellValue(value.toString());
        else if (value instanceof Integer)
            cell.setCellValue((Integer) value);
        else if (value instanceof Double)
            cell.setCellValue((Double) value);
        else if (value instanceof Date)
            setDateValue(cell, (Date) value);
        else if (value instanceof Long)
            cell.setCellValue((Long) value);
        else
            cell.setCellValue("Unknown type");
    }

    private Row getNextDataRow(Row row, Object object) throws InvocationTargetException, IllegalAccessException {
        int nextDataRowIdx = row.getRowNum() + getNumOfRows(object);
        Row nextDataRow = sheet.getRow(nextDataRowIdx);
        if (nextDataRow == null) {
            nextDataRow = sheet.createRow(nextDataRowIdx);
            dataRows++;
        }
        return nextDataRow;
    }

    private int getNumOfRows(Object object) throws InvocationTargetException, IllegalAccessException {
        int size = 1;
        Method[] methods = object.getClass().getDeclaredMethods();
        for (Method method : methods) {
//            System.out.println(method.getName());
            if (method.getName().contains("get")) {
                Object property = method.invoke(object);
                if (property != null) {
                    int temp = 0;
                    if (property instanceof List) {
                        for (Object element : (List) property) {
                            int temp2 = getNumOfRows(element);
                            temp += temp2;
                        }
                    } else if (!(property instanceof Integer)
                            && !(property instanceof String)
                            && !(property instanceof Double)
                            && !(property instanceof Date)) {

                        temp = getNumOfRows(property);
                    }
                    if (temp > size)
                        size = temp;
                }
            }
        }
        return size;
    }

    private void setDateValue(Cell cell, Date value) {
        cell.setCellValue(value);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat((DEFAULT_DATE_FORMAT)));
        cell.setCellStyle(style);
    }

    private void setParameters() {
        setAllHeaders();
        headerEndCol = allHeaders.get(allHeaders.size() - 1).getCell().getColumnIndex();
    }

    private void setAllHeaders() {
        allHeaders = new ArrayList<>();
        for (Header header : headers) {
            addToAllHeaders(header, allHeaders);
        }
    }

    private void addToAllHeaders(Header header, List<Header> allHeaders) {
        if (header.getSubHeaders() != null)
            for (Header temp : header.getSubHeaders())
                addToAllHeaders(temp, allHeaders);
        else
            allHeaders.add(header);
    }

    private void setHeaderStyle(int startRow, int startCol, int endRow, int endCol) {
        mergeRowCells(startRow, endRow, startCol, endCol);
        mergeColCells(startRow, endRow, startCol, endCol);
        setAllBorders(startRow, endRow, startCol, endCol);
        sheet.createFreezePane(0, endRow + 1);
        for (int i = startCol; i <= endCol; i++) {
            sheet.autoSizeColumn(i, true);
        }
    }

    private void setAllBorders(int firstRow, int lastRow, int firstCol, int lastCol) {
        CellStyle style = workbook.createCellStyle();
        setCellStyle(style);
        for (int i = firstRow; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            for (int j = firstCol; j <= lastCol; j++) {
                Cell cell = row.getCell(j);
                if (cell == null)
                    cell = row.createCell(j);
                CellStyle cellStyle = cell.getCellStyle();
                if (cellStyle.getIndex() == 0)
                    cell.setCellStyle(style);
                else
                    setCellStyle(cellStyle);
            }
        }
    }

    private void setCellStyle(CellStyle cellStyle) {
        //Set borders
        cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
        cellStyle.setBorderRight(CellStyle.BORDER_THIN);
        cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
    }

    /**
     * Check if the given cell is in a merged region of the sheet
     *
     * @param cell
     * @return true if it's in one of the merged regions, false otherwise
     */
    private boolean isInMergedRegion(Cell cell) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex()))
                return true;
        }
        return false;
    }

    /**
     * Check if the cells under the given cell can be merged along with the given cell
     *
     * @param cell
     * @return true if can be merged down, false otherwise
     */
    private boolean canBeMergedDown(Cell cell) {
        for (int i = cell.getRowIndex() + 1; i <= headerEndRow; i++) {
            Cell nextCell = sheet.getRow(i).getCell(cell.getColumnIndex());
            if (nextCell != null && !(nextCell.getCellType() == Cell.CELL_TYPE_BLANK)) {
                return false;
            }
        }
        return true;
    }

    private Row getOrCreateRow(int rowIdx) {
        Row row = this.sheet.getRow(rowIdx);
        if (row == null) {
            row = this.sheet.createRow(rowIdx);
        }
        return row;
    }

    /**
     * Returns a LinkedHashMap of header name and field mappings
     *
     * @param fieldMap formatted as "Name:name,Gender:sex,...,Role:role,Role:Name:role.name,...", see
     *                 {@link #createTable(LinkedHashMap, List)} for more details on this formatting
     * @return a ordered map
     */
    private LinkedHashMap<String, String> getFieldMap(String fieldMap) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        String[] strings = fieldMap.split(",");
        for (String s : strings) {
            String[] temp = s.split(":");
            map.put(temp[0], temp[1]);
        }
        return map;
    }
}
