package com.example.demo;

import com.atomikos.icatch.jta.J2eeUserTransaction;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	@Qualifier("userTransaction")
	public UserTransaction userTransaction() throws Throwable {
		UserTransactionImp userTransactionImp = new UserTransactionImp();
		userTransactionImp.setTransactionTimeout(60);
		return userTransactionImp;
	}

	@Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
	public TransactionManager atomikosTransactionManager() throws Throwable {
		UserTransactionManager userTransactionManager = new UserTransactionManager();
		userTransactionManager.setForceShutdown(false);
		// for hibernate add in the following to set the transaction manager
		//AtomikosJtaPlatform.transactionManager = userTransactionManager;
		return userTransactionManager;
	}
	@Bean(name = "transactionManager")
	@DependsOn({ "userTransaction", "atomikosTransactionManager"})
	public PlatformTransactionManager transactionManager() throws Throwable {
		UserTransaction userTransaction = userTransaction();

		// for hibernate add in the following to set the user transaction
		// AtomikosJtaPlatform.transaction = userTransaction;

		TransactionManager atomikosTransactionManager = atomikosTransactionManager();
		return new JtaTransactionManager(userTransaction, atomikosTransactionManager);
	}

	@RestController
	public static class XaApiRestController {

		private final JdbcTemplate jdbcTemplateODS, jdbcTemplateGOPS;

		public XaApiRestController(@Qualifier ("datasourceODS") DataSource dataSourceODS,
								   @Qualifier ("datasourceGOPS") DataSource dataSourceGOPS)  {
			this.jdbcTemplateODS = new JdbcTemplate(dataSourceODS);
			this.jdbcTemplateGOPS = new JdbcTemplate(dataSourceGOPS);
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

			if (rollback.orElse(false)) {
				throw new RuntimeException("Throwing exception to cause transaction rollback");
			}
		}

	}

}
