package com.instaclustr.sstable.generator;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;
import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Murmur3Partitioner;
import org.apache.cassandra.io.sstable.CQLSSTableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cassandra3SSTableGenerator extends SSTableGenerator {

    private static final Logger logger = LoggerFactory.getLogger(Cassandra3SSTableGenerator.class);

    @Override
    public void generate(final Iterator<MappedRow> rowsIterator) {

        MappedRow actualRow = null;

        try (final CQLSSTableWriter writer = getWriter(spec, rowMapper)) {
            while (rowsIterator.hasNext()) {

                actualRow = rowsIterator.next();

                if (actualRow.values != null && !actualRow.values.isEmpty()) {
                    writer.addRow(actualRow.values);
                }
            }
        } catch (IOException ex) {
            if (actualRow != null) {
                logger.error(format("Unable to write row using values %s with types %s", actualRow.values, actualRow.types), ex);
            } else {
                logger.error("Unable to write", ex);
            }
        }
    }

    @Override
    public CQLSSTableWriter getWriter(BulkLoaderSpec spec, RowMapper rowMapper) {
        final Path tableDir = spec.outputDir.resolve(spec.keyspace).resolve(spec.table);

        if (!Files.exists(tableDir)) {
            try {
                Files.createDirectories(tableDir);
            } catch (IOException ex) {
                throw new SSTableGeneratorException(format("Unable to create directory %s", tableDir), ex.getCause());
            }
        }

        final CQLSSTableWriter.Builder builder = CQLSSTableWriter.builder();

        String createSchemaStatement;

        try {
            if (!Files.exists(spec.schema)) {
                throw new IllegalStateException(format("Schema file %s does not exist!", spec.schema));
            }

            createSchemaStatement = new String(Files.readAllBytes(spec.schema));
        } catch (Exception ex) {
            throw new SSTableGeneratorException(format("Unable to read schema at %s", spec.schema), ex);
        }

        builder.inDirectory(tableDir.toFile())
            .forTable(createSchemaStatement)
            .using(rowMapper.insertStatement())
            .withBufferSizeInMB(spec.bufferSize);

        if (spec.sorted) {
            builder.sorted();
        }

        builder.withPartitioner(new PartitionerConverter().convert(spec.partitioner));

        return builder.build();
    }

    protected static class PartitionerConverter extends com.instaclustr.sstable.generator.PartitionerConverter<IPartitioner> {

        @Override
        public IPartitioner convert(final String value) {
            if (Partitioner.parse(value) == Partitioner.MURMUR) {
                return Murmur3Partitioner.instance;
            }
            throw new IllegalStateException(format("Unsupported partitioner '%s', supported are: %s", value, asList(Partitioner.values())));
        }
    }
}
