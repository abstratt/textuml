package com.abstratt.mdd.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;

import com.abstratt.pluginutils.ISharedContextRunnable;
import com.abstratt.pluginutils.LogUtils;
import com.abstratt.resman.Resource;
import com.abstratt.resman.ResourceException;
import com.abstratt.resman.ResourceManager;
import com.abstratt.resman.Task;

public class RepositoryService {

    static {
        // attempts to enable thread-local cache adapters first thing
        System.setProperty("org.eclipse.uml2.common.util.CacheAdapter.ThreadLocal", "true");
    }

    public final static Boolean ENABLED = CacheAdapterManager.isEnabled();
    /*
     * Maximum number of repository instances to create for an application (# of
     * requests served simultaneously).
     */
    public final static int CLONES;
    static {
        int value = 10;
        try {
            value = Integer.parseInt(System.getProperty("mdd.repositoryClones", "" + value));
        } catch (RuntimeException e) {
            LogUtils.logWarning(MDDCore.PLUGIN_ID, "Invalid value for mdd.repositoryClones, default applied", e);
        }
        CLONES = value;
    }
    public final static RepositoryService DEFAULT = ENABLED ? build() : null;

    private static RepositoryService build() {
        try {
            return new RepositoryService(new RepositoryProvider());
        } catch (RuntimeException e) {
            LogUtils.logError(MDDCore.PLUGIN_ID, "Could not build repository service", e);
            return null;
        }
    }

    private ResourceManager<RepositoryKey> resourceManager;

    public RepositoryService(RepositoryProvider repositoryProvider) {
        this.resourceManager = ResourceManager.create(4, repositoryProvider);
    }

    public <R, T extends Throwable> R runInRepository(URI repoURI, final ISharedContextRunnable<IRepository, R> runnable)
            throws CoreException {
        return resourceManager.runTask(RepositoryKey.from(repoURI), new Task<R>() {
            @Override
            public R run(Resource<?> resource) {
                return runnable.runInContext(getCurrentRepository());
            }
        });
    }

    public <R, T extends Throwable> R runTask(URI repoURI, final Task<R> task) throws ResourceException {
        return resourceManager.runTask(RepositoryKey.from(repoURI), task);
    }

    public void unregisterRepository(URI repoURI) {
        resourceManager.remove(RepositoryKey.from(repoURI));
    }

    public Resource<?> getCurrentResource() {
        return resourceManager.getCurrentResource();
    }

    public boolean isInSession() {
        return resourceManager.inTask();
    }

    public static boolean isValidContext() {
        return DEFAULT == null || DEFAULT.isInSession();
    }

    public IRepository getCurrentRepository() {
        return getCurrentResource().getFeature(IRepository.class);
    }

    public void synchronizeCurrent() {
        resourceManager.synchronizeCurrent();
    }

    public void clear() {
        resourceManager.clear();
    }

    public boolean isEmpty() {
        return resourceManager.isEmpty();
    }

    public <F> F getFeature(Class<F> featureClass) {
        return (F) getCurrentResource().getFeature(featureClass);
    }
}
