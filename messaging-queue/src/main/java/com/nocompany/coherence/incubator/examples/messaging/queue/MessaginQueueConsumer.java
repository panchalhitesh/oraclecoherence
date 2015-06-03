package com.nocompany.coherence.incubator.examples.messaging.queue;

import com.oracle.coherence.common.identifiers.Identifier;
import com.oracle.coherence.patterns.messaging.MessagingSession;
import com.oracle.coherence.patterns.messaging.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;

/**
 * Created by Hitesh on 5/28/2015.
 */
public class MessaginQueueConsumer extends Observable implements Runnable{
    private Identifier identifier;
    private MessagingSession session;
    private boolean keepRunning = true;
    private Object consumedObject;
    private Subscriber subscriber;
    Logger logger = LoggerFactory.getLogger(MessaginQueueConsumer.class);

    public void run() {
        while (keepRunning){
            setConsumedObject(subscriber.getMessage());

        }
    }

    private void setConsumedObject(Object consumedObject) {
        this.consumedObject = consumedObject;
        setChanged();
        notifyObservers(consumedObject);
    }

    public void setKeepRunning(boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

    public MessaginQueueConsumer(Identifier identifier, MessagingSession session) {
        this.identifier = identifier;
        this.session = session;
        this.subscriber = session.subscribe(identifier);
    }

}
