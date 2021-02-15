package net.ivanhjc.utility.db.model.enums;

import net.ivanhjc.utility.data.ListUtils;
import net.ivanhjc.utility.data.StringUtils;

public enum FileType {
    SPREADSHEET,
    UNKNOWN;

    public static FileType getFileType(String file) {
        if (ListUtils.contains("xlsx", StringUtils.substringAfterLast(StringUtils.substringBeforeLast(file, "."), "."))) {
            return SPREADSHEET;
        }
        return UNKNOWN;
    }
}
