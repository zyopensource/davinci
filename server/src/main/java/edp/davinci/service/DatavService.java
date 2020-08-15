package edp.davinci.service;

import edp.davinci.dto.widgetDto.WidgetWithSubscribe;
import edp.davinci.model.DatavWidgetSubscribe;
import edp.davinci.model.User;
import edp.davinci.model.Widget;

import java.util.List;

/**
 * @author linda
 */
public interface DatavService {
    /**
     * 是否有订阅指标
     *
     * @param widgetId
     * @param user
     * @return
     */
    boolean isSubscribe(Long widgetId, User user);

    /**
     * 获取用户订阅的指标
     *
     * @param user
     * @return
     */
    List<WidgetWithSubscribe> getSubscribeWidgets(User user);

    /**
     * 指标订阅
     *
     * @param datavWidgetSubscribe
     * @return
     */
    DatavWidgetSubscribe widgetSubscribe(DatavWidgetSubscribe datavWidgetSubscribe, User user);
    /**
     * 保存订阅指标的坐标
     *
     * @param datavWidgetSubscribes
     * @return
     */
    int widgetSubscribePosition(List<DatavWidgetSubscribe> datavWidgetSubscribes, User user);
    /**
     * 取消指标订阅
     *
     * @param datavWidgetSubscribe
     * @return
     */
    void cancelWidgetSubscribe(DatavWidgetSubscribe datavWidgetSubscribe, User user);


}
