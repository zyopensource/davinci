package edp.davinci.addons;

public class DataProfileItem {

	String fieldName;
	String[] fieldValues;
	
	public DataProfileItem() {}
	
	
	public DataProfileItem(String fieldName, String[] fieldValues) {
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
