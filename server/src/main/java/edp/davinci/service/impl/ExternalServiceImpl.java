/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package edp.davinci.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import edp.davinci.core.utils.HttpClientUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import edp.core.utils.SqlExtUtils;
import edp.davinci.addons.UserDataProfileItem;
import edp.davinci.service.ExternalService;

/**
 * <br>
 * Class Name   : ExternalServiceImpl
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
@Service
public class ExternalServiceImpl implements ExternalService, EnvironmentAware {

    static Cache<String, Object> cache = CacheBuilder
            .newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(300, TimeUnit.SECONDS)
            .build();

    @Autowired
    private RestTemplate restTemplate;

    @Value("${data-profile.service-baseurl}/user/profiles?u=")
    private String queryUserDataProfileUrl;

    @Value("${data-profile.qywx-service-baseurl}")
    private String queryQywxUserInfoBaseurl;

    @Value("${data-profile.qywx-agentId}")
    private String qywxAgentId;
    //后续考虑映射多个字段
    private Map<String, String> dataProfileColumnMappings = new HashMap<>();

    @Override
    public List<UserDataProfileItem> queryUserDataProfiles(String email) {
        //from cache
        String key = email;
        List<UserDataProfileItem> lists = (List<UserDataProfileItem>) cache.getIfPresent(key);
        if (lists != null) {
            return lists;
        }

        String url = queryUserDataProfileUrl + email;
        ParameterizedTypeReference<List<UserDataProfileItem>> arearesponseType = new ParameterizedTypeReference<List<UserDataProfileItem>>() {
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-invoker-appid", "davinci");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<String>(null, headers);

        try {
            lists = restTemplate
                    .exchange(url, HttpMethod.GET, entity, arearesponseType)
                    .getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
        }

        if (lists != null && !lists.isEmpty()) {
            lists = lists.stream().filter(e -> e.isAllPrivileges() || e.getValues().length > 0).collect(Collectors.toList());
            for (UserDataProfileItem item : lists) {
                if (item.getName() != null && dataProfileColumnMappings.containsKey(item.getName())) {
                    item.setName(dataProfileColumnMappings.get(item.getName()));
                }
            }
        }

        if (lists == null) lists = new ArrayList<>(0);
        cache.put(key, lists);

        return lists;
    }


    @Override
    public void setEnvironment(Environment env) {
        String property = env.getProperty("data-profile.field-column-mappings");
        String[] level1s = StringUtils.splitByWholeSeparator(property, ";");
        for (String str : level1s) {
            String[] level2s = StringUtils.splitByWholeSeparator(str, "=");
            dataProfileColumnMappings.put(level2s[0].trim(), level2s[1].trim());
            //
            SqlExtUtils.addFilterColumn(level2s[1].trim());
        }
    }

    @Override
    public JSONObject queryQywxUserInfo(String code) {
        Map<String, String> pararms = new HashMap();
        pararms.put("code", code);
        pararms.put("agentId", qywxAgentId);
        JSONObject object = null;
        try {
            object = JSON.parseObject(HttpClientUtil.doJsonPost(queryQywxUserInfoBaseurl, pararms));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

}
