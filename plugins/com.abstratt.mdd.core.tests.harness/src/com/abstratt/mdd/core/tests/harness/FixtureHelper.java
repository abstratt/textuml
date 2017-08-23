package com.abstratt.mdd.core.tests.harness;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.Package;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.ICompilationDirector;
import com.abstratt.mdd.frontend.core.LocationContext;
import com.abstratt.mdd.frontend.internal.core.CompilationDirector;

public class FixtureHelper {

    private String extension;

    public FixtureHelper(String extension) {
        this.extension = extension;
    }

    public FixtureHelper() {
        this.extension = ICompilationDirector.MDD_FILE_EXTENSION;
    }

    public static void assertCompilationSuccessful(IProblem[] result) {
        boolean anyErrors = Arrays.stream(result).anyMatch(it -> it.getSeverity() == Severity.ERROR);
        assertTrue(result, !anyErrors);
    }

    public static void assertTrue(IProblem[] result, boolean condition) {
        if (condition)
            return;
        Assert.fail(joinMessages(result));
    }

    public static String joinMessages(IProblem[] result) {
        StringBuffer errorMessages = new StringBuffer();
        if (result.length > 0) {
            for (int i = 0; i < result.length; i++) {
                errorMessages.append(result[i]);
                errorMessages.append(", ");
            }
            errorMessages.delete(errorMessages.length() - 2, errorMessages.length());
        } else
            errorMessages.append("No problems found");
        return errorMessages.toString();
    }

    public static void dumpModelSet(IRepository repository) {
        Package[] topLevel = repository.getTopLevelPackages(null);
        for (Package package1 : topLevel) {
            System.out.println(package1.eResource().getURI());
        }
    }

    public IProblem[] parse(IFileStore baseDir, IRepository repository, String... sources) throws CoreException {
        Map<String, String> sourcesByPaths = new LinkedHashMap<String, String>();
        for (int i = 0; i < sources.length; i++)
            sourcesByPaths.put("foo" + i + '.' + extension, sources[i]);
        return parseFiles(baseDir, repository, sourcesByPaths);
    }

    /**
     * Compiles the given sources into the given repository.
     * 
     * @param repository
     * @param sources
     * @return
     * @throws CoreException
     */
    public IProblem[] parseFiles(IFileStore baseDir, IRepository repository, Map<String, String> sourcesByPaths)
            throws CoreException {
        IFileStore project = baseDir;
        IFileStore sourceRoot = project;
        IFileStore defaultOutputDir = project;
        List<IFileStore> sourceFiles = createProject(sourceRoot, sourcesByPaths);
        ICompilationDirector director = CompilationDirector.getInstance();
        LocationContext context = new LocationContext(defaultOutputDir);
        long start = System.currentTimeMillis();
        IProblem[] result = director.compile(sourceFiles.toArray(new IFileStore[0]), repository, context,
                ICompilationDirector.FULL_BUILD | ICompilationDirector.DEBUG, null);
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms - " + baseDir);
        return result;
    }

    public List<IFileStore> createProject(IFileStore sourceRoot, Map<String, String> sourcesByPaths)
            throws CoreException {
        List<IFileStore> sourceFiles = new ArrayList<IFileStore>();
        for (Entry<String, String> entry : sourcesByPaths.entrySet()) {
            IFileStore newFile = sourceRoot.getChild(entry.getKey());
            sourceFiles.add(newFile);
            newFile.getParent().mkdir(EFS.NONE, null);
            final OutputStream stream = new BufferedOutputStream(newFile.openOutputStream(EFS.NONE, null), 8192);
            try {
                stream.write(entry.getValue().getBytes());
            } catch (IOException e) {
                // should never happen, we are parsing in-memory stuff
                Assert.fail(e.toString());
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    // not interested
                }
            }
        }
        return sourceFiles;
    }

    public String getExtension() {
        return extension;
    }
}
