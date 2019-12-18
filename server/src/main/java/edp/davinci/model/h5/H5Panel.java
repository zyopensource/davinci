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
    private String dashboardName;
    private String shareToken;
    private List<H5Widget> h5Widgets;
}
