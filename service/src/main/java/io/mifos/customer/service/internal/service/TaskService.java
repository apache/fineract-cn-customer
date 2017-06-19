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
package io.mifos.customer.service.internal.service;

import io.mifos.customer.api.v1.domain.TaskDefinition;
import io.mifos.customer.service.internal.mapper.TaskDefinitionMapper;
import io.mifos.customer.service.internal.repository.CustomerEntity;
import io.mifos.customer.service.internal.repository.CustomerRepository;
import io.mifos.customer.service.internal.repository.TaskDefinitionRepository;
import io.mifos.customer.service.internal.repository.TaskInstanceRepository;
import io.mifos.customer.service.internal.repository.TaskDefinitionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

  private final TaskDefinitionRepository taskDefinitionRepository;
  private final TaskInstanceRepository taskInstanceRepository;
  private final CustomerRepository customerRepository;

  @Autowired
  public TaskService(final TaskDefinitionRepository taskDefinitionRepository,
                     final TaskInstanceRepository taskInstanceRepository,
                     final CustomerRepository customerRepository) {
    super();
    this.taskDefinitionRepository = taskDefinitionRepository;
    this.taskInstanceRepository = taskInstanceRepository;
    this.customerRepository = customerRepository;
  }

  public Boolean taskDefinitionExists(final String identifier) {
    return this.taskDefinitionRepository.existsByIdentifier(identifier);
  }

  public Optional<TaskDefinition> findByIdentifier(final String identifier) {
    final TaskDefinitionEntity taskDefinitionEntity = this.taskDefinitionRepository.findByIdentifier(identifier);
    if (taskDefinitionEntity != null) {
      return Optional.of(TaskDefinitionMapper.map(taskDefinitionEntity));
    } else {
      return Optional.empty();
    }
  }

  public List<TaskDefinition> fetchAll() {
    return this.taskDefinitionRepository.findAll()
        .stream()
        .map(TaskDefinitionMapper::map)
        .collect(Collectors.toList());
  }

  public List<TaskDefinition> findTasksByCustomer(final String customerIdentifier, Boolean includeExecuted) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(customerIdentifier);
    return this.taskInstanceRepository.findByCustomer(customerEntity)
        .stream()
        .filter(taskInstanceEntity -> (includeExecuted ? true : taskInstanceEntity.getExecutedBy() == null))
        .map(taskInstanceEntity -> TaskDefinitionMapper.map(taskInstanceEntity.getTaskDefinition()))
        .collect(Collectors.toList());
  }
}
