package com.instaclustr.sstable.generator.specs;

import java.nio.file.Path;

import picocli.CommandLine.Option;

public class BulkLoaderSpec {

    @Option(names = {"--output-dir", "-d"},
        paramLabel = "[DIRECTORY]",
        required = true,
        description = "Destination where SSTables will be generated.")
    public Path outputDir;

    @Option(names = {"--keyspace", "-k"},
        paramLabel = "[KEYSPACE]",
        required = true,
        description = "Keyspace for which SSTables will be generated.")
    public String keyspace;

    @Option(names = {"--table", "-t"},
        paramLabel = "[TABLE]",
        required = true,
        description = "Table for which SSTables will be generated.")
    public String table;

    @Option(names = {"--schema", "-s"},
        paramLabel = "[PATH]",
        required = true,
        description = "Path to CQL schema where CREATE TABLE statement is specified.")
    public Path schema;

    @Option(names = {"--sorted"},
        description = "Whether input data are already sorted (in terms of CQL)")
    public boolean sorted;

    @Option(names = {"--partitioner"},
        defaultValue = "murmur",
        description = "Paritioner used for SSTable generation, defaults to 'murmur'")
    public String partitioner;

    @Option(names = {"--bufferSize"},
        description = "How much data will be buffered before being written as a new SSTable, in megabytes. Defaults to 128",
        defaultValue = "128")
    public int bufferSize;

    @Option(names = {"--numberOfRecords"},
        defaultValue = "100",
        description = "Number of records to generate when using random command")
    public int numberOfRecords;

    @Option(names = {"--threads"},
        required = false,
        defaultValue = "1",
        description = "Number of threads to use for generation.")
    public int threads;

    @Option(names = {"--file", "-f"},
        description = "file to digest, irrelevant for random loader")
    public Path file;

    @Option(names = {"--generationImpl"},
        description = "Fully qualified class name of generation implementation. It has to implement RowMapper interface. "
            + "In case this flag is not used, a JAR on class path with the implementation has to be specified and it "
            + "has to contain implementation of RowMapper and it has to provide SPI entry in resources.")
    public String generationImplementation;

    @Override
    public String toString() {
        return "BulkLoaderSpec{" +
            "outputDir=" + outputDir +
            ", keyspace='" + keyspace + '\'' +
            ", table='" + table + '\'' +
            ", schema=" + schema +
            ", sorted=" + sorted +
            ", partitioner=" + partitioner +
            ", bufferSize=" + bufferSize +
            ", numberOfRecords=" + numberOfRecords +
            ", threads=" + threads +
            ", file=" + file +
            ", generationImplementation='" + generationImplementation + '\'' +
            '}';
    }
}
