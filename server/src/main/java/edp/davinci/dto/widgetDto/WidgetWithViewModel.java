package edp.davinci.dto.widgetDto;

import edp.davinci.model.Widget;
import lombok.Data;

/**
 * @program davinci
 * @description: 指标关联上维度
 * @author: lindajian
 * @create: 2019/12/16 10:18
 */
@Data
public class WidgetWithViewModel extends Widget {
    private String model;
}
