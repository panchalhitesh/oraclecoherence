package com.nocompany.coherence.incubator.examples.messaging.queue;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.MessagingSession;
import org.apache.log4j.net.SyslogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by Hitesh on 5/28/2015.
 */
public class MessagingQueueProducer implements  Runnable{

    private Identifier identifier;
    private MessagingSession session;
    private boolean keepRunning = true;
    private Object data;
    Logger logger = LoggerFactory.getLogger(MessagingQueueProducer.class);


    public void run() {
        while(keepRunning) {
            session.publishMessage(identifier, this.data);
        }
    }

    public MessagingQueueProducer(Identifier identifier, MessagingSession session,Object publishedData) {
        this.identifier = identifier;
        this.session = session;
        this.data = publishedData;
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }
}
