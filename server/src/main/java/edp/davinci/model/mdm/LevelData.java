package edp.davinci.model.mdm;

import lombok.Data;

/**
 * @author linda
 */
@Data
public class LevelData {
    private String id;
    private String name;
    private String longName;
    private String parentId;

    public LevelData(String id, String name, String longName, String parentId) {
        this.id = id;
        this.name = name;
        this.longName = longName;
        this.parentId = parentId;
    }
}
