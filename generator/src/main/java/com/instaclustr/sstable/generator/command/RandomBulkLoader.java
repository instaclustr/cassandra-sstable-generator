package com.instaclustr.sstable.generator.command;

import com.instaclustr.sstable.generator.BulkLoader;
import com.instaclustr.sstable.generator.Generator;
import com.instaclustr.sstable.generator.SSTableGenerator;
import com.instaclustr.sstable.generator.cli.CLIApplication;
import com.instaclustr.sstable.generator.loader.RandomGenerator;
import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;
import picocli.CommandLine.Command;

@Command(name = "random",
         mixinStandardHelpOptions = true,
         description = "tool for bulk-loading of random data",
         sortOptions = false,
         versionProvider = CLIApplication.class)
public class RandomBulkLoader extends BulkLoader {

    public static void main(final String[] args) {
        System.exit(CLIApplication.execute(new RandomBulkLoader(), args));
    }

    @Override
    public Generator getLoader(final BulkLoaderSpec bulkLoaderSpec, final SSTableGenerator ssTableWriter) {
        return new RandomGenerator(ssTableWriter, bulkLoaderSpec.numberOfRecords);
    }
}
