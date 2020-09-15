package net.unit8.depict.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.unit8.depict.model.DepictingProject;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Listing dependencies.
 * 
 * @goal dependencies
 * @phase package
 * @requiresProject true
 * @requiresDependencyResolution test
 */
public class DependenciesMojo extends AbstractMojo {

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

    /**
     * Skip this goal
     *
     * @parameter expression="${depict.dependencies.skip}" default-value="false"
     */
    private boolean skip;

    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Output dependencies is skipped.");
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        FileOutputStream fos = null;
        try {
            DepictingProject depictingProject = new DepictingProject(project, projectBuilder, localRepository);
            fos = new FileOutputStream(outputFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, depictingProject);
            getLog().info("Output dependencies to " + outputFile);
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