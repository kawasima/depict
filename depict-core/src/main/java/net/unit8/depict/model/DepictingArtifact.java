package net.unit8.depict.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;

import java.io.*;
import java.util.*;

public class DepictingArtifact implements Serializable {
    private static final long serialVersionUID = -5725431214678424712L;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String scope;
    private final boolean optional;
    private final boolean snapshot;
    private final MavenProject project;

    public DepictingArtifact(Artifact artifact, MavenProjectBuilder projectBuilder, ArtifactRepository localRepository, List remoteRepository) throws IOException, ProjectBuildingException {
        groupId = artifact.getGroupId();
        artifactId = artifact.getArtifactId();
        version = artifact.getVersion();
        type = artifact.getType();
        scope = artifact.getScope();
        optional = artifact.isOptional();
        snapshot = artifact.isSnapshot();
        project = projectBuilder.buildFromRepository(artifact, remoteRepository, localRepository);
    }

    public Map<String, DepictingDependency> getRequires() {
        if (project == null) return Collections.emptyMap();

        Map<String, DepictingDependency> depictingDependencies = new HashMap<String, DepictingDependency>();
        for (Dependency dep : (List<Dependency>) project.getDependencies()) {
            DepictingDependency dependency = new DepictingDependency(dep);
            depictingDependencies.put(dependency.getName(), dependency);
        }
        return depictingDependencies;
    }

    @JsonIgnore
    public String getName() {
        return ArtifactUtils.versionlessKey(groupId, artifactId);
    }

    @JsonIgnore
    public String getGroupId() {
        return groupId;
    }

    @JsonIgnore
    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version.toString();
    }

    public String getType() {
        return type;
    }

    public String getScope() {
        return scope;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isSnapshot() {
        return snapshot;
    }
}
