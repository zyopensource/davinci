package test;

import edp.davinci.model.mdm.Department;
import edp.davinci.service.ExternalService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.base.BaseJunit4Test;

import java.util.List;

public class MdmTest extends BaseJunit4Test {
    @Autowired
    private ExternalService externalService;

    @Test
    public void querydepartMentTest(){
        List<Department> departments = externalService.queryDepartments();
        System.out.println("ss");

    }
}
