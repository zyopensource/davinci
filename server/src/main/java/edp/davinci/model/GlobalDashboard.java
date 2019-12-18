package edp.davinci.model;

import edp.davinci.dto.widgetDto.WidgetWithViewModel;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * @program davinci-parent_3.01
 * @description: 用于全局的指标搜索
 * @author: lindajian
 * @create: 2019/11/26 17:11
 */
@Data
@NotNull(message = "global dashboard cannot be null")
public class GlobalDashboard {
    @Min(value = 1L, message = "Invalid dashboard id")
    private Long id;

    @NotBlank(message = "dashboard name cannot be EMPTY")
    private String name;
    @Min(value = (short) 0, message = "Invalid dashboard type")
    @Max(value = (short) 2, message = "Invalid dashboard type")
    private short type;

    @Min(value = 1L, message = "Invalid project id")
    private Long projectId;
    private String projectName;
    private String projectUrlId;

    @Min(value = 1L, message = "Invalid dashboardPortal id")
    private Long dashboardPortalId;
    private String dashboardPortalName;
    private String dashboardPortalUrlId;

    private List<WidgetWithViewModel> widgets;

}
