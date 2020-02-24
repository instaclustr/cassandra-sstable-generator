package org.apache.cassandra.tools;

import com.instaclustr.sstable.generator.cli.CLIApplication;
import picocli.CommandLine;

public class Cassandra3CustomBulkLoader extends CassandraBulkLoader {

    public static void main(String[] args) {
        System.exit(CLIApplication.execute(new CommandLine(new CassandraBulkLoader()), args));
    }

    @Override
    public void run0(final String[] flags) {
        try {
            CustomBulkLoader.main(flags);
        } catch (BulkLoadException ex) {
            throw new RuntimeException(ex);
        }
    }
}
