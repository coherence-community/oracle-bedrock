package com.oracle.bedrock.runtime.k8s;

import java.util.List;
import java.util.Objects;

/**
 * A representation of a Kubernetes Pod container.
 *
 * @author Jonathan Knight
 */
public class PodContainer
        extends Pod
{
    /**
     * The name of this container.
     */
    private final String containerName;


    /**
     * Create a {@link PodContainer} in a Pod in the default namespace.
     *
     * @param k8s            the {@link K8sCluster} containing the parent Pod
     * @param podName        the name of the Pod
     * @param containerName  the name of this container
     */
    public PodContainer(K8sCluster k8s, String podName, String containerName)
    {
        this(k8s, podName, containerName, null);
    }


    /**
     * Create a {@link PodContainer}.
     *
     * @param k8s            the {@link K8sCluster} containing the parent Pod
     * @param podName        the name of the Pod
     * @param containerName  the name of this container
     * @param namespace      the namespace that the parent Pod is running in
     */
    public PodContainer(K8sCluster k8s, String podName, String containerName, String namespace)
    {
        super(k8s, podName, namespace);

        if (containerName == null || containerName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Container name cannot be null or blank");
        }

        this.containerName = containerName;
    }


    /**
     * Obtain the name of this container.
     *
     * @return  the name of this container
     */
    public String getContainerName()
    {
        return containerName;
    }


    /**
     * Obtain the base kubectl arguments.
     *
     * @param command  the kubectl command
     *
     * @return  the base kubectl arguments
     */
    @Override
    protected List<String> getArgs(String command)
    {
        List<String> args = super.getArgs(command);

        args.add("--container");
        args.add(containerName);

        return args;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        PodContainer that = (PodContainer) o;
        return Objects.equals(getNamespace(), that.getNamespace())
                && Objects.equals(getPodName(), that.getPodName())
                && Objects.equals(containerName, that.containerName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getPodName(), getNamespace(), containerName);
    }
}
