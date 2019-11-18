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

	String fieldName;
	String[] fieldValues;
	
	public UserDataProfileItem() {}
	
	
	public UserDataProfileItem(String fieldName, String[] fieldValues) {
		this.fieldName = fieldName;
		this.fieldValues = fieldValues;
	}


	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String[] getFieldValues() {
		return fieldValues;
	}
	public void setFieldValues(String[] fieldValues) {
		this.fieldValues = fieldValues;
	}
	
	
}
