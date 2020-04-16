package com.example.demo;

import com.atomikos.icatch.jta.J2eeUserTransaction;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	@Qualifier("atomikosTransactionManager")
	UserTransactionManager atomikosTransactionManager() throws Exception {
		UserTransactionManager utm = new UserTransactionManager();
		utm.init();
		return utm;
	}

	@Bean
	@Qualifier("atomikosUserTransaction")
	J2eeUserTransaction atomikosUserTransaction() {
		return new J2eeUserTransaction();
	}

	@Bean
	JtaTransactionManager transactionManager(UserTransactionManager atomikosTransactionManager,
											 J2eeUserTransaction atomikoUserTransaction) {
		JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
		jtaTransactionManager.setTransactionManager(atomikosTransactionManager);
		jtaTransactionManager.setUserTransaction(atomikoUserTransaction);
		jtaTransactionManager.setAllowCustomIsolationLevels(true);
		return jtaTransactionManager;

	}

	@RestController
	public static class XaApiRestController {

		private final JdbcTemplate jdbcTemplateODS, jdbcTemplateGOPS;

		private static UserTransactionManager userTransactionManager;
//		private static TransactionManager transactionManager;


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
