package edp.davinci.addons;

/**
 * 
 * <br>
 * Class Name   : DataProfileItem
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
public class UserDataProfileItem {
	
	String name;
	private boolean  allPrivileges;
	String[] values = new String[0];
	
	
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the values
	 */
	public String[] getValues() {
		return values;
	}
	/**
	 * @param values the values to set
	 */
	public void setValues(String[] values) {
		this.values = values;
	}

}
