package net.unit8.depict.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Project
 */
public class DepictingProject implements Serializable {
    private String name;
    private String version;
    private List<DepictingArtifact> depictingArtifacts;

    public DepictingProject(MavenProject project) throws IOException, XmlPullParserException {
        name = ArtifactUtils.versionlessKey(project.getGroupId(), project.getArtifactId());
        version = project.getVersion();
        depictingArtifacts = new ArrayList<DepictingArtifact>();
        for (Artifact artifact: (Set<Artifact>)project.getArtifacts()) {
            depictingArtifacts.add(new DepictingArtifact(artifact));
        }
    }
    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, DepictingArtifact> getDependencies() {
        HashMap<String, DepictingArtifact> dependencies = new HashMap<String, DepictingArtifact>();
        for (DepictingArtifact artifact: depictingArtifacts) {
            dependencies.put(artifact.getName(), artifact);
        }
        return dependencies;
    }
}
