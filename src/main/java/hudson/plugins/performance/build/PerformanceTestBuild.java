package hudson.plugins.performance.build;

import hudson.Extension;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.plugins.performance.Messages;
import hudson.tasks.BatchFile;
import hudson.tasks.BuildStep;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * "Build step" for running performance test
 */
public class PerformanceTestBuild extends Builder implements BuildStep {

    protected final static String CHECK_COMMAND = "bzt --help";
    protected final static String PERFORMANCE_TEST_COMMAND = "bzt";
    protected final static String DEFAULT_CONFIG_FILE = "defaultReport.yml";

    @Symbol("performanceTest")
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.PerformanceTest_Name();
        }
    }


    private String testConfigurationFiles;
    private String testOptions;

    @DataBoundConstructor
    public PerformanceTestBuild(String testConfigurationFiles, String testOptions) throws IOException {
        this.testConfigurationFiles = (testConfigurationFiles == null) ? StringUtils.EMPTY : testConfigurationFiles;
        this.testOptions = (testOptions == null) ? StringUtils.EMPTY : testOptions;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        boolean isBZTInstall = new Shell(CHECK_COMMAND).perform(build, launcher, listener); // TODO: off help output
        if (!isBZTInstall) {
            return false;
        }

        String bztExecution = PERFORMANCE_TEST_COMMAND + ' ' +
                extractDefaultReportToWorkspace(build.getWorkspace()) + " " +
                testConfigurationFiles + " " +
                testOptions;

        Builder builder = Functions.isWindows() ? new BatchFile(bztExecution) : new Shell(bztExecution);
        return builder.perform(build, launcher, listener);
        // TODO: add post build action
    }


    protected String extractDefaultReportToWorkspace(FilePath workspace) throws IOException, InterruptedException {
        FilePath defaultConfig = workspace.child(DEFAULT_CONFIG_FILE);
        defaultConfig.copyFrom(getClass().getResourceAsStream(DEFAULT_CONFIG_FILE));
        return defaultConfig.getRemote();
    }

    public String getTestConfigurationFiles() {
        return testConfigurationFiles;
    }

    public String getTestOptions() {
        return testOptions;
    }

    @DataBoundSetter
    public void setTestConfigurationFiles(String testConfigurationFiles) {
        this.testConfigurationFiles = testConfigurationFiles;
    }

    @DataBoundSetter
    public void setTestOptions(String testOptions) {
        this.testOptions = testOptions;
    }
}
