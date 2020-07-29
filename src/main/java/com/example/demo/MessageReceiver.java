package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
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

    private JdbcTemplate jdbcTemplateODS, jdbcTemplateGOPS;


    public MessageReceiver(JdbcTemplate jdbcTemplateODS,
                           JdbcTemplate jdbcTemplateGOPS) {
        this.jdbcTemplateODS = jdbcTemplateODS;
        this.jdbcTemplateGOPS = jdbcTemplateGOPS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 300)
    public void onMessage(Message message) {

        try {

            if (this.jdbcTemplateODS == null || this.jdbcTemplateGOPS == null) {
                throw new RuntimeException("templates are null - oh dear");
            }

            String messageStr = message.getBody(String.class);

            System.out.println("Message received: '" + messageStr + "'");

            int idloc = messageStr.indexOf("id: ");

            String idToUpdate = messageStr.substring(idloc + 4, idloc + 6);

            System.out.println("Updating for ID: " + idToUpdate);

            this.jdbcTemplateODS.update(" update MESSAGESPJA SET PROCESSED='Y' WHERE ID = ? ", idToUpdate);

            this.jdbcTemplateGOPS.update(" update MESSAGESPJA SET PROCESSED='Y' WHERE ID = ? ", idToUpdate);

            if (message.getBody(String.class).contains("fail")) {
                throw new RuntimeException("Failing in messagelistener-onMessage()");
            }

        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
