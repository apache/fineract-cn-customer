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
import io.mifos.customer.api.v1.client.TaskAlreadyExistsException;
import io.mifos.customer.api.v1.client.TaskNotFoundException;
import io.mifos.customer.api.v1.domain.TaskDefinition;
import io.mifos.customer.service.rest.config.CustomerRestConfiguration;
import io.mifos.customer.util.TaskGenerator;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestTaskDefinition {
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
  public void shouldCreateTask() throws Exception {
    final TaskDefinition taskDefinition = TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE);
    this.customerManager.createTask(taskDefinition);

    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());

    final TaskDefinition savedTaskDefinition = this.customerManager.findTask(taskDefinition.getIdentifier());
    Assert.assertNotNull(savedTaskDefinition);
    Assert.assertEquals(taskDefinition.getIdentifier(), savedTaskDefinition.getIdentifier());
    Assert.assertEquals(taskDefinition.getType(), savedTaskDefinition.getType());
    Assert.assertEquals(taskDefinition.getName(), savedTaskDefinition.getName());
    Assert.assertEquals(taskDefinition.getDescription(), savedTaskDefinition.getDescription());
    Assert.assertEquals(taskDefinition.getMandatory(), savedTaskDefinition.getMandatory());
    Assert.assertEquals(taskDefinition.getPredefined(), savedTaskDefinition.getPredefined());
    Assert.assertArrayEquals(taskDefinition.getCommands(), savedTaskDefinition.getCommands());
  }

  @Test
  public void shouldNotCreateTaskAlreadyExists() throws Exception {
    final TaskDefinition taskDefinition = TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE);
    this.customerManager.createTask(taskDefinition);

    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());

    try {
      this.customerManager.createTask(taskDefinition);
      Assert.fail();
    } catch (final TaskAlreadyExistsException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void shouldFindTask() throws Exception {
    final TaskDefinition taskDefinition = TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE);
    this.customerManager.createTask(taskDefinition);

    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());

    final TaskDefinition savedTaskDefinition = this.customerManager.findTask(taskDefinition.getIdentifier());
    Assert.assertNotNull(savedTaskDefinition);
  }

  @Test
  public void shouldNotFindTaskNotFound() throws Exception {
    try {
      this.customerManager.findTask(RandomStringUtils.randomAlphanumeric(8));
      Assert.fail();
    } catch (TaskNotFoundException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void shouldFetchAllTasks() throws Exception {
    Arrays.asList(
        TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE),
        TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE),
        TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE)
    ).forEach(taskDefinition -> {
      this.customerManager.createTask(taskDefinition);
      try {
        this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());
      } catch (final InterruptedException ex) {
        Assert.fail();
      }
    });

    final List<TaskDefinition> taskDefinitions = this.customerManager.fetchAllTasks();
    Assert.assertTrue(taskDefinitions.size() >= 3);
  }

  @Test
  public void shouldUpdateTask() throws Exception {
    final TaskDefinition taskDefinition = TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.FALSE, Boolean.FALSE);
    this.customerManager.createTask(taskDefinition);

    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, taskDefinition.getIdentifier());

    final TaskDefinition updatedTaskDefinition = TaskGenerator.createRandomTask(TaskDefinition.Type.CUSTOM, Boolean.TRUE, Boolean.TRUE);
    updatedTaskDefinition.setIdentifier(taskDefinition.getIdentifier());
    updatedTaskDefinition.setCommands(TaskDefinition.Command.REOPEN.name());

    this.customerManager.updateTask(updatedTaskDefinition.getIdentifier(), updatedTaskDefinition);

    this.eventRecorder.wait(CustomerEventConstants.PUT_TASK, taskDefinition.getIdentifier());

    final TaskDefinition fetchedTaskDefinition = this.customerManager.findTask(updatedTaskDefinition.getIdentifier());
    Assert.assertNotNull(fetchedTaskDefinition);
    Assert.assertEquals(updatedTaskDefinition.getIdentifier(), fetchedTaskDefinition.getIdentifier());
    Assert.assertEquals(updatedTaskDefinition.getType(), fetchedTaskDefinition.getType());
    Assert.assertEquals(updatedTaskDefinition.getName(), fetchedTaskDefinition.getName());
    Assert.assertEquals(updatedTaskDefinition.getDescription(), fetchedTaskDefinition.getDescription());
    Assert.assertEquals(updatedTaskDefinition.getMandatory(), fetchedTaskDefinition.getMandatory());
    Assert.assertEquals(updatedTaskDefinition.getPredefined(), fetchedTaskDefinition.getPredefined());
    Assert.assertArrayEquals(updatedTaskDefinition.getCommands(), fetchedTaskDefinition.getCommands());
  }
}
