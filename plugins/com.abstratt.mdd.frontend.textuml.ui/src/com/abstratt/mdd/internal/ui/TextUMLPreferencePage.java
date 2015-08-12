/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TextUMLPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button formatOnSaveCheckBox;
	private CheckboxTableViewer checkboxTableViewer;
	private Table table;
	private Map<String, String> options = new HashMap<String, String>();

	{
		options.put(TextUMLUIPlugin.SHOW_ATTR, "Show attributes");
		options.put(TextUMLUIPlugin.SHOW_OP, "Show operations");
		options.put(TextUMLUIPlugin.SHOW_DATATYPE, "Show datatypes");
		options.put(TextUMLUIPlugin.SHOW_DEPS, "Show dependencies");
		options.put(TextUMLUIPlugin.SHOW_ASSOCINCLASS, "Show assocs under class(experimental)");
	}

	public TextUMLPreferencePage() {
		super();
	}

	public TextUMLPreferencePage(String title) {
		super(title);
	}

	public TextUMLPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Renders a human readable representation of the meta model contributors.
	 */
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		Group editorOptions = new Group(parent, SWT.LEFT);
		GridLayout layout = new GridLayout();
		editorOptions.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		editorOptions.setLayoutData(data);
		editorOptions.setText("TextUML editor options");

		formatOnSaveCheckBox = new Button(editorOptions, SWT.CHECK);
		formatOnSaveCheckBox.setText("Auto format on save");

		new Label(editorOptions, SWT.NONE);
		final Label outlineLabel = new Label(editorOptions, SWT.NONE);
		outlineLabel.setText("Outline options:");
		checkboxTableViewer = CheckboxTableViewer.newCheckList(editorOptions, SWT.BORDER);
		checkboxTableViewer.setLabelProvider(new TableLabelProvider());
		checkboxTableViewer.setContentProvider(new ArrayContentProvider());
		table = checkboxTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setupData();

		boolean formatOnSaveSetting = TextUMLUIPlugin.getDefault().getPluginPreferences()
		        .getBoolean(TextUMLUIPlugin.FORMAT_ON_SAVE);
		formatOnSaveCheckBox.setSelection(formatOnSaveSetting);

		return editorOptions;
	}

	private void setupData() {
		checkboxTableViewer.setInput(options.values());
		checkboxTableViewer.setCheckedElements(findCheckedElements());
	}

	private Object[] findCheckedElements() {
		Preferences preferences = TextUMLUIPlugin.getDefault().getPluginPreferences();
		String rawString = preferences.getString(TextUMLUIPlugin.OPTIONS);
		List a = new ArrayList();
		if (isNotEmpty(rawString)) {
			String[] selected = rawString.split(",");
			for (String optId : selected) {
				a.add(options.get(optId));
			}
		}
		return a.toArray();
	}

	private boolean isNotEmpty(String rawString) {
		return rawString != null && !rawString.equals("");
	}

	@Override
	public boolean performOk() {
		Preferences preferences = TextUMLUIPlugin.getDefault().getPluginPreferences();
		boolean formatOnSaveSetting = formatOnSaveCheckBox.getSelection();
		preferences.setValue(TextUMLUIPlugin.FORMAT_ON_SAVE, formatOnSaveSetting);
		preferences.setValue(TextUMLUIPlugin.OPTIONS, createStoreString());
		TextUMLUIPlugin.getDefault().savePluginPreferences();
		return true;
	}

	@Override
	protected void performDefaults() {
		Preferences preferences = TextUMLUIPlugin.getDefault().getPluginPreferences();
		preferences.setToDefault(TextUMLUIPlugin.FORMAT_ON_SAVE);
		super.performDefaults();
	}

	@Override
	public void init(IWorkbench workbench) {
		//
	}

	private String createStoreString() {
		Object[] checkedElements = checkboxTableViewer.getCheckedElements();
		String result = "";
		for (int i = 0; i < checkedElements.length; i++) {
			Object object = checkedElements[i];
			if (object instanceof String && options.containsValue((String) object)) {
				String key = getKeyFromValue((String) object);
				result += key;
			}
			if (i < checkedElements.length - 1) {
				result += ",";
			}
		}
		return result;
	}

	public String getKeyFromValue(String value) {
		Set<String> ref = options.keySet();
		Iterator<String> it = ref.iterator();
		while (it.hasNext()) {
			String o = it.next();
			if (options.get(o).equals(value)) {
				return o;
			}
		}
		return null;
	}

}
