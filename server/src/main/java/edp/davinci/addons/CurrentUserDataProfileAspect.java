package edp.davinci.addons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import edp.davinci.model.User;
import edp.davinci.service.ExternalService;

/**
 * 
 * <br>
 * Class Name   : CurrentUserDataProfileAspect
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
//@Aspect // 切面注解
//@Component
public class CurrentUserDataProfileAspect {
	
	private static final String _ALL = "_ALL";
	@Autowired
    private ExternalService externalService;
	
	@Value("${data-profile.none-config-ignore:false}")
	private boolean noneConfigIgnore;

	@Pointcut("execution(* edp.davinci.service.impl.ViewServiceImpl.getData(..)) "
			+ "|| execution(* edp.davinci.service.impl.ViewServiceImpl.executeSql(..)) "
			+ "|| execution(* edp.davinci.service.impl.ViewServiceImpl.getDistinctValue(..))")
	public void pointcut() {
		
	}

	@Before("pointcut()")
	public void beforeProcess(JoinPoint jp) {
		Object[] args = jp.getArgs();
		User user = null;
		for (Object arg : args) {
			if(arg instanceof User){
				user = (User) arg;
				break;
			}
		}
		if(user == null)return;
		
		//获取外部权限数据
        List<UserDataProfileItem> userDataProfiles = externalService.queryUserDataProfiles(user.getEmail());
        
        if(noneConfigIgnore && (userDataProfiles == null || userDataProfiles.isEmpty())){
        	return;
        }
        
        Map<String, UserDataProfileItem> map = new HashMap<String, UserDataProfileItem>(userDataProfiles == null ? 0 : userDataProfiles.size());
        if(!userDataProfiles.isEmpty()){
			for (UserDataProfileItem item : userDataProfiles) {
				if(_ALL.equals(item.getName())){
					return;
				}
				map.put(item.getName(), item);
			}
		}
        UserDataProfileContextHolder.set(map);
	}
	
	@After(value = "pointcut()")
    public void after(JoinPoint joinPoint) {
		UserDataProfileContextHolder.unset();
	}

}
