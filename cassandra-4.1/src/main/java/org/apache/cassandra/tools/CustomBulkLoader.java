package org.apache.cassandra.tools;

import static com.datastax.driver.core.JdkSSLOptions.builder;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.EncryptionOptions;
import org.apache.cassandra.io.sstable.SSTableLoader;
import org.apache.cassandra.security.SSLFactory;
import org.apache.cassandra.streaming.StreamResultFuture;
import org.apache.cassandra.tools.BulkLoader.ExternalClient;
import org.apache.cassandra.tools.BulkLoader.ProgressIndicator;
import org.apache.cassandra.utils.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBulkLoader {

    private static final Logger logger = LoggerFactory.getLogger(CustomBulkLoader.class);

    public static void main(final String[] args) throws Exception {
        LoaderOptions options = LoaderOptions.builder().parseArgs(args).build();
        load(options);
    }

    public static void load(LoaderOptions options) throws Exception {
        // Upstream version contain this, we have to comment this out in order not to fail when using it from our BulkLoader
        //DatabaseDescriptor.toolInitialization(false);

        final OutputHandler handler = new OutputHandler.SystemOutput(options.verbose, options.debug);

        final SSTableLoader loader = new SSTableLoader(
            options.directory,
            new ExternalClient(
                options.hosts,
                options.storagePort,
                options.authProvider,
                options.serverEncOptions,
                buildSSLOptions(options.clientEncOptions)),
            handler,
            options.connectionsPerHost,
            options.targetKeyspace);

        DatabaseDescriptor.setStreamThroughputOutboundBytesPerSec(options.throttleBytes);
        DatabaseDescriptor.setInterDCStreamThroughputOutboundBytesPerSec(options.interDcThrottleBytes);
        StreamResultFuture future;

        ProgressIndicator indicator = new ProgressIndicator();

        try {
            if (options.noProgress) {
                future = loader.stream(options.ignores);
            } else {
                future = loader.stream(options.ignores, indicator);
            }

        } catch (Exception e) {
            logger.error("Unable to load sstables!", e.getCause());
            e.printStackTrace(System.err);
            throw e;
        }

        try {
            future.get();

            // Give sockets time to gracefully close
            Thread.sleep(1000);
            // System.exit(0); // We need that to stop non daemonized threads
        } catch (Exception e) {
            logger.error("Streaming to the following hosts failed: {}", loader.getFailedHosts());
            e.printStackTrace(System.err);
            throw e;
        }
    }

    private static com.datastax.driver.core.SSLOptions buildSSLOptions(EncryptionOptions encryptionOptions) {

        if (!encryptionOptions.getEnabled()) {
            return null;
        }

        SSLContext sslContext;
        try {
            sslContext = SSLFactory.createSSLContext(encryptionOptions, true);
        } catch (IOException e) {
            throw new RuntimeException("Could not create SSL Context.", e);
        }

        return builder()
            .withSSLContext(sslContext)
            .withCipherSuites(encryptionOptions.cipher_suites.toArray(new String[0]))
            .build();
    }
}