package edp.davinci.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edp.core.annotation.AuthIgnore;
import edp.core.annotation.CurrentUser;
import edp.core.enums.HttpCodeEnum;
import edp.core.exception.NotFoundException;
import edp.core.exception.ServerException;
import edp.core.exception.UnAuthorizedExecption;
import edp.core.utils.RedisUtils;
import edp.core.utils.TokenUtils;
import edp.davinci.common.controller.BaseController;
import edp.davinci.core.common.Constants;
import edp.davinci.core.common.ResultMap;
import edp.davinci.dao.WidgetMapper;
import edp.davinci.dto.projectDto.ProjectDetail;
import edp.davinci.dto.viewDto.Param;
import edp.davinci.dto.widgetDto.WidgetWithViewModel;
import edp.davinci.model.*;
import edp.davinci.model.h5.H5Panel;
import edp.davinci.model.h5.H5Widget;
import edp.davinci.service.*;
import edp.davinci.service.impl.UserServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stringtemplate.v4.ST;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
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
    @Autowired
    private ExternalService externalService;
    @Resource
    private WidgetMapper widgetMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private DatavService datavService;
    public static String H5_INDEX_RECORD_REDIS_KEY = "DATAV_H5_INDEX_RECORD_@";

    /**
     * 检查用户获取token
     *
     * @param request
     * @return
     */
    @AuthIgnore
    @GetMapping("/token/{code}")
    public ResponseEntity checkAndGetToken(HttpServletRequest request, @PathVariable String code) {
        User user;
        try {
            if (code == null) {
                return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
            }
            JSONObject result = externalService.queryQywxUserInfo(code);
            log.info("qywx result:{}",result);
            if (result.getString("code") == null || result.getInteger("code") != 200) {
                return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
            }
            JSONObject userInfo = result.getJSONObject("data");
            String email = userInfo.getString("email");
            user = userService.getByUsername(email.replace("@zy.com", ""));
            if (user == null) {
                return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
        }
        return ResponseEntity.ok(new ResultMap().success(tokenUtils.generateContinuousToken(user)));
    }

    /**
     * 校验token和用户的身份
     *
     * @param request
     * @return
     */
    @AuthIgnore
    @GetMapping("/token/{token}/{code}")
    public ResponseEntity checkUserAndToken(HttpServletRequest request, HttpServletResponse response, @PathVariable String token, @PathVariable String code) {
        try {
            response.sendError(HttpCodeEnum.FORBIDDEN.getCode(), "ERROR Permission denied");
            if (code == null) {
                return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
            }
            JSONObject result = externalService.queryQywxUserInfo(code);
            log.info("qywx result:{}",result);
            if (result.getString("code") == null || result.getInteger("code") != 200) {
                return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
            }
            JSONObject userInfo = result.getJSONObject("data");
            String userName = userInfo.getString("email").replace("@zy.com", "");
            if (!userName.equals(tokenUtils.getUsername(token))) {
                response.sendError(HttpCodeEnum.FORBIDDEN.getCode(), "ERROR Permission denied");
                return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.FORBIDDEN.getCode()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(new ResultMap().fail(HttpCodeEnum.NOT_FOUND.getCode()));
        }
        return ResponseEntity.ok(new ResultMap().success(token));
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
                                           HttpServletRequest request) {
        List<H5Widget> h5Widgets = null;
        try {
            List<GlobalDashboard> dashboards = dashboardService.getGlobalDashboards(user);
            List<WidgetWithViewModel> widgets = new ArrayList<>();
            for (GlobalDashboard globalDashboard : dashboards) {
                List<WidgetWithViewModel> widgetWithViewModels = globalDashboard.getWidgets();
                widgets.addAll(widgetWithViewModels);
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
            h5Widgets = new ArrayList<>();
            for (WidgetWithViewModel widget : widgets) {
                H5Widget h5Widget = new H5Widget();
                Long id = widget.getId();
                h5Widget.setId(id);
                h5Widget.setProjectId(widget.getProjectId());
                h5Widget.setText(widget.getName());
//                h5Widget.setShareToken(widgetService.shareWidget(id, user, ""));
                h5Widget.setModel(JSON.parseObject(widget.getModel()));
                h5Widget.setConfig(JSON.parseObject(widget.getConfig()));
                h5Widgets.add(h5Widget);
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (UnAuthorizedExecption unAuthorizedExecption) {
            unAuthorizedExecption.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(h5Widgets));
    }

    /**
     * 保存用户redis上的指标感兴趣记录
     *
     * @param user
     * @param params
     * @param request
     * @return
     */
    @ApiOperation(value = "save redis h5 index record")
    @PostMapping("/saveWidgetsRecords")
    public ResponseEntity saveWidgetsRecords(@ApiIgnore @CurrentUser User user,
                                             @RequestBody Params params,
                                             HttpServletRequest request) {

        try {
            String key = H5_INDEX_RECORD_REDIS_KEY + user.getId();
            redisUtils.set(key, params.getRecords());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(params.getRecords()));
    }

    /**
     * 获取用户redis上的指标感兴趣记录
     *
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get redis h5 index record")
    @GetMapping("/getWidgetsRecords")
    public ResponseEntity getWidgetsRecords(@ApiIgnore @CurrentUser User user,
                                            HttpServletRequest request) {
        String values = "[]";
        try {
            String key = H5_INDEX_RECORD_REDIS_KEY + user.getId();
            values = redisUtils.get(key).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(values));
    }

    /**
     * 获取权限内所有看板列表
     *
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get global dashboards")
    @GetMapping("/panels")
    public ResponseEntity getAllPanels(@ApiIgnore @CurrentUser User user,
                                       HttpServletRequest request) {
        List<H5Panel> panels = new ArrayList<>();
        List<GlobalDashboard> dashboards = dashboardService.getGlobalDashboards(user);
        Set<Long> dashboardIds = dashboards.stream().map(GlobalDashboard::getId).collect(Collectors.toSet());
        for (GlobalDashboard globalDashboard : dashboards) {
            H5Panel h5Panel = new H5Panel();
//            try {
//                h5Panel.setShareToken(dashboardService.shareDashboard(dashboardId, "", user));
//            } catch (Exception e) {
//                continue;
//            }
            List<H5Widget> h5Widgets = new ArrayList<>();
            for (Long dashboardId : dashboardIds) {
                if (globalDashboard.getId().equals(dashboardId)) {
                    h5Panel.setProjectId(globalDashboard.getProjectId());
                    h5Panel.setDashboardId(dashboardId);
                    h5Panel.setDashboardName(globalDashboard.getName());
                    h5Panel.setDashboardPortalId(globalDashboard.getDashboardPortalId());
                    h5Panel.setDashboardPortalName(globalDashboard.getDashboardPortalName());
                    List<WidgetWithViewModel> widgetWithViewModels = globalDashboard.getWidgets();
                    for (WidgetWithViewModel widget : widgetWithViewModels) {
                        H5Widget h5Widget = new H5Widget();
                        Long id = widget.getId();
                        h5Widget.setId(id);
                        h5Widget.setText(widget.getName());
//                        h5Widget.setShareToken(widgetService.shareWidget(id, user, ""));
                        h5Widget.setModel(JSON.parseObject(widget.getModel()));
                        h5Widget.setConfig(JSON.parseObject(widget.getConfig()));
                        h5Widgets.add(h5Widget);
                    }
                }
            }
            h5Panel.setH5Widgets(h5Widgets);
            panels.add(h5Panel);
        }

        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payloads(panels));
    }

    /**
     * 获取widget列表
     *
     * @param id
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get widget info")
    @GetMapping("/widgets/{id}")
    public ResponseEntity getWidgetInfo(@PathVariable Long id,
                                        @ApiIgnore @CurrentUser User user,
                                        HttpServletRequest request) {
        if (invalidId(id)) {
            ResultMap resultMap = new ResultMap(tokenUtils).failAndRefreshToken(request).message("Invalid id");
            return ResponseEntity.status(resultMap.getCode()).body(resultMap);
        }
        Widget widget = widgetMapper.getById(id);

        if (null == widget) {
            log.info("widget {} not found", id);
            throw new NotFoundException("widget is not found");
        }
        widget.setIsSubscribe(datavService.isSubscribe(widget.getId(),user));
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(widget));
    }


    /**
     * 获取订阅指标列表
     *
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get subscribe info")
    @GetMapping("/subscribe/list")
    public ResponseEntity getSubscribeWidgetInfo(@ApiIgnore @CurrentUser User user,
                                                 HttpServletRequest request) {
        List<Widget> widgets = datavService.getSubscribeWidgets(user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(widgets));
    }

    /**
     * 指标订阅
     *
     * @param datavWidgetSubscribe
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get subscribe info")
    @PostMapping("/subscribe")
    public ResponseEntity subscribeWidget(@Valid @RequestBody DatavWidgetSubscribe datavWidgetSubscribe,
                                          @ApiIgnore @CurrentUser User user,
                                          HttpServletRequest request) {
        DatavWidgetSubscribe datavWidgetSubscribeNew = datavService.widgetSubscribe(datavWidgetSubscribe, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(datavWidgetSubscribeNew));
    }

    /**
     * 获取订阅指标列表
     *
     * @param user
     * @param request
     * @return
     */
    @ApiOperation(value = "get subscribe info")
    @PostMapping("/subscribe/canel")
    public ResponseEntity canelSubscribeWidget(@Valid @RequestBody DatavWidgetSubscribe datavWidgetSubscribe,
                                               @ApiIgnore @CurrentUser User user,
                                               HttpServletRequest request) {
        datavService.cancelWidgetSubscribe(datavWidgetSubscribe, user);
        return ResponseEntity.ok(new ResultMap(tokenUtils).successAndRefreshToken(request).payload(datavWidgetSubscribe));
    }

    @Data
    static class Params {
        private String records;
    }
}
