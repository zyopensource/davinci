package edp.davinci.model.h5;

import lombok.Data;

import java.util.List;

/**
 * @program davinci
 * @description: H5移动端的看板
 * @author: lindajian
 * @create: 2019/12/17 10:49
 */
@Data
public class H5Panel {
    private Long projectId;
    private Long dashboardId;
    private String dashboardName;
    private Long dashboardPortalId;
    private String dashboardPortalName;
    private String shareToken;
    private List<H5Widget> h5Widgets;
}
