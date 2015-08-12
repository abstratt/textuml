/*******************************************************************************
 * Copyright (c) 2006, 2009 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.internal.ui;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;

/**
 * A cache for repositories in the workspace.
 */
public class RepositoryCache {

	private static volatile long workspaceGeneration = 0;

	private static class LocalCache {
		private long localVersion;
		private Map<String, SoftReference<IRepository>> cachedReferences = new HashMap<String, SoftReference<IRepository>>();

		public IRepository getRepository(URI repositoryBaseURI) throws CoreException {
			if (localVersion != workspaceGeneration) {
				Collection<SoftReference<IRepository>> toDispose = new ArrayList<SoftReference<IRepository>>(
				        cachedReferences.values());
				cachedReferences.clear();
				localVersion = workspaceGeneration;
				// TODO this could be done in the background
				for (SoftReference<IRepository> current : toDispose) {
					IRepository repo = current.get();
					if (repo != null)
						repo.dispose();
				}
			}
			SoftReference<IRepository> reference = cachedReferences.get(repositoryBaseURI.toString());
			IRepository repository = reference == null ? null : reference.get();
			if (repository == null) {
				repository = MDDCore.createRepository(repositoryBaseURI);
				cachedReferences.put(repositoryBaseURI.toString(), new SoftReference<IRepository>(repository));
			}
			return repository;
		}
	}

	// a thread-safe cache of repositories
	private static final ThreadLocal<LocalCache> repositoryCache = new ThreadLocal<LocalCache>() {
		protected LocalCache initialValue() {
			return new LocalCache();
		}
	};

	private static RepositoryCache instance = new RepositoryCache();

	public static RepositoryCache getInstance() {
		return instance;
	}

	public void start() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {

			public void resourceChanged(IResourceChangeEvent event) {
				workspaceGeneration++;
			}
		});
	}

	public void stop() {

	}

	/**
	 * Returns a thread-local shared copy of a repository.
	 * 
	 * @param projectRepositoryURI
	 * @return a shared copy of a repository
	 * @throws CoreException
	 */
	public IRepository getRepository(URI projectRepositoryURI) throws CoreException {
		return repositoryCache.get().getRepository(projectRepositoryURI);
	}
}
