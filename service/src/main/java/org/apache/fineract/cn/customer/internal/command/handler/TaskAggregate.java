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
package org.apache.fineract.cn.customer.internal.command.handler;

import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.domain.Command;
import org.apache.fineract.cn.customer.api.v1.domain.TaskDefinition;
import org.apache.fineract.cn.customer.internal.command.AddTaskDefinitionToCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.CreateTaskDefinitionCommand;
import org.apache.fineract.cn.customer.internal.command.ExecuteTaskForCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.UpdateTaskDefinitionCommand;
import org.apache.fineract.cn.customer.internal.mapper.TaskDefinitionMapper;
import org.apache.fineract.cn.customer.internal.mapper.TaskInstanceMapper;
import org.apache.fineract.cn.customer.internal.repository.CustomerEntity;
import org.apache.fineract.cn.customer.internal.repository.CustomerRepository;
import org.apache.fineract.cn.customer.internal.repository.TaskDefinitionEntity;
import org.apache.fineract.cn.customer.internal.repository.TaskDefinitionRepository;
import org.apache.fineract.cn.customer.internal.repository.TaskInstanceEntity;
import org.apache.fineract.cn.customer.internal.repository.TaskInstanceRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.lang.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings({"WeakerAccess", "unused"})
@Aggregate
public class TaskAggregate {

  private final TaskDefinitionRepository taskDefinitionRepository;
  private final TaskInstanceRepository taskInstanceRepository;
  private final CustomerRepository customerRepository;

  @Autowired
  public TaskAggregate(final TaskDefinitionRepository taskDefinitionRepository,
                       final TaskInstanceRepository taskInstanceRepository,
                       final CustomerRepository customerRepository) {
    super();
    this.taskDefinitionRepository = taskDefinitionRepository;
    this.taskInstanceRepository = taskInstanceRepository;
    this.customerRepository = customerRepository;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_TASK)
  public String createTaskDefinition(final CreateTaskDefinitionCommand createTaskDefinitionCommand) {
    this.taskDefinitionRepository.save(TaskDefinitionMapper.map(createTaskDefinitionCommand.taskDefinition()));
    return createTaskDefinitionCommand.taskDefinition().getIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_TASK)
  public String updateTaskDefinition(final UpdateTaskDefinitionCommand updateTaskDefinitionCommand) {
    final TaskDefinitionEntity taskDefinitionEntity = this.taskDefinitionRepository.findByIdentifier(updateTaskDefinitionCommand.identifier());

    final TaskDefinition updatedTaskDefinition = updateTaskDefinitionCommand.taskDefinition();
    taskDefinitionEntity.setName(updatedTaskDefinition.getName());
    taskDefinitionEntity.setDescription(updatedTaskDefinition.getDescription());
    taskDefinitionEntity.setAssignedCommands(StringUtils.join(updatedTaskDefinition.getCommands(), ";"));
    taskDefinitionEntity.setMandatory(updatedTaskDefinition.getMandatory());
    taskDefinitionEntity.setPredefined(updatedTaskDefinition.getPredefined());

    this.taskDefinitionRepository.save(taskDefinitionEntity);

    return updatedTaskDefinition.getIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_CUSTOMER)
  public String addTaskToCustomer(final AddTaskDefinitionToCustomerCommand addTaskDefinitionToCustomerCommand) {
    final TaskDefinitionEntity taskDefinitionEntity =
        this.taskDefinitionRepository.findByIdentifier(addTaskDefinitionToCustomerCommand.taskIdentifier());

    final CustomerEntity customerEntity = findCustomerEntityOrThrow(addTaskDefinitionToCustomerCommand.customerIdentifier());

    this.taskInstanceRepository.save(TaskInstanceMapper.create(taskDefinitionEntity, customerEntity));

    return addTaskDefinitionToCustomerCommand.customerIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_CUSTOMER)
  public String executeTaskForCustomer(final ExecuteTaskForCustomerCommand executeTaskForCustomerCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(executeTaskForCustomerCommand.customerIdentifier());
    final List<TaskInstanceEntity> taskInstanceEntities = this.taskInstanceRepository.findByCustomer(customerEntity);
    if (taskInstanceEntities != null) {
      final Optional<TaskInstanceEntity> taskInstanceEntityOptional = taskInstanceEntities
          .stream()
          .filter(
              taskInstanceEntity -> taskInstanceEntity.getTaskDefinition().getIdentifier().equals(executeTaskForCustomerCommand.taskIdentifier())
                  && taskInstanceEntity.getExecutedBy() == null
          )
          .findAny();

      if (taskInstanceEntityOptional.isPresent()) {
        final TaskInstanceEntity taskInstanceEntity = taskInstanceEntityOptional.get();
        taskInstanceEntity.setExecutedBy(UserContextHolder.checkedGetUser());
        taskInstanceEntity.setExecutedOn(LocalDateTime.now(Clock.systemUTC()));
        this.taskInstanceRepository.save(taskInstanceEntity);
      }
    }

    return executeTaskForCustomerCommand.customerIdentifier();
  }

  @Transactional
  public void onCustomerCommand(final CustomerEntity customerEntity, Command.Action action) {
    final List<TaskDefinitionEntity> predefinedTasks =
        this.taskDefinitionRepository.findByAssignedCommandsContaining(action.name());
    if (predefinedTasks != null && predefinedTasks.size() > 0) {
      this.taskInstanceRepository.save(
          predefinedTasks
              .stream()
              .filter(TaskDefinitionEntity::isPredefined)
              .map(taskDefinitionEntity -> TaskInstanceMapper.create(taskDefinitionEntity, customerEntity))
              .collect(Collectors.toList())
      );
    }
  }

  @Transactional
  public Boolean openTasksForCustomerExist(final CustomerEntity customerEntity, final String command) {
    final List<TaskInstanceEntity> taskInstanceEntities = this.taskInstanceRepository.findByCustomer(customerEntity);

    //noinspection SimplifiableIfStatement
    if (taskInstanceEntities != null) {
      return taskInstanceEntities
          .stream()
              .filter(taskInstanceEntity -> taskInstanceEntity.getTaskDefinition().getAssignedCommands().contains(command))
              .filter(taskInstanceEntity -> taskInstanceEntity.getTaskDefinition().isMandatory())
              .anyMatch(taskInstanceEntity -> taskInstanceEntity.getExecutedBy() == null);
    } else {
      return false;
    }
  }

  private CustomerEntity findCustomerEntityOrThrow(String identifier) {
    return this.customerRepository.findByIdentifier(identifier)
        .orElseThrow(() -> ServiceException.notFound("Customer ''{0}'' not found", identifier));
  }
}
