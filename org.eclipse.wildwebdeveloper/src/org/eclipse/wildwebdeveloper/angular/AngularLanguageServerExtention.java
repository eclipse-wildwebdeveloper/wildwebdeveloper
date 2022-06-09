package org.eclipse.wildwebdeveloper.angular;

import java.util.Map;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;

public interface AngularLanguageServerExtention {

	@JsonNotification(value = "angular/projectLoadingStart")
	public void projectLoadingStart(Object object);
	
	@JsonNotification(value = "angular/projectLoadingFinish")
	public void projectLoadingFinish(Object object);
	
	@JsonNotification(value = "angular/projectLanguageService")
	public void projectLanguageService(Map<String,Object> data);

	@JsonNotification(value = "angular/suggestStrictMode")
	public void suggestStrictMode(Object o);

	@JsonNotification(value = "angular/suggestIvyLanguageServiceMode")
	public void suggestIvyLanguageServiceMode(Object o);
	
	@JsonNotification(value = "angular/NgccProgressEnd")
	public void ngccProgressEnd(Object o);
}
