module com.oracle.bedrock.testsupport {
    exports com.oracle.bedrock.testsupport;
    exports com.oracle.bedrock.testsupport.deferred;
    exports com.oracle.bedrock.testsupport.deferred.options;
    exports com.oracle.bedrock.testsupport.junit;
    exports com.oracle.bedrock.testsupport.junit.options;
    exports com.oracle.bedrock.testsupport.matchers;

    requires transitive com.oracle.bedrock.runtime;
    requires static junit;
    requires static org.junit.jupiter.api;
    requires org.hamcrest;
    requires java.logging;
}