package net.ivanhjc.utility.model.enums;

/**
 * @author Ivan Huang on 2018/4/12 20:20.
 */
public enum RespCode {
    SUCCESS("200", "成功"),
    FAILURE("300", "失败");

    public final String code;
    public String message;

    RespCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
