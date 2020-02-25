package com.instaclustr.sstable.generator;

import com.instaclustr.sstable.generator.cli.CLIApplication;
import com.instaclustr.sstable.generator.command.CSVBulkLoader;
import com.instaclustr.sstable.generator.command.FixedBulkLoader;
import com.instaclustr.sstable.generator.command.RandomBulkLoader;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(subcommands = {CSVBulkLoader.class, RandomBulkLoader.class, FixedBulkLoader.class},
    synopsisSubcommandLabel = "COMMAND",
    versionProvider = LoaderApplication.class
)
public class LoaderApplication extends CLIApplication {

    public static void main(String[] args) {
        execute(new CommandLine(new LoaderApplication()), args);
    }
}
