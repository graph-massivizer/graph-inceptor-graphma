package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.plugin.use.PluginDependency;
import org.gradle.api.artifacts.ExternalModuleDependencyBundle;
import org.gradle.api.artifacts.MutableVersionConstraint;
import org.gradle.api.provider.Provider;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import java.util.Map;
import org.gradle.api.internal.attributes.ImmutableAttributesFactory;
import org.gradle.api.internal.artifacts.dsl.CapabilityNotationParser;
import javax.inject.Inject;

/**
 * A catalog of dependencies accessible via the `libs` extension.
 */
@NonNullApi
public class LibrariesForLibs extends AbstractExternalDependencyFactory {

    private final AbstractExternalDependencyFactory owner = this;
    private final JcstressLibraryAccessors laccForJcstressLibraryAccessors = new JcstressLibraryAccessors(owner);
    private final JunitLibraryAccessors laccForJunitLibraryAccessors = new JunitLibraryAccessors(owner);
    private final VersionAccessors vaccForVersionAccessors = new VersionAccessors(providers, config);
    private final BundleAccessors baccForBundleAccessors = new BundleAccessors(objects, providers, config, attributesFactory, capabilityNotationParser);
    private final PluginAccessors paccForPluginAccessors = new PluginAccessors(providers, config);

    @Inject
    public LibrariesForLibs(DefaultVersionCatalog config, ProviderFactory providers, ObjectFactory objects, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) {
        super(config, providers, objects, attributesFactory, capabilityNotationParser);
    }

        /**
         * Creates a dependency provider for assertj (org.assertj:assertj-core)
         * This dependency was declared in catalog libs.versions.toml
         */
        public Provider<MinimalExternalModuleDependency> getAssertj() {
            return create("assertj");
    }

    /**
     * Returns the group of libraries at jcstress
     */
    public JcstressLibraryAccessors getJcstress() {
        return laccForJcstressLibraryAccessors;
    }

    /**
     * Returns the group of libraries at junit
     */
    public JunitLibraryAccessors getJunit() {
        return laccForJunitLibraryAccessors;
    }

    /**
     * Returns the group of versions at versions
     */
    public VersionAccessors getVersions() {
        return vaccForVersionAccessors;
    }

    /**
     * Returns the group of bundles at bundles
     */
    public BundleAccessors getBundles() {
        return baccForBundleAccessors;
    }

    /**
     * Returns the group of plugins at plugins
     */
    public PluginAccessors getPlugins() {
        return paccForPluginAccessors;
    }

    public static class JcstressLibraryAccessors extends SubDependencyFactory {

        public JcstressLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for test (org.openjdk.jcstress:jcstress-core)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getTest() {
                return create("jcstress.test");
        }

    }

    public static class JunitLibraryAccessors extends SubDependencyFactory {
        private final JunitJupiterLibraryAccessors laccForJunitJupiterLibraryAccessors = new JunitJupiterLibraryAccessors(owner);
        private final JunitPlatformLibraryAccessors laccForJunitPlatformLibraryAccessors = new JunitPlatformLibraryAccessors(owner);
        private final JunitVintageLibraryAccessors laccForJunitVintageLibraryAccessors = new JunitVintageLibraryAccessors(owner);

        public JunitLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for bom (org.junit:junit-bom)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getBom() {
                return create("junit.bom");
        }

        /**
         * Returns the group of libraries at junit.jupiter
         */
        public JunitJupiterLibraryAccessors getJupiter() {
            return laccForJunitJupiterLibraryAccessors;
        }

        /**
         * Returns the group of libraries at junit.platform
         */
        public JunitPlatformLibraryAccessors getPlatform() {
            return laccForJunitPlatformLibraryAccessors;
        }

        /**
         * Returns the group of libraries at junit.vintage
         */
        public JunitVintageLibraryAccessors getVintage() {
            return laccForJunitVintageLibraryAccessors;
        }

    }

    public static class JunitJupiterLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public JunitJupiterLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for jupiter (org.junit.jupiter:junit-jupiter)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() {
                return create("junit.jupiter");
        }

            /**
             * Creates a dependency provider for api (org.junit.jupiter:junit-jupiter-api)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getApi() {
                return create("junit.jupiter.api");
        }

            /**
             * Creates a dependency provider for engine (org.junit.jupiter:junit-jupiter-engine)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getEngine() {
                return create("junit.jupiter.engine");
        }

            /**
             * Creates a dependency provider for migration (org.junit.jupiter:junit-jupiter-migrationsupport)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getMigration() {
                return create("junit.jupiter.migration");
        }

            /**
             * Creates a dependency provider for params (org.junit.jupiter:junit-jupiter-params)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getParams() {
                return create("junit.jupiter.params");
        }

    }

    public static class JunitPlatformLibraryAccessors extends SubDependencyFactory {

        public JunitPlatformLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for launcher (org.junit.platform:junit-platform-launcher)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getLauncher() {
                return create("junit.platform.launcher");
        }

            /**
             * Creates a dependency provider for runner (org.junit.platform:junit-platform-runner)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getRunner() {
                return create("junit.platform.runner");
        }

            /**
             * Creates a dependency provider for suite (org.junit.platform:junit-platform-suite-engine)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getSuite() {
                return create("junit.platform.suite");
        }

    }

    public static class JunitVintageLibraryAccessors extends SubDependencyFactory implements DependencyNotationSupplier {

        public JunitVintageLibraryAccessors(AbstractExternalDependencyFactory owner) { super(owner); }

            /**
             * Creates a dependency provider for vintage (junit:junit)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> asProvider() {
                return create("junit.vintage");
        }

            /**
             * Creates a dependency provider for engine (org.junit.vintage:junit-vintage-engine)
             * This dependency was declared in catalog libs.versions.toml
             */
            public Provider<MinimalExternalModuleDependency> getEngine() {
                return create("junit.vintage.engine");
        }

    }

    public static class VersionAccessors extends VersionFactory  {

        private final JcstressVersionAccessors vaccForJcstressVersionAccessors = new JcstressVersionAccessors(providers, config);
        private final JunitVersionAccessors vaccForJunitVersionAccessors = new JunitVersionAccessors(providers, config);
        public VersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: assertj (3.24.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getAssertj() { return getVersion("assertj"); }

            /**
             * Returns the version associated to this alias: junit4 (4.13.2)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getJunit4() { return getVersion("junit4"); }

        /**
         * Returns the group of versions at versions.jcstress
         */
        public JcstressVersionAccessors getJcstress() {
            return vaccForJcstressVersionAccessors;
        }

        /**
         * Returns the group of versions at versions.junit
         */
        public JunitVersionAccessors getJunit() {
            return vaccForJunitVersionAccessors;
        }

    }

    public static class JcstressVersionAccessors extends VersionFactory  {

        public JcstressVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Returns the version associated to this alias: jcstress.test (0.16)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getTest() { return getVersion("jcstress.test"); }

    }

    public static class JunitVersionAccessors extends VersionFactory  implements VersionNotationSupplier {

        private final JunitPlatformVersionAccessors vaccForJunitPlatformVersionAccessors = new JunitPlatformVersionAccessors(providers, config);
        public JunitVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the version associated to this alias: junit (5.9.3)
         * If the version is a rich version and that its not expressible as a
         * single version string, then an empty string is returned.
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> asProvider() { return getVersion("junit"); }

        /**
         * Returns the group of versions at versions.junit.platform
         */
        public JunitPlatformVersionAccessors getPlatform() {
            return vaccForJunitPlatformVersionAccessors;
        }

    }

    public static class JunitPlatformVersionAccessors extends VersionFactory  implements VersionNotationSupplier {

        public JunitPlatformVersionAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

        /**
         * Returns the version associated to this alias: junit.platform (1.9.3)
         * If the version is a rich version and that its not expressible as a
         * single version string, then an empty string is returned.
         * This version was declared in catalog libs.versions.toml
         */
        public Provider<String> asProvider() { return getVersion("junit.platform"); }

            /**
             * Returns the version associated to this alias: junit.platform.suite (1.10.0)
             * If the version is a rich version and that its not expressible as a
             * single version string, then an empty string is returned.
             * This version was declared in catalog libs.versions.toml
             */
            public Provider<String> getSuite() { return getVersion("junit.platform.suite"); }

    }

    public static class BundleAccessors extends BundleFactory {

        public BundleAccessors(ObjectFactory objects, ProviderFactory providers, DefaultVersionCatalog config, ImmutableAttributesFactory attributesFactory, CapabilityNotationParser capabilityNotationParser) { super(objects, providers, config, attributesFactory, capabilityNotationParser); }

            /**
             * Creates a dependency bundle provider for stressTestDependencies which is an aggregate for the following dependencies:
             * <ul>
             *    <li>org.openjdk.jcstress:jcstress-core</li>
             * </ul>
             * This bundle was declared in catalog libs.versions.toml
             */
            public Provider<ExternalModuleDependencyBundle> getStressTestDependencies() {
                return createBundle("stressTestDependencies");
            }

            /**
             * Creates a dependency bundle provider for testDependencies which is an aggregate for the following dependencies:
             * <ul>
             *    <li>org.junit.jupiter:junit-jupiter-api</li>
             *    <li>org.junit.jupiter:junit-jupiter-engine</li>
             *    <li>org.junit.jupiter:junit-jupiter-params</li>
             *    <li>org.junit.platform:junit-platform-suite-engine</li>
             *    <li>org.assertj:assertj-core</li>
             * </ul>
             * This bundle was declared in catalog libs.versions.toml
             */
            public Provider<ExternalModuleDependencyBundle> getTestDependencies() {
                return createBundle("testDependencies");
            }

    }

    public static class PluginAccessors extends PluginFactory {

        public PluginAccessors(ProviderFactory providers, DefaultVersionCatalog config) { super(providers, config); }

            /**
             * Creates a plugin provider for buildParameters to the plugin id 'org.gradlex.build-parameters'
             * This plugin was declared in catalog libs.versions.toml
             */
            public Provider<PluginDependency> getBuildParameters() { return createPlugin("buildParameters"); }

    }

}
