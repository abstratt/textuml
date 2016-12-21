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
package com.abstratt.mdd.internal.ui.editors.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.textuml.renderer.TextUMLRenderer;
import com.abstratt.mdd.internal.ui.editors.TextUMLEditor;
import com.abstratt.pluginutils.LogUtils;

public class TextUMLViewer extends SourceEditor {

    private static class InMemoryStorage implements IStorage {
        private InputStream contents;
        private IStorage storage;

        public InMemoryStorage(InputStream contents, IStorage storage) {
            this.contents = contents;
            this.storage = storage;
        }

        public InputStream getContents() throws CoreException {
            return contents;
        }

        public IPath getFullPath() {
            return storage.getFullPath();
        }

        public String getName() {
            return storage.getName();
        }

        public boolean isReadOnly() {
            return true;
        }

        public Object getAdapter(Class adapter) {
            return storage.getAdapter(adapter);
        }
    }

    private static class InMemoryStorageEditorInput implements IStorageEditorInput {

        private IFileEditorInput wrappedEditorInput;

        private IStorage inMemoryStorage;

        public IStorage getStorage() throws CoreException {
            return inMemoryStorage;
        }

        public boolean exists() {
            return false;
        }

        public ImageDescriptor getImageDescriptor() {
            return wrappedEditorInput.getImageDescriptor();
        }

        public String getName() {
            return inMemoryStorage.getName();
        }

        public IPersistableElement getPersistable() {
            return wrappedEditorInput.getPersistable();
        }

        public String getToolTipText() {
            return wrappedEditorInput.getToolTipText();
        }

        public Object getAdapter(Class adapter) {
            return wrappedEditorInput.getAdapter(adapter);
        }

        public InMemoryStorageEditorInput(IFileEditorInput wrapped, InputStream inoutStream) throws CoreException {
            this.wrappedEditorInput = wrapped;
            this.inMemoryStorage = new InMemoryStorage(inoutStream, wrapped.getStorage());
        }

    }
    
    private class LoadJob extends Job {
		public LoadJob() {
			super("Rendering UML model");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			safeDoSetInput(originalInput);
			return Status.OK_STATUS;
		}
    	
    }
    
    private LoadJob loadJob = new LoadJob();
	private IEditorInput originalInput;

    @Override
    protected void doSetInput(IEditorInput input) throws CoreException {
    	this.originalInput = input;
    	loadJob.schedule();
    }

	private void safeDoSetInput(IEditorInput input) {
		if (input instanceof InMemoryStorageEditorInput)
			input = ((InMemoryStorageEditorInput) input).wrappedEditorInput;
		if (!(input instanceof IFileEditorInput))
            return;
        FileEditorInput storageInput = (FileEditorInput) input;
        registerChangeListener(storageInput.getFile());
        java.net.URI editorInputURI = storageInput.getURI();
        final ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = resourceSet.createResource(URI.createURI(editorInputURI.toASCIIString()));
        InputStream stream = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        try {
            stream = new URL(editorInputURI.toASCIIString()).openStream();
            Map<String, Object> options = new HashMap<String, Object>();
            options.put(XMLResource.OPTION_RECORD_UNKNOWN_FEATURE, Boolean.TRUE);
            options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, XMLResource.OPTION_PROCESS_DANGLING_HREF_DISCARD);
            options.put(XMLResource.OPTION_EXTENDED_META_DATA, new BasicExtendedMetaData());
            resource.load(stream, options);
            TextUMLRenderer renderer = new TextUMLRenderer();
            renderer.render(resource, baos);
        } catch (FileNotFoundException e) {
        	new PrintStream(baos).println("No file found at: " + editorInputURI);
        } catch (IOException e) {
        	e.printStackTrace(new PrintStream(baos));
            LogUtils.logError(TextUMLEditor.PLUGIN_ID, "Error loading contents", e);
        } finally {
            try {
                if (stream != null)
                    stream.close();
            } catch (IOException e) {
                // so what...
            }
            MDDUtil.unloadResources(resourceSet);
        }
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Display.getDefault().asyncExec(() -> { 
			try {
				super.doSetInput(new InMemoryStorageEditorInput(storageInput, bais));
			} catch (CoreException e) {
	            LogUtils.logError(TextUMLEditor.PLUGIN_ID, "Error loading contents", e);
			}
		});
	}

    private void registerChangeListener(IFile modelFile) {
		modelFile.getWorkspace().addResourceChangeListener((IResourceChangeEvent event) -> {
	        if (event.getDelta() == null)
	            return;
	        IResourceDelta interestingChange = event.getDelta().findMember(modelFile.getFullPath());
	        if (interestingChange != null)
	        	loadJob.schedule();
		}, IResourceChangeEvent.POST_CHANGE);
	}

    @Override
    protected void createActions() {
        // disable any actions
    }
    
}
