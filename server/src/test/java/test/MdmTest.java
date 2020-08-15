package test;

import edp.core.utils.TokenUtils;
import edp.davinci.model.User;
import edp.davinci.model.mdm.CostCenter;
import edp.davinci.model.mdm.Department;
import edp.davinci.service.ExternalService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.base.BaseJunit4Test;

import java.util.List;

public class MdmTest extends BaseJunit4Test{
    @Autowired
    private ExternalService externalService;
    @Autowired
    public TokenUtils tokenUtils;
    @Test
    public void querydepartMentTest(){
        List<Department> departments = externalService.queryDepartments();
        System.out.println("ss");

    }
    @Test
    public void queryCostCenterTest(){
        List<CostCenter> costCenters = externalService.queryMdmCostCenters();
        System.out.println("ss");

    }

    @Test
    public void tokenTest(){
        User user = new User();
        user.setUsername("zhengweijie99");
        user.setPassword("LDAP");
        System.out.println(tokenUtils.generateContinuousToken(user));

    }
}
