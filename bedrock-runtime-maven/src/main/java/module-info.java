open module com.oracle.bedrock.maven {
    exports com.oracle.bedrock.maven;

    requires transitive com.oracle.bedrock.runtime;

    requires java.logging;

    requires org.apache.maven.resolver;

    requires maven.settings;
    requires maven.settings.builder;
    requires org.apache.maven.resolver.connector.basic;
    requires org.apache.maven.resolver.impl;
    requires org.apache.maven.resolver.spi;
    requires org.apache.maven.resolver.transport.file;
    requires org.apache.maven.resolver.transport.http;
    requires org.apache.maven.resolver.util;
requires maven.resolver.provider;
}