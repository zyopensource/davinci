package edp.davinci.dto.widgetDto;

import edp.davinci.model.Widget;
import lombok.Data;

/**
 * @author linda
 */
@Data
public class WidgetWithSubscribe extends Widget {

    private Long subscribeId;
    private String position;

}