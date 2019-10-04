package core.model;

/**
 * api 발송에 필요한 정보
 * 
 * <ul>
 * 전송 이전 세팅 옵션
 * <li>saveApiLog 로그 저장 여부</li>
 * <li>timeoutMillis 타임아웃 직접 지정</li>
 * </ul>
 * 
 * <ul>
 * 전송 이후 세팅 됨
 * <li>apiTime 소요된 시간</li>
 * </ul>
 */
public class ApiInfo {
	/**
	 * 추가 옵션 정보
	 */
	private boolean saveApiLog;
	private Integer timeoutMillis;
	// 소요 시간
	private long apiTime;
	
	/**
	 * api 인터페이스 정보
	 */
	private String apiId;
	private String apiName;
	private String apiUrl;
	
	public ApiInfo() {
	}
	public ApiInfo(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public String getApiId() {
		return apiId;
	}
	
	public void setApiId(String apiId) {
		this.apiId = apiId;
	}
	
	public String getApiName() {
		return apiName;
	}
	
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}
	
	public String getApiUrl() {
		return apiUrl;
	}
	
	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
	
	public boolean isSaveApiLog() {
		return saveApiLog;
	}
	
	public void setSaveApiLog(boolean saveApiLog) {
		this.saveApiLog = saveApiLog;
	}
	
	public long getApiTime() {
		return apiTime;
	}
	
	public void setApiTime(long apiTime) {
		this.apiTime = apiTime;
	}
	
	public Integer getTimeoutMillis() {
		return timeoutMillis;
	}
	
	public void setTimeoutMillis(Integer timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}
	
}
