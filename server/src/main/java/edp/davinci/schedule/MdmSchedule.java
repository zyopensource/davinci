package edp.davinci.schedule;

import edp.core.utils.RedisUtils;
import edp.davinci.common.controller.BaseController;
import edp.davinci.model.mdm.Department;
import edp.davinci.service.DatavService;
import edp.davinci.service.ExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

import  edp.davinci.core.common.Constants;

/**
 * @author linda
 */

@Slf4j
@Component
public class MdmSchedule extends BaseController {
    @Autowired
    private ExternalService externalService;


    /**
     * 部门同步
     */
    @Scheduled(cron = "0 0/30 * * * *")
    public void departmentsSync() {
        List<Department> departments = externalService.queryMdmDepartments();
        String key = Constants.MDM_DEPARTMENTS_REDIS_KEY;
        redisUtils.set(key, departments);
    }
}
