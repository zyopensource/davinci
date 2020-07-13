package edp.davinci.core.enums.types;

/**
 * @author linda
 */

public enum FastCalculateTypeEnum {
    /**
     *
     */
    YOY("yoy"),
    QOQ("qoq"),
    ;
    private String name;

    FastCalculateTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
