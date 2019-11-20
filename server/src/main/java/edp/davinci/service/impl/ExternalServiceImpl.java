/**
 * Confidential and Proprietary Copyright 2019 By 卓越里程教育科技有限公司 All Rights Reserved
 */
package edp.davinci.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import edp.davinci.addons.UserDataProfileItem;
import edp.davinci.service.ExternalService;

/**
 * 
 * <br>
 * Class Name   : ExternalServiceImpl
 *
 * @author jiangwei
 * @version 1.0.0
 * @date 2019年11月18日
 */
@Service
public class ExternalServiceImpl implements ExternalService{

	@Autowired
	private RestTemplate restTemplate;

	@Value("${external.data-profile-service-baseurl}/user/profiles?u=")
	private String queryUserDataProfileUrl;

	@Override
	public List<UserDataProfileItem> queryUserDataProfiles(String email) {
		String url = queryUserDataProfileUrl + email;
		ParameterizedTypeReference<List<UserDataProfileItem>> arearesponseType = new ParameterizedTypeReference<List<UserDataProfileItem>>() {
		};
		List<UserDataProfileItem> lists = restTemplate
				.exchange(url, HttpMethod.GET, null, arearesponseType)
				.getBody();
		
		if(lists != null && !lists.isEmpty()){
			lists = lists.stream().filter(e -> e.isAllPrivileges() || e.getValues().length > 0).collect(Collectors.toList());
		}
		return lists;
	}
}
