package edp.davinci.addons;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import edp.core.exception.ServerException;
import edp.davinci.model.User;

/**
 * 
 * <br>
 * Class Name   : CurrentUserDataProfileAspect
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
@Aspect // 切面注解
@Component
public class CurrentUserDataProfileAspect {

	@Pointcut("execution(* edp.davinci.service.impl.ViewServiceImpl.getData(..)) || execution(* edp.davinci.service.impl.ViewServiceImpl.executeSql(..))")
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
		if(user == null || user.getUserDataProfile() == null)return;
		UserDataProfile userDataProfile = user.getUserDataProfile();
		if(!userDataProfile.isAllPrivileges() 
				&& (userDataProfile.getProfileItems() == null || userDataProfile.getProfileItems().isEmpty())){
			throw new ServerException("not assign any data permissions");
		}
        UserDataProfileContextHolder.set(userDataProfile.getProfileItems());
	}
	
	@After(value = "pointcut()")
    public void after(JoinPoint joinPoint) {
		UserDataProfileContextHolder.unset();
	}

}
