package net.unit8.depict.model;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Project
 */
public class DepictingProject implements Serializable {
    private static final long serialVersionUID = 8255692331693057698L;
    private String name;
    private String version;
    private List<DepictingArtifact> depictingArtifacts;

    public DepictingProject(MavenProject project, MavenProjectBuilder projectBuilder, ArtifactRepository localRepository) throws IOException, ProjectBuildingException {
        List remoteRepository = project.getRemoteArtifactRepositories();

        name = ArtifactUtils.versionlessKey(project.getGroupId(), project.getArtifactId());
        version = project.getVersion();
        depictingArtifacts = new ArrayList<DepictingArtifact>();
        for (Artifact artifact: (Set<Artifact>)project.getArtifacts()) {
            depictingArtifacts.add(new DepictingArtifact(artifact, projectBuilder, localRepository, remoteRepository));
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
