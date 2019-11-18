package edp.davinci.addons;

import java.util.List;

/**
 * 
 * 
 * <br>
 * Class Name   : UserDataProfileContextHolder
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
public class UserDataProfileContextHolder {

	private static ThreadLocal<List<UserDataProfileItem>> contextHolder = new ThreadLocal<>();
	
	public static void set(List<UserDataProfileItem> items){
		contextHolder.set(items);
	}
	
	public static void unset(){
		contextHolder.remove();
	}
	
	public static List<UserDataProfileItem> getDataProfiles(){
		return contextHolder.get() == null ? null : contextHolder.get();
	}
}
