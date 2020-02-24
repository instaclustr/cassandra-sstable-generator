package org.apache.cassandra.tools;

import java.util.ArrayList;
import java.util.Collection;

import com.instaclustr.sstable.generator.cli.CLIApplication;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;
import com.instaclustr.sstable.generator.specs.CassandraBulkLoaderSpec;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "load",
    mixinStandardHelpOptions = true,
    description = "tool for bulk-loading of data to Cassandra",
    sortOptions = false,
    versionProvider = CLIApplication.class)
public class CassandraBulkLoader implements Runnable {

    @Mixin
    public CassandraBulkLoaderSpec cassandraBulkLoaderSpec;

    public CassandraBulkLoader() {

    }

    public CassandraBulkLoader(final CassandraBulkLoaderSpec cassandraBulkLoaderSpec) {
        this.cassandraBulkLoaderSpec = cassandraBulkLoaderSpec;
    }

    @Override
    public void run() {
        final Collection<String> flags = new ArrayList<>();

        flags.add("-v");
        flags.add("-d");
        flags.add(cassandraBulkLoaderSpec.node);

        flags.add("-f");
        flags.add(cassandraBulkLoaderSpec.cassandraYaml.toFile().getAbsolutePath());

        flags.add(cassandraBulkLoaderSpec.sstablesDir.toString());

        try {
            run0(flags.toArray(new String[]{}));
        } catch (final Throwable t) {
            throw new SSTableGeneratorException("Error occurred while loading SSTables into Cassandra.", t);
        }
    }

    public void run0(final String[] flags) {
        throw new IllegalStateException("Override and implement this method.");
    }
}
