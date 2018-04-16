/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.customer.catalog;

import com.google.common.collect.Lists;
import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.client.CustomerManager;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.catalog.api.v1.CatalogEventConstants;
import org.apache.fineract.cn.customer.catalog.api.v1.client.CatalogAlreadyInUseException;
import org.apache.fineract.cn.customer.catalog.api.v1.client.CatalogManager;
import org.apache.fineract.cn.customer.catalog.api.v1.client.FieldAlreadyInUseException;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Catalog;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Field;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Option;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Value;
import org.apache.fineract.cn.customer.catalog.util.CatalogGenerator;
import org.apache.fineract.cn.customer.rest.config.CustomerRestConfiguration;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import org.apache.fineract.cn.api.context.AutoUserContext;
import org.apache.fineract.cn.test.env.TestEnvironment;
import org.apache.fineract.cn.test.fixture.TenantDataStoreContextTestRule;
import org.apache.fineract.cn.test.fixture.cassandra.CassandraInitializer;
import org.apache.fineract.cn.test.fixture.mariadb.MariaDBInitializer;
import org.apache.fineract.cn.test.listener.EnableEventRecording;
import org.apache.fineract.cn.test.listener.EventRecorder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestCatalog {
  private static final String APP_NAME = "customer-v1";
  @Configuration
  @EnableEventRecording
  @EnableFeignClients(basePackages = {"org.apache.fineract.cn.customer.api.v1.client",
      "org.apache.fineract.cn.customer.catalog.api.v1.client"})
  @RibbonClient(name = APP_NAME)
  @ComponentScan(
      basePackages = {
          "org.apache.fineract.cn.customer.listener",
          "org.apache.fineract.cn.customer.catalog.listener"
      }
  )
  @Import({CustomerRestConfiguration.class})
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }
  }
  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static MariaDBInitializer mariaDBInitializer = new MariaDBInitializer();
  private final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer, mariaDBInitializer);

  @ClassRule
  public static TestRule orderClassRules = RuleChain
          .outerRule(testEnvironment)
          .around(cassandraInitializer)
          .around(mariaDBInitializer)
          .around(tenantDataStoreContext);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
          = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment, this::waitForInitialize);


  @Autowired
  private CatalogManager catalogManager;
  @Autowired
  private CustomerManager customerManager;
  @Autowired
  private EventRecorder eventRecorder;

  private AutoUserContext userContext;

  public TestCatalog() {
    super();
  }

  @Before
  public void prepareTest() {
    final String TEST_USER = "nunkare";
    userContext = tenantApplicationSecurityEnvironment.createAutoUserContext(TEST_USER);
  }

  @After
  public void cleanupTest() {
    userContext.close();
  }

  public boolean waitForInitialize() {
    try {
      return this.eventRecorder.wait(CustomerEventConstants.INITIALIZE, CustomerEventConstants.INITIALIZE);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void shouldCreateCatalog() throws Exception {
    final Catalog catalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(catalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());

    final Catalog savedCatalog = this.catalogManager.findCatalog(catalog.getIdentifier());
    Assert.assertEquals(catalog.getIdentifier(), savedCatalog.getIdentifier());
    Assert.assertEquals(catalog.getName(), savedCatalog.getName());
    Assert.assertEquals(catalog.getDescription(), savedCatalog.getDescription());
    Assert.assertNotNull(savedCatalog.getCreatedBy());
    Assert.assertNotNull(savedCatalog.getCreatedOn());
    Assert.assertTrue(catalog.getFields().size() == savedCatalog.getFields().size());
    savedCatalog.getFields().forEach(field -> {
      if (field.getOptions() != null) {
        Assert.assertTrue(field.getOptions().size() > 0);
        Assert.assertEquals(Integer.valueOf(1), field.getOptions().get(0).getValue());
      } else {
        if (field.getDataType().equals(Field.DataType.SINGLE_SELECTION.name())) {
          Assert.fail();
        }
      }
    });
  }

  @Test
  public void shouldFetchCatalogs() throws Exception {
    final List<Catalog> catalogs = Arrays.asList(
        CatalogGenerator.createRandomCatalog(),
        CatalogGenerator.createRandomCatalog(),
        CatalogGenerator.createRandomCatalog());

    catalogs.forEach(catalog -> {
      this.catalogManager.createCatalog(catalog);
      try {
        this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());
      } catch (InterruptedException e) {
        Assert.fail();
      }
    });

    final List<Catalog> fetchedCatalogs = this.catalogManager.fetchCatalogs();
    Assert.assertTrue(fetchedCatalogs.size() >= 3);
  }

  @Test
  public void shouldSaveCustomValues() throws Exception {
    final Catalog randomCatalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(randomCatalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, randomCatalog.getIdentifier());

    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    randomCustomer.setCustomValues(randomCatalog.getFields()
        .stream()
        .map(field -> {
          final Value value = new Value();
          value.setCatalogIdentifier(randomCatalog.getIdentifier());
          value.setFieldIdentifier(field.getIdentifier());
          switch (Field.DataType.valueOf(field.getDataType())) {
            case NUMBER:
              value.setValue("123.45");
              break;
            case SINGLE_SELECTION:
              value.setValue("1");
          }
          return value;
        })
        .collect(Collectors.toList())
    );

    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    final Customer savedCustomer = this.customerManager.findCustomer(randomCustomer.getIdentifier());
    Assert.assertTrue(savedCustomer.getCustomValues().size() == 2);
  }

  @Test
  public void shouldDeleteCatalog() throws Exception {
    final Catalog catalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(catalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());

    this.catalogManager.deleteCatalog(catalog.getIdentifier());
    Assert.assertTrue(this.eventRecorder.wait(CatalogEventConstants.DELETE_CATALOG, catalog.getIdentifier()));
  }

  @Test(expected = CatalogAlreadyInUseException.class)
  public void shouldNotDeleteCatalogUsed() throws Exception {
    final Catalog catalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(catalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());

    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    randomCustomer.setCustomValues(catalog.getFields()
        .stream()
        .map(field -> {
          final Value value = new Value();
          value.setCatalogIdentifier(catalog.getIdentifier());
          value.setFieldIdentifier(field.getIdentifier());
          switch (Field.DataType.valueOf(field.getDataType())) {
            case NUMBER:
              value.setValue("25.00");
              break;
            case SINGLE_SELECTION:
              value.setValue("1");
          }
          return value;
        })
        .collect(Collectors.toList())
    );

    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    this.catalogManager.deleteCatalog(catalog.getIdentifier());
  }

  @Test
  public void shouldDeleteField() throws Exception {
    final Catalog catalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(catalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());

    final Optional<Field> optionalField = catalog.getFields().stream().findFirst();
    optionalField.ifPresent(field -> {
      this.catalogManager.deleteField(catalog.getIdentifier(), field.getIdentifier());
      try {
        Assert.assertTrue(this.eventRecorder.wait(CatalogEventConstants.DELETE_FIELD, field.getIdentifier()));
      } catch (final InterruptedException iex) {
        Assert.fail(iex.getMessage());
      }
    });

    final Catalog savedCatalog = this.catalogManager.findCatalog(catalog.getIdentifier());
    Assert.assertEquals(1, savedCatalog.getFields().size());
  }

  @Test(expected = FieldAlreadyInUseException.class)
  public void shouldNotDeleteField() throws Exception {
    final Catalog catalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(catalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());

    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    randomCustomer.setCustomValues(catalog.getFields()
        .stream()
        .map(field -> {
          final Value value = new Value();
          value.setCatalogIdentifier(catalog.getIdentifier());
          value.setFieldIdentifier(field.getIdentifier());
          switch (Field.DataType.valueOf(field.getDataType())) {
            case NUMBER:
              value.setValue("37.00");
              break;
            case SINGLE_SELECTION:
              value.setValue("1");
          }
          return value;
        })
        .collect(Collectors.toList())
    );

    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    final Optional<Field> optionalField = catalog.getFields().stream().findFirst();
    optionalField.ifPresent(field -> this.catalogManager.deleteField(catalog.getIdentifier(), field.getIdentifier()));
  }

  @Test
  public void shouldUpdateField() throws Exception {
    final Catalog catalog = CatalogGenerator.createRandomCatalog();

    this.catalogManager.createCatalog(catalog);
    this.eventRecorder.wait(CatalogEventConstants.POST_CATALOG, catalog.getIdentifier());

    final Optional<Field> optionalField = catalog.getFields()
        .stream()
        .filter(field -> field.getDataType().equals(Field.DataType.SINGLE_SELECTION.name()))
        .findFirst();

    optionalField.ifPresent(field -> {
      final Option option = new Option();
      option.setLabel("new-option");
      option.setValue(2);
      field.setOptions(Lists.newArrayList(option));
      this.catalogManager.updateField(catalog.getIdentifier(), field.getIdentifier(), field);
      try {
        Assert.assertTrue(this.eventRecorder.wait(CatalogEventConstants.PUT_FIELD, field.getIdentifier()));
      } catch (final InterruptedException iex) {
        Assert.fail(iex.getMessage());
      }
    });

    final Catalog savedCatalog = this.catalogManager.findCatalog(catalog.getIdentifier());
    final Optional<Field> optionalFetchedField = savedCatalog.getFields()
        .stream()
        .filter(field -> field.getDataType().equals(Field.DataType.SINGLE_SELECTION.name()))
        .findFirst();

    if (optionalFetchedField.isPresent()) {
      final Field field = optionalFetchedField.get();
      Assert.assertEquals(1, field.getOptions().size());
      final Optional<Option> optionalOption = field.getOptions().stream().findFirst();
      Assert.assertTrue(optionalOption.isPresent());
      final Option option = optionalOption.get();
      Assert.assertEquals(Integer.valueOf(2), option.getValue());
    } else {
      Assert.fail();
    }
  }
}
