package org.eclipse.wildwebdeveloper.json.schema;

import java.util.ArrayList;
import java.util.List;

public class Catalog {

	private String $schema;
	private int version;
	private List<Schema> schemas = new ArrayList<>();

	public String get$schema() {
		return $schema;
	}

	public void set$schema(String $schema) {
		this.$schema = $schema;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public List<Schema> getSchemas() {
		return schemas;
	}

	public void setSchemas(List<Schema> schemas) {
		this.schemas = schemas;
	}

}
