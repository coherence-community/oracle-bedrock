open module com.oracle.bedrock.runtime.coherence {
    exports com.oracle.bedrock.runtime.coherence;
    exports com.oracle.bedrock.runtime.coherence.callables;
    exports com.oracle.bedrock.runtime.coherence.options;

    requires transitive com.oracle.bedrock.runtime;
    requires static com.oracle.bedrock.runtime.remote;
    requires com.oracle.coherence.ce;

    requires java.management;

}