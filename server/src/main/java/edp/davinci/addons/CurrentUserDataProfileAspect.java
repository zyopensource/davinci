package edp.davinci.addons;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import edp.davinci.model.User;

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
		if(user == null)return;
		//TODO 加载用户数据权限维度数据
		List<DataProfileItem> dataProfiles = new ArrayList<DataProfileItem>();
		dataProfiles.add(new DataProfileItem("account_id", new String[]{"2155","13233"}));
		UserDataProfileContextHolder.set(dataProfiles);
	}
	
	@After(value = "pointcut()")
    public void after(JoinPoint joinPoint) {
		UserDataProfileContextHolder.unset();
	}

}
