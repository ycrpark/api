package core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelService {
	private Logger log = LoggerFactory.getLogger(ParallelService.class);
	
	private ParallelService() {
	};
	
	private static class Loader {
		public static final ParallelService INSTANCE = new ParallelService();
	}
	
	public static ParallelService getInstance() {
		return Loader.INSTANCE;
	}
	
	public void run(Runnable runnable, int count) {
		// TODO
		for(int i = 0; i < count; i++) {
			new Thread(runnable).start();
		}
	}
	
}
