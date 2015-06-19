package com.facebook.presto.jdbc6.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.net.HostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.facebook.presto.jdbc6.utils.InputStreamRedirecter.redirect;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.util.concurrent.Executors.newCachedThreadPool;

public class Java6TestPrestoServer
{
    private static final Logger LOG = LoggerFactory.getLogger(Java6TestPrestoServer.class);
    private static final String JAVA_8_TESTING_PRESTO_SERVER_LAUNCHER_CLASSNAME = "com.facebook.presto.server.testing.TestingPrestoServerLauncher";
    private static final Splitter CLASSPATH_SPLITTER = Splitter.on(File.pathSeparatorChar);
    private static final Joiner CLASSPATH_JOINER = Joiner.on(File.pathSeparatorChar);
    private final List<Catalog> catalogs;
    private final List<String> pluginClassNames;
    private HostAndPort address;
    private Process process;
    private ExecutorService executorService;
    private final String java8Binary;

    public static class Catalog
    {
        public final String catalogName;
        public final String connectorName;

        public Catalog(String catalogName, String connectorName)
        {
            this.catalogName = catalogName;
            this.connectorName = connectorName;
        }
    }

    public Java6TestPrestoServer(List<String> pluginClassNames, List<Catalog> catalogs)
    {
        this.pluginClassNames = checkNotNull(pluginClassNames, "pluginClassNames is null");
        this.catalogs = checkNotNull(catalogs, "catalog is null");
        this.executorService = newCachedThreadPool();
        String java8Home = System.getProperty("java8.home");
        if (java8Home == null) {
            throw new IllegalStateException("java8.home system property not set");
        }
        this.java8Binary = java8Home + "/bin/java";
    }

    public void start()
            throws Exception
    {
        String myClassPath = getMyFilteredClassPath();

        List<String> command = newArrayList();

        command.add(java8Binary);
        command.add("-cp");
        command.add(myClassPath);
        command.add(JAVA_8_TESTING_PRESTO_SERVER_LAUNCHER_CLASSNAME);
        for (String pluginClassName : pluginClassNames) {
            command.add("--plugin");
            command.add(pluginClassName);
        }

        for (Catalog catalog : catalogs) {
            command.add("--catalog");
            command.add(catalog.catalogName + ":" + catalog.connectorName);
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(command);
        LOG.debug("starting testing presto server with cmd: {}", command);


        process = processBuilder.start();

        executorService = newCachedThreadPool();
        redirect(process.getErrorStream(), System.err, executorService);
        address = readAddressFrom(process.getInputStream());
        redirect(process.getInputStream(), System.out, executorService);
    }

    private HostAndPort readAddressFrom(InputStream inputStream)
            throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String addressLine = reader.readLine();
        checkState(addressLine != null, "could not read address line from server");
        ArrayList<String> addressElements = newArrayList(Splitter.on(":").split(addressLine));
        checkState(addressElements.size() == 2, "bad format of address line '" + addressLine + "'");
        return HostAndPort.fromParts(addressElements.get(0), parseInt(addressElements.get(1)));
    }

    private String getMyFilteredClassPath()
    {
        String classPathString = System.getProperty("java.class.path");
        final String javaHome = System.getProperty("java.home");

        return CLASSPATH_JOINER.join(filter(
                copyOf(CLASSPATH_SPLITTER.split(classPathString)),
                new Predicate<String>()
                {
                    @Override
                    public boolean apply(String path)
                    {
                        return !path.startsWith(javaHome);
                    }
                }));
    }

    public HostAndPort getAddress()
    {
        Preconditions.checkState(address != null, "server not initialized");
        return address;
    }

    public void close()
    {
        process.destroy();
        executorService.shutdown();
    }
}
