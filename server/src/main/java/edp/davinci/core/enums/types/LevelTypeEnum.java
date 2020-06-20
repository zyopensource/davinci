package edp.davinci.core.enums.types;

/**
 * @author linda
 */

public enum LevelTypeEnum {

    /**
     *
     */
    Department("department"),
    CostCenter("costCenter"),
    Subject("subject"),
    ;
    private String name;

    LevelTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
