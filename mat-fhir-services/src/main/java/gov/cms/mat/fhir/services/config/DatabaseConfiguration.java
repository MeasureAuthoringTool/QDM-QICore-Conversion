package gov.cms.mat.fhir.services.config;

import com.cognitivemedicine.config.utils.ConfigUtils;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "gov.cms.mat.fhir.services.repository")
public class DatabaseConfiguration {
    @Bean(name = "dataSourceMAT", destroyMethod = "close")
    public DataSource dataSource() {
        ConfigUtils config = ConfigUtils.getInstance(ConfigUtilConstants.CONTEXT_NAME);
        String host = config.getString(ConfigUtilConstants.KEY_MYSQL_HOST);
        String port = config.getString(ConfigUtilConstants.KEY_MYSQL_PORT);
        String dbName = config.getString(ConfigUtilConstants.KEY_MYSQL_DBNAME);
        String username = config.getString(ConfigUtilConstants.KEY_MYSQL_USERNAME);
        String password = config.getString(ConfigUtilConstants.KEY_MYSQL_PASSWORD);

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?createDatabaseIfNotExist=true&serverTimezone=UTC";
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery("select 1;");
        return dataSource;
    }

    @Bean (name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSourceHIE);
        bean.setJpaVendorAdapter(jpaVendorAdapter);
        bean.setPackagesToScan("gov.cms.mat.fhir.commons.model");
        return bean;
    }

    @Bean( name = "jpaVendorAdapter")
    public HibernateJpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(true);
        adapter.setGenerateDdl(true);
        adapter.setDatabase(Database.MYSQL);
        return adapter;
    }

    @Bean(name = "transactionManager")
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager factory = new JpaTransactionManager();
        return factory;
    }

    @Autowired
    private DataSource dataSourceHIE;

    @Autowired
    private HibernateJpaVendorAdapter jpaVendorAdapter;
}
