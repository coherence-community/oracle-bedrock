open module com.oracle.bedrock.runtime.coherence.testing {
    exports com.oracle.bedrock.runtime.coherence.testing.junit;
    exports com.oracle.bedrock.runtime.coherence.testing.matchers;

    requires com.oracle.bedrock.runtime.coherence;
    requires com.oracle.bedrock.testsupport;

    requires com.oracle.coherence;

    requires static junit;
    requires static org.junit.jupiter.api;
    requires static org.hamcrest;

    requires java.logging;
}
