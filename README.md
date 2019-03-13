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
Copyright (c) 2010, 2019 Oracle and/or its affiliates.  Licensed under the [Common Development and
Distribution License v1.0](LICENSE.md) ("CDDL")

## Building

Bedrock is a Maven project and can be built with standard Maven commands.

### Prerequisites

1. The Coherence modules have a dependency on four versions of Coherence. The exact versions can be found in the `coherence.version` property in `pom.xml` files 
    [bedrock-coherence/3.7.1/pom.xml](./bedrock-coherence/3.7.1/pom.xml)  
    [bedrock-coherence/12.1.2/pom.xml](./bedrock-coherence/12.1.2/pom.xml)  
    [bedrock-coherence/12.1.3/pom.xml](./bedrock-coherence/12.1.3/pom.xml)  
    [bedrock-coherence/12.2.1/pom.xml](./bedrock-coherence/12.2.1/pom.xml)  
    
    As Coherence is not available in public Maven repos the `coherence.jar` files for each of those versions needs to be loaded to your own local or remote Maven repository under the groupId `com.oracle.coherence` and artifactId `coherence`.

2. There are a number of tests in the `bedrock-runtime-remote-tests` module that will attempt to SSH into your local machine using private/public key pair. For this to work you need a key pair configured.

    1. By default the private key file is called `127.0.0.1_dsa` and corresponding public key `127.0.0.1_dsa.pub`. You can run the build with a system property to change this to any valid key that you already have configured for ssh'ing into your machine: For example to use `id_rsa` then add `-Dbedrock.remote.privatekey.file=~/.ssh/id_rsa` to the Maven command line.
    
    2. Alternatively you may need to generate a new key pair using `ssh-keygen`. **NOTE** ensure that they type of key-pair generated is a valid type to SSH into your machine. For example on MacOS an RSA key pair will be fine, a DSA key pair may not, so even though the default file name used in tests has the suffix `_dsa` it can contain any type of key.
    
        ```
        ssh-keygen -t rsa -f 127.0.0.1_dsa
        ```   
  
        **Do not** set a passphrase for the keys. 
        
    3. Add the public key to your `~/.ssh/authorized_keys` file:
    
        ```
        cat ~/.ssh/127.0.0.1_dsa >> ~/.ssh/authorized_keys
        ```   
    
    4. Restart your SSH daemon to pick up the change to the `~/.ssh/authorized_keys` file. On MacOS run
        ```
        sudo launchctl stop com.openssh.sshd
        sudo launchctl start com.openssh.sshd
        ```

    5. Test that you can ssh into your own machine with the new key
        ```
        ssh -i ~/.ssh/foo_rsa <your-user-name>@127.0.0.1
        ```
        
### Run the build
To run a full build:
```
mvn clean install
```
To run a full build without tests:
```
mvn clean install -DskipTests
```
    