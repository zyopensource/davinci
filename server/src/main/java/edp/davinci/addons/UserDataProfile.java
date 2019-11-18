/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package edp.davinci.addons;

import java.util.List;

/**
 * 
 * <br>
 * Class Name   : UserDataProfile
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
public class UserDataProfile {

	private boolean  allPrivileges;
	private List<UserDataProfileItem> profileItems;
	
	/**
	 * @return the allPrivileges
	 */
	public boolean isAllPrivileges() {
		return allPrivileges;
	}
	/**
	 * @param allPrivileges the allPrivileges to set
	 */
	public void setAllPrivileges(boolean allPrivileges) {
		this.allPrivileges = allPrivileges;
	}
	/**
	 * @return the profileItems
	 */
	public List<UserDataProfileItem> getProfileItems() {
		return profileItems;
	}
	/**
	 * @param profileItems the profileItems to set
	 */
	public void setProfileItems(List<UserDataProfileItem> profileItems) {
		this.profileItems = profileItems;
	}
	
	
}
