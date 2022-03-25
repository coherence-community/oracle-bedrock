/*
 * File: P4CopyrightRangeProvider.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.bedrock.maven;

import com.mycila.maven.plugin.license.AbstractLicenseMojo;
import com.mycila.maven.plugin.license.PropertiesProvider;
import com.mycila.maven.plugin.license.document.Document;
import com.perforce.p4java.PropertyDefs;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An implementation of {@link PropertiesProvider} that adds {@value #COPYRIGHT_LAST_YEAR_KEY} and
 * {@value #COPYRIGHT_YEARS_KEY} values - see
 * {@link #getAdditionalProperties(AbstractLicenseMojo, Properties, Document)}.
 *
 * @author Jonathan Knight
 */
public class P4CopyrightRangeProvider
        implements PropertiesProvider
{

    public static final String COPYRIGHT_LAST_YEAR_KEY = "license.p4.copyrightLastYear";
    public static final String COPYRIGHT_CREATION_YEAR_KEY = "license.p4.copyrightCreationYear";
    public static final String COPYRIGHT_LAST_YEAR_TIME_ZONE_KEY = "license.p4.copyrightLastYearTimeZone";
    public static final String COPYRIGHT_YEARS_KEY = "license.p4.copyrightYears";
    public static final String INCEPTION_YEAR_KEY = "project.inceptionYear";

    private final Map<File, P4Lookup> p4LookupMap = new ConcurrentHashMap<>();

    public P4CopyrightRangeProvider()
    {
    }

    /**
     * Returns an unmodifiable map containing the three entries {@value #COPYRIGHT_LAST_YEAR_KEY}, {@value #COPYRIGHT_YEARS_KEY},
     * and {@value #COPYRIGHT_CREATION_YEAR_KEY}, whose values are set based on inspecting git history.
     *
     * <ul>
     * <li>{@value #COPYRIGHT_LAST_YEAR_KEY} key stores the year from the committer date of the last git commit that has
     * modified the supplied {@code document}.
     * <li>{@value #COPYRIGHT_YEARS_KEY} key stores the range from {@value #INCEPTION_YEAR_KEY} value to
     * {@value #COPYRIGHT_LAST_YEAR_KEY} value. If both values a equal, only the {@value #INCEPTION_YEAR_KEY} value is
     * returned; otherwise, the two values are combined using dash, so that the result is e.g. {@code "2000 - 2010"}.
     * <li>{@value #COPYRIGHT_CREATION_YEAR_KEY} key stores the year from the committer date of the first git commit for
     * the supplied {@code document}.
     * </ul>
     * The {@value #INCEPTION_YEAR_KEY} value is read from the supplied properties and it must available. Otherwise a
     * {@link RuntimeException} is thrown.
     */
    @Override
    public Map<String, String> getAdditionalProperties(AbstractLicenseMojo mojo, Properties properties, Document document)
    {
        mojo.debug("Obtain additional properties for baseDir=%s doc=%s", mojo.defaultBasedir, document.getFile());

        String inceptionYear = properties.getProperty(INCEPTION_YEAR_KEY);
        if (inceptionYear == null)
        {
            throw new RuntimeException("'" + INCEPTION_YEAR_KEY + "' must have a value for file "
                                       + document.getFile().getAbsolutePath());
        }
        final int inceptionYearInt;
        try
        {
            inceptionYearInt = Integer.parseInt(inceptionYear);
        }
        catch (NumberFormatException e1)
        {
            throw new RuntimeException("'" + INCEPTION_YEAR_KEY + "' must be an integer ; found = " + inceptionYear + " file: "
                                       + document.getFile().getAbsolutePath());
        }

        try
        {
            Map<String, String> result = new HashMap<>(3);
            P4Lookup p4Lookup = getP4Lookup(mojo, properties);
            int copyrightEnd = p4Lookup.getYearOfLastChange(mojo, document.getFile());
            result.put(COPYRIGHT_LAST_YEAR_KEY, Integer.toString(copyrightEnd));
            String copyrightYears;
            if (inceptionYearInt >= copyrightEnd)
            {
                copyrightYears = inceptionYear;
            }
            else
            {
                copyrightYears = inceptionYear + ", " + copyrightEnd;
            }
            result.put(COPYRIGHT_YEARS_KEY, copyrightYears);

            int copyrightStart = 2000;
            result.put(COPYRIGHT_CREATION_YEAR_KEY, Integer.toString(copyrightStart));

            mojo.debug("Obtained additional properties: %s", result);

            return Collections.unmodifiableMap(result);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not compute the year of the last P4 commit for file "
                                       + document.getFile().getAbsolutePath(), e);
        }
    }

    private synchronized P4Lookup getP4Lookup(AbstractLicenseMojo mojo, Properties props) {
        String path = mojo.defaultBasedir.getAbsolutePath();
        mojo.debug("Find P4Lookup for basedir %s", path);

        for (Map.Entry<File, P4Lookup> entry : p4LookupMap.entrySet()) {
            mojo.debug("Checking existing P4Lookup for basedir %s", entry.getKey());
            if (path.startsWith(entry.getKey().getAbsolutePath()))
            {
                mojo.debug("Found existing P4Lookup for basedir %s", entry.getKey());
                return entry.getValue();
            }
        }
        return p4LookupMap.computeIfAbsent(mojo.defaultBasedir, k -> createP4Lookup(mojo, k, props));
    }

    private P4Lookup createP4Lookup(AbstractLicenseMojo mojo, File baseDir, Properties props)
    {
        mojo.debug("Creating P4Lookup for basedir %s", baseDir);

        try
        {
            TimeZone timeZone;
            String tzString = props.getProperty(COPYRIGHT_LAST_YEAR_TIME_ZONE_KEY);
            timeZone = tzString == null ? P4Lookup.DEFAULT_ZONE : TimeZone.getTimeZone(tzString);

            String serverName = String.format("p4java://%s", props.getProperty(PropertyDefs.P4JAVA_PROP_KEY_PREFIX + "server"));

            IServer server = ServerFactory.getServer(serverName, props);
            server.connect();
            server.setUserName(props.getProperty(PropertyDefs.USER_NAME_KEY));
            server.login(props.getProperty(PropertyDefs.PASSWORD_KEY));
            server.setWorkingDirectory(baseDir.getAbsolutePath());

            String clientName = props.getProperty(PropertyDefs.CLIENT_NAME_KEY);
            IClient client = server.getClient(clientName);
            if (client != null)
            {
                server.setCurrentClient(client);
            }

            List<IFileSpec> specs = FileSpecBuilder.makeFileSpecList(baseDir.getAbsolutePath() + "/...");
            IFileSpec topSpec = specs.get(0);
            
            IFileSpec fileSpec = client.localWhere(specs).get(0);
            String depotPath = topSpec.getDepotPathString();
            int depotPathLen = depotPath.length() - 3;
            String localPath = fileSpec.getLocalPathString();
            String prefix = localPath.substring(0, localPath.length() - 3);

            List<IFileSpec> depotFiles = server.getDepotFiles(specs, false);
            mojo.debug("Found %d P4 depot files under %s", depotFiles.size(), specs.get(0));

            Calendar calendar = Calendar.getInstance(timeZone);
            int thisYear = calendar.get(Calendar.YEAR);

            Map<String, Integer> years = new HashMap<>();

            for (IFileSpec spec : depotFiles)
            {
                String path = spec.getDepotPathString();
                String name = prefix + path.substring(depotPathLen);
                Date date = spec.getDate();
                if (date != null)
                {
                    calendar.setTimeInMillis(date.getTime());
                    years.put(name, calendar.get(Calendar.YEAR));
                }
                else
                {
                    years.put(name, thisYear);
                }
            }

            List<IFileSpec> diffFiles = client.getDiffFiles(specs, Integer.MAX_VALUE, true, true,
                                                            false, false, false,
                                                            false, false);
            for (IFileSpec spec : diffFiles)
            {
                String path = spec.getDepotPathString();
                if (path != null)
                {
                    String name = prefix + path.substring(depotPathLen);
                    Date date = spec.getDate();
                    if (date != null)
                    {
                        calendar.setTimeInMillis(date.getTime());
                        years.put(name, calendar.get(Calendar.YEAR));
                    }
                    else
                    {
                        years.put(name, thisYear);
                    }
                }
            }

            server.disconnect();

            return new P4Lookup(years, thisYear);
        }
        catch (URISyntaxException | P4JavaException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * A holder for a map of P4 file names to the year of latest modification.
     */
    public static class P4Lookup
    {
        public static final TimeZone DEFAULT_ZONE = TimeZone.getTimeZone("GMT");

        private final Map<String, Integer> years;
        private final int currentYear;

        public P4Lookup(Map<String, Integer> years, int currentYear)
        {
            this.years = years;
            this.currentYear = currentYear;
        }

        int getYearOfLastChange(AbstractLicenseMojo mojo, File file)
        {
            String path = file.getAbsolutePath();
            Integer year = years.get(path);
            if (year == null)
            {
                mojo.debug("Cannot find year in P4 for file %s", path);
                return currentYear;
            }
            mojo.debug("Year in P4 for file %s is %d", path, year);
            return year;
        }
    }
}
