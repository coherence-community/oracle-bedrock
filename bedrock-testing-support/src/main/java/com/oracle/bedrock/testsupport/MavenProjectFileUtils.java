package com.oracle.bedrock.testsupport;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.stream.Stream;

import static org.junit.Assert.fail;

/**
 * A utility class to locate folders used during a Maven build.
 *
 * @author jk  2018.10.17
 */
public class MavenProjectFileUtils
{
    /**
     * This method is to locate the build folder.
     * <p>
     * In a Maven build this will be the module's target folder.
     * <p>
     * If the <code>project.build.directory</code> System property is
     * set then that location will be returned otherwise the
     * {@link java.security.ProtectionDomain} of the specified
     * test class will be used to locate the build folder.
     *
     * @param classTest the test class to use to locate the build folder
     * @return the top level build folder.
     */
    public static File locateBuildFolder(Class classTest)
    {
        String buildFolderProperty = System.getProperty("project.build.directory");
        File buildFolder;

        if (buildFolderProperty == null || buildFolderProperty.trim().isEmpty())
        {
            try
            {
                URL url = classTest.getProtectionDomain().getCodeSource().getLocation();

                File file = new File(url.toURI());

                while (!file.getName().equals("target"))
                {
                    file = file.getParentFile();
                }

                buildFolder = file;
            }
            catch (URISyntaxException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            buildFolder = new File(buildFolderProperty);

            if (buildFolder.exists())
            {
                if (!buildFolder.isDirectory())
                {
                    fail("The project.build.directory property is not a directory: " + buildFolderProperty);
                }
            }
            else
            {
                fail("The project.build.directory property was set to a non-existent folder: " + buildFolderProperty);
            }
        }

        return buildFolder;
    }


    /**
     * Locate the base folder to use for test output logs for the specified test class.
     * <p>
     * By default for a Maven build this will be <code>target/test-output/functional</code>.
     *
     * @param classTest the test class being executed
     * @return the location to put test logs
     */
    public static File ensureTestOutputBaseFolder(Class classTest)
    {
        File fileBuild = ensureFolders(locateBuildFolder(classTest));
        File fileTestOut = ensureFolders(new File(fileBuild, "test-output"));

        return ensureFolders(new File(fileTestOut, "functional"));
    }


    /**
     * Locate the folder to use for test output logs for the specified test class.
     * <p>
     * By default for a Maven build this will be <code>target/test-output/functional/class-name</code>.
     *
     * @param classTest the test class being executed
     * @param sSuffix   the folder suffix
     *
     * @return the location to put test logs
     */
    public static File ensureTestOutputFolder(Class classTest, String sSuffix)
    {
        File fileBuild = ensureFolders(locateBuildFolder(classTest));
        File fileTestOut = ensureFolders(new File(fileBuild, "test-output"));
        File fileTestOutFunctional = ensureFolders(new File(fileTestOut, "functional"));
        File fileTest = new File(fileTestOutFunctional, classTest.getSimpleName());

        if (sSuffix != null && !sSuffix.trim().isEmpty())
        {
            fileTest = new File(fileTest, sSuffix);
        }

        return ensureFolders(fileTest);
    }


    private static File ensureFolders(File file)
    {
        if (!file.exists())
        {
            file.mkdirs();
        }

        return file;
    }


    /**
     * Recursively delete the specified folder.
     *
     * @param folder the folder to delete
     */
    public static void recursiveDelete(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File each : files)
            {
                recursiveDelete(each);
            }
        }

        folder.delete();
    }

    /**
     * Determine if a file contains any line matching the
     * specified regex.
     *
     * @param file  the file to search
     * @param regex the regular expression to look for
     * @return {@code true} if the file contains a line matching
     * the regular expression
     */
    public static boolean fileContains(File file, String regex)
    {
        try
        {
            Stream<String> stream = Files.lines(file.toPath());
            return stream.anyMatch(s -> s.matches(regex));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("There was an error while reading the file" + file);
        }
        return false;
    }
}
