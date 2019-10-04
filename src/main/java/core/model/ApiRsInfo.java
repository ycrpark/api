package core.model;

import org.springframework.http.HttpStatus;

public class ApiRsInfo<T> {
	private T response;
	private HttpStatus httpStatus;
	
	
	public T getResponse() {
		return response;
	}
	public void setResponse(T response) {
		this.response = response;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	
	public boolean isSuccExecuteApi() {
		if(this.response != null && this.httpStatus != null) {
			if(this.httpStatus.series() != HttpStatus.Series.CLIENT_ERROR && this.httpStatus.series() != HttpStatus.Series.SERVER_ERROR) {
				return true;
			}
		}
		
		return false;
	}
	
	public Integer getHttpStatusCode() {
		return httpStatus != null ? httpStatus.value() : null;
	}
	
	public String getHttpStatusMsg() {
		return httpStatus != null ? httpStatus.value() + " " + httpStatus.getReasonPhrase() : null;
	}
}
