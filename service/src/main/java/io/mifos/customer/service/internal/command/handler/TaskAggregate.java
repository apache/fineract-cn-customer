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
package io.mifos.customer.service.internal.command.handler;

import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.domain.Command;
import io.mifos.customer.api.v1.domain.TaskDefinition;
import io.mifos.customer.service.internal.command.AddTaskDefinitionToCustomerCommand;
import io.mifos.customer.service.internal.command.CreateTaskDefinitionCommand;
import io.mifos.customer.service.internal.command.ExecuteTaskForCustomerCommand;
import io.mifos.customer.service.internal.command.UpdateTaskDefinitionCommand;
import io.mifos.customer.service.internal.mapper.TaskDefinitionMapper;
import io.mifos.customer.service.internal.mapper.TaskInstanceMapper;
import io.mifos.customer.service.internal.repository.CustomerEntity;
import io.mifos.customer.service.internal.repository.CustomerRepository;
import io.mifos.customer.service.internal.repository.TaskDefinitionEntity;
import io.mifos.customer.service.internal.repository.TaskDefinitionRepository;
import io.mifos.customer.service.internal.repository.TaskInstanceEntity;
import io.mifos.customer.service.internal.repository.TaskInstanceRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    taskDefinitionEntity.setAssignedCommands(StringUtils.join(updatedTaskDefinition.getCommands(), ","));
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

    final CustomerEntity customerEntity =
        this.customerRepository.findByIdentifier(addTaskDefinitionToCustomerCommand.customerIdentifier());

    this.taskInstanceRepository.save(TaskInstanceMapper.create(taskDefinitionEntity, customerEntity));

    return addTaskDefinitionToCustomerCommand.customerIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_CUSTOMER)
  public String executeTaskForCustomer(final ExecuteTaskForCustomerCommand executeTaskForCustomerCommand) {
    final CustomerEntity customerEntity =
        this.customerRepository.findByIdentifier(executeTaskForCustomerCommand.customerIdentifier());
    final List<TaskInstanceEntity> taskInstanceEntities = this.taskInstanceRepository.findByCustomer(customerEntity);
    if (taskInstanceEntities != null) {
      final Optional<TaskInstanceEntity> taskInstanceEntityOptional = taskInstanceEntities
          .stream()
          .filter(
              taskInstanceEntity -> taskInstanceEntity.getTaskDefinition().getIdentifier()
                  .equals(executeTaskForCustomerCommand.taskIdentifier()))
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
  public Boolean openTasksForCustomerExist(final CustomerEntity customerEntity) {
    final List<TaskInstanceEntity> taskInstanceEntities = this.taskInstanceRepository.findByCustomer(customerEntity);

    //noinspection SimplifiableIfStatement
    if (taskInstanceEntities != null) {
      return taskInstanceEntities
          .stream()
              .filter(taskInstanceEntity -> taskInstanceEntity.getTaskDefinition().isMandatory())
              .filter(taskInstanceEntity -> taskInstanceEntity.getExecutedBy() == null)
          .findAny().isPresent();
    } else {
      return false;
    }
  }
}
