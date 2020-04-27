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

            System.out.println("Message received: " + message.getBody(String.class));

            this.jdbcTemplateODS.update(" update MESSAGESPJA SET PROCESSED='Y' WHERE ID = ? ", 1);

            this.jdbcTemplateGOPS.update(" update MESSAGESPJA SET PROCESSED='Y' WHERE ID = ? ", 1);

        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }
}
