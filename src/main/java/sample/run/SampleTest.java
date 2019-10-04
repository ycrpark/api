package sample.run;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.google.gson.Gson;

import core.model.ApiInfo;
import core.service.ApiService;
import core.service.ParallelService;
import core.service.PolingApiService;
import sample.model.SampleRs;
import sample.service.ProcessService;

public class SampleTest {
	private static Logger log = LoggerFactory.getLogger(SampleTest.class);
	
	private static ApiService apiService = ApiService.getInstance();
	private static PolingApiService polingApiService = PolingApiService.getInstance();
	private static ParallelService parallelService = ParallelService.getInstance();
	
	private static ProcessService processService = ProcessService.getInstance();
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		HttpGet get = new HttpGet("https://httpbin.org/get?a=2&a=1&b=2");
		get.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		String str = polingApiService.sendApi(get);
		log.error(str);
		SampleRs ret = polingApiService.sendApi(get, SampleRs.class);
		log.error("{}", ret);
		
		HttpPost post = new HttpPost("https://httpbin.org/post");
		post.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
		SampleRs rq = new SampleRs();
		rq.setOrigin("gg");
		post.setEntity(new StringEntity(new Gson().toJson(rq)));
		ret = polingApiService.sendApi(post, SampleRs.class, 2500);
		log.error("{}", ret);
		
		ApiInfo apiInfo = new ApiInfo();
		
		ret = apiService.sendApiAndGetSuccRs(new ApiInfo("https://httpbin.org/get"), HttpMethod.GET, null, SampleRs.class);
		log.error("aa {}", ret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String string = apiService.sendApiAndGetSuccRs(new ApiInfo("https://httpbin.org/post"), HttpMethod.POST, headers, rq, String.class);
		log.error("zzzzz {}", string);
		ret = apiService.sendApiAndGetSuccRs(new ApiInfo("https://httpbin.org/post"), HttpMethod.POST, headers, rq, SampleRs.class);
		log.error("zzzzzzz {}", ret);
	}
	
}
