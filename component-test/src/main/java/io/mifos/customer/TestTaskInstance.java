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
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.fixture.mariadb.MariaDBInitializer;
import io.mifos.core.test.listener.EnableEventRecording;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.client.CustomerManager;
import io.mifos.customer.api.v1.client.TaskExecutionException;
import io.mifos.customer.api.v1.domain.Command;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.customer.api.v1.domain.TaskDefinition;
import io.mifos.customer.service.rest.config.CustomerRestConfiguration;
import io.mifos.customer.util.CustomerGenerator;
import io.mifos.customer.util.IdentificationCardGenerator;
import org.junit.*;
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

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestTaskInstance {
  private static final String APP_NAME = "customer-v1";

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
  private CustomerManager customerManager;

  @Autowired
  private EventRecorder eventRecorder;

  private AutoUserContext userContext;

  @Before
  public void prepareTest() {
    String TEST_USER = "maatkare";
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
  public void shouldProceedCustomerWorkFlowWithMandatoryIdTasks() throws Exception {
    // create a predefined and mandatory task validating every state transition
    // has a an ID card assigned
    final TaskDefinition taskDefinition = new TaskDefinition();
    taskDefinition.setIdentifier("nat-id");
    taskDefinition.setType(TaskDefinition.Type.ID_CARD.name());
    taskDefinition.setName("National ID is needed.");
    taskDefinition.setCommands(
        TaskDefinition.Command.ACTIVATE.name(),
        TaskDefinition.Command.UNLOCK.name(),
        TaskDefinition.Command.REOPEN.name()
    );
    taskDefinition.setPredefined(Boolean.TRUE);
    taskDefinition.setMandatory(Boolean.TRUE);

    this.customerManager.createTask(taskDefinition);
    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());

    // create a random customer
    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    // try to activate the customer with missing ID card
    final Command activateCustomer = new Command();
    activateCustomer.setAction(Command.Action.ACTIVATE.name());
    this.customerManager.customerCommand(randomCustomer.getIdentifier(), activateCustomer);
    Assert.assertFalse(this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, randomCustomer.getIdentifier()));

    // assert client is still in pending
    final Customer stillPendingCustomer = this.customerManager.findCustomer(randomCustomer.getIdentifier());
    Assert.assertEquals(Customer.State.PENDING.name(), stillPendingCustomer.getCurrentState());

    try {
      // try to close the task
      this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), taskDefinition.getIdentifier());
      Assert.fail();
    } catch (final TaskExecutionException ex) {
      // do nothing, expected
    }

    // set the ID card for the customer
    this.customerManager.createIdentificationCard(randomCustomer.getIdentifier(), IdentificationCardGenerator.createRandomIdentificationCard());
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, randomCustomer.getIdentifier());

    // close the task
    this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), taskDefinition.getIdentifier());
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, randomCustomer.getIdentifier());

    // try to activate customer
    this.customerManager.customerCommand(randomCustomer.getIdentifier(), activateCustomer);
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, randomCustomer.getIdentifier());

    // assert customer is now active
    final Customer activatedCustomer = this.customerManager.findCustomer(randomCustomer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), activatedCustomer.getCurrentState());

    // set predefined to false so it does not have a side effect on other tests
    taskDefinition.setPredefined(false);
    this.customerManager.updateTask(taskDefinition.getIdentifier(), taskDefinition);
    this.eventRecorder.wait(CustomerEventConstants.PUT_TASK, taskDefinition.getIdentifier());
  }

  @Test
  public void shouldListNonMandatoryTasks() throws Exception{
    final TaskDefinition taskDefinition = new TaskDefinition();
    taskDefinition.setIdentifier("customid");
    taskDefinition.setType(TaskDefinition.Type.CUSTOM.name());
    taskDefinition.setName("Do the barrel roll");
    taskDefinition.setCommands(
            TaskDefinition.Command.ACTIVATE.name(),
            TaskDefinition.Command.UNLOCK.name(),
            TaskDefinition.Command.REOPEN.name()
    );
    taskDefinition.setPredefined(Boolean.TRUE);
    taskDefinition.setMandatory(Boolean.FALSE);

    this.customerManager.createTask(taskDefinition);
    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());

    // create a random customer
    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    final List<TaskDefinition> tasksForCustomer = this.customerManager.findTasksForCustomer(randomCustomer.getIdentifier(), false);

    Assert.assertEquals(1, tasksForCustomer.size());
  }

}
