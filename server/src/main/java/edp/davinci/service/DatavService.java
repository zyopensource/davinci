package edp.davinci.service;

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
    List<Widget> getSubscribeWidgets(User user);

    /**
     * 指标订阅
     *
     * @param datavWidgetSubscribe
     * @return
     */
    DatavWidgetSubscribe widgetSubscribe(DatavWidgetSubscribe datavWidgetSubscribe, User user);

    /**
     * 取消指标订阅
     *
     * @param datavWidgetSubscribe
     * @return
     */
    void cancelWidgetSubscribe(DatavWidgetSubscribe datavWidgetSubscribe, User user);


}
