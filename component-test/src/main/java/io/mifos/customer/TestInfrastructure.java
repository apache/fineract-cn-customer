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
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.fixture.mariadb.MariaDBInitializer;
import io.mifos.core.test.listener.EnableEventRecording;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.service.rest.config.CustomerRestConfiguration;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestInfrastructure {
  private static final String APP_NAME = "customer-v1";

  @Configuration
  @EnableEventRecording
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
  private EventRecorder eventRecorder;

  @Autowired
  private DataSource dataSource;

  public boolean waitForInitialize() {
    try {
      return this.eventRecorder.wait(CustomerEventConstants.INITIALIZE, CustomerEventConstants.INITIALIZE);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void shouldInitializeCustomer() throws Exception {
    try (final Connection connection = this.dataSource.getConnection()) {
      final DatabaseMetaData databaseMetaData = connection.getMetaData();
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_customers", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_addresses", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_contact_details", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_identification_cards", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_commands", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_task_definitions", null).next());
      Assert.assertTrue(databaseMetaData.getTables(null, null, "maat_task_instances", null).next());
    }
  }
}
