package com.instaclustr.sstable.generator.specs;

import java.nio.file.Path;

import picocli.CommandLine.ITypeConverter;
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

    @Option(names = {"--keyspace", "-ks"},
        description = "Keyspace to stream, use for Cassandra 4.")
    public String keyspace;

    @Option(names = {"--cassandra-version"},
        converter = CassandraVersionConverter.class,
        description = "Version of Cassandra to load for, might be 2, 3 or 4, defaults to 3")
    public CassandraVersion cassandraVersion;

    private static final class CassandraVersionConverter implements ITypeConverter<CassandraVersion> {

        @Override
        public CassandraVersion convert(final String value) throws Exception {
            return CassandraVersion.parse(value);
        }
    }

    public enum CassandraVersion {
        V2("2"),
        V3("3"),
        V4("4");

        final String version;

        CassandraVersion(final String version) {
            this.version = version;
        }

        public static CassandraVersion parse(final String version) {
            if (version == null) {
                return V3;
            }

            if (version.equals("3")) {
                return V3;
            }

            if (version.equals("4")) {
                return V4;
            }

            return V3;
        }
    }

    @Override
    public String toString() {
        return "CassandraBulkLoaderSpec{" +
            "cassandraYaml=" + cassandraYaml +
            ", node='" + node + '\'' +
            ", sstablesDir=" + sstablesDir +
            ", keyspace=" + keyspace +
            '}';
    }
}
