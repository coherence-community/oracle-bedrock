open module com.oracle.bedrock.runtime {
    exports com.oracle.bedrock.runtime;
    exports com.oracle.bedrock.runtime.concurrent;
    exports com.oracle.bedrock.runtime.concurrent.callable;
    exports com.oracle.bedrock.runtime.concurrent.options;
    exports com.oracle.bedrock.runtime.concurrent.runnable;
    exports com.oracle.bedrock.runtime.concurrent.socket;
    exports com.oracle.bedrock.runtime.console;
    exports com.oracle.bedrock.runtime.java;
    exports com.oracle.bedrock.runtime.java.container;
    exports com.oracle.bedrock.runtime.java.features;
    exports com.oracle.bedrock.runtime.java.io;
    exports com.oracle.bedrock.runtime.java.options;
    exports com.oracle.bedrock.runtime.java.profiles;
    exports com.oracle.bedrock.runtime.network;
    exports com.oracle.bedrock.runtime.options;
    
    requires transitive com.oracle.bedrock;

    requires java.logging;
    requires java.management;
    requires java.rmi;
}
