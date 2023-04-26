/*
 * File: Maven.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.bedrock.maven;

import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.version.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Maven {@link Profile} provides the ability define a Maven environment, configuration,
 * together with zero or more {@link Artifact}s to be resolved to automatically construct a
 * {@link ClassPath} for a {@link JavaApplication}.
 * <p>
 * {@link Maven} {@link Profile}s are {@link ComposableOption}s.  When multiple are provided
 * as a parameter, they will be composed in order of declaration into a single new instance.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class Maven implements Profile, ComposableOption<Maven>
{
    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(Maven.class.getName());

    /**
     * The {@link File} for the user settings (ie: settings.xml)
     * <p>
     * When <code>null</code> we assume ${user.home}/.m2/settings.xml
     */
    private File userSettingsFile;

    /**
     * The {@link File} for the global settings
     * <p>
     * When <code>null</code> we assume M2_HOME/conf/settings.xml
     */
    private File globalSettingsFile;

    /**
     * Should we operate in offline mode?
     * <p>
     * When <code>null</code> we assume online (false)
     */
    private Boolean isOffline;

    /**
     * The scope to use when resolving {@link Artifact}s.
     * <p>
     * When <code>null</code> we assume {@link JavaScopes#RUNTIME}.
     */
    private String scope;

    /**
     * The list of {@link Artifact}s, indexed by group, artifact, classifier and extension,
     * thus allowing overriding of artifacts with different versions.
     */
    private final LinkedHashMap<String, Artifact> artifacts;

    /**
     * The additional {@link ClassPath} to add to the {@link ClassPath} of the resolved artifacts.
     */
    private ClassPath additionalClassPath;


    /**
     * Constructs a {@link Maven} {@link Profile} based on another {@link Maven} {@link Profile}.
     *
     * @param maven the {@link Maven} {@link Profile}
     */
    private Maven(Maven maven)
    {
        this(maven.globalSettingsFile,
             maven.userSettingsFile,
             maven.isOffline,
             maven.scope,
             maven.artifacts,
             maven.additionalClassPath);
    }


    /**
     * Constructs a {@link Maven} {@link Profile}.
     *
     * @param globalSettingsFile  the location of the global settings.xml (use {@code null} for the default)
     * @param userSettingsFile    the location of the user settings.xml (use {@code null} for the default)
     * @param isOffline           if {@link Maven} is to operate in offline-mode  (use {@code null} for default)
     * @param scope               the scope to use for resolving artifacts
     * @param artifacts           the table of {@link Artifact}s
     * @param additionalClassPath the additional {@link ClassPath} to add to the resolved artifacts
     */
    private Maven(File                              globalSettingsFile,
                  File                              userSettingsFile,
                  Boolean                           isOffline,
                  String                            scope,
                  LinkedHashMap<String, Artifact>   artifacts,
                  ClassPath                         additionalClassPath)
    {
        this.globalSettingsFile  = globalSettingsFile;
        this.userSettingsFile    = userSettingsFile;
        this.isOffline           = isOffline;
        this.scope               = scope;
        this.artifacts           = new LinkedHashMap<>();
        this.additionalClassPath = additionalClassPath;

        // add all of the artifacts (if there are any)
        if (artifacts != null)
        {
            this.artifacts.putAll(artifacts);
        }
    }


    /**
     * Obtain the Maven {@link Settings} for this {@link Maven} profile
     * given the specified {@link OptionsByType}.
     *
     * @param optionsByType the launch {@link OptionsByType}
     *
     * @return a new {@link Settings}
     */
    private Settings getSettings(OptionsByType optionsByType)
    {
        SettingsBuilder                settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

        DefaultSettingsBuildingRequest request         = new DefaultSettingsBuildingRequest();

        request.setGlobalSettingsFile(globalSettingsFile);

        request.setUserSettingsFile(userSettingsFile);

        // we use the system-properties of the current process to resolve artifacts
        request.setSystemProperties(System.getProperties());

        Settings settings;

        try
        {
            settings = settingsBuilder.build(request).getEffectiveSettings();

            return settings;
        }
        catch (SettingsBuildingException e)
        {
            LOGGER.log(Level.WARNING, "Could not process settings.xml: " + e.getMessage(), e);

            throw new IllegalArgumentException("Could not process the settings.xml", e);
        }
    }


    /**
     * Obtains the {@link RepositorySystem} we'll use for resolving {@link Artifact}s.
     *
     * @return a new {@link RepositorySystem}.
     */
    private RepositorySystem newRepositorySystem()
    {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * pre-populated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler()
                                {
                                    @Override
                                    public void serviceCreationFailed(Class<?>  type,
                                                                      Class<?>  impl,
                                                                      Throwable exception)
                                    {
                                        exception.printStackTrace();
                                    }
                                });

        return locator.getService(RepositorySystem.class);
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        // resolve the class path based on the required maven artifacts
        try
        {
            perform(
                (system, session, repositories, scope) -> {

                // we only filter based on the scope
                    DependencyFilter filter = DependencyFilterUtils.classpathFilter(scope);

                    // collect class paths for each resolved artifact
                    LinkedHashSet<ClassPath> artifactPaths = new LinkedHashSet<>();

                    for (Artifact artifact : artifacts.values())
                    {
                        CollectRequest collectRequest = new CollectRequest();

                        collectRequest.setRoot(new Dependency(artifact, scope));
                        collectRequest.setRepositories(repositories);

                        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, filter);

                        List<ArtifactResult> artifactResults = system.resolveDependencies(session,
                                                                                          dependencyRequest)
                                                                                          .getArtifactResults();

                        for (ArtifactResult artifactResult : artifactResults)
                        {
                            artifactPaths.add(ClassPath.ofFile(artifactResult.getArtifact().getFile()));
                        }
                    }

                    // define the ClassPath based on the artifact paths
                    ClassPath classPath = new ClassPath(artifactPaths);

                    // add the additional ClassPaths (when defined)
                    classPath = additionalClassPath == null ? classPath : new ClassPath(classPath, additionalClassPath);

                    optionsByType.add(classPath);
                },
                optionsByType);
        }
        catch (RepositoryException e)
        {
            throw new RuntimeException("Failed to resolve artifact", e);
        }
    }


    @Override
    public void onLaunched(Platform      platform,
                           Application   application,
                           OptionsByType optionsByType)
    {
        // nothing to do after launch
    }


    @Override
    public void onClosing(Platform      platform,
                          Application   application,
                          OptionsByType optionsByType)
    {
        // nothing to do before closing
    }


    /**
     * Create a {@link Maven} {@link Profile} consisting of the specified {@link Artifact},
     * parsed from the {@link String} in the following format:
     * {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}.
     *
     * @param artifact the g:a[:e][:c]:v coordinates of the {@link Artifact}
     *
     * @return a new {@link Maven} {@link Profile}
     */
    public static Maven artifact(String artifact)
    {
        Maven           maven           = Maven.autoDetect();

        DefaultArtifact defaultArtifact = new DefaultArtifact(artifact);

        maven.artifacts.put(getArtifactIdentity(defaultArtifact), new DefaultArtifact(artifact));

        return maven;
    }


    /**
     * Create a {@link Maven} {@link Profile} consisting of the artifact specified groupId,
     * artifactId and version (using the default extension .jar and no classifier)
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     *
     * @return a new {@link Maven} {@link Profile}
     */
    public static Maven artifact(String groupId,
                                 String artifactId,
                                 String version)
    {
        Maven           maven           = Maven.autoDetect();

        DefaultArtifact defaultArtifact = new DefaultArtifact(groupId, artifactId, "jar", version);

        maven.artifacts.put(getArtifactIdentity(defaultArtifact), defaultArtifact);

        return maven;
    }


    /**
     * Create a {@link Maven} {@link Profile} consisting of the artifact specified groupId,
     * artifactId, version and extension (without a classifier)
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     * @param extension  the extension
     *
     * @return a new {@link Maven} {@link Profile}
     */
    public static Maven artifact(String groupId,
                                 String artifactId,
                                 String version,
                                 String extension)
    {
        Maven           maven           = Maven.autoDetect();

        DefaultArtifact defaultArtifact = new DefaultArtifact(groupId, artifactId, extension, version);

        maven.artifacts.put(getArtifactIdentity(defaultArtifact), defaultArtifact);

        return maven;
    }


    /**
     * Create a {@link Maven} {@link Profile} consisting of the artifact specified groupId,
     * artifactId, version, extension and classifier
     *
     * @param groupId    the group id
     * @param artifactId the artifact id
     * @param version    the version
     * @param extension  the extension
     * @param classifier the classifier
     *
     * @return a new {@link Maven} {@link Profile}
     */
    public static Maven artifact(String groupId,
                                 String artifactId,
                                 String version,
                                 String extension,
                                 String classifier)
    {
        Maven           maven           = Maven.autoDetect();

        DefaultArtifact defaultArtifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version);

        maven.artifacts.put(getArtifactIdentity(defaultArtifact), defaultArtifact);

        return maven;
    }


    /**
     * Create a default {@link Maven} {@link Profile} that is offline.
     *
     * @return an offline {@link Maven} {@link Profile}
     */
    public static Maven offline()
    {
        Maven maven = Maven.autoDetect();

        maven.isOffline = true;

        return maven;
    }


    /**
     * Create a default {@link Maven} {@link Profile} using the specified offline status
     *
     * @param isOffline <code>true</code> if the {@link Maven} {@link Profile} should be offline,
     *                  <code>false</code> otherwise
     *
     * @return a {@link Maven} {@link Profile} with the specified offline status
     */
    public static Maven offline(boolean isOffline)
    {
        Maven maven = Maven.autoDetect();

        maven.isOffline = isOffline;

        return maven;
    }


    /**
     * Create a default {@link Maven} {@link Profile} and use the specified user settings.xml location.
     *
     * @param settingsFileLocation the location of the Maven settings.xml file, or <code>null</code>
     *                             to use the default/auto-detect
     *
     * @return a {@link Maven} {@link Profile} with the specified user settings file
     */
    public static Maven settings(String settingsFileLocation)
    {
        Maven maven = Maven.autoDetect();

        maven.userSettingsFile = settingsFileLocation == null ? null : new File(settingsFileLocation);

        return maven;
    }


    /**
     * Create a default {@link Maven} {@link Profile} and use the specified user settings.xml location.
     *
     * @param settingsFileLocation the location of the Maven settings.xml file, or <code>null</code>
     *                             to use the default/auto-detect
     *
     * @return a {@link Maven} {@link Profile} with the specified user settings file
     */
    public static Maven settings(File settingsFileLocation)
    {
        Maven maven = Maven.autoDetect();

        maven.userSettingsFile = settingsFileLocation;

        return maven;
    }


    /**
     * Create a default {@link Maven} {@link Profile} and use the specified scope when resolving
     * artifacts.
     *
     * @param scope the Maven Scope for artifact resolution, or <code>null</code> to use the default
     *              (which is RUNTIME)
     *
     * @return a {@link Maven} {@link Profile} with the specified scope
     *
     * @see JavaScopes
     */
    public static Maven scope(String scope)
    {
        Maven maven = Maven.autoDetect();

        maven.scope = scope;

        return maven;
    }


    /**
     * Create a default {@link Maven} {@link Profile} that includes the specified {@link ClassPath}, that is
     * included after any resolved Maven artifacts.
     *
     * @param classPath the additional {@link ClassPath}
     *
     * @return a {@link Maven} {@link Profile} with the additional {@link ClassPath}
     */
    public static Maven include(ClassPath classPath)
    {
        Maven maven = Maven.autoDetect();

        maven.additionalClassPath = classPath;

        return maven;
    }


    /**
     * Creates a {@link Maven} profile based on and composed by a number of other
     * {@link Maven} profiles.
     *
     * @param mavens the other {@link Maven} profiles
     *
     * @return a new {@link Maven} profile
     */
    public static Maven from(Maven... mavens)
    {
        Maven maven = autoDetect();

        if (maven != null)
        {
            // compose maven options
            for (Maven m : mavens)
            {
                maven = maven.compose(m);
            }
        }

        return maven;
    }


    /**
     * Obtains a {@link Maven} {@link Profile}, automatically detecting the
     * configuration from the underlying environment, without any {@link Artifact}s
     * defined, and being online by default.
     *
     * @return a {@link Maven} {@link Profile}
     */
    @OptionsByType.Default
    public static Maven autoDetect()
    {
        return new Maven(null, null, null, null, null, null);
    }


    @Override
    public Maven compose(Maven other)
    {
        // construct a new Maven profile
        Maven maven = new Maven(this);

        // override the global settings
        // (if defined by the other Maven profile)
        if (other.globalSettingsFile != null)
        {
            maven.globalSettingsFile = other.globalSettingsFile;
        }

        // override the global settings
        // (if defined by the other Maven profile)
        if (other.userSettingsFile != null)
        {
            maven.userSettingsFile = other.userSettingsFile;
        }

        // override the scope
        // (if defined by the other Maven profile)
        if (other.scope != null)
        {
            maven.scope = other.scope;
        }

        // override the offline state
        // (if defined by the other Maven profile)
        if (other.isOffline != null)
        {
            maven.isOffline = other.isOffline;
        }

        // add all of the artifacts
        // (we combine them together)
        for (Artifact artifact : other.artifacts.values())
        {
            maven.artifacts.put(getArtifactIdentity(artifact), artifact);
        }

        // include the additional ClassPath
        if (other.additionalClassPath != null)
        {
            maven.additionalClassPath = maven.additionalClassPath == null
                                        ? other.additionalClassPath : new ClassPath(maven.additionalClassPath,
                                                                                    other.additionalClassPath);
        }

        return maven;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Maven))
        {
            return false;
        }

        Maven maven = (Maven) o;

        if (userSettingsFile != null
            ? !userSettingsFile.equals(maven.userSettingsFile) : maven.userSettingsFile != null)
        {
            return false;
        }

        if (globalSettingsFile != null
            ? !globalSettingsFile.equals(maven.globalSettingsFile) : maven.globalSettingsFile != null)
        {
            return false;
        }

        if (isOffline != null ? !isOffline.equals(maven.isOffline) : maven.isOffline != null)
        {
            return false;
        }

        if (scope != null ? !scope.equals(maven.scope) : maven.scope != null)
        {
            return false;
        }

        if (artifacts != null ? !artifacts.equals(maven.artifacts) : maven.artifacts != null)
        {
            return false;
        }

        return additionalClassPath != null
               ? additionalClassPath.equals(maven.additionalClassPath) : maven.additionalClassPath == null;

    }


    @Override
    public int hashCode()
    {
        int result = userSettingsFile != null ? userSettingsFile.hashCode() : 0;

        result = 31 * result + (globalSettingsFile != null ? globalSettingsFile.hashCode() : 0);
        result = 31 * result + (isOffline != null ? isOffline.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (artifacts != null ? artifacts.hashCode() : 0);
        result = 31 * result + (additionalClassPath != null ? additionalClassPath.hashCode() : 0);

        return result;
    }


    /**
     * Obtains the identity of an {@link Artifact}, not including the version.
     *
     * @param artifact  the {@link Artifact}
     *
     * @return a {@link String}-based identity of an {@link Artifact}
     */
    private static String getArtifactIdentity(Artifact artifact)
    {
        if (artifact == null)
        {
            return "";
        }
        else
        {
            return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getClassifier() + ":"
                   + artifact.getExtension();
        }
    }


    /**
     * Performs the specified {@link RepositorySystemOperation} against the Maven repository
     * using the settings defined by the profile.
     *
     * @throws RepositoryException when an exception occurs interacting with the repository
     */
    private void perform(RepositorySystemOperation operation,
                         OptionsByType             optionsByType) throws RepositoryException
    {
        // obtain the PlatformSeparators
        PlatformSeparators separators = optionsByType.get(PlatformSeparators.class);

        // define the global settings location if it's not defined
        if (globalSettingsFile == null)
        {
            // determine the location of the Maven Home
            String mavenHome = System.getenv("M2_HOME");

            globalSettingsFile = new File(System.getProperty("maven.home",
                                                             mavenHome != null ? mavenHome : ""),
                                          "conf" + separators.getFileSeparator() + "settings.xml");
        }

        // define the user settings location if it's not defined
        if (userSettingsFile == null)
        {
            userSettingsFile = new File(System.getProperty("user.home") + separators.getFileSeparator() + ".m2"
                                        + separators.getFileSeparator() + "settings.xml");
        }

        // define the scope (if it's not defined)
        if (scope == null || scope.isEmpty())
        {
            this.scope = JavaScopes.RUNTIME;
        }

        // acquire the Maven Settings for the profile
        Settings settings = getSettings(optionsByType);

        // ----- establish the repository system using the settings -----
        RepositorySystem system = newRepositorySystem();

        // ----- establish the session for the repository system -----
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        session.setOffline(isOffline == null ? false : isOffline);

        session.setCache(new DefaultRepositoryCache());

        // define the local repository
        File localRepositoryLocation = new File(System.getProperty("user.home") + separators.getFileSeparator() + ".m2"
                                                + separators.getFileSeparator() + "repository");

        LocalRepository localRepo = new LocalRepository(localRepositoryLocation);

        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        // ----- establish the remote repositories to use from the settings -----

        Map<String, org.apache.maven.settings.Profile> profiles           = settings.getProfilesAsMap();
        ArrayList<RemoteRepository>                    remoteRepositories = new ArrayList<>(20);

        for (String profileName : settings.getActiveProfiles())
        {
            for (Repository repo : profiles.get(profileName).getRepositories())
            {
                RemoteRepository remoteRepository = new RemoteRepository.Builder(repo.getId(),
                                                                                 "default",
                                                                                 repo.getUrl()).build();

                remoteRepositories.add(remoteRepository);
            }
        }

        // perform the operation
        operation.perform(system, session, remoteRepositories, scope);
    }


    /**
     * Obtains the versions of a Maven artifact resolvable from the {@link Maven} profile.
     *
     * @param groupId        the Maven Group Id
     * @param artifactId     the Maven Artifact Id
     * @param extension      the Maven Artifact Extension
     * @param classifier     the Maven Artifact Classifier
     * @param versionPattern the Maven Artifact Version number range pattern. eg: "[1.0,)"
     * @param options        the {@link Option}s
     *                       (for example {@link PlatformSeparators} if the local platform defaults are unacceptable)
     *
     * @return a {@link List} of available artifact versions, empty if none are available (not null)
     *
     * @see <a href="https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html">Maven Version Number Ranges</a>
     * @see <a href="https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN401">Oracle's guidelines on Maven Version Number ranges</a>
     */
    public List<String> versionsOf(String    groupId,
                                   String    artifactId,
                                   String    extension,
                                   String    classifier,
                                   String    versionPattern,
                                   Option... options) throws RepositoryException
    {
        OptionsByType optionsByType = OptionsByType.of(options);

        List<String>  versions      = new ArrayList<>();

        perform(
            (system, session, repositories, scope) -> {
                Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + extension + ":" + classifier
                                                        + ":" + versionPattern);

                VersionRangeRequest rangeRequest = new VersionRangeRequest();

                rangeRequest.setArtifact(artifact);
                rangeRequest.setRepositories(repositories);

                VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

                for (Version version : rangeResult.getVersions())
                {
                    versions.add(version.toString());
                }
            },
            optionsByType);

        return versions;
    }


    /**
     * Obtains the versions of a Maven artifact resolvable from the {@link Maven} profile.
     *
     * @param groupId        the Maven Group Id
     * @param artifactId     the Maven Artifact Id
     * @param extension      the Maven Artifact Extension
     * @param versionPattern the Maven Artifact Version number range pattern. eg: "[1.0,)"
     * @param options        the {@link Option}s
     *                       (for example {@link PlatformSeparators} if the local platform defaults are unacceptable)
     *
     * @return a {@link List} of available artifact versions, empty if none are available (not null)
     *
     * @see <a href="https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html">Maven Version Number Ranges</a>
     * @see <a href="https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN401">Oracle's guidelines on Maven Version Number ranges</a>
     */
    public List<String> versionsOf(String    groupId,
                                   String    artifactId,
                                   String    extension,
                                   String    versionPattern,
                                   Option... options) throws RepositoryException
    {
        OptionsByType optionsByType = OptionsByType.of(options);

        List<String>  versions      = new ArrayList<>();

        perform(
            (system, session, repositories, scope) -> {
                Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + extension + ":"
                                                        + versionPattern);

                VersionRangeRequest rangeRequest = new VersionRangeRequest();

                rangeRequest.setArtifact(artifact);
                rangeRequest.setRepositories(repositories);

                VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

                for (Version version : rangeResult.getVersions())
                {
                    versions.add(version.toString());
                }
            },
            optionsByType);

        return versions;
    }


    /**
     * Obtains the versions of a Maven artifact resolvable from the {@link Maven} profile.
     *
     * @param groupId        the Maven Group Id
     * @param artifactId     the Maven Artifact Id
     * @param versionPattern the Maven Artifact Version number range pattern. eg: "[1.0,)"
     * @param options        the {@link Option}s
     *                       (for example {@link PlatformSeparators} if the local platform defaults are unacceptable)
     *
     * @return a {@link List} of available artifact versions, empty if none are available (not null)
     *
     * @see <a href="https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html">Maven Version Number Ranges</a>
     * @see <a href="https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN401">Oracle's guidelines on Maven Version Number ranges</a>
     */
    public List<String> versionsOf(String    groupId,
                                   String    artifactId,
                                   String    versionPattern,
                                   Option... options) throws RepositoryException
    {
        OptionsByType optionsByType = OptionsByType.of(options);

        List<String>  versions      = new ArrayList<>();

        perform(
            (system, session, repositories, scope) -> {
                Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + versionPattern);

                VersionRangeRequest rangeRequest = new VersionRangeRequest();

                rangeRequest.setArtifact(artifact);
                rangeRequest.setRepositories(repositories);

                VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

                for (Version version : rangeResult.getVersions())
                {
                    versions.add(version.toString());
                }
            },
            optionsByType);

        return versions;
    }


    @FunctionalInterface
    private interface RepositorySystemOperation
    {
        /**
         * Perform an operation against a {@link RepositorySystem}.
         *
         * @param system       the {@link RepositorySystem}
         * @param session      the {@link RepositorySystemSession}
         * @param repositories the {@link RemoteRepository}s
         * @param scope        the Maven scope of the operation
         *
         * @throws RepositoryException when an exception occurs interacting with the repository
         */
        void perform(RepositorySystem        system,
                     RepositorySystemSession session,
                     List<RemoteRepository>  repositories,
                     String                  scope) throws RepositoryException;
    }
}
