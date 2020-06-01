package net.unit8.depict.mojo;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
/**
 * Listing dependencies.
 * 
 * @goal export
 * @requiresDependencyResolution test
 */
class ExportMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;


    public void execute() throws MojoExecutionException, MojoFailureException {
        ObjectMapper mapper = new ObjectMapper();

        getLog().info(project.getArtifacts().toString());
        try {
            for (Artifact artifact : (Set<Artifact>) project.getArtifacts()) {
                List dependencyTrail = artifact.getDependencyTrail();
                getLog().info(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(artifact));
            }
        } catch (IOException ex) {
            throw new MojoExecutionException("JSON serialization error", ex);
        }
    }

}