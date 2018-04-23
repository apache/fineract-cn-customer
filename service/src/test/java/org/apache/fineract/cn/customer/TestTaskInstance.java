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
package org.apache.fineract.cn.customer;

import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.client.TaskExecutionException;
import org.apache.fineract.cn.customer.api.v1.domain.Command;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.api.v1.domain.IdentificationCard;
import org.apache.fineract.cn.customer.api.v1.domain.TaskDefinition;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import org.apache.fineract.cn.customer.util.IdentificationCardGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestTaskInstance extends AbstractCustomerTest {

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
    final IdentificationCard randomIdentificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    this.customerManager.createIdentificationCard(randomCustomer.getIdentifier(), randomIdentificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, randomIdentificationCard.getNumber());

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

    taskDefinition.setPredefined(false);
    this.customerManager.updateTask(taskDefinition.getIdentifier(), taskDefinition);
    this.eventRecorder.wait(CustomerEventConstants.PUT_TASK, taskDefinition.getIdentifier());
  }

  @Test
  public void shouldUnlockCustomerMultipleTasks() throws Exception{
    final TaskDefinition customTask1 = new TaskDefinition();
    customTask1.setIdentifier("custom-task-1");
    customTask1.setType(TaskDefinition.Type.CUSTOM.name());
    customTask1.setName("Do the barrel roll");
    customTask1.setCommands(
        TaskDefinition.Command.ACTIVATE.name(),
        TaskDefinition.Command.UNLOCK.name()
    );
    customTask1.setPredefined(Boolean.TRUE);
    customTask1.setMandatory(Boolean.TRUE);

    this.customerManager.createTask(customTask1);
    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, customTask1.getIdentifier());

    final TaskDefinition customTask2 = new TaskDefinition();
    customTask2.setIdentifier("custom-task-2");
    customTask2.setType(TaskDefinition.Type.CUSTOM.name());
    customTask2.setName("Do the barrel roll");
    customTask2.setCommands(
        TaskDefinition.Command.ACTIVATE.name(),
        TaskDefinition.Command.UNLOCK.name()
    );
    customTask2.setPredefined(Boolean.TRUE);
    customTask2.setMandatory(Boolean.TRUE);

    this.customerManager.createTask(customTask2);
    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, customTask2.getIdentifier());

    // create a random customer
    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    // close the task
    this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), customTask1.getIdentifier());
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, randomCustomer.getIdentifier());

    this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), customTask2.getIdentifier());
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, randomCustomer.getIdentifier());

    this.eventRecorder.clear();

    final Command activateCustomer = new Command();
    activateCustomer.setAction(Command.Action.ACTIVATE.name());
    this.customerManager.customerCommand(randomCustomer.getIdentifier(), activateCustomer);
    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, randomCustomer.getIdentifier()));

    final Command lockCustomer = new Command();
    lockCustomer.setAction(Command.Action.LOCK.name());
    this.customerManager.customerCommand(randomCustomer.getIdentifier(), lockCustomer);
    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, randomCustomer.getIdentifier()));

    // close the task
    this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), customTask1.getIdentifier());
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, randomCustomer.getIdentifier());

    this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), customTask2.getIdentifier());
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, randomCustomer.getIdentifier());

    final Command unlockCustomer = new Command();
    unlockCustomer.setAction(Command.Action.UNLOCK.name());
    this.customerManager.customerCommand(randomCustomer.getIdentifier(), unlockCustomer);
    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.UNLOCK_CUSTOMER, randomCustomer.getIdentifier()));

    final Customer customer = this.customerManager.findCustomer(randomCustomer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), customer.getCurrentState());

    // set predefined to false so it does not have a side effect on other tests
    customTask1.setPredefined(false);
    this.customerManager.updateTask(customTask1.getIdentifier(), customTask1);
    this.eventRecorder.wait(CustomerEventConstants.PUT_TASK, customTask1.getIdentifier());

    customTask2.setPredefined(false);
    this.customerManager.updateTask(customTask2.getIdentifier(), customTask2);
    this.eventRecorder.wait(CustomerEventConstants.PUT_TASK, customTask2.getIdentifier());
  }

  @Test(expected = TaskExecutionException.class)
  public void shouldNotProceedFourEyesWrongSigner() throws Exception {
    final TaskDefinition fourEyesTask = new TaskDefinition();
    fourEyesTask.setIdentifier("4-eyes-task-1");
    fourEyesTask.setType(TaskDefinition.Type.FOUR_EYES.name());
    fourEyesTask.setName("Do the barrel roll");
    fourEyesTask.setCommands(
        TaskDefinition.Command.ACTIVATE.name()
    );
    fourEyesTask.setPredefined(Boolean.TRUE);
    fourEyesTask.setMandatory(Boolean.TRUE);

    this.customerManager.createTask(fourEyesTask);
    this.eventRecorder.wait(CustomerEventConstants.POST_TASK, fourEyesTask.getIdentifier());

    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomer.getIdentifier());

    this.customerManager.taskForCustomerExecuted(randomCustomer.getIdentifier(), fourEyesTask.getIdentifier());
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, randomCustomer.getIdentifier());

    fourEyesTask.setPredefined(false);
    this.customerManager.updateTask(fourEyesTask.getIdentifier(), fourEyesTask);
    this.eventRecorder.wait(CustomerEventConstants.PUT_TASK, fourEyesTask.getIdentifier());
  }
}
