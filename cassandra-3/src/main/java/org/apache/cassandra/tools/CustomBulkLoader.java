package org.apache.cassandra.tools;

import static com.datastax.driver.core.JdkSSLOptions.builder;

import javax.net.ssl.SSLContext;
import java.io.IOException;

import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.EncryptionOptions;
import org.apache.cassandra.io.sstable.SSTableLoader;
import org.apache.cassandra.security.SSLFactory;
import org.apache.cassandra.streaming.StreamResultFuture;
import org.apache.cassandra.tools.BulkLoader.ProgressIndicator;
import org.apache.cassandra.utils.JVMStabilityInspector;
import org.apache.cassandra.utils.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomBulkLoader {

    private static final Logger logger = LoggerFactory.getLogger(CustomBulkLoader.class);

    public static void main(String[] args) throws BulkLoadException {
        LoaderOptions options = LoaderOptions.builder().parseArgs(args).build();
        load(options);
    }

    public static void load(LoaderOptions options) throws BulkLoadException {

        // Upstream version contain this, we have to comment this out in order not to fail when using it from our BulkLoader
        // DatabaseDescriptor.toolInitialization();

        OutputHandler handler = new OutputHandler.SystemOutput(options.verbose, options.debug);

        SSTableLoader loader = new SSTableLoader(
            options.directory.getAbsoluteFile(),
            new BulkLoader.ExternalClient(
                options.hosts,
                options.nativePort,
                options.authProvider,
                options.storagePort,
                options.sslStoragePort,
                options.serverEncOptions,
                buildSSLOptions(options.clientEncOptions)),
            handler,
            options.connectionsPerHost);

        DatabaseDescriptor.setStreamThroughputOutboundMegabitsPerSec(options.throttle);
        DatabaseDescriptor.setInterDCStreamThroughputOutboundMegabitsPerSec(options.interDcThrottle);
        StreamResultFuture future;

        ProgressIndicator indicator = new ProgressIndicator();

        try {
            if (options.noProgress) {
                future = loader.stream(options.ignores);
            } else {
                future = loader.stream(options.ignores, indicator);
            }

        } catch (Exception e) {
            JVMStabilityInspector.inspectThrowable(e);
            System.err.println(e.getMessage());
            if (e.getCause() != null) {
                logger.error("", e.getCause());
            }
            throw new RuntimeException(e);
        }

        try {
            future.get();

            // Give sockets time to gracefully close
            Thread.sleep(1000);
            // System.exit(0); // We need that to stop non daemonized threads
        } catch (Exception e) {
            System.err.println("Streaming to the following hosts failed:");
            System.err.println(loader.getFailedHosts());
            e.printStackTrace(System.err);
            throw new BulkLoadException(e);
        }
    }

    private static com.datastax.driver.core.SSLOptions buildSSLOptions(EncryptionOptions.ClientEncryptionOptions clientEncryptionOptions) {

        if (!clientEncryptionOptions.enabled) {
            return null;
        }

        SSLContext sslContext;
        try {
            sslContext = SSLFactory.createSSLContext(clientEncryptionOptions, true);
        } catch (IOException e) {
            throw new RuntimeException("Could not create SSL Context.", e);
        }

        return builder()
            .withSSLContext(sslContext)
            .withCipherSuites(clientEncryptionOptions.cipher_suites)
            .build();
    }
}
