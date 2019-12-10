package edp.davinci.controller;

import com.zy.spring.core.wx.api.WxAccessTokenApi;
import com.zy.spring.core.wx.api.WxAuthApi;
import com.zy.spring.core.wx.result.WxToken;
import com.zy.spring.core.wx.result.WxUserInfoResult;
import edp.core.annotation.AuthIgnore;
import edp.core.annotation.CurrentUser;
import edp.core.enums.HttpCodeEnum;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dto.widgetDto.WidgetWithRelationDashboardId;
import edp.davinci.model.GlobalDashboard;
import edp.davinci.model.H5Widget;
import edp.davinci.model.User;
import edp.davinci.model.Widget;
import edp.davinci.service.DashboardPortalService;
import edp.davinci.service.DashboardService;
import edp.davinci.service.WidgetService;
import edp.davinci.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @program davinci
 * @description: H5
 * @author: lindajian
 * @create: 2019/12/04 15:17
 */
@Api(value = "/h5", tags = "h5", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@ApiResponses(@ApiResponse(code = 404, message = "resource not found"))
@Slf4j
@RestController
@RequestMapping(value = Constants.BASE_API_PATH + "/h5", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class H5Controller extends BaseController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private WidgetService widgetService;
    /**
     * 检查用户获取token
     *
     * @param request
     * @return
     */
    @AuthIgnore
    @GetMapping("/token/{code}")
    public ResponseEntity checkAndGetToken(HttpServletRequest request, @PathVariable String code) {
        if(code == null){
            return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
        }
//        String userId = "dlzyueqy_13128688632";
//        WxToken wxToken = WxAccessTokenApi.getAccessToken("wx110001df99daf0de","OOX5XjDVa0by4GA6RWNAgor3jYrQ_bilCZuK3mJjFkU");
//        WxAuthApi
        User user = userService.getByUsername("lindajian");
        if(user == null){
            return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));

        }
        return ResponseEntity.ok(new ResultMap().success(tokenUtils.generateContinuousToken(user)));
    }
    /**
     * 获取权限内所有dashboardPortal列表
     *
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get global dashboards")
    @GetMapping("/widgets")
    public ResponseEntity getAllDashboards(@ApiIgnore @CurrentUser User user,
                                           HttpServletRequest request)  {
        List<GlobalDashboard> dashboards = dashboardService.getGlobalDashboards(user);
        List<WidgetWithRelationDashboardId> widgets = new ArrayList<>();
        for(GlobalDashboard globalDashboard :dashboards){
            Set<WidgetWithRelationDashboardId> widgetWithRelationDashboardId =  globalDashboard.getWidgets();
            widgets.addAll(widgetWithRelationDashboardId);
        }
        //用来临时存储person的id
        List<Long> ids = new ArrayList<>();
        // 过滤去重
        widgets = widgets.stream().filter(
                v -> {
                    boolean flag = !ids.contains(v.getId());
                    ids.add(v.getId());
                    return flag;
                }
        ).collect(Collectors.toList());
        List<H5Widget> h5Widgets = new ArrayList<>();
        for(WidgetWithRelationDashboardId widget : widgets){
            H5Widget h5Widget = new H5Widget();
            Long id = widget.getId();
            h5Widget.setId(id);
            h5Widget.setText(widget.getName());
            h5Widget.setShareToken(widgetService.shareWidget(id, user, ""));
            h5Widgets.add(h5Widget);
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(h5Widgets));
    }
}
