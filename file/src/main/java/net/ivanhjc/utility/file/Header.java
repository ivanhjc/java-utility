package net.ivanhjc.utility.file;

import org.apache.poi.ss.usermodel.Cell;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A header is a cell used as a table header to indicate where the the values should be populated. A header holds three properties: a cell marking the location of the value,
 * a field referring to the value and an optional list of sub-headers. For example, A user may has these properties: id, name,
 * mobile, etc, and the headers in this case should look like this:
 * <p>
 * <table border="1">
 * <tr><th>ID</th><th>Name</th><th>Mobile</th></tr>
 * <tr><td>1</td><td>Ivan</td><td>12345678912</td></tr>
 * <tr><td>2</td><td>Jack</td><td>12345678913</td></tr>
 * </table>
 * </p>
 * <p>
 * <p>
 * Also, the user may have two cars and if you want to show the cars' info in the table as well it should look like this:
 * <p>
 * <table border="1">
 * <tr><th>ID</th><th>Name</th><th>Mobile</th><th colspan="3">Cars</th></tr>
 * <tr><th></th><th></th><th></th><th>License</th><th>Model</th><th>Color</th></tr>
 * <tr><td>1</td><td>Ivan</td><td>12345678912</td><td>ABC123</td><td>X5</td><td>Red</td></tr>
 * <tr><td></td><td></td><td></td><td>ABC124</td><td>X6</td><td>White</td></tr>
 * <tr><td>2</td><td>Jack</td><td>12345678912</td><td>DEFG123</td><td>X5</td><td>Red</td></tr>
 * <tr><td></td><td></td><td></td><td>DEFG124</td><td>X6</td><td>White</td></tr>
 * </table>
 * </p>
 * <p>
 * Sub-headers may have their own sub-headers as well and the level of subheaders is not limited.
 */
class Header {
    private Cell cell;
    private Method method;
    private Field field;
    private List<Header> subHeaders;

    public Header() {
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Cell getCell() {
        return cell;
    }

    /**
     * Set the method which returns a value to fill cells of the same column.
     *
     * @param method the method associated with the cells of the same column
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public void setSubHeaders(List<Header> subHeaders) {
        this.subHeaders = subHeaders;
    }

    public List<Header> getSubHeaders() {
        return subHeaders;
    }

    /**
     * @return a String representation of a header object in the form "[rowIndex, columnIndex, className, methodName]"
     */
    @Override
    public String toString() {
        return String.format("[%d, %d, %s, %s, %s]",
                cell.getRowIndex(),
                cell.getColumnIndex(),
                cell.getStringCellValue(),
                method == null ? null : method.getDeclaringClass().getName(),
                method == null ? null : method.getName());
    }
}