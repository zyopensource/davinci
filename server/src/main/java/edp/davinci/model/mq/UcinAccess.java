package edp.davinci.model.mq;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linda
 */
@Data
public class UcinAccess implements Serializable {
    private String pptId;
    private String pptName;
    private String name;
    private String email;
    private String appId;
    /**
     * ADD/REMOVE
     */
    private String type;

}
