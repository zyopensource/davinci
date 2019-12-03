package edp.davinci.addons;

import java.util.Map;

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

	private static ThreadLocal<Map<String, UserDataProfileItem>> contextHolder = new ThreadLocal<>();
	
	public static void set(Map<String, UserDataProfileItem> map){
		contextHolder.set(map);
	}
	
	public static void unset(){
		contextHolder.remove();
	}
	
	public static Map<String, UserDataProfileItem> getDataProfiles(){
		return contextHolder.get() == null ? null : contextHolder.get();
	}
}
