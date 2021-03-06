package net.optionfactory.hj.anydb;

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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.sql.DataSource;
import net.optionfactory.hj.JsonDriver;
import net.optionfactory.hj.JsonDynamicType;
import net.optionfactory.hj.JsonType;
import net.optionfactory.hj.anydb.JsonDriverTest.SpringConf;
import net.optionfactory.hj.gson.GsonJsonDriver;
import net.optionfactory.hj.jackson.JacksonJsonDriver;
import net.optionfactory.hj.spring.SpringDriverLocator;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.Columns;
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

    @Type(type = JsonType.TYPE)
    @JsonType.Conf(driver = "gsonDriver", locator = SpringDriverLocator.class)
    public List<Map<Integer, Set<Long>>> fieldMappedWithGson;

    @Type(type = JsonType.TYPE)
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

    final Serializable id = tt.execute(status -> {
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

    final Serializable id = tt.execute(status -> {
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

  @Entity
  @Table(name = "entityWithDynamicJsonFields")
  public static class EntityWithDynamicJsonFields {

    @Id
    @GeneratedValue
    public Integer id;

    @Type(type = JsonDynamicType.TYPE)
    @JsonType.Conf(driver = "gsonDriver", locator = SpringDriverLocator.class)
    @Columns(columns = {
      @Column(name = "field_gson_type")
      , 
      @Column(name = "field_gson_value")
    })
    public Object fieldMappedWithGson;

    @Type(type = JsonDynamicType.TYPE)
    @JsonType.Conf(driver = "jacksonDriver", locator = SpringDriverLocator.class)
    @Columns(columns = {
      @Column(name = "field_jackson_type")
      , 
      @Column(name = "field_jackson_value")
    })
    public Object fieldMappedWithJackson;

  }

  @Test
  public void canSaveAndRetrieveDynamicTypeWithGson() {
    final StringWrapper value = new StringWrapper();
    value.a = "something";

    final Serializable id = tt.execute(status -> {
      final EntityWithDynamicJsonFields entity = new EntityWithDynamicJsonFields();
      entity.fieldMappedWithGson = value;
      return hibernate.getCurrentSession().save(entity);
    });

    final StringWrapper reloadedValue = tt.execute(status -> {
      final EntityWithDynamicJsonFields loaded = hibernate.getCurrentSession().get(EntityWithDynamicJsonFields.class,
              id);
      return (StringWrapper) loaded.fieldMappedWithGson;
    });

    Assert.assertEquals(value.a, reloadedValue.a);
  }

  @Test
  public void canSaveAndRetrieveDynamicTypeWithJackson() {
    final StringWrapper value = new StringWrapper();
    value.a = "something";

    final Serializable id = tt.execute(status -> {
      final EntityWithDynamicJsonFields entity = new EntityWithDynamicJsonFields();
      entity.fieldMappedWithJackson = value;
      return hibernate.getCurrentSession().save(entity);
    });

    final StringWrapper reloadedValue = tt.execute(status -> {
      final EntityWithDynamicJsonFields loaded = hibernate.getCurrentSession().get(EntityWithDynamicJsonFields.class,
              id);
      return (StringWrapper) loaded.fieldMappedWithJackson;
    });

    Assert.assertEquals(value.a, reloadedValue.a);
  }

}
