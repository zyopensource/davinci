package edp.davinci.model.mdm;

import lombok.Data;

/**
 * @author linda
 */
@Data
public class CostCenter {
    private String ID;
    private String DepartmentCode;
    private String DepartmentName;
    private String DepartmentLongName;
    private String DepartmentId;
    private String SuperiorDepartmentId;

    public CostCenter(String ID, String departmentCode, String departmentName, String departmentLongName, String departmentId, String superiorDepartmentId) {
        this.ID = ID;
        DepartmentCode = departmentCode;
        DepartmentName = departmentName;
        DepartmentLongName = departmentLongName;
        DepartmentId = departmentId;
        SuperiorDepartmentId = superiorDepartmentId;
    }
}
