package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jta.atomikos.AtomikosConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.Assert;

@EnableTransactionManagement
@Configuration
public class MessagingConfig {

    @Autowired
    private AtomikosConnectionFactoryBean ConnectionFactory;

    @Autowired
    private JtaTransactionManager JtaTransactionManager;

    @Value("${gops.test.queue}")
    private String gops_test_queue_name;

    @Bean
    @Transactional
    public DefaultMessageListenerContainer messageListenerContainer() {

        Assert.isInstanceOf(AtomikosConnectionFactoryBean.class, ConnectionFactory);
        Assert.isInstanceOf(JtaTransactionManager.class, JtaTransactionManager);

        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setTransactionManager(JtaTransactionManager);
        container.setConnectionFactory(this.ConnectionFactory);
        container.setDestinationName(gops_test_queue_name);
        container.setMessageListener(new MessageReceiver());
        container.setSessionTransacted(true);
        return container;

    }
}
