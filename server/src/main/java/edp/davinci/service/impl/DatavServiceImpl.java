package edp.davinci.service.impl;

import edp.core.exception.NotFoundException;
import edp.core.exception.ServerException;
import edp.core.exception.UnAuthorizedExecption;
import edp.davinci.dao.DatavMapper;
import edp.davinci.dto.projectDto.ProjectInfo;
import edp.davinci.model.DatavWidgetSubscribe;
import edp.davinci.model.Project;
import edp.davinci.model.User;
import edp.davinci.model.Widget;
import edp.davinci.service.DatavService;
import edp.davinci.service.ProjectService;
import edp.davinci.service.WidgetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author linda
 */
@Slf4j
@Service("DatavService")
public class DatavServiceImpl implements DatavService {

    @Resource
    private DatavMapper datavMapper;

    @Autowired
    private WidgetService widgetService;

    @Autowired
    private ProjectService projectService;

    @Override
    public boolean isSubscribe(Long widgetId, User user) {
        List<Widget> widgets = datavMapper.selectSubscribeWidgets(user.getId());
        if (widgets.size() == 0) {
            return false;
        }
        List<Widget> widgetList = widgets.stream().filter(widget -> widgetId.equals(widget.getId())).collect(Collectors.toList());
        if (widgetList.size() == 0) {
            return false;

        }
        return true;
    }

    @Override
    public List<Widget> getSubscribeWidgets(User user) {
        List<ProjectInfo> projects = projectService.getProjects(user);
        Set<Long> widgetIds = new HashSet<>();
        for (ProjectInfo projectInfo : projects) {
            List<Long> widgetIdList = widgetService.getWidgets(projectInfo.getId(), user).stream().map(v -> v.getId()).collect(Collectors.toList());
            widgetIds.addAll(widgetIdList);
        }
        List<Widget> widgets = datavMapper.selectSubscribeWidgets(user.getId()).stream().filter(v -> widgetIds.contains(v.getId())).collect(Collectors.toList());
        return widgets;
    }

    @Override
    public DatavWidgetSubscribe widgetSubscribe(DatavWidgetSubscribe datavWidgetSubscribe, User user) throws ServerException {
        datavWidgetSubscribe.createdBy(user.getId());
        datavWidgetSubscribe.setUserId(user.getId());
        int insert = datavMapper.insertSubscribeWidgets(datavWidgetSubscribe);
        if (insert > 0) {
            log.info("widgetSubscribe ({}) create by (:{})", datavWidgetSubscribe.toString(), user.getId());
            return datavWidgetSubscribe;
        } else {
            log.info("create widgetSubscribe error");
            throw new ServerException("create widgetSubscribe error");
        }
    }

    @Override
    public void cancelWidgetSubscribe(DatavWidgetSubscribe datavWidgetSubscribe, User user) throws ServerException {
        int update = datavMapper.deleteSubscribeWidgets(user.getId(), datavWidgetSubscribe.getWidgetId());
        if (update > 0) {
            log.info("cancelWidgetSubscribe ({}) create by (:{})", datavWidgetSubscribe.toString(), user.getId());
        } else {
            log.info("cancel widgetSubscribe error");
            throw new ServerException("cancel widgetSubscribe error");
        }
    }
}
