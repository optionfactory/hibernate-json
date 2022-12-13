package net.optionfactory.hj.anydb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.JsonType;
import net.optionfactory.hj.anydb.JsonDriverTest.SpringConf;
import net.optionfactory.hj.gson.GsonJsonDriver;
import net.optionfactory.hj.jackson.JacksonJsonDriver;
import net.optionfactory.hj.spring.SpringDriverLocator;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Type;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class JsonDriverTest {

  @EnableSpringConfigured
  public static class SpringConf {

    @Bean
    public DataSource dataSource() {
      return new SimpleDriverDataSource(new org.hsqldb.jdbcDriver(), "jdbc:hsqldb:mem:test", "sa", "");
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(final DataSource dataSource) throws IOException, Exception {
      final Properties hp = new Properties();

      final LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
      factory.setDataSource(dataSource);
      factory.setPackagesToScan(SpringConf.class.getPackage().getName());
      factory.setHibernateProperties(hp);
      hp.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
      hp.put("hibernate.hbm2ddl.auto", "create-drop");
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
  @Table(name = "entityWithJsonFields")
  public static class EntityWithJsonFields {

    @Id
    @GeneratedValue
    public Integer id;

    @Type(JsonType.class)
    @JsonType.Conf(driver = "gsonDriver", locator = SpringDriverLocator.class)
    public List<Map<Integer, Set<Long>>> fieldMappedWithGson;

    @Type(JsonType.class)
    @JsonType.Conf(driver = "jacksonDriver", locator = SpringDriverLocator.class)
    public List<Map<Integer, Set<Long>>> fieldMappedWithJackson;

  }

  @Autowired
  private SessionFactory hibernate;

  @Autowired
  private TransactionTemplate tt;

  @Test
  public void canSaveAndRetrieveWithGson() {
    final Long longValue = 123L;

    final Object id = tt.execute(status -> {
      final EntityWithJsonFields entity = new EntityWithJsonFields();
      entity.fieldMappedWithGson = Arrays.asList(Collections.singletonMap(1, Collections.singleton(longValue)));
      return hibernate.getCurrentSession().save(entity);
    });

    final Long reloadedValue = tt.execute(status -> {
      final EntityWithJsonFields loaded = hibernate.getCurrentSession().get(EntityWithJsonFields.class, id);
      return loaded.fieldMappedWithGson.get(0).get(1).iterator().next();
    });

    Assert.assertEquals(longValue, reloadedValue);
  }

  @Test
  public void canSaveAndRetrieveWithJackson() {
    final Long longValue = 456L;

    final Object id = tt.execute(status -> {
      final EntityWithJsonFields entity = new EntityWithJsonFields();
      entity.fieldMappedWithJackson = Arrays.asList(Collections.singletonMap(1, Collections.singleton(longValue)));
      return hibernate.getCurrentSession().save(entity);
    });

    final Long reloadedValue = tt.execute(status -> {
      final EntityWithJsonFields loaded = hibernate.getCurrentSession().get(EntityWithJsonFields.class, id);
      return loaded.fieldMappedWithJackson.get(0).get(1).iterator().next();
    });

    Assert.assertEquals(longValue, reloadedValue);
  }

  public static class StringWrapper {

    public String a;
  }

}
