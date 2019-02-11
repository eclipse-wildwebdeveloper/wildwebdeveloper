package org.eclipse.wildwebdeveloper.json.schema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.google.gson.Gson;

public class SchemaStore {

	public static final String CATALOG_URL = "http://schemastore.org/api/json/catalog.json";

	public static Catalog loadCatalog() {

		Catalog catalog = null;

		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(new URL(CATALOG_URL).openStream(), StandardCharsets.UTF_8))) {
			String content = buffer.lines().collect(Collectors.joining());
			catalog = new Gson().fromJson(content, Catalog.class);
		} catch (IOException e1) {
			Display.getDefault().asyncExec(() -> {
				MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Can't load schemastore",
						"Can't load schemastore catalog from " + CATALOG_URL + " url");
			});
		}

		return catalog;
	}

	public static Map<String, List<String>> getSchemaAssociations() {
		Catalog catalog = loadCatalog();

		Map<String, List<String>> schemaAssociations = new HashMap<>();

		if (Objects.nonNull(catalog)) {
			schemaAssociations = catalog.getSchemas().stream()
					.filter(s -> s.getFileMatch() != null && s.getUrl() != null)
					.flatMap(it -> it.getFileMatch().stream().map(match -> new Tuple<>(match, it.getUrl())))
					.collect(Collectors.groupingBy(t -> t.t1, Collectors.mapping(Tuple::getT2, Collectors.toList())));
		}

		return schemaAssociations;
	}
}
