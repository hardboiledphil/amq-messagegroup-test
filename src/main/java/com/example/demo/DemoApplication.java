package com.example.demo;

import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.atomikos.AtomikosConnectionFactoryBean;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@EnableJms
@EnableTransactionManagement
@Configuration
@ComponentScan("com.example.demo")
@ImportResource({"classpath*:JTAConfig.xml"})
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean(name = "ConnectionFactory")
	public ConnectionFactory jmsConnectionFactory() throws Throwable {
		System.out.println("jmsConnectionFactory being created");

		ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
		activeMQXAConnectionFactory.setBrokerURL("tcp://localhost:6116?jms.redeliveryPolicy.maximumRedeliveries=1");
		activeMQXAConnectionFactory.setUser("admin");
		activeMQXAConnectionFactory.setPassword("admin");

		AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
		atomikosConnectionFactoryBean.setUniqueResourceName("GOPS-EOD-MANAGER");
		atomikosConnectionFactoryBean.setLocalTransactionMode(false);
		atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
		atomikosConnectionFactoryBean.setMinPoolSize(3);
		atomikosConnectionFactoryBean.setMaxPoolSize(10);

		return atomikosConnectionFactoryBean;
	}

	@Bean(name = "jmsTemplate")
	@DependsOn("ConnectionFactory")
	public JmsTemplate jmsTemplate(@Qualifier ("ConnectionFactory") ConnectionFactory jmsConnectionFactory) {
		System.out.println("jmsTemplate being created");
		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setConnectionFactory(jmsConnectionFactory);
		jmsTemplate.setSessionTransacted(true);
		return jmsTemplate;
	}

	@Bean(name = "jdbcTemplateODS")
	@DependsOn("datasourceODS")
	public JdbcTemplate jdbcTemplateODS(@Qualifier ("datasourceODS") DataSource dataSourceODS) {
		return new JdbcTemplate(dataSourceODS);
	}

	@Bean(name = "jdbcTemplateGOPS")
	@DependsOn("datasourceGOPS")
	public JdbcTemplate jdbcTemplateGOPS(@Qualifier ("datasourceGOPS") DataSource dataSourceGOPS) {
		return new JdbcTemplate(dataSourceGOPS);
	}

	@RestController
	public static class XaApiRestController {

		private final JmsTemplate jmsTemplate;

		private final JdbcTemplate jdbcTemplateODS, jdbcTemplateGOPS;

		public XaApiRestController(@Qualifier ("jmsTemplate") JmsTemplate jmsTemplate,
								   @Qualifier ("jdbcTemplateODS") JdbcTemplate jdbcTemplateODS,
								   @Qualifier ("jdbcTemplateGOPS") JdbcTemplate jdbcTemplateGOPS)  {
			this.jmsTemplate = jmsTemplate;
			this.jdbcTemplateODS = jdbcTemplateODS;
			this.jdbcTemplateGOPS = jdbcTemplateGOPS;
		}

		@PostMapping
		@Transactional
		public void write(@RequestBody Map<String, String> payload,
						  @RequestParam Optional<Boolean> rollback) {

			if (this.jdbcTemplateODS == null || this.jdbcTemplateGOPS == null) {
				System.out.println("db templates returning null");
				return;
			}

			String id = payload.get("ID");
			String message = payload.get("MESSAGE");

			this.jdbcTemplateODS.update(" insert into MESSAGESPJA (ID, MESSAGE, PROCESSED) values (?,?,'N')",
					id, message);
			this.jdbcTemplateGOPS.update(" insert into MESSAGESPJA (ID, MESSAGE, PROCESSED) values (?,?,'N')",
					id, message);

			String messageToSend = "Added id: " + id + " message: " + message + " to both databases";

			this.jmsTemplate.convertAndSend("GOPS.TEST.QUEUE", messageToSend);

			if (rollback.orElse(false)) {
				throw new RuntimeException("Throwing exception to cause transaction rollback");
			}
		}

	}

}
