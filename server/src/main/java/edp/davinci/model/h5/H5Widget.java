package edp.davinci.model.h5;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @program davinci
 * @description: H5移动端的指标
 * @author: lindajian
 * @create: 2019/12/06 18:50
 */
@Data
public class H5Widget {
    private Long id;

    private String text;

    private String shareToken;

    private JSONObject model;

    private JSONObject config;

}
