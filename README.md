Oracle Bedrock
==============

Oracle Bedrock provides a general purpose Java framework for the development, orchestration and testing of highly concurrent distributed applications.

It's often used for orchestrating and testing multi-server, multi-process distributed applications, meaning it's
especially useful for working with Coherence-based applications.  However it can be used for any type of application or server, Java or not.

Oracle Bedrock provides extremely uniform mechanisms to start/stop and manage applications and processes on a variety of platforms, including;

* Local Platforms
* Remote Platforms (via ssh, powershell et al)
* Virtualized Platforms (via Vagrant), including machine / platform orchestration (VirtualBox, VMWare etc)
* Containerized Platforms (via Docker), including image management
* Java Virtual Machines (aka: in-process applications)

Which means it can orchestrate launching applications/servers in any environment, locally, on-premise, across data-centers or in one or more clouds.

For example:  To Launch a "HelloWorld" Java Application on the current classpath on the LocalPlatform, use the following:

```
    LocalPlatform platform = LocalPlatform.get();

    try (JavaApplication application = platform.launch(JavaApplication.class,
                                                       ClassName.of(HelloWorld.class))) {

        // potentially do something with the application ...

        // wait until it finishes execution
        application.waitFor();
    }
```

To launch this application on another platform, simply change the platform.   The rest of the code remains the same.

```
    // launch using a RemotePlatform
    RemotePlatform platform = new RemotePlatform(address, username, authentication);

    // ... or launch inside the running JavaVirtualMachine Platform (ie: in-process)
    JavaVirtualMachine platform = JavaVirtualMachine.get();
```

For Java-based applications, Oracle Bedrock uniquely provides support for:

* Packaging and automatically deploying applications (based on a ClassPath or Maven Dependency POM)
* Dynamically interacting with applications at runtime, without requiring technologies like RMI, including the ability to dynamically execute lambdas / remote callables / runnables through an ExecutorService like interface.

```
    // execute the lambda in the java application, where ever it is running!
    application.submit(() -> { System.out.println("Hello World");});

    // request a lambda to execute (in the java application) and return a result (as a CompleteableFuture)
    CompletableFuture<String> property = application.submit(() -> System.getProperty("os.name"));
```

Lastly, Oracle Bedrock provides a powerful extension to testing tools, allow developers to Eventually assert that
conditions in concurrent data-structures and distributed applications are reached.

```
   // ensure that the application internal state reaches some condition
   Eventually.assertThat(application, () -> { someFunction() }, is(someValue));
```

## Contributing
Oracle Bedrock is an open source project. Pull Requests are accepted. See
[CONTRIBUTING](CONTRIBUTING.md) for details.

## License
Copyright (c) 2010, 2017 Oracle and/or its affiliates.  Licensed under the [Common Development and
Distribution License v1.0](LICENSE.md) ("CDDL")
