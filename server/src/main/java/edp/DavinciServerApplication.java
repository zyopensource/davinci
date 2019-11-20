/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2019 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 *
 */

package edp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration.class})
@EnableScheduling
public class DavinciServerApplication {

    public static void main(String[] args) {
        System.setProperty("mail.mime.splitlongparameters", "false");
        SpringApplication.run(DavinciServerApplication.class, args);
    }

    public RestTemplate restTemplate(){
    	OkHttp3ClientHttpRequestFactory factory = new OkHttp3ClientHttpRequestFactory();  
        factory.setReadTimeout(10000);//ms  
        factory.setConnectTimeout(1000);//ms 
        
        RestTemplate restTemplate = new RestTemplate(factory);
//        restTemplate.getInterceptors().add(new ClientHttpRequestInterceptor() {
//			@Override
//			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
//					throws IOException {
//				request.getHeaders().add(headerName, headerValue);
//				return execution.execute(request, body);
//			}
//		});
		return restTemplate;
    }
}

