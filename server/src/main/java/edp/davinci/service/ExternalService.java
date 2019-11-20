/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package edp.davinci.service;

import java.util.List;

import edp.davinci.addons.UserDataProfileItem;

/**
 * 
 * <br>
 * Class Name   : ExternalService
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
public interface ExternalService {
	
	List<UserDataProfileItem> queryUserDataProfiles(String email);
}
