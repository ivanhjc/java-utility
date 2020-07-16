package net.ivanhjc.utility.auto.enums;

/**
 * @author Administrator on 2019/1/30 20:18.
 */
public enum AnnotationOption {
    REPOSITORY("import org.springframework.stereotype.Repository;\n", "@Repository\n"),
    MAPPER("import org.apache.ibatis.annotations.Mapper;\n", "@Mapper\n"),
    PARAM("import org.apache.ibatis.annotations.Param;\n", "@Param\n"),
    NONE("", "");

    public final String importStr;
    public final String annotation;

    AnnotationOption(String importStr, String annotation) {
        this.importStr = importStr;
        this.annotation = annotation;
    }

    public AnnotationOption getSpecified(AnnotationOption[] annotationOptions) {
        for (AnnotationOption a : annotationOptions) {
            if (this == a) {
                return this;
            }
        }
        return NONE;
    }
}
