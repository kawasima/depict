package net.unit8.depict.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

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
class DependenciesMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter default-value="depict.dependencies.json" required=true
     */
    private String outputFile;

    public void execute() throws MojoExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        FileOutputStream fos = null;
        try {
            DepictingProject depictingProject = new DepictingProject(project);
            fos = new FileOutputStream(outputFile);
            mapper.writerWithDefaultPrettyPrinter().writeValue(fos, depictingProject);
        } catch (IOException e) {
            throw new MojoExecutionException("JSON serialization error", e);
        } catch (XmlPullParserException e) {
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