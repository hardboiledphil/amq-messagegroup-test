package com.example.demo;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.apache.activemq.artemis.jms.client.ActiveMQXAConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jta.atomikos.AtomikosConnectionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.ConnectionFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
@EnableJms
@Configuration
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	@Qualifier("userTransaction")
	public UserTransaction userTransaction() throws Throwable {
		System.out.println("userTransaction being created");
		UserTransactionImp userTransactionImp = new UserTransactionImp();
		userTransactionImp.setTransactionTimeout(60);
		return userTransactionImp;
	}

	@Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
	public TransactionManager atomikosTransactionManager() throws Throwable {
		System.out.println("atomikosTransactionManager being created");
		UserTransactionManager userTransactionManager = new UserTransactionManager();
		userTransactionManager.setForceShutdown(false);
		// for hibernate add in the following to set the transaction manager
		//AtomikosJtaPlatform.transactionManager = userTransactionManager;
		return userTransactionManager;
	}
	@Bean(name = "transactionManager")
	@DependsOn({ "userTransaction", "atomikosTransactionManager"})
//	public PlatformTransactionManager transactionManager(@Qualifier ("userTransaction") UserTransaction userTransaction,
//														 @Qualifier ("atomikosTransactionManager") TransactionManager atomikosTransactionManager) throws Throwable {
	public PlatformTransactionManager transactionManager() throws Throwable {
		System.out.println("transactionManager being created");

		UserTransaction userTransaction = userTransaction();

		// for hibernate add in the following to set the user transaction
		// AtomikosJtaPlatform.transaction = userTransaction;

		TransactionManager atomikosTransactionManager = atomikosTransactionManager();
		return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
	}

	@Bean(name = "jmsConnectionFactory")
	public ConnectionFactory jmsConnectionFactory() throws Throwable {
		System.out.println("jmsConnectionFactory being created");

		ActiveMQXAConnectionFactory activeMQXAConnectionFactory = new ActiveMQXAConnectionFactory();
		activeMQXAConnectionFactory.setBrokerURL("tcp://localhost:6116?jms.redeliveryPolicy.maximumRedeliveries=1");
		activeMQXAConnectionFactory.setUser("admin");
		activeMQXAConnectionFactory.setPassword("admin");

		AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
		atomikosConnectionFactoryBean.setLocalTransactionMode(false);
		atomikosConnectionFactoryBean.setXaConnectionFactory(activeMQXAConnectionFactory);
		return atomikosConnectionFactoryBean;
	}

	@Bean(name = "jmsTemplate")
	@DependsOn("jmsConnectionFactory")
	public JmsTemplate jmsTemplate(@Qualifier ("jmsConnectionFactory") ConnectionFactory jmsConnectionFactory) {
		System.out.println("jmsTemplate being created");
		JmsTemplate jmsTemplate = new JmsTemplate();
		jmsTemplate.setConnectionFactory(jmsConnectionFactory);
		jmsTemplate.setSessionTransacted(true);
		return jmsTemplate;
	}


	@RestController
	public static class XaApiRestController {

		private final JdbcTemplate jdbcTemplateODS, jdbcTemplateGOPS;
		private final JmsTemplate jmsTemplate;

		public XaApiRestController(@Qualifier ("datasourceODS") DataSource dataSourceODS,
								   @Qualifier ("datasourceGOPS") DataSource dataSourceGOPS,
								   @Qualifier ("jmsTemplate") JmsTemplate jmsTemplate)  {
			this.jdbcTemplateODS = new JdbcTemplate(dataSourceODS);
			this.jdbcTemplateGOPS = new JdbcTemplate(dataSourceGOPS);
			this.jmsTemplate = jmsTemplate;
		}

		@PostMapping
		@Transactional
		public void write(@RequestBody Map<String, String> payload,
						  @RequestParam Optional<Boolean> rollback) {

			String id = payload.get("ID");
			String message = payload.get("MESSAGE");

			this.jdbcTemplateODS.update(" insert into MESSAGESPJA (ID, MESSAGE) values (?,?)",
					id, message);
			this.jdbcTemplateGOPS.update(" insert into MESSAGESPJA (ID, MESSAGE) values (?,?)",
					id, message);

			String messageToSend = "Added id: " + id + " message: " + message + " to both databases";

			this.jmsTemplate.convertAndSend("GOPS.TEST.QUEUE", messageToSend);

			if (rollback.orElse(false)) {
				throw new RuntimeException("Throwing exception to cause transaction rollback");
			}
		}

	}

}
