package core.service;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class PolingApiService {
	private Logger log = LoggerFactory.getLogger(PolingApiService.class);
	
	private static final int DEFAULT_TIME_OUT = 100000;
	
	private static PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
	
	private static Gson gson = new Gson();
	
	
	private static CloseableHttpClient defaultHttpClient = null;
	
	private PolingApiService() {
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
		public static final PolingApiService INSTANCE = new PolingApiService();
	}
	public static PolingApiService getInstance() {
		return Loader.INSTANCE;
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
