package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@EnableTransactionManagement
@Component
public class MessageReceiver implements MessageListener {

    private Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 300)
    public void onMessage(Message message) {

        try {
            System.out.println("Message received: " + message.getBody(String.class));
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
