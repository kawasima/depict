package net.unit8.depict.mojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.*;

public class DepictingArtifact implements Serializable {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String scope;
    private final boolean optional;
    private final boolean snapshot;
    private List<DepictingDependency> dependencies;

    private Model model = null;

    public DepictingArtifact(Artifact artifact) throws IOException, XmlPullParserException {
        groupId = artifact.getGroupId();
        artifactId = artifact.getArtifactId();
        version = artifact.getVersion();
        type = artifact.getType();
        scope = artifact.getScope();
        optional = artifact.isOptional();
        snapshot = artifact.isSnapshot();

        // TODO refers workspace
        File pom = new File(artifact.getFile().getParent(), artifact.getFile().getName().replaceAll("\\.\\w+$", ".pom"));
        if (pom.exists()) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            InputStream in = null;
            try {
                in = new FileInputStream(pom);
                model = pomReader.read(in);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    public Map<String, DepictingDependency> getRequires() {
        if (model == null) return Collections.emptyMap();

        Map<String, DepictingDependency> depictingDependencies = new HashMap<String, DepictingDependency>();
        for (Dependency dep : (List<Dependency>)model.getDependencies()) {
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
