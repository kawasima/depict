class DependenciesPluginTest {
    @Test
    public void test() {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'net.unit8.depict'
    }
}
