package net.ivanhjc.utility.db;

import net.ivanhjc.utility.data.EscapeTypes;
import net.ivanhjc.utility.data.SplitRegex;
import net.ivanhjc.utility.data.StringUtils;
import net.ivanhjc.utility.db.model.enums.FileType;
import net.ivanhjc.utility.db.model.params.DBTableLocator;
import net.ivanhjc.utility.db.model.params.ImportFromFileToDatabasesConfiguration;
import net.ivanhjc.utility.file.POIUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DataMigrateEngine {
    private static final Logger LOG = LogManager.getLogger();
    private ImportFromFileToDatabasesConfiguration[] importFromFileToDatabasesConfigurations;

    private DataMigrateEngine(ImportFromFileToDatabasesConfiguration[] importFromFileToDatabasesConfigurations) {
        this.importFromFileToDatabasesConfigurations = importFromFileToDatabasesConfigurations;
    }

    public static class Builder {
        /**
         * Multiple configurations for importing
         */
        private ImportFromFileToDatabasesConfiguration[] importFromFileToDatabasesConfigurations;

        public Builder setImportFromFileToDatabasesConfigurations(ImportFromFileToDatabasesConfiguration[] configurations) {
            this.importFromFileToDatabasesConfigurations = configurations;
            return this;
        }

        public DataMigrateEngine build() {
            return new DataMigrateEngine(importFromFileToDatabasesConfigurations);
        }
    }

    /**
     * Import data from a file to one or multiple database tables, which should have the same table definition. Or
     * import with multiple configurations. Before calling this method you should set the importing configurations first
     * by using {@link Builder#setImportFromFileToDatabasesConfigurations(ImportFromFileToDatabasesConfiguration[])}.
     * Currently supported file types are documented in {@link ImportFromFileToDatabasesConfiguration#getSource()}.
     * Currently supported database types are documented in {@link ImportFromFileToDatabasesConfiguration#getTargets()}.
     */
    public void importFromFilesToDatabases() throws SQLException, IOException {
        if (ArrayUtils.isEmpty(importFromFileToDatabasesConfigurations)) {
            throw new RuntimeException("Error: configurations not found!");
        }

        LOG.info("Importing...");
        // Verification
        for (ImportFromFileToDatabasesConfiguration configuration : importFromFileToDatabasesConfigurations) {
            switch (FileType.getFileType(configuration.getSource())) {
                case SPREADSHEET:
                    importFromSpreadsheetToDatabase(configuration);
                    break;
                case UNKNOWN:
            }
        }
    }

    private void importFromSpreadsheetToDatabase(ImportFromFileToDatabasesConfiguration configuration) throws IOException,
            SQLException {
        POIUtils poiUtils = new POIUtils().open(StringUtils.substringBeforeLast(configuration.getSource(), "."),
                StringUtils.substringAfterLast(configuration.getSource(), "."));
        Sheet sheet = poiUtils.getSheet();
        List<String> fields = configuration.getFields();
        if (fields == null) {
            fields = new ArrayList<>();
            int i = 0;
            while (true) {
                String value = poiUtils.getCellValue(0, i++);
                if (value == null) {
                    break;
                } else {
                    fields.add(value);
                }
            }
        }

        String sql = generateImportSQL(sheet, fields, configuration.getStartRow(), configuration.getTargets()[0]);
        LOG.debug(sql);
        for (DBTableLocator table : configuration.getTargets()) {
            Connection connection = DBUtils.getConnection(table.getConnection());
            try (PreparedStatement ps = connection.prepareStatement("USE " + table.getSchema());
                 PreparedStatement ps2 = connection.prepareStatement(sql)) {
                ps.execute();
                ps2.executeUpdate();
            }
        }
        LOG.info("Import done!");
    }

    private String generateImportSQL(Sheet sheet, List<String> fields, int startRow, DBTableLocator table)
            throws SQLException {
        Connection connection = DBUtils.getConnection(table.getConnection());
        Map<Integer, Map<String, Object>> fieldValueMap = new HashMap<>();
        List<String> fieldForSql = new ArrayList<>();
        StringBuilder fieldsBuilder = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            String str = fields.get(i);
            if (str.isEmpty()) {
                fieldForSql.add(null);
                continue;
            }

            if (str.contains(".")) {
                String field1 = StringUtils.substringBefore(str, "[");
                String table2 = StringUtils.substringBetween(str, "[", ".");
                String field2;
                StringBuilder whereClause = new StringBuilder();
                if (str.contains("]->")) {
                    field2 = str.substring(str.indexOf(".") + 1, str.lastIndexOf("["));
                    String values = str.substring(str.lastIndexOf("[") + 1, str.indexOf("]"));
                    String sqlValues = StringUtils.encloseSubstrings(values, ",", SplitRegex.DROPPED, "'", ",");
                    whereClause.append(" WHERE ").append(field2).append(" IN (").append(sqlValues).append(")");
                } else {
                    field2 = StringUtils.substringBetween(str, ".", "-");
                }
                String field3 = StringUtils.substringBetween(str, ">", "]");
                String sql = "SELECT " + field2 + ", " + field3 + " FROM " + table2 + whereClause;
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ResultSet rs = ps.executeQuery();
                    Map<String, Object> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getString(field2), rs.getObject(field3));
                    }
                    fieldValueMap.put(i, map);
                    fieldForSql.add(field1);
                }
            } else if (str.contains(",")) {
                String[] pairs = StringUtils.substringBetween(str, "[", "]").split(",");
                Map<String, Object> map1 = new HashMap<>();
                for (String s : pairs) {
                    String[] pair = s.split(":");
                    map1.put(pair[1], Integer.valueOf(pair[0]));
                }
                fieldValueMap.put(i, map1);
                fieldForSql.add(StringUtils.substringBefore(str, "["));
            } else if (str.contains("[")) {
                fieldForSql.add(StringUtils.substringBefore(str, "["));
            } else {
                fieldForSql.add(str);
            }
            fieldsBuilder.append(fieldForSql.get(i)).append(",");
        }
        fieldsBuilder.deleteCharAt(fieldsBuilder.length() - 1);

        StringBuilder report = new StringBuilder("Report:\n");
        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO ").append(table.getTable()).append(" (")
                .append(fieldsBuilder).append(") VALUES \n");
        int skippedRows = 0, importedRows = 0;
        row:
        for (int i = startRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                skippedRows++;
                report.append("Skipped row ").append(i).append(": Empty row\n");
                continue;
            }
            StringBuilder rowBuilder = new StringBuilder();
            int lastCellNum = row.getLastCellNum();
            for (int j = 0; j < fieldForSql.size(); j++) {
                String field = fieldForSql.get(j);
                if (field == null) {
                    continue;
                }

                String value;
                if (j < lastCellNum) {
                    value = POIUtils.getCellValue(row.getCell(j));
                } else {
                    value = StringUtils.substringBetween(fields.get(j), "[", "]");
                }

                if (value == null) {
                    value = "null";
                } else if (!NumberUtils.isParsable(value)) {
                    Map<String, Object> map = fieldValueMap.get(j);
                    if (map != null) {
                        Object val = map.get(value);
                        if (val == null) {
                            skippedRows++;
                            report.append("Skipped row ").append(i).append(": No mapping found for field ").append("\"")
                                    .append(field).append("\" value \"").append(value).append("\"\n");
                            continue row;
                        } else if (val instanceof String) {
                            value = "'" + EscapeTypes.SQL.escape((String) val) + "'";
                        } else {
                            value = val.toString();
                        }
                    } else if (!value.equals("now()")) {
                        value = "'" + EscapeTypes.SQL.escape(value) + "'";
                    }
                }
                rowBuilder.append(value).append(",");
            }
            sqlBuilder.append("(").append(rowBuilder.deleteCharAt(rowBuilder.length() - 1)).append("),\n");
            importedRows++;
        }
        sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length()).append(";");
        report.append("Rows imported: ").append(importedRows).append("\nRows skipped: ").append(skippedRows).append("\n");
        LOG.info(report.toString());
        return sqlBuilder.toString();
    }
}
