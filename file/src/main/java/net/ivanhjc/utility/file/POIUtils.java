package net.ivanhjc.utility.file;

import net.ivanhjc.utility.data.DateUtils;
import net.ivanhjc.utility.data.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.text.SimpleDateFormat;
import java.util.*;

// TODO: 2017/3/15 Add new fields to existing field table and automatically put these fields to their corresponding locations in the exported sheet

/**
 * This utility class is intended to process MS office documents and tries to be as intuitive as possible. It provides some
 * frequent operations just as when you working on the UI of these documents. For example, you would get an instance of this
 * class first before you can do many tasks. The you can create a new document ({@link #open()}) or open an existing one ({@link #open(String, String)}).
 *
 * @author Ivan Huang 2017/2/28 13:41
 */
public class POIUtils {

    private static final Logger LOG = LogManager.getLogger();

    private static final String FIELD_DELIMITER = ".";
    private static final String DEFAULT_PROPERTY_DELIMITER = ".";
    private static final String DEFAULT_DATE_FORMAT = "yyyy/m/d";
    private static final int DEFAULT_START_ROW = 0;
    private static final int DEFAULT_START_COLUMN = 0;

    private XSSFWorkbook workbook;
    private Sheet sheet;
    private List<Header> headers;
    //    private List<T> data;
    private int tableStartRow; // the first row of the table
    private int tableStartCol; // the first column of the table
    private int headerEndRow; // the index of the last row of header area
    private int headerEndCol; // the index of the last column of header area
    private int dataRows = 1; // the number of rows of data area
    private List<Header> allHeaders; // All headers from left to right and from bottom to top
    private String beanPackage; //the package of beans that are to be processed
    private String saveDir;
    private String fileName;

    /**
     * Creates an instance of this util.
     */
    public POIUtils() {

    }

    //======================Workbook level operations======================

    /**
     * By default this creates a new workbook named "output.xlsx" and creates a new sheet named "Sheet1". After you've done you can save the workbook by calling
     * {@link #save()}
     */
    public void open() {
        this.workbook = new XSSFWorkbook();
        this.sheet = workbook.createSheet("Sheet1");
    }

    /**
     * Opens an existing workbook and its existing sheet for processing. If the workbook has no such named sheet create a new one with the name.
     *
     * @param workbook the file path of the workbook
     * @param sheet    name of the sheet to work on
     */
    public void open(String workbook, String sheet) throws IOException {
        this.workbook = new XSSFWorkbook(workbook);
        this.sheet = this.workbook.getSheet(sheet);
        if (this.sheet == null) {
            this.sheet = this.workbook.createSheet(sheet);
        }
    }

    /**
     * Saves the workbook to default location "{classpath}/target/output.xlsx".
     *
     * @return the saved file
     */
    public File save() throws IOException {
        return saveAs("target", "output.xlsx");
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
        LOG.info("File saved -> " + file.getAbsolutePath());
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
        this.workbook.getPackage().revert();
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

    /**
     * Specifying the package of the bean classes.
     *
     * @param beanPackage full name of the package, e.g. "com.google.ai.bean"
     */
    public void setBeanPackage(String beanPackage) {
        this.beanPackage = beanPackage;
    }

    public void resetDataRows() {
        dataRows = 1;
    }

    /* -----------------------------------------------------------------Operations on a sheet*/

    /**
     * Given a headerPropertyMap and a list of beans, create a table with headers and data on the processing sheet.
     * This put the table in the top-left corner of the sheet by default. Before calling this method you may need
     * to specify the package by calling setBeanPackage() where the type of the data should be looked for, or else
     * it looks up in the root directory.
     *
     * @param headerPropertyMap a header-property relationship representation wrapped in a LinkedHashMap object.<br>
     *                          A header is the name of a column, that is, the value in the first cell of the column. A header may have sub-headers.
     *                          A property denotes how to obtain values for the column of this header. In the LinkedHashMap object the header names
     *                          are used as keys and properties as values. Sub-headers are marked by ":". For example, if a header named "project"
     *                          has sub-headers "id", "name", and "date", they are represented as "project:id", "project:name", and "project:date"
     *                          to be used as keys. Sub-headers can again have their own sub-headers and so on. The values are strings in the form
     *                          "class.property" which means cells under this header should contain values returned by the specified property of
     *                          the specified class objects. Order is important for headerPropertyMap to work. Specifically, properties that belong
     *                          to the same class should be in a sequence where there are no intermittent properties from other classes. <br>
     * @param data              data to be written to the table
     */
    public <T> void insertTable(LinkedHashMap<String, String> headerPropertyMap, List<T> data) throws Exception {
        insertTable(DEFAULT_START_ROW, DEFAULT_START_COLUMN, headerPropertyMap, data);
    }

    /**
     * Given a headerPropertyMap and a list of beans, create a table with headers and data on the processing sheet
     *
     * @param startRow          zero-based
     * @param startCol          zero-based
     * @param headerPropertyMap a header-property relationship representation wrapped in a LinkedHashMap object.<br>
     *                          A header is the name of a column, that is, the value in the first cell of the column. A header may have sub-headers.
     *                          A property denotes how to obtain values for the column of this header. In the LinkedHashMap object the header names
     *                          are used as keys and properties as values. Sub-headers are marked by ":". For example, if a header named "project"
     *                          has sub-headers "id", "name", and "date", they are represented as "project:id", "project:name", and "project:date"
     *                          to be used as keys. Sub-headers can again have their own sub-headers and so on. The values are strings in the form
     *                          "class.property" which means cells under this header should contain values returned by the specified property of
     *                          the specified class objects. Order is important for headerPropertyMap to work. Specifically, properties that belong
     *                          to the same class should be in a sequence where there are no intermittent properties from other classes. <br>
     * @param data              data to be written to the table
     *                          <p>
     *                          <br><br>
     *                          A usage case is given as below:<br>
     *                          HeaderPropertyMap:<br>
     *                          <pre><code style="font-family: consolas">
     *
     *                                 LinkedHashMap<String, String> map = new LinkedHashMap<>();<br>
     *                                 map.put("id", "Class.id");<br>
     *                                 map.put("name", "Class.name");<br>
     *                                 map.put("headTeacher", "Class.headTeacher");<br>
     *                                 map.put("monitor", "Class.monitor");<br>
     *                                 map.put("student", "Class.studentList");<br>
     *                                 map.put("student:id", "Student.id");<br>
     *                                 map.put("student:name", "Student.name");<br>
     *                                 map.put("student:age", "Student.age");<br>
     *                                 map.put("student:address", "Student.address");<br>
     *                                 map.put("student:dateEnter", "Student.dateEnter");<br>
     *                                 map.put("term", "Class.term");<br><br>
     *
     *                                 Data:
     *                                 <pre><code style="font-family: consolas">
     *                                  SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");<br>
     *                                  Student student1 = new Student();<br>
     *                                  student1.setId(1);<br>
     *                                  student1.setName("Evan");<br>
     *                                  student1.setAge(12);<br>
     *                                  student1.setAddress("Street1");<br>
     *                                  student1.setDateEnter(sdf.parse("2017/8/1"));<br>
     *                                  Student student2 = new Student();<br>
     *                                  student2.setId(2);<br>
     *                                  student2.setName("Mary");<br>
     *                                  student2.setAge(11);<br>
     *                                  student2.setAddress("Street2");<br>
     *                                  student2.setDateEnter(sdf.parse("2017/8/1"));<br>
     *                                  Student student3 = new Student();<br>
     *                                  student3.setId(1);<br>
     *                                  student3.setName("Mark");<br>
     *                                  student3.setAge(16);<br>
     *                                  student3.setAddress("Block1");<br>
     *                                  student3.setDateEnter(sdf.parse("2017/8/1"));<br>
     *                                  Class class1 = new Class();<br>
     *                                  class1.setId(1);<br>
     *                                  class1.setName("C1");<br>
     *                                  class1.setHeadTeacher("Alexander");<br>
     *                                  class1.setMonitor("Joey");<br>
     *                                  class1.setTerm("1");<br>
     *                                  class1.setStudentList(Arrays.asList(student1, student2));<br>
     *                                  Class class2 = new Class();<br>
     *                                  class2.setId(2);<br>
     *                                  class2.setName("C2");<br>
     *                                  class2.setHeadTeacher("Jack");<br>
     *                                  class2.setMonitor("Mark");<br>
     *                                  class2.setTerm("1");<br>
     *                                  class2.setStudentList(Arrays.asList(student3));<br>
     *                                  List<Class> classes = new ArrayList<>();<br>
     *                                  classes.add(class1);<br>
     *                                  classes.add(class2);<br>
     *                                 </code></pre>
     *                                 </p>
     *                          <p>
     *                                 This produces the table:<br>
     *                                 <pre><code style="font-family: consolas">
     *                                  ----------------------------------------------------------------<br>
     *                                  |id|name|headTeacher|monitor|            student          |term|<br>
     *                                  ----------------------------------------------------------------<br>
     *                                  |  |    |           |       |id|name|age|address|dateEnter|    |<br>
     *                                  ----------------------------------------------------------------<br>
     *                                  |1 |C1  |Alexander  |Joey   |1 |Evan|12 |Street1|2017/8/1 |1   |<br>
     *                                  ----------------------------------------------------------------<br>
     *                                  |  |    |           |       |2 |Mary|11 |Street2|2017/8/1 |1   |<br>
     *                                  ----------------------------------------------------------------<br>
     *                                  |2 |C2  |Jack       |Mark   |1 |Mark|16 |Block1 |2017/8/1 |1   |<br>
     *                                  ----------------------------------------------------------------<br>
     *                                 </code></pre>
     */
    public <T> void insertTable(final int startRow, final int startCol, LinkedHashMap<String, String> headerPropertyMap, List<T> data) throws Exception {
        this.tableStartRow = startRow;
        this.tableStartCol = startCol;
        List<String> names = new ArrayList<>(headerPropertyMap.keySet());
        List<String> properties = new ArrayList<>(headerPropertyMap.values());
        this.headerEndRow = startRow;
        this.headers = createHeaders(this.sheet.createRow(startRow), startCol, names, properties);
        setParameters();
        createDataRows(data);
        setStyle(startRow, startCol);
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

    public XSSFWorkbook getWorkbook() {
        return workbook;
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

    public static String getCellValue(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                return null;
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell))
                    return DateUtils.DATE_FORMAT_01.format(cell.getDateCellValue());
                return String.valueOf((int) cell.getNumericCellValue());
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
    private List<Header> createHeaders(Row row, final int startCol, List<String> names, List<String> properties) throws NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        List<Header> headers = new ArrayList<>();

        int rowNum = row.getRowNum();
        for (int i = 0; i < names.size(); i++) {
            Header header = new Header();
            String name = names.get(i);
            String property = properties.get(i);

            if (!name.contains(FIELD_DELIMITER)) {
                int colIdx = startCol + i - subHeaderNums[rowNum];
                Cell cell = row.createCell(colIdx);
                cell.setCellValue(name);
                header.setCell(cell);
                if (!property.isEmpty()) {
                    Method method = getMethod(property);
                    header.setMethod(method);
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
                    nextNames.add(nextName);
                    nextProperties.add(property);

                    if (nextNames.size() >= 2 && !nextNames.get(nextNames.size() - 2).contains(FIELD_DELIMITER) && nextName.contains(FIELD_DELIMITER)) {
                        subHeaderNum++;
                    }

                    if (++i == names.size())
                        break;
                    name = names.get(i);
                    property = properties.get(i);
                }

                i--; //The next name and property after breaking the while loop won't be processed if not this since they have been taken out in the loop.

                Header lastHeader = headers.get(headers.size() - 1);
                int newStartCol = lastHeader.getCell().getColumnIndex();
//                int newEndCol = newStartCol + nextNames.size() - 1 - subHeaderNum;


                List<Header> subHeaders = createHeaders(nextRow, newStartCol, nextNames, nextProperties);
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
    private Method getMethod(String property) throws ClassNotFoundException, NoSuchMethodException {
        String[] properties = property.split("\\.");
        String className = beanPackage == null ? properties[0] : beanPackage.concat(".").concat(properties[0]);
        String propertyName = properties[1];
        String methodName;
        if (Character.isLowerCase(propertyName.charAt(1))) {
            methodName = "get".concat(StringUtils.capitalize(propertyName));
        } else {
            methodName = "get".concat(propertyName); // For xFoo type of properties
        }
        Method method = Class.forName(className).getMethod(methodName);
        return method;
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException {
        Method method = Class.forName("GeneratorTest.LogData").getMethod("getLicense");
        System.out.println(method);
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
            Row row = sheet.createRow(headerEndRow + dataRows);
            T rowData = data.get(i);
            int temp = dataRows;
            createDataRow(row, headers, rowData);
            if (dataRows == temp) // dataRows may not be incremented if all list values of this rowData are 0 length.
                dataRows++;
        }
    }

    private void createDataRow(Row row, List<Header> headers, Object data) throws InvocationTargetException, IllegalAccessException {
        for (Header header : headers) {
//            System.out.println(header + ", " + data);
            if (header.getMethod() == null) { //If a header has no associated method, it's either (1) a header merely used to group a list of subheaders, or (2) a header that temporarily has no value.
                if (header.getSubHeaders() != null) //It's (1)
                    createDataRow(row, header.getSubHeaders(), data);
                continue;
            }
            Object value = header.getMethod().invoke(data);
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

    private void setStyle(int startRow, int startCol) {
        mergeRowCells(startRow, headerEndRow, startCol, headerEndCol);
        mergeColCells(startRow, headerEndRow, startCol, headerEndCol);
        setAllBorders(startRow, headerEndRow, startCol, headerEndCol);
        sheet.createFreezePane(0, headerEndRow + 1);
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

//    --------------------------------------------------------------------------------------Private members
}
