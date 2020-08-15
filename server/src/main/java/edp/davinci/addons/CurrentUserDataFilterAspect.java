package edp.davinci.addons;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.SqlOperatorEnum;
import edp.davinci.core.enums.types.LevelTypeEnum;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.dto.viewDto.Model;
import edp.davinci.dto.viewDto.ViewWithSource;
import edp.davinci.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限过滤
 *
 * @author linda
 */
//@Aspect // 切面注解
//@Component
@Slf4j
public class CurrentUserDataFilterAspect {


    @Pointcut("execution(* edp.davinci.service.impl.ViewServiceImpl.getResultDataList(..)) "
//            + "|| execution(* edp.davinci.service.impl.ViewServiceImpl.executeSql(..)) " 执行sql不需要数据过滤
            + "|| execution(* edp.davinci.service.impl.ViewServiceImpl.getDistinctValueData(..))")
    public void pointcut() {

    }

    @Around(value = "pointcut()")
    public Object aroundAdvice(ProceedingJoinPoint point) throws Throwable {
        Object[] args = point.getArgs();
        //方法名
        String methodName = point.getSignature().getName();
        String sql;
        if ("getResultDataList".equals(methodName)) {
            ViewWithSource viewWithSource = (ViewWithSource) args[1];
            User user = (User) args[3];
            sql = getDataFilterSql(viewWithSource,user);
            viewWithSource.setSql(sql);
            args[1] = viewWithSource;
        }
        if ("getDistinctValueData".equals(methodName)) {
            ViewWithSource viewWithSource = (ViewWithSource) args[1];
            User user = (User) args[3];
            sql = getDataFilterSql(viewWithSource,user);
            viewWithSource.setSql(sql);
            args[1] = viewWithSource;
        }
        Object result;
        try {
            result = point.proceed(args);
        } catch (Throwable e) {
            log.error("PermitAspect point.proceed异常", e.getMessage(), e);
            throw e;
        }
        return result;
    }
    private String getDataFilterSql(ViewWithSource viewWithSource ,User user){
        STGroup stg = new STGroupFile(Constants.SQL_TEMPLATE);

        String department =user.getDepartment();
        String[] departmentValues = null;
        if(StringUtils.isNotEmpty(department)){
            departmentValues = StringUtils.split(department,",");
        }
        String sql = viewWithSource.getSql();
        String model = viewWithSource.getModel();
        JSONObject modelObj = JSON.parseObject(model);
        List<String> departmentColums = new ArrayList();
        for (String column : modelObj.keySet()) {
            Model modelVal = JSON.parseObject(modelObj.getString(column), Model.class);
            if (LevelTypeEnum.Department.getName().equals(modelVal.getVisualType())) {
                departmentColums.add(column);
            }
        }
        if(departmentColums.size() == 0 || departmentValues == null || departmentValues.length == 0){
            return  sql;
        }
        List<SqlFilter> sqlFilters = new ArrayList<>();
            for (String column : departmentColums) {
                SqlFilter sqlFilter = new SqlFilter();
                sqlFilter.setName(column);
                sqlFilter.setOperator(SqlOperatorEnum.REGEXP.getValue());
                sqlFilter.setValue("'"+StringUtils.join(departmentValues,"|")+"'");
                sqlFilters.add(sqlFilter);
            }
        ST st = stg.getInstanceOf("queryJurisdictionFilterSql");
        st.add("originSql", sql);
        st.add("filters", sqlFilters);
        return st.render();
    }
}
