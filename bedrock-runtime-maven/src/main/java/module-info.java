module com.oracle.bedrock.maven {
    exports com.oracle.bedrock.maven;

    requires transitive com.oracle.bedrock.runtime;

    requires java.logging;

    requires aether.api;
    requires aether.connector.basic;
    requires aether.impl;
    requires aether.spi;
    requires aether.transport.file;
    requires aether.transport.http;
    requires aether.util;

    requires license.maven.plugin;

    requires maven.aether.provider;
    requires maven.settings;
    requires maven.settings.builder;

    requires p4java;

    provides com.mycila.maven.plugin.license.PropertiesProvider
            with com.oracle.bedrock.maven.P4CopyrightRangeProvider;
}