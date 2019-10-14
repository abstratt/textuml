package com.abstratt.mdd.core.tests.harness;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.RepositoryService;

public abstract class AbstractRepositoryBuildingTests extends AbstractRepositoryTests {
    protected boolean dump;
    protected final FixtureHelper fixtureHelper = new FixtureHelper(getExtension());

    public AbstractRepositoryBuildingTests(String name) {
        super(name);
    }

    public IProblem[] compile(String... sources) throws CoreException {
        return compile(fixtureHelper, sources);
    }

    public IProblem[] compile(FixtureHelper fixtureHelper, String... sources) throws CoreException {
        return fixtureHelper.parse(getRepositoryDir(), null, sources);
    }

    @Override
    protected void runTest() throws Throwable {
        final Throwable[] collected = { null };
        runInContext(new Runnable() {
            public void run() {
                try {
                    originalRunTest();
                } catch (Throwable e) {
                    collected[0] = e;
                }
            }
        });
        if (collected[0] != null)
            throw collected[0];
    }

    public IProblem[] parseAndCheck(String... sources) throws CoreException {
        IProblem[] results = parse(sources);
        FixtureHelper.assertCompilationSuccessful(results);
        return results;
    }

    protected void parseAndCheckInContext(final String... toBuild) throws Exception {
        runInContext(new Runnable() {
            @Override
            public void run() {
                try {
                    parseAndCheck(toBuild);
                } catch (CoreException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public IProblem[] parse(String... sources) throws CoreException {
        IProblem[] parseResults = fixtureHelper.parse(getRepositoryDir(), null, sources);
        if (parseResults.length == 0)
            compilationCompleted();
        return parseResults;
    }

    protected <T extends IProblem> T assertExpectedProblem(Class<T> expected, IProblem[] actual) {
        assertTrue(Arrays.asList(actual).toString(), actual.length >= 1);
        for (IProblem p : actual) {
            if (expected.isInstance(p))
                return (T) p;
        }
        fail(Arrays.asList(actual).toString());
        return null;
    }

    public void setUp() throws Exception {
        RepositoryService.DEFAULT.clear();
        Assert.assertTrue(RepositoryService.DEFAULT.isEmpty());
        if (Boolean.getBoolean("debug.memory")) {
            System.gc();
            System.out.println(getName() + " - before: " + memory());
        }
        clearLocation(getBaseDir());
        Assert.assertFalse(getBaseDir().fetchInfo().exists());
        baseDir.mkdir(EFS.NONE, null);
        Properties settings = createDefaultSettings();
        saveSettings(getRepositoryDir(), settings);
    }

    protected void tearDown() throws Exception {
        if (dump)
            FixtureHelper.dumpModelSet(getRepository());
        super.tearDown();
        RepositoryService.DEFAULT.unregisterRepository(getRepositoryURI());
        clearLocation(getBaseDir());
        if (Boolean.getBoolean("debug")) {
            System.gc();
            System.out.println(getName() + " - after: " + memory());
        }
    }

    protected void clearLocation(IFileStore toDelete) {
        for (int tries = 0; tries < 5; tries++) {
            try {
                toDelete.delete(EFS.NONE, null);
                break;
            } catch (CoreException e) {
                // sometimes delete fails for no apparent reason (maybe other
                // threads?), but if we try again after some wait, it just
                // works...
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    // break;
                }
                if (Boolean.getBoolean("debug"))
                    System.out.println("Failed deleting " + toDelete.toURI() + "(attempt #" + (tries + 1) + ")");
            }
        }
    }

    protected void compilationCompleted() throws CoreException {
        // do nothing
    }
}
