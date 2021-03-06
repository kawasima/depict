package net.unit8.depict.gradle;

import groovy.util.Node;
import net.unit8.depict.gradle.task.DependenciesTask;
import org.gradle.api.*;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.UrlArtifactRepository;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.internal.artifacts.Module;
import org.gradle.api.internal.artifacts.configurations.DependencyMetaDataProvider;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.internal.versionmapping.DefaultVersionMappingStrategy;
import org.gradle.api.publish.internal.versionmapping.VersionMappingStrategyInternal;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.internal.artifact.MavenArtifactNotationParserFactory;
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication;
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal;
import org.gradle.api.publish.maven.internal.publication.WritableMavenProjectIdentity;
import org.gradle.api.publish.maven.internal.publisher.MutableMavenProjectIdentity;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.GenerateMavenPom;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.typeconversion.NotationParser;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Gradle plugin for Depict.
 *
 * @author kawasima
 */
public class DepictPlugin implements Plugin<Project> {
    private final InstantiatorFactory instantiatorFactory;
    private final ObjectFactory objectFactory;
    private final DependencyMetaDataProvider dependencyMetaDataProvider;
    private final FileResolver fileResolver;
    private final ImmutableAttributesFactory immutableAttributesFactory;
    private final ProviderFactory providerFactory;

    @Inject
    public DepictPlugin(InstantiatorFactory instantiatorFactory,
                        ObjectFactory objectFactory,
                        DependencyMetaDataProvider dependencyMetaDataProvider,
                        FileResolver fileResolver,
                        ImmutableAttributesFactory immutableAttributesFactory,
                        ProviderFactory providerFactory) {
        this.instantiatorFactory = instantiatorFactory;
        this.objectFactory = objectFactory;
        this.dependencyMetaDataProvider = dependencyMetaDataProvider;
        this.fileResolver = fileResolver;
        this.immutableAttributesFactory = immutableAttributesFactory;
        this.providerFactory = providerFactory;

    }

    @Override
    public void apply(final Project project) {
        final MavenPublicationFactory mavenPublicationFactory = new MavenPublicationFactory(
                dependencyMetaDataProvider,
                instantiatorFactory.decorateLenient(),
                fileResolver,
                project.getPluginManager(),
                project.getExtensions());
        // Gradle maven publication plugin
        final MavenPublication publication = mavenPublicationFactory.create("depict");
        project.getPlugins().withType(MavenPublishPlugin.class, new Action<MavenPublishPlugin>() {
            @Override
            public void execute(MavenPublishPlugin mavenPublishPlugin) {
                project.getExtensions().configure(PublishingExtension.class, new Action<PublishingExtension>() {
                    @Override
                    public void execute(PublishingExtension extension) {
                        extension.getPublications().add(publication);
                    }
                });
            }
        });

        final DirectoryProperty buildDir = project.getLayout().getBuildDirectory();
        TaskProvider<GenerateMavenPom> generatorTask = project.getTasks().register("generateMavenPomForDepict", GenerateMavenPom.class, new Action<GenerateMavenPom>() {
            @Override
            public void execute(final GenerateMavenPom generatePomTask) {
                generatePomTask.setDescription("Generates the Maven POM file for publication 'depict'.");
                publication.from(project.getComponents().getByName("java"));
                if (publication.getGroupId() == null || publication.getGroupId().isEmpty()) {
                    publication.setGroupId("net.unit8.depict");
                }
                MavenPom pom = publication.getPom();

                final List<URI> repositories = new ArrayList<URI>();
                project.getRepositories().all(new Action<ArtifactRepository>() {
                    @Override
                    public void execute(ArtifactRepository repository) {
                        if (repository instanceof UrlArtifactRepository) {
                            repositories.add(((UrlArtifactRepository) repository).getUrl());
                        }
                    }
                });
                pom.withXml(new Action<XmlProvider>() {
                    @Override
                    public void execute(XmlProvider xmlProvider) {
                        Node repositoriesNode = xmlProvider.asNode().appendNode("repositories");
                        int repoNo = 0;
                        for (URI uri : repositories) {
                            Node repositoryNode = repositoriesNode.appendNode("repository");
                            repositoryNode.appendNode("id", "repo-" + repoNo);
                            repositoryNode.appendNode("url", uri.toASCIIString());
                            repoNo++;
                        }
                    }
                });
                generatePomTask.setPom(pom);
                if (generatePomTask.getDestination() == null) {
                    generatePomTask.setDestination(buildDir.file("publications/" + publication.getName() + "/pom-default.xml"));
                }
                project.getPluginManager().withPlugin("org.gradle.java", new Action<AppliedPlugin>() {
                    @Override
                    public void execute(AppliedPlugin plugin) {
                        // If the Java plugin is applied, we want to express that the "compile" and "runtime" variants
                        // are mapped to some attributes, which can be used in the version mapping strategy.
                        // This is only required for POM publication, because the variants have _implicit_ attributes that we want explicit for matching
                        generatePomTask.withCompileScopeAttributes(immutableAttributesFactory.of(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_API)))
                                .withRuntimeScopeAttributes(immutableAttributesFactory.of(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME)));
                    }
                });
            }
        });
        ((MavenPublicationInternal) publication).setPomGenerator(generatorTask);

        project.getTasks().register("depictDependencies", DependenciesTask.class, new Action<DependenciesTask>() {
            @Override
            public void execute(DependenciesTask task) {
                task.dependsOn("generateMavenPomForDepict");
            }
        });
    }

    private class MavenPublicationFactory implements NamedDomainObjectFactory<MavenPublication> {
        private final Instantiator instantiator;
        private final DependencyMetaDataProvider dependencyMetaDataProvider;
        private final FileResolver fileResolver;
        private final PluginManager plugins;
        private final ExtensionContainer extensionContainer;

        private MavenPublicationFactory(DependencyMetaDataProvider dependencyMetaDataProvider,
                                        Instantiator instantiator,
                                        FileResolver fileResolver,
                                        PluginManager plugins,
                                        ExtensionContainer extensionContainer) {
            this.dependencyMetaDataProvider = dependencyMetaDataProvider;
            this.instantiator = instantiator;
            this.fileResolver = fileResolver;
            this.plugins = plugins;
            this.extensionContainer = extensionContainer;
        }

        @Override
        public MavenPublication create(final String name) {
            MutableMavenProjectIdentity projectIdentity = createProjectIdentity();
            NotationParser<Object, MavenArtifact> artifactNotationParser = new MavenArtifactNotationParserFactory(instantiator, fileResolver).create();
            VersionMappingStrategyInternal versionMappingStrategy = objectFactory.newInstance(DefaultVersionMappingStrategy.class);
            configureDefaultConfigurationsUsedWhenMappingToResolvedVersions(versionMappingStrategy);
            return objectFactory.newInstance(
                    DefaultMavenPublication.class,
                    name,
                    projectIdentity,
                    artifactNotationParser,
                    versionMappingStrategy
            );
        }

        private void configureDefaultConfigurationsUsedWhenMappingToResolvedVersions(final VersionMappingStrategyInternal versionMappingStrategy) {
            plugins.withPlugin("org.gradle.java", new Action<AppliedPlugin>() {
                @Override
                public void execute(AppliedPlugin plugin) {
                    SourceSet mainSourceSet = extensionContainer.getByType(SourceSetContainer.class).getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                    // setup the default configurations used when mapping to resolved versions
                    versionMappingStrategy.defaultResolutionConfiguration(Usage.JAVA_API, mainSourceSet.getCompileClasspathConfigurationName());
                    versionMappingStrategy.defaultResolutionConfiguration(Usage.JAVA_RUNTIME, mainSourceSet.getRuntimeClasspathConfigurationName());
                }
            });
            plugins.withPlugin("org.gradle.java-platform", new Action<AppliedPlugin>() {
                @Override
                public void execute(AppliedPlugin plugin) {
                    versionMappingStrategy.defaultResolutionConfiguration(Usage.JAVA_API, JavaPlatformPlugin.CLASSPATH_CONFIGURATION_NAME);
                    versionMappingStrategy.defaultResolutionConfiguration(Usage.JAVA_RUNTIME, JavaPlatformPlugin.CLASSPATH_CONFIGURATION_NAME);
                }
            });
        }

        private MutableMavenProjectIdentity createProjectIdentity() {
            final Module module = dependencyMetaDataProvider.getModule();
            MutableMavenProjectIdentity projectIdentity = new WritableMavenProjectIdentity(objectFactory);
            projectIdentity.getGroupId().set(providerFactory.provider(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return module.getGroup();
                }
            }));
            projectIdentity.getArtifactId().set(providerFactory.provider(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return module.getName();
                }
            }));
            projectIdentity.getVersion().set(providerFactory.provider(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return module.getVersion();
                }
            }));
            return projectIdentity;
        }
    }
}
