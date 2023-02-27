package me.xanium.gemseconomy;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class GemsEconomyLoader implements PluginLoader {
    @Override public void classloader(@NotNull final PluginClasspathBuilder builder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("aliyunmaven", "default", "https://maven.aliyun.com/repository/public/").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:5.0.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-paper:1.8.1"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("cloud.commandframework:cloud-minecraft-extras:1.8.1"), null));

        resolver.addRepository(new RemoteRepository.Builder("minebench", "default", "https://repo.minebench.de").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("de.themoep.utils:lang-bukkit:1.3-SNAPSHOT"), null));

        builder.addLibrary(resolver);
    }
}
