/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Victor Rubezhny (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.wildwebdeveloper.xml.internal.ui.preferences;

import java.io.File;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.wildwebdeveloper.xml.internal.Activator;

public class XMLPreferenceInitializer extends AbstractPreferenceInitializer {
	private static final IPreferenceStore STORE = Activator.getDefault().getPreferenceStore();
	public static final String XML_PREFERENCES_CATAGLOGS = "wildwebdeveloper.xml.catalogs";
	public static final Comparator<File> FILE_CASE_INSENSITIVE_ORDER = new FileComparator();
			
	private static class FileComparator implements Comparator<File> {

	    @Override
	    public int compare(File n1, File n2){
	    	return String.CASE_INSENSITIVE_ORDER.compare(n1.getAbsolutePath(), n2.getAbsolutePath());
	    }    

	}
	
	@Override
	public void initializeDefaultPreferences() {
		STORE.setDefault(XML_PREFERENCES_CATAGLOGS, "");
	}
	
	public static Set<File> getCatalogs(IPreferenceStore store) {
		Set<File> catalogs = new TreeSet<File>(FILE_CASE_INSENSITIVE_ORDER);

		for (String filepath : store.getString(XMLPreferenceInitializer.XML_PREFERENCES_CATAGLOGS).split(",")) {
			if (!filepath.isEmpty()) {
				catalogs.add(new File(filepath));
			}
		}
		
		return catalogs;
	}
	
	public static void storeCatalogs(IPreferenceStore store, Set<File> catalogs) {
		String catalogsStr = "";
		if (!catalogs.isEmpty()) {
			for (File f : catalogs) {
				catalogsStr += f.getAbsolutePath() + ",";
			}
		}
		store.setValue(XMLPreferenceInitializer.XML_PREFERENCES_CATAGLOGS, catalogsStr);
	}
}