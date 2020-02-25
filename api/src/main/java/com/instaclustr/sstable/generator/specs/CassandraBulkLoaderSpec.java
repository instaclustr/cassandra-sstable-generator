package com.instaclustr.sstable.generator.specs;

import java.nio.file.Path;

import picocli.CommandLine.Option;

public class CassandraBulkLoaderSpec {

    @Option(names = {"--cassandra-yaml"},
        description = "Path to cassandra.yaml file for loading generated SSTables to Cassandra")
    public Path cassandraYaml;

    @Option(names = {"--nodes"},
        description = "Comma separated list of nodes to stream generated SSTables to.")
    public String node = "127.0.0.1";

    @Option(names = {"--sstables-dir", "-ss"},
        paramLabel = "[DIRECTORY]",
        required = true,
        description = "Source of SSTables to stream.")
    public Path sstablesDir;

    @Override
    public String toString() {
        return "CassandraBulkLoaderSpec{" +
            "cassandraYaml=" + cassandraYaml +
            ", node='" + node + '\'' +
            ", sstablesDir=" + sstablesDir +
            '}';
    }
}
