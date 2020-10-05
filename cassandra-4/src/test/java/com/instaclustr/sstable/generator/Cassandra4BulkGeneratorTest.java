package com.instaclustr.sstable.generator;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.github.nosan.embedded.cassandra.EmbeddedCassandraFactory;
import com.github.nosan.embedded.cassandra.api.Cassandra;
import com.github.nosan.embedded.cassandra.api.Version;
import com.github.nosan.embedded.cassandra.artifact.Artifact;
import com.instaclustr.sstable.generator.cli.CLIApplication;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;
import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;
import com.instaclustr.sstable.generator.specs.CassandraBulkLoaderSpec;
import com.instaclustr.sstable.generator.specs.CassandraBulkLoaderSpec.CassandraVersion;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.tools.Cassandra4CustomBulkLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import picocli.CommandLine.Command;

@RunWith(JUnit4.class)
public class Cassandra4BulkGeneratorTest {

    private static final String CASSANDRA_VERSION = System.getProperty("cassandra.version", "4.0-beta2");

    private static final String KEYSPACE = "test";

    private static final String TABLE = "test";

    private static Artifact CASSANDRA_ARTIFACT = Artifact.ofVersion(Version.of(CASSANDRA_VERSION));

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testBulkLoading() {

        final Path cassandraDir = new File("target/cassandra").toPath().toAbsolutePath();

        EmbeddedCassandraFactory cassandraFactory = new EmbeddedCassandraFactory();
        cassandraFactory.setWorkingDirectory(cassandraDir);
        cassandraFactory.setArtifact(CASSANDRA_ARTIFACT);
        cassandraFactory.getJvmOptions().add("-Xmx1g");
        cassandraFactory.getJvmOptions().add("-Xms1g");
        Cassandra cassandra = null;

        try {
            cassandra = cassandraFactory.create();
            cassandra.start();

            waitForCql();

            executeWithSession(session -> {
                session.execute(String.format("CREATE KEYSPACE %s WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };", KEYSPACE));
                session.execute(String.format("CREATE TABLE IF NOT EXISTS %s.%s (id uuid, name text, surname text, PRIMARY KEY (id));", KEYSPACE, TABLE));
            });

            System.setProperty("cassandra.storagedir", cassandraDir.resolve("data").toAbsolutePath().toString());
            System.setProperty("cassandra.config", "file://" + findCassandraYaml(new File("target/cassandra/conf").toPath()).toAbsolutePath().toString());

            DatabaseDescriptor.toolInitialization(false);

            // SSTable generation

            final BulkLoaderSpec bulkLoaderSpec = new BulkLoaderSpec();

            bulkLoaderSpec.bufferSize = 128;
            bulkLoaderSpec.file = Paths.get("");
            bulkLoaderSpec.keyspace = KEYSPACE;
            bulkLoaderSpec.table = TABLE;
            bulkLoaderSpec.partitioner = "murmur";
            bulkLoaderSpec.sorted = false;
            bulkLoaderSpec.threads = 1;

            bulkLoaderSpec.generationImplementation = TestFixedImplementation.class.getName();
            bulkLoaderSpec.outputDir = folder.getRoot().toPath();
            bulkLoaderSpec.schema = Paths.get(new File("src/test/resources/cassandra/cql/table.cql").getAbsolutePath());

            final BulkLoader bulkLoader = new TestBulkLoader();

            bulkLoader.bulkLoaderSpec = bulkLoaderSpec;

            bulkLoader.run();

            // Cassandra load

            final CassandraBulkLoaderSpec cassandraBulkLoaderSpec = new CassandraBulkLoaderSpec();
            cassandraBulkLoaderSpec.node = "127.0.0.1";
            cassandraBulkLoaderSpec.cassandraYaml = findCassandraYaml(new File("target/cassandra/conf").toPath());
            cassandraBulkLoaderSpec.sstablesDir = Paths.get(folder.getRoot().getAbsolutePath(), KEYSPACE, TABLE);
            cassandraBulkLoaderSpec.keyspace = "test";
            cassandraBulkLoaderSpec.cassandraVersion = CassandraVersion.V4;

            final CassandraBulkLoader cassandraBulkLoader = new Cassandra4CustomBulkLoader();
            cassandraBulkLoader.cassandraBulkLoaderSpec = cassandraBulkLoaderSpec;

            cassandraBulkLoader.run();

            executeWithSession(session -> assertFalse(session.execute(select().from(KEYSPACE, TABLE)).all().isEmpty()));
        } finally {
            cassandra.stop();
        }
    }

    private void waitForCql() {
        org.awaitility.Awaitility.await()
            .pollInterval(10, TimeUnit.SECONDS)
            .pollInSameThread()
            .timeout(1, TimeUnit.MINUTES)
            .until(() -> {
                try (final Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build()) {
                    cluster.connect();
                    return true;
                } catch (final Exception ex) {
                    return false;
                }
            });
    }

    public void executeWithSession(Consumer<Session> supplier) {
        try (final Cluster cluster = Cluster.builder().addContactPoint("127.0.0.1").build()) {
            try (final Session session = cluster.connect()) {
                supplier.accept(session);
            }
        }
    }

    private Path findCassandraYaml(final Path confDir) {

        try {
            return Files.list(confDir)
                .filter(path -> path.getFileName().toString().contains("-cassandra.yaml"))
                .findFirst()
                .orElseThrow(RuntimeException::new);
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to list or there is not any file ending on -cassandra.yaml" + confDir);
        }
    }

    @Command(name = "fixed",
        mixinStandardHelpOptions = true,
        description = "tool for bulk-loading of fixed data",
        sortOptions = false,
        versionProvider = CLIApplication.class)
    public static final class TestBulkLoader extends BulkLoader {

        @Override
        public Generator getLoader(final BulkLoaderSpec bulkLoaderSpec, final SSTableGenerator ssTableWriter) {
            return new TestGenerator(ssTableWriter);
        }

        private static final class TestGenerator implements Generator {

            private final SSTableGenerator ssTableGenerator;

            public TestGenerator(final SSTableGenerator ssTableGenerator) {
                this.ssTableGenerator = ssTableGenerator;
            }

            @Override
            public void generate(final RowMapper rowMapper) {
                try {
                    ssTableGenerator.generate(rowMapper.get().filter(Objects::nonNull).map(MappedRow::new).iterator());
                } catch (final Exception ex) {
                    throw new SSTableGeneratorException("Unable to generate SSTables from FixedLoader.", ex);
                }
            }
        }
    }
}
