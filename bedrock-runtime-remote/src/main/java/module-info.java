open module com.oracle.bedrock.runtime.remote {
    exports com.oracle.bedrock.runtime.remote;
    exports com.oracle.bedrock.runtime.remote.http;
    exports com.oracle.bedrock.runtime.remote.java;
    exports com.oracle.bedrock.runtime.remote.java.options;
    exports com.oracle.bedrock.runtime.remote.options;
    exports com.oracle.bedrock.runtime.remote.ssh;

    requires transitive com.oracle.bedrock.runtime;

    requires java.logging;

    requires jdk.httpserver;
    requires com.jcraft.jsch;
}