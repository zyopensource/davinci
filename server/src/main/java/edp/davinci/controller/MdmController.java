package edp.davinci.controller;

import edp.core.annotation.CurrentUser;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.model.User;
import edp.davinci.model.mdm.CostCenter;
import edp.davinci.model.mdm.Department;
import edp.davinci.model.mdm.Subject;
import edp.davinci.service.ExternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author linda
 */
@Api(value = "/mdm", tags = "mdm", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiResponses(@ApiResponse(code = 404, message = "resource not found"))
@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/mdm", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class MdmController extends BaseController {
    @Autowired
    private ExternalService externalService;

    @ApiOperation(value = "get departments")
    @GetMapping("/departments")
    public ResponseEntity getDepartments(@ApiIgnore @CurrentUser User user,
                                         HttpServletRequest request) {
        List<Department> departments = externalService.queryDepartments();
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(departments));
    }
    @ApiOperation(value = "get costCenters")
    @GetMapping("/costCenters")
    public ResponseEntity getCostCenters(@ApiIgnore @CurrentUser User user,
                                         HttpServletRequest request) {
        List<CostCenter> costCenters = externalService.queryCostCenters();
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(costCenters));
    }
    @ApiOperation(value = "get subjects")
    @GetMapping("/subjects")
    public ResponseEntity getSubjects(@ApiIgnore @CurrentUser User user,
                                         HttpServletRequest request) {
        List<Subject> subjects = externalService.querySubjects();
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(subjects));
    }
}
