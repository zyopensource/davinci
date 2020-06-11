package edp.davinci.model.mdm;

import lombok.Data;

/**
 * @author linda
 */
@Data
public class Department {
    private String id;
    private String serialNo;
    private String name;
    private String longSerialNo;
    private String displayName;
    private String parentId;
    private String departmentType;
    private Integer isVirtual;
    private Integer isLeaf;
    private Integer isArea;
    private String wechatIsShow;
    private String leaderStaffId;
    private String leaderName;
    private Integer enabled;
    private Integer treeLevel;
    private String parentSerialNo;
    private String createdAt;
    private String updatedAt;
}
