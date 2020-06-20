/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package edp.davinci.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import edp.davinci.addons.UserDataProfileItem;
import edp.davinci.model.mdm.CostCenter;
import edp.davinci.model.mdm.Department;
import edp.davinci.model.mdm.Subject;

/**
 * <br>
 * Class Name   : ExternalService
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
public interface ExternalService {

    List<UserDataProfileItem> queryUserDataProfiles(String email);

    JSONObject queryQywxUserInfo(String code);


    List<Department> queryDepartments();

    List<Department> queryMdmDepartments();

	/**
	 * 获取成本中心
	 * @return
	 */
	List<CostCenter> queryCostCenters();

	/**
	 * 接口服务获取成本中心
	 * @return
	 */
    List<CostCenter> queryMdmCostCenters();

	/**
	 * 获取支出科目
	 * @return
	 */
	List<Subject> querySubjects();

	/**
	 * 接口服务获取支出科目
	 * @return
	 */
    List<Subject> queryMdmSubjects();
}
