package com.instaclustr.sstable.generator.command;

import com.instaclustr.sstable.generator.BulkLoader;
import com.instaclustr.sstable.generator.Generator;
import com.instaclustr.sstable.generator.SSTableGenerator;
import com.instaclustr.sstable.generator.cli.CLIApplication;
import com.instaclustr.sstable.generator.loader.FixedGenerator;
import com.instaclustr.sstable.generator.specs.BulkLoaderSpec;
import picocli.CommandLine.Command;

@Command(name = "fixed",
         mixinStandardHelpOptions = true,
         description = "tool for bulk-loading of fixed data",
         sortOptions = false,
         versionProvider = CLIApplication.class)
public class FixedBulkLoader extends BulkLoader {

    @Override
    public Generator getLoader(final BulkLoaderSpec bulkLoaderSpec, final SSTableGenerator ssTableWriter) {
        return new FixedGenerator(ssTableWriter);
    }
}
