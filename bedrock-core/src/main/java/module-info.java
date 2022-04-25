open module com.oracle.bedrock {
    requires jakarta.el;
    requires java.xml;
    requires org.mockito;
    requires java.logging;
    requires java.management;
    exports com.oracle.bedrock;
    exports com.oracle.bedrock.annotations;
    exports com.oracle.bedrock.deferred;
    exports com.oracle.bedrock.deferred.atomic;
    exports com.oracle.bedrock.deferred.jmx;
    exports com.oracle.bedrock.deferred.options;
    exports com.oracle.bedrock.diagnostics;
    exports com.oracle.bedrock.extensible;
    exports com.oracle.bedrock.io;
    exports com.oracle.bedrock.lang;
    exports com.oracle.bedrock.options;
    exports com.oracle.bedrock.predicate;
    exports com.oracle.bedrock.table;
    exports com.oracle.bedrock.util;
}
