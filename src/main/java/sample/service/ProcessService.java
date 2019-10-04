package sample.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.service.ApiService;

public class ProcessService {
	private Logger log = LoggerFactory.getLogger(ProcessService.class);
	
	private static ApiService apiService = ApiService.getInstance();
	
	private ProcessService() {
	};
	
	private static class Loader {
		public static final ProcessService INSTANCE = new ProcessService();
	}
	
	public static ProcessService getInstance() {
		return Loader.INSTANCE;
	}
	
}
