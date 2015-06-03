package com.nocompany.coherence.incubator.examples;


import com.nocompany.coherence.incubator.examples.messaging.queue.MessaginQueueConsumer;
import com.nocompany.coherence.incubator.examples.messaging.queue.MessagingQueueProducer;
import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.DefaultMessagingSession;
import com.oracle.coherence.patterns.messaging.MessagingSession;
import com.oracle.tools.deferred.Eventually;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.coherence.CoherenceCacheServerSchema;
import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.network.AvailablePortIterator;
import com.oracle.tools.util.Capture;
import com.tangosol.net.CacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Observable;
import java.util.Observer;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for simple MessagingQueueProducerConsumerTest.
 */
public class MessagingQueueProducerConsumerTest {
    Logger logger = LoggerFactory.getLogger(MessagingQueueProducerConsumerTest.class);
    private String serverCacheConfig = "coherence-messagingpattern-test-cache-config.xml";
    private String clientCacheConfig = "coherence-messagingpattern-test-client-cache-config.xml";
    private String commonPofConfig = "coherence-messagingpattern-test-pof-config.xml";
    private int CLUSTER_SIZE = 1;
    private int PROXY_SIZE = 1;
    // acquire the platform on which we'll create the cluster member
    LocalPlatform platform = LocalPlatform.getInstance();
    // acquire a set of available ports on the platform
    AvailablePortIterator availablePorts;
    CoherenceCacheServerSchema nrStorageMembers, nrProxyMembers;
    CoherenceClusterBuilder clusterBuilder;
    CoherenceCluster cluster;
    String QUEUE_NAME = "test.queue";

    int noOfConsumedMessages = 0;
    Long publishedObject,consumedObject;
    @BeforeClass
    public void initializeTestEnvironment(){
        logger.info("Starting Cluster Test Environment");
        availablePorts = platform.getAvailablePorts();
        Capture<Integer> clusterPort = new Capture<Integer>(availablePorts);
        Capture<Integer> extendProxyNodePort = new Capture<Integer>(availablePorts);
        String hostName = availablePorts.getInetAddress().getHostName();

        nrStorageMembers = new CoherenceCacheServerSchema()
                .setClusterPort(clusterPort)
                .setCacheConfigURI(serverCacheConfig)
                .setPofConfigURI(commonPofConfig)
                .setStorageEnabled(true)
                .setSiteName("LOCAL")
                .setRoleName("STORAGE-1")
                .setSystemProperty("tangosol.coherence.proxy.enabled", false)
                .useLocalHostMode();


        nrProxyMembers = new CoherenceCacheServerSchema()
                .setClusterPort(clusterPort)
                .setCacheConfigURI(serverCacheConfig)
                .setPofConfigURI(commonPofConfig)
                .setStorageEnabled(false)
                .setSiteName("LOCAL")
                .setRoleName("PROXY-1")
                .setSystemProperty("tangosol.coherence.extend.enabled", true)
                .setSystemProperty("proxy.host", hostName)
                //.setSystemProperty("proxy.port",extendProxyNodePort)
                .useLocalHostMode();

        // configure our CoherenceClusterBuilder
        clusterBuilder = new CoherenceClusterBuilder();

        // instruct the builder the schema to use for some cache servers to build
        clusterBuilder.addSchema("CacheStorageServer", nrStorageMembers, CLUSTER_SIZE, platform);
        clusterBuilder.addSchema("CacheProxyServer", nrProxyMembers, PROXY_SIZE, platform);
        //System Console
        cluster = clusterBuilder.realize(new SystemApplicationConsole());

        // ensure that the expected cluster is created
        Eventually.assertThat(invoking(cluster).getClusterSize(), is(CLUSTER_SIZE+PROXY_SIZE));
        logger.info("Coherence Environment Up and Running...");
    }

    @AfterClass
    public void deInitializeTestEnvironment(){
        logger.info("Coherence Environment Shutting...");
        cluster.close();
    }

    @Test
    public void testQueueConsumer(){

        System.setProperty("tangosol.coherence.cacheconfig", clientCacheConfig);
        System.setProperty("tangosol.pof.config", commonPofConfig);

        MessagingSession session = DefaultMessagingSession.getInstance();
        assertNotNull(session);

        Identifier queueIdentifier  = session.createQueue(QUEUE_NAME);
        assertNotNull(queueIdentifier);

        publishedObject = System.nanoTime();
        final MessagingQueueProducer queueProducer = new MessagingQueueProducer(queueIdentifier,session, publishedObject);
        final MessaginQueueConsumer queueConsumer = new MessaginQueueConsumer(queueIdentifier,session);
        Observer queueMessageConsumerObserver = new Observer() {
            public void update(Observable observable, Object changedObject) {
                assertEquals(publishedObject,changedObject);
                consumedObject = (Long)changedObject;
                queueProducer.setKeepRunning(false);
                queueConsumer.setKeepRunning(false);
                noOfConsumedMessages++;
            }
        };
        queueConsumer.addObserver(queueMessageConsumerObserver);
        Thread producerThread = new Thread(queueProducer);
        Thread consumerThread = new Thread(queueConsumer);
        consumerThread.start();
        producerThread.start();

        try {
            Thread.currentThread().sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Message Consumed {}",noOfConsumedMessages);
        assertTrue(noOfConsumedMessages > 0);
        assertEquals(publishedObject,consumedObject);
    }
}
