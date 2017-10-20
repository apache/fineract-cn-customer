/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos.customer;

import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.listener.EnableEventRecording;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.client.CustomerDocumentsManager;
import io.mifos.customer.api.v1.client.CustomerManager;
import io.mifos.customer.service.rest.config.CustomerRestConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {AbstractCustomerTest.TestConfiguration.class})
public class AbstractCustomerTest extends SuiteTestEnvironment {
  static final String TEST_USER = "maatkare";
  private static final String LOGGER_NAME = "test-logger";

  @Configuration
  @EnableEventRecording
  @EnableFeignClients(basePackages = {"io.mifos.customer.api.v1.client"})
  @RibbonClient(name = APP_NAME)
  @ComponentScan(
      basePackages = {
          "io.mifos.customer.listener"
      }
  )
  @Import({CustomerRestConfiguration.class})
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean(name = LOGGER_NAME)
    public Logger logger() {
      return LoggerFactory.getLogger(LOGGER_NAME);
    }
  }

  @ClassRule
  public final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer, mariaDBInitializer);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
      = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment, this::waitForInitialize);

  @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
  @Autowired
  CustomerManager customerManager;

  @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
  @Autowired
  CustomerDocumentsManager customerDocumentsManager;

  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiringInspection"})
  @Autowired
  EventRecorder eventRecorder;

  @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
  @Autowired
  @Qualifier(LOGGER_NAME)
  Logger logger;

  private AutoUserContext userContext;

  public AbstractCustomerTest() {
    super();
  }

  @Before
  public void prepareTest() {
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
}
