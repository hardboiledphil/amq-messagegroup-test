package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jta.atomikos.AtomikosConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@EnableTransactionManagement
@Configuration
public class MessagingConfig {

    @Autowired
    private AtomikosConnectionFactoryBean jmsConnectionFactory;

    @Value("${gops.test.queue}")
    private String gops_test_queue_name;

    @Bean
    @Transactional
    public DefaultMessageListenerContainer messageListenerContainer() {

        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(this.jmsConnectionFactory);
        container.setDestinationName(gops_test_queue_name);
        container.setMessageListener(new MessageReceiver());
        container.setSessionTransacted(true);
        return container;

    }
}
