package org.eclipse.wildwebdeveloper.json.schema;

import java.util.List;
import java.util.Map;

public class Schema {

	private String name;
	private String description;
	private List<String> fileMatch;
	private String url;
	private Map<String, String> versions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getFileMatch() {
		return fileMatch;
	}

	public void setFileMatch(List<String> fileMatch) {
		this.fileMatch = fileMatch;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getVersions() {
		return versions;
	}

	public void setVersions(Map<String, String> versions) {
		this.versions = versions;
	}

}
