package edp.davinci.dto.viewDto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author linda
 */
@Data
public class TypeGroup {
    /**
     * 字段名
     */
    @NotBlank(message = "Invalid group column")
    private String column;
    /**
     * 字段聚合运算
     */
    private String columnAgg;

    /**
     * 字段别名
     */
    private String columnAlias;

    /**
     * 维度类型
     */
    private String visualType;

    /**
     * 类型值
     */
    private String value;

    /**
     * 维度group聚合值
     */
    private String agg;

    public void setColumn(String column) {
        this.column = column;
        this.columnAlias = column;
        this.agg = column;
    }

    public TypeGroup(String column, String visualType, String value) {
        this.column = column;
        this.columnAlias = column;
        this.agg = column;
        this.visualType = visualType;
        this.value = value;
    }

    public TypeGroup() {
    }
}
