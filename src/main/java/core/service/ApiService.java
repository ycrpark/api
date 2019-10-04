package core.service;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

public class ApiService {
	private Logger log = LoggerFactory.getLogger(ApiService.class);
	
	private static final int DEFAULT_TIME_OUT = 100000;
	
	private static PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
	
	private static Gson gson = new Gson();
	
	
	private static CloseableHttpClient defaultHttpClient = null;
	
	private ApiService() {
		manager.setMaxTotal(100);
		manager.setDefaultMaxPerRoute(50);
		
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(DEFAULT_TIME_OUT)
				.setConnectionRequestTimeout(DEFAULT_TIME_OUT)
				.setSocketTimeout(DEFAULT_TIME_OUT)
				.build();
		
		defaultHttpClient = HttpClients.custom()
				.setConnectionManager(manager)
				.setDefaultRequestConfig(config)
				.build();
	};
	private static class Loader {
		public static final ApiService INSTANCE = new ApiService();
	}
	public static ApiService getInstance() {
		return Loader.INSTANCE;
	}

	/**
	 * restTemplate 사용
	 * 
	 * @param <T1> request body object
	 * @param <T2> response object
	 * @param url
	 * @param httpMethod
	 * @param headers
	 * @param apiRq
	 * @param responseClass
	 * @param timeout
	 * @return
	 */
	public <T1, T2> T2 sendApi(String url, HttpMethod httpMethod, HttpHeaders headers, T1 apiRq, Class<T2> responseClass, Integer timeout) {
		int readTimeOut;
		int connTimeOut;
		if(timeout == null) {
			readTimeOut = DEFAULT_TIME_OUT;
			connTimeOut = DEFAULT_TIME_OUT;
		} else {
			readTimeOut = timeout;
			connTimeOut = timeout;
		}
		
		if(headers == null) {
			headers = new HttpHeaders();
		}

		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setReadTimeout(readTimeOut);
		requestFactory.setConnectTimeout(connTimeOut);

		RestTemplate restTemplate = new RestTemplate(requestFactory);
		HttpEntity<?> entity = null;
		if(apiRq == null) {
			entity = new HttpEntity<String>(headers);
		} else {
			entity = new HttpEntity<T1>(apiRq, headers);
		}

		long startTime = System.currentTimeMillis();

		T2 apiRs = null;
		try {
			apiRs = restTemplate.exchange(url, httpMethod, entity, responseClass).getBody();
		} catch (Exception e) {
			log.error("sendApi error : {}", e);
		} finally {
			log.warn("sendApi time : {}", System.currentTimeMillis() - startTime);
		}

		return apiRs;
	}
	
	/**
	 * pooling api
	 * 
	 * @param <T> response object
	 * @param request
	 * @param responseClass
	 * @param timeout
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T sendApi(HttpUriRequest request, Class<T> responseClass, int timeout) {
		CloseableHttpClient httpClient = null;
		if(timeout <= 0) {
			httpClient = defaultHttpClient;
		} else {
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(timeout)
					.setConnectionRequestTimeout(timeout)
					.setSocketTimeout(timeout)
					.build();
			
			httpClient = HttpClients.custom()
					.setConnectionManager(manager)
					.setDefaultRequestConfig(config)
					.build();
		}
		
		long startTime = System.currentTimeMillis();
		
		T apiRs = null;
		try {
			String response = EntityUtils.toString(httpClient.execute(request).getEntity());
			log.warn("response: {}", response);
			if(responseClass == null) {
				apiRs = (T) response;
			} else {
				apiRs = gson.fromJson(response, responseClass);
			}
		} catch (Exception e) {
			log.error("sendApi error : {}", e);
		} finally {
			log.info("sendApi time : {}", System.currentTimeMillis() - startTime);
		}
		
		return apiRs;
	}
	
	public <T> T sendApi(HttpUriRequest request, Class<T> responseClass) {
		return sendApi(request, responseClass, 0);
	}
	
	public <T> T sendApi(HttpUriRequest request) {
		return sendApi(request, null, 0);
	}
	
}
