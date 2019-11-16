package edp.davinci.addons;

import java.util.List;

public class UserDataProfileContextHolder {

	private static ThreadLocal<List<DataProfileItem>> contextHolder = new ThreadLocal<>();
	
	public static void set(List<DataProfileItem> items){
		contextHolder.set(items);
	}
	
	public static void unset(){
		contextHolder.remove();
	}
	
	public static List<DataProfileItem> getDataProfiles(){
		return contextHolder.get() == null ? null : contextHolder.get();
	}
}
