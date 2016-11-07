package com.abstratt.mdd.frontend.internal.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.uml2.uml.resource.UMLResource;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.core.ModelException;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.frontend.core.BasicProblemTracker;
import com.abstratt.mdd.frontend.core.FrontEnd;
import com.abstratt.mdd.frontend.core.ICompilationDirector;
import com.abstratt.mdd.frontend.core.InternalProblem;
import com.abstratt.mdd.frontend.core.LocationContext;
import com.abstratt.mdd.frontend.core.spi.AbortedCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.CompilationContext;
import com.abstratt.mdd.frontend.core.spi.ICompiler;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.ISourceAnalyzer;
import com.abstratt.pluginutils.ISharedContextRunnable;
import com.abstratt.pluginutils.LogUtils;
import com.sun.javafx.collections.IntegerArraySyncer;

public class CompilationDirector implements ICompilationDirector {

    static class FrontEndLanguage {
        private static FrontEndLanguage build(IConfigurationElement configEl) {
            FrontEndLanguage language = new FrontEndLanguage();
            language.name = configEl.getAttribute("name");
            language.fileExtension = configEl.getAttribute("file-extension");
            language.auxiliary = Boolean.parseBoolean(configEl.getAttribute("auxiliary"));
            language.configElement = configEl;
            return language;
        }

        private IConfigurationElement configElement;
        private String fileExtension;

        private String name;

        private boolean auxiliary;

        public ICompiler createCompiler() throws CoreException {
            return (ICompiler) configElement.createExecutableExtension("compiler");
        }

        public String getName() {
            return name;
        }

        public boolean isAuxiliary() {
            return auxiliary;
        }
    }

    private static CompilationDirector instance;

    public synchronized static CompilationDirector getInstance() {
        if (instance == null)
            new CompilationDirector();
        return instance;
    }

    private Map<String, FrontEndLanguage> languages;

    private CompilationDirector() {
        instance = this;
        initLanguages();
    }

    private void clean(IFileStore root, boolean deleteIfEmpty, IProgressMonitor monitor) throws CoreException {
        if (monitor.isCanceled())
            throw new OperationCanceledException();
        if (isUMLFile(root) && MDDUtil.isGenerated(root.toURI())) {
            safeDelete(root);
            return;
        }
        IFileStore[] children = root.childStores(EFS.NONE, null);
        for (int i = 0; i < children.length; i++)
            clean(children[i], false, monitor);
        if (deleteIfEmpty && root.childStores(EFS.NONE, null).length == 0)
            root.delete(EFS.NONE, null);
    }

    private boolean isUMLFile(IFileStore toCheck) {
        IFileInfo info = toCheck.fetchInfo();
        return info.exists() && !info.isDirectory() && toCheck.getName().endsWith('.' + UMLResource.FILE_EXTENSION);
    }

    /*
     * Resilient delete. Will retry a few times if necessary before giving up.
     * This should get rid of most issues with concurrent file access on
     * Windows.
     */
    private void safeDelete(IFileStore root) throws CoreException {
        for (int i = 0; i < 3; i++)
            try {
                root.delete(EFS.NONE, null);
                // worked!
                return;
            } catch (CoreException e) {
                try {
                    Thread.sleep(50 + 100 * i);
                } catch (InterruptedException e1) {
                    // nah
                }
            }
        root.delete(EFS.NONE, null);
    }

    /**
     * Deletes any output files that are stale.
     * 
     * @param context
     * @param monitor
     * @throws CoreException
     */
    private void clean(LocationContext context, IProgressMonitor monitor) throws CoreException {
        // simple implementation for now: just wipe the whole thing
        // ideally we would build a graph of dependencies by reading all sources
        // and
        // delete any output files that are old and any depending output files
        // (recursively).
        IFileStore[] outputPaths = context.getOutputPaths();
        monitor.beginTask("Cleaning output folders", outputPaths.length);
        try {
            for (int i = 0; i < outputPaths.length; i++) {
                clean(outputPaths[i], false, monitor);
                monitor.worked(1);
            }
        } finally {
            monitor.done();
        }
    }

    public synchronized IProblem[] compile(IFileStore[] toCompile, IRepository repository,
            final LocationContext context, final int mode, IProgressMonitor initialMonitor) throws CoreException {
        // Assert.isTrue(RepositoryService.isValidContext());
        boolean created = false;
        final IProgressMonitor monitor = initialMonitor == null ? new NullProgressMonitor() : initialMonitor;
        if (toCompile == null)
            toCompile = context.getSourcePaths();
        monitor.beginTask("Compiling source files", IProgressMonitor.UNKNOWN);
        // empty all output locations before we start
        clean(context, new SubProgressMonitor(monitor, toCompile.length));
        if (mode == CLEAN || toCompile.length == 0) {
            // nothing else to do
            return new IProblem[0];
        }
        if (repository == null) {
            IRepository actualRepository = repository = MDDCore.createRepository(MDDUtil.fromJavaToEMF(context.getDefaultOutputPath().toURI()));
            Optional<String> loadedPackages = Optional.ofNullable(repository.getProperties().getProperty(IRepository.LOADED_PACKAGES));
            loadedPackages
            	.ifPresent(it -> 
            		Arrays.stream(it.split(","))
            			.forEach(it2 -> 
            				actualRepository.loadPackage(MDDUtil.fromJavaToEMF(URI.create(it2)))
        				)
    			);
            for (IFileStore relatedRootPath : context.getRelatedPaths())
                for (IFileStore relatedEntry : relatedRootPath.childStores(EFS.NONE, null))
                    if (isUMLFile(relatedEntry))
                        repository.loadPackage(MDDUtil.fromJavaToEMF(relatedEntry.toURI()));
            created = true;
        }
        final BasicProblemTracker problemTracker = new BasicProblemTracker();
        final ReferenceTracker refTracker = new ReferenceTracker();
        final IFileStore[] tmpToCompile = toCompile;
        try {
            final IProgressMonitor[] tmpMonitor = { monitor };
            repository.buildRepository(new ISharedContextRunnable<IRepository, Object>() {
                @Override
                public Object runInContext(IRepository repository) {
                    try {
                        Map<IFileStore, ICompiler> auxiliaryUnits = new LinkedHashMap<IFileStore, ICompiler>();
                        for (int i = 0; i < tmpToCompile.length; i++) {
                            IFileStore baseOutputPath = context.getOutputPath(tmpToCompile[i]);
                            if (baseOutputPath == null)
                                baseOutputPath = context.getDefaultOutputPath();
                            int unitsInTree = compileTree(repository, tmpToCompile[i], baseOutputPath, mode,
                                    new SubProgressMonitor(tmpMonitor[0], IProgressMonitor.UNKNOWN), refTracker,
                                    problemTracker, auxiliaryUnits);
                            if (unitsInTree == 0)
                                problemTracker
                                        .add(new UnclassifiedProblem(IProblem.Severity.INFO, "Nothing to compile"));
                        }
                        refTracker.resolve(repository, problemTracker);
                        if (repository.getWeaver() != null)
                            repository.getWeaver().repositoryComplete(repository);
                        if (!problemTracker.hasProblems(Severity.ERROR)) {
                            Set<Entry<IFileStore, ICompiler>> entries = auxiliaryUnits.entrySet();
                            for (Entry<IFileStore, ICompiler> postponed : entries) {
                                compileUnit(repository, postponed.getKey(), mode, null, problemTracker, postponed
                                        .getValue(), new SubProgressMonitor(tmpMonitor[0], IProgressMonitor.UNKNOWN));
                            }
                        }
                    } catch (ModelException e) {
                        problemTracker.add(e.getProblem());
                    } catch (CoreException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            }, monitor);
        } catch (OperationCanceledException e) {
            // ignore
        } catch (AbortedCompilationException e) {
            // aborted
        } catch (Exception e) {
            problemTracker.add(new InternalProblem(e));
            LogUtils.logError(MDDCore.PLUGIN_ID, "Unexpected exception while compiling", e);
        } finally {
            monitor.done();
            if (created)
                repository.dispose();
        }
        IProblem[] allProblems = problemTracker.getAllProblems();
        Arrays.sort(allProblems);
        return allProblems;
    }

    private int compileTree(IRepository newRepo, IFileStore store, IFileStore baseDestination, int mode,
            IProgressMonitor monitor, IReferenceTracker refTracker, IProblemTracker problems,
            Map<IFileStore, ICompiler> auxiliaryUnits) throws CoreException {
        try {
            if (!store.fetchInfo().exists())
                return 0;
            if (!store.fetchInfo().isDirectory()) {
                FrontEndLanguage language = findLanguage(store,
                        newRepo.getProperties().getProperty(IRepository.DEFAULT_LANGUAGE));
                if (language == null)
                    return 0;
                ICompiler compiler = language.createCompiler();
                if (language.isAuxiliary()) {
                    auxiliaryUnits.put(store, compiler);
                } else {
                    compileUnit(newRepo, store, mode, refTracker, problems, compiler, new SubProgressMonitor(monitor,
                            IProgressMonitor.UNKNOWN));
                }
                return 1;
            }
            IFileStore[] children = store.childStores(EFS.NONE, null);
            // sort files so order of classes in the resulting model is
            // predictable
            Arrays.sort(children, new Comparator<IFileStore>() {
                @Override
                public int compare(IFileStore o1, IFileStore o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            int compiledUnits = 0;
            for (int i = 0; i < children.length; i++)
                compiledUnits += compileTree(newRepo, children[i], baseDestination, mode, new SubProgressMonitor(
                        monitor, IProgressMonitor.UNKNOWN), refTracker, problems, auxiliaryUnits);
            return compiledUnits;
        } finally {
            monitor.done();
        }
    }

    /**
     * Compiles a single source code unit.
     * 
     * @param newRepo
     * @param source
     * @param baseDestination
     * @param mode
     * @param deferred
     * @param problemTracker
     * @throws CoreException
     */
    private boolean compileUnit(IRepository newRepo, final IFileStore source, int mode, IReferenceTracker refTracker,
            final IProblemTracker problemTracker, ICompiler compiler, IProgressMonitor monitor) throws CoreException {
        if (monitor.isCanceled())
            throw new OperationCanceledException();
        if (!source.fetchInfo().exists())
            return false;
        Assert.isLegal(!source.fetchInfo().isDirectory());
        monitor.beginTask("Compiling " + source.toURI().getPath(), 1);
        LocalProblemTracker localProblemTracker = new LocalProblemTracker(problemTracker, source);
        Reader contents = null;
        try {
            contents = new InputStreamReader(new BufferedInputStream(source.openInputStream(EFS.NONE, null), 8192));
            compiler.compile(contents,
                    new CompilationContext(refTracker, localProblemTracker, newRepo, source.getName(),
                            (mode & DEBUG) != 0));
        } catch (AbortedScopeCompilationException e) {
            // continue with next source unit
        } catch (AbortedCompilationException e) {
            throw e;
        } catch (RuntimeException e) {
            LogUtils.logError(MDDCore.PLUGIN_ID, "Unexpected exception while compiling", e);
            Throwable root = e;
            while (root.getCause() != null)
                root = root.getCause();
            String message = root.toString();
            UnclassifiedProblem toReport = new UnclassifiedProblem("Unexpected error: " + message);
            localProblemTracker.add(toReport);
        } finally {
            if (contents != null)
                try {
                    contents.close();
                } catch (IOException e) {
                    // we don't care about these
                }
            monitor.done();
        }
        // we compiled this unit
        return true;
    }

    private FrontEndLanguage findLanguage(IFileStore source, String defaultLanguage) {
        String fullName = source.getName();
        if (languages.containsKey(fullName))
            // full-name matching is stronger
            return languages.get(fullName);
        String extension = new Path(fullName).getFileExtension();
        // a null or .mdd extension means we should use the default extension
        // (if available)
        if (extension == null || extension.equals(MDD_FILE_EXTENSION)) {
            if (defaultLanguage == null)
                return null;
            extension = defaultLanguage;
        }
        return languages.get(extension);
    }

    /**
     * Populates the language catalog from the extension registry.
     */
    private void initLanguages() {
        IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(FrontEnd.PLUGIN_ID, "frontEndLanguage");
        IConfigurationElement[] elements = point.getConfigurationElements();
        languages = new HashMap<String, FrontEndLanguage>();
        for (int i = 0; i < elements.length; i++) {
            FrontEndLanguage current = FrontEndLanguage.build(elements[i]);
            languages.put(current.fileExtension, current);
        }
    }

    @Override
    public String format(String extension, String toFormat) throws CoreException {
        FrontEndLanguage language = languages.get(extension);
        return language == null ? toFormat : language.createCompiler().format(toFormat);
    }

    @Override
    public List<ISourceAnalyzer.SourceElement> analyze(String extension, String toAnalyze) throws CoreException {
        FrontEndLanguage language = languages.get(extension);
        if (language != null) {
            ICompiler compiler = language.createCompiler();
            if (compiler instanceof ISourceAnalyzer)
                return ((ISourceAnalyzer) compiler).analyze(toAnalyze);
        }
        return null;
    }
}
