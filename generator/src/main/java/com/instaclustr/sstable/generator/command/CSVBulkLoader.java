package com.instaclustr.sstable.generator.command;

import static java.lang.String.format;

import java.nio.file.Files;

import com.instaclustr.sstable.generator.BulkLoader;
import com.instaclustr.sstable.generator.Generator;
import com.instaclustr.sstable.generator.SSTableGenerator;
import com.instaclustr.sstable.generator.cli.CLIApplication;
import com.instaclustr.sstable.generator.exception.SSTableGeneratorException;
import com.instaclustr.sstable.generator.loader.CSVGenerator;
import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;
import picocli.CommandLine.Command;

@Command(name = "csv",
    mixinStandardHelpOptions = true,
    description = "tool for bulk-loading of data from csv",
    sortOptions = false,
    versionProvider = CLIApplication.class)
public class CSVBulkLoader extends BulkLoader {

    public static void main(final String[] args) {
        System.exit(CLIApplication.execute(new CSVBulkLoader(), args));
    }

    public Generator getLoader(final BulkLoaderSpec bulkLoaderSpec, final SSTableGenerator ssTableGenerator) {
        return new CSVGenerator(ssTableGenerator, () -> {
            try {
                if (!Files.exists(bulkLoaderSpec.file)) {
                    throw new IllegalStateException(format("File %s does not exist!", bulkLoaderSpec.file));
                }
                return CSVGenerator.CsvListReaderFactory.fromFile(bulkLoaderSpec.file.toFile());
            } catch (Exception ex) {
                throw new SSTableGeneratorException("Unable to create CsvListReader.", ex);
            }
        });
    }
}
