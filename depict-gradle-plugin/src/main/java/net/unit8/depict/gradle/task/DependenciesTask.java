package net.unit8.depict.gradle.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.unit8.depict.model.DepictingProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.*;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.eclipse.aether.RepositorySystemSession;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

public class DependenciesTask extends DefaultTask {
    @OutputFile
    File outputFile;

    private static final File DEFAULT_OUTPUT_FILE = new File("depict.dependencies.json");
    private final ObjectMapper mapper = new ObjectMapper();

    @TaskAction
    public void dependencies() {
        URI localRepositoryURL = getProject().getRepositories().mavenLocal().getUrl();
        Provider<RegularFile> pom = getProject().getLayout().getBuildDirectory().file("publications/depict/pom-default.xml");
        FileOutputStream fos = null;
        try {
            // create a Plexus container
            ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
            ContainerConfiguration cc = new DefaultContainerConfiguration()
                    .setClassWorld(classWorld)
                    .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
                    .setAutoWiring(true)
                    .setJSR250Lifecycle(true)
                    .setName("maven");
            DefaultPlexusContainer container = new DefaultPlexusContainer(cc);
            container.setLookupRealm(null);
            LegacySupport legacySupport = container.lookup(LegacySupport.class);
            MavenExecutionRequest execRequest = new DefaultMavenExecutionRequest();
            execRequest.setSystemProperties(System.getProperties());
            RepositorySystemSession repositorySession = MavenRepositorySystemUtils.newSession();
            MavenSession session = new MavenSession(container, repositorySession, execRequest, null);
            legacySupport.setSession(session);
            MavenProjectBuilder projectBuilder = container.lookup(MavenProjectBuilder.class);
            ArtifactRepositoryLayout repositoryLayout = container.lookup(ArtifactRepositoryLayout.class, "default");
            RepositorySystem repositorySystem = container.lookup(RepositorySystem.class);
            ArtifactRepository localRepository = repositorySystem.createArtifactRepository("local", localRepositoryURL.toString(),
                    repositoryLayout,
                    new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE),
                    new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE));

            ProfileManager profileManager = new DefaultProfileManager(container);

            MavenProject project = projectBuilder.buildWithDependencies(pom.get().getAsFile(),
                    localRepository,
                    profileManager);
            DepictingProject depictingProject = new DepictingProject(project, projectBuilder, localRepository);
            if (outputFile == null) {
                outputFile = new File(getProject().getProjectDir(), DEFAULT_OUTPUT_FILE.getName());
            }
            fos = new FileOutputStream(outputFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, depictingProject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException closeEx) {
                    throw new RuntimeException(closeEx);
                }
            }
        }
    }

    public File getOutputFile() {
        if (outputFile == null) {
            return new File("depict.dependencies.json");
        } else {
            return outputFile;
        }
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
