package net.unit8.depict.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Listing dependencies.
 * 
 * @goal dependencies
 * @phase package
 * @requiresProject true
 * @requiresDependencyResolution test
 */
class DependenciesMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @component
     */
    private MavenProjectBuilder projectBuilder;

    /**
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter default-value="depict.dependencies.json" required=true
     */
    private String outputFile;

    public void execute() throws MojoExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        FileOutputStream fos = null;
        try {
            DepictingProject depictingProject = new DepictingProject(project, projectBuilder, localRepository);
            fos = new FileOutputStream(outputFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, depictingProject);
        } catch (IOException e) {
            throw new MojoExecutionException("JSON serialization error", e);
        } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("POM file error", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException closeEx) {
                    throw new MojoExecutionException("File close error", closeEx);
                }
            }
        }
    }
}