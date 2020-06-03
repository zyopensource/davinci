package edp.davinci.model;

import edp.core.model.RecordInfo;
import lombok.Data;

/**
 * @author linda
 */
@Data
public class DatavWidgetSubscribe extends RecordInfo<DatavWidgetSubscribe> {
    private Long id;
    private Long userId;
    private Long widgetId;
    private boolean isDelete;
}
