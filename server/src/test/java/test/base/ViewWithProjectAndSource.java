package test.base;

import edp.davinci.model.View;
import lombok.Data;

@Data
public class ViewWithProjectAndSource {
    private Long projectId;

    private Long sourceId;

    private String sql;
    private String projectName;
    private String sourceName;
    private String sourceConfig;
}
