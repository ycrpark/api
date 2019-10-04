package core.service;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import core.model.ApiInfo;
import core.model.ApiRsInfo;

public class ApiService {
	private Logger log = LoggerFactory.getLogger(ApiService.class);
	
	private static final int DEFAULT_TIME_OUT = 100000;
	
	
	private ApiService() {
		
	};
	private static class Loader {
		public static final ApiService INSTANCE = new ApiService();
	}
	public static ApiService getInstance() {
		return Loader.INSTANCE;
	}

	/**
	 * RestTemplate 사용 API 호출
	 * httpStatus 오류 발생시에도 항상 포함 응답
	 * 
	 * GET, DELETE 등 body없는 api발송
	 * 
	 * @param <T>
	 * @param apiInfo
	 * @param httpMethod
	 * @param headers
	 * @param responseClass
	 * @return
	 */
	public <T> ApiRsInfo<T> sendApi(ApiInfo apiInfo, HttpMethod httpMethod, HttpHeaders headers, Class<T> responseClass) {
		return sendApi(apiInfo, httpMethod, headers, null, responseClass);
	}
	
	/**
	 * RestTemplate 사용 API 호출
	 * httpStatus 오류 발생시에도 항상 포함 응답
	 * 
	 * POST, PUT 등 body있는 api발송
	 * 
	 * @param <T1> request body object
	 * @param <T2> response object
	 * @param apiInfo 발송 정보
	 * @param httpMethod
	 * @param headers
	 * @param apiRq
	 * @param responseClass
	 * @return
	 */
	public <T1, T2> ApiRsInfo<T2> sendApi(ApiInfo apiInfo, HttpMethod httpMethod, HttpHeaders headers, T1 apiRq, Class<T2> responseClass) {
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		Integer timeoutMillis = apiInfo.getTimeoutMillis();
		if(timeoutMillis == null || timeoutMillis <= 0) {
			timeoutMillis = DEFAULT_TIME_OUT;
		}
		
		apiInfo.setTimeoutMillis(timeoutMillis);
		requestFactory.setReadTimeout(timeoutMillis);
		requestFactory.setConnectTimeout(timeoutMillis);
		
		RestTemplate restTemplate = new RestTemplate(requestFactory);
		// 오류 직접 처리
		restTemplate.setErrorHandler(new ResponseErrorHandler() {
			@Override
			public boolean hasError(ClientHttpResponse response) throws IOException {
				return false;
			}
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				
			}
		});

		HttpEntity<?> entity = null;
		if(apiRq == null) {
			entity = new HttpEntity<String>(headers); 
		} else {
			entity = new HttpEntity<T1>(apiRq, headers);
		}
		
		final ApiRsInfo<T2> apiRsInfo = new ApiRsInfo<T2>();
		Exception exception = null;
		long startTime = System.currentTimeMillis();
		try {
			ResponseEntity<T2> responseEntity = restTemplate.exchange(apiInfo.getApiUrl(), httpMethod, entity, responseClass);
			apiRsInfo.setHttpStatus(responseEntity.getStatusCode());
			apiRsInfo.setResponse(responseEntity.getBody());
			
			// rs 4xx or 5xx
			if(!apiRsInfo.isSuccExecuteApi()) {
				// 4xx
				if(apiRsInfo.getHttpStatus().series() == HttpStatus.Series.CLIENT_ERROR) {
					throw new HttpClientErrorException(apiRsInfo.getHttpStatus());
				// 5xx
				} else if(apiRsInfo.getHttpStatus().series() == HttpStatus.Series.SERVER_ERROR) {
					throw new HttpServerErrorException(apiRsInfo.getHttpStatus());
				} else {
					throw new RestClientException("Unknown status code [" + apiRsInfo.getHttpStatus() + "]");
				}
			}
		} catch (ResourceAccessException re) {
			// 504 exception 으로 넘어와서 따로 처리
			if(StringUtils.contains(re.getMessage(), "timed out"))  {
				apiRsInfo.setHttpStatus(HttpStatus.GATEWAY_TIMEOUT);
			}
			exception = re;
		} catch (Exception ex) {
			exception = ex;
		} finally {
			try {
				apiInfo.setApiTime(System.currentTimeMillis() - startTime);
				afterProcess(apiInfo, apiRq, apiRsInfo, exception);
			} catch(Exception e) {
				log.error("{} api after processing failed. : {}", apiInfo != null ? apiInfo.getApiName() : "", e.toString());
			}
		}
		
		return apiRsInfo;
	}
	
	/**
	 * 알리미, 로그 저장 등
	 * 
	 * @param apiRq rq오브젝트 or String
	 * @param apiRs rs오브젝트 or String
	 */
	private <T1, T2> void afterProcess(ApiInfo apiInfo, T1 apiRq, ApiRsInfo<T2> apiRsInfo, Exception e) {
		if(e != null || apiInfo.isSaveApiLog()) {
			
		}
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
	
}
