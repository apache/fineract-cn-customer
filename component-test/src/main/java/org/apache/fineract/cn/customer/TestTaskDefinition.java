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
import org.apache.fineract.cn.customer.api.v1.client.TaskAlreadyExistsException;
import org.apache.fineract.cn.customer.api.v1.client.TaskNotFoundException;
import org.apache.fineract.cn.customer.api.v1.domain.TaskDefinition;
import org.apache.fineract.cn.customer.util.TaskGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestTaskDefinition extends AbstractCustomerTest {

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
