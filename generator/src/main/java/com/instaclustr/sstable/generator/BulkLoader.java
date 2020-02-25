package com.instaclustr.sstable.generator;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import com.instaclustr.sstable.generator.cli.JarManifestVersionProvider;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;
import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

public abstract class BulkLoader implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(BulkLoader.class);

    @Spec
    public CommandSpec spec;

    @Mixin
    public BulkLoaderSpec bulkLoaderSpec;

    public abstract Generator getLoader(final BulkLoaderSpec bulkLoaderSpec,
                                        final SSTableGenerator ssTableGenerator);

    public void run() {
        JarManifestVersionProvider.logCommandVersionInformation(spec);

        final RowMapper rowMapper = getRowMapper();

        final GenerationThread[] threads = new GenerationThread[bulkLoaderSpec.threads];

        final SSTableGenerator ssTableGenerator = getSSTableGenerator().withBulkLoaderSpec(bulkLoaderSpec).withRowMapper(rowMapper);

        for (int i = 0; i < bulkLoaderSpec.threads; i++) {
            threads[i] = new GenerationThread(getLoader(bulkLoaderSpec, ssTableGenerator), getRowMapper());
            threads[i].start();
        }

        while (!Arrays.stream(threads).allMatch(val -> val.status)) {
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private SSTableGenerator getSSTableGenerator() {
        final ServiceLoader<SSTableGenerator> serviceLoader = ServiceLoader.load(SSTableGenerator.class);

        final Iterator<SSTableGenerator> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }

        throw new SSTableGeneratorException("Unable to locate an instance of SSTableGenerator on the class path.");
    }

    private RowMapper getRowMapper() {
        RowMapper rowMapper = getRowMapperFromFlag()
            .orElseGet(() -> getRowMapperFromServiceLoader().orElseThrow(() -> new SSTableGeneratorException("Unable to create an instace of RowMapper.")));

        if (rowMapper.insertStatement() == null) {
            throw new SSTableGeneratorException(format("RowMapper implementation %s has insertStatement() method returning null.",
                                                       rowMapper.getClass().getCanonicalName()));
        }

        return rowMapper;
    }

    private Optional<RowMapper> getRowMapperFromFlag() {
        if (bulkLoaderSpec.generationImplementation != null) {
            try {
                final Class<?> aClass = Class.forName(bulkLoaderSpec.generationImplementation);
                final Object o = aClass.newInstance();

                if (o instanceof RowMapper) {
                    return Optional.of((RowMapper) o);
                }
            } catch (final Throwable ex) {
                logger.error(format("Unable to instantiate class %s: %s", bulkLoaderSpec.generationImplementation, ex.getMessage()));
            }
        }

        return Optional.empty();
    }

    private Optional<RowMapper> getRowMapperFromServiceLoader() {
        final ServiceLoader<RowMapper> serviceLoader = ServiceLoader.load(RowMapper.class);

        final Iterator<RowMapper> iterator = serviceLoader.iterator();

        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        }

        return Optional.empty();
    }

    private static class GenerationThread extends Thread {

        private final Generator generator;
        private final RowMapper rowMapper;
        private boolean status = false;

        public GenerationThread(final Generator generator,
                                final RowMapper rowMapper) {
            this.generator = generator;
            this.rowMapper = rowMapper;
        }

        @Override
        public void run() {
            generator.generate(rowMapper);
            status = true;
        }
    }
}
