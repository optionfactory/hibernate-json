package net.optionfactory.hj.pgsql;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.DataSource;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.JsonType;
import net.optionfactory.hj.JsonType.ColumnType;
import net.optionfactory.hj.gson.GsonJsonDriver;
import net.optionfactory.hj.jackson.JacksonJsonDriver;
import net.optionfactory.hj.pgsql.JsonbDriverTest.SpringConf;
import net.optionfactory.hj.postgres.PostgreSQL94DialectWithJsonb;
import net.optionfactory.hj.spring.SpringDriverLocator;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Type;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@ContextConfiguration(classes = SpringConf.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class JsonbDriverTest {

    @EnableSpringConfigured
    public static class SpringConf {

        @Bean
        public DataSource dataSource() {
            return new SimpleDriverDataSource(new Driver(), "jdbc:postgresql://127.0.0.1:5432/test", "test", "test");
        }

        @Bean
        public LocalSessionFactoryBean sessionFactory(final DataSource dataSource) throws IOException, Exception {
            final Properties hp = new Properties();
            
            final LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan(new String[]{SpringConf.class.getPackage().getName()});
            factory.setHibernateProperties(hp);
            hp.put("hibernate.dialect", PostgreSQL94DialectWithJsonb.class.getName());
            hp.put("hibernate.hbm2ddl.auto", "update");
            return factory;
        }
        

        @Bean
        public PlatformTransactionManager transactionManager(SessionFactory sessionFatory) {
            final HibernateTransactionManager txm = new HibernateTransactionManager();
            txm.setSessionFactory(sessionFatory);
            return txm;
        }

        @Bean
        public TransactionTemplate transactionTemplate(PlatformTransactionManager ptm) {
            return new TransactionTemplate(ptm);
        }

        @Bean
        public JsonDriver gsonDriver() {
            return new GsonJsonDriver(new Gson());
        }
        @Bean
        public JsonDriver jacksonDriver() {
            return new JacksonJsonDriver(new ObjectMapper());
        }

    }

    @Entity
    @Table(name="PostgresTest")
    public static class PostgresTest {

        @Id
        @GeneratedValue
        public Integer id;

        @Type(type = JsonType.TYPE)
        @JsonType.Conf(driver = "jacksonDriver", type = ColumnType.PostgresJsonb, locator=SpringDriverLocator.class)
        public List<Map<Integer, Set<Long>>> fieldMappedWithJackson;        
        
    }

    
    
    @Autowired
    private SessionFactory hibernate;

    @Autowired
    private TransactionTemplate tt;
    
    @Test
    public void canSaveAndRetrieveWithJackson() {
        final Long longValue = 456L;
        
        final Serializable id = tt.execute(status -> {            
            final PostgresTest entity = new PostgresTest();
            entity.fieldMappedWithJackson = Arrays.asList(Collections.singletonMap(1, Collections.singleton(longValue)));
            return hibernate.getCurrentSession().save(entity);
        });

        final Long reloadedValue = tt.execute(status -> {
            final PostgresTest loaded = (PostgresTest) hibernate.getCurrentSession().get(PostgresTest.class, id);
            return loaded.fieldMappedWithJackson.get(0).get(1).iterator().next();
        });

        Assert.assertEquals(longValue, reloadedValue);
    }

}
