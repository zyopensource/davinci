package edp.davinci.dao;

import edp.davinci.model.DatavWidgetSubscribe;
import edp.davinci.model.Widget;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author linda
 */
@Component
public interface DatavMapper {

    List<Widget> selectSubscribeWidgets(@Param("userId") Long userId);

    Widget selectSubscribeWidgetByWidgetId(@Param("widgetId") Long widgetId);

    @Update({"update datav_rel_user_widget_subscribe set is_delete=1 where user_id=#{userId} and widget_id=#{widgetId}"})
    int deleteSubscribeWidgets(@Param("userId") Long userId,@Param("widgetId") Long widgetId);

    int insertSubscribeWidgets(DatavWidgetSubscribe datavWidgetSubscribe);
}
