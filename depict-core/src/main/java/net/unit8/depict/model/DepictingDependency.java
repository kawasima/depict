package net.unit8.depict.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Dependency;

import java.io.Serializable;

/**
 * Requires
 */
public class DepictingDependency implements Serializable {
    private static final long serialVersionUID = -6666876811712047374L;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String scope;
    private final boolean optional;

    public DepictingDependency(Dependency dependency) {
        groupId = dependency.getGroupId();
        artifactId = dependency.getArtifactId();
        version = dependency.getVersion();
        type = dependency.getType();
        scope = dependency.getScope();
        optional = dependency.isOptional();
    }

    @JsonIgnore
    public String getGroupId() {
        return groupId;
    }

    @JsonIgnore
    public String getArtifactId() {
        return artifactId;
    }

    @JsonIgnore
    public String getName() {
        return ArtifactUtils.versionlessKey(groupId, artifactId);
    }

    public String getVersion() {
        return version;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getType() {
        return type;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getScope() {
        return scope;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public boolean isOptional() {
        return optional;
    }
}
