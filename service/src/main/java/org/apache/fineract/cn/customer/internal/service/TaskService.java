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
package org.apache.fineract.cn.customer.internal.service;

import org.apache.fineract.cn.customer.api.v1.domain.TaskDefinition;
import org.apache.fineract.cn.customer.internal.mapper.TaskDefinitionMapper;
import org.apache.fineract.cn.customer.internal.repository.CustomerRepository;
import org.apache.fineract.cn.customer.internal.repository.TaskDefinitionEntity;
import org.apache.fineract.cn.customer.internal.repository.TaskDefinitionRepository;
import org.apache.fineract.cn.customer.internal.repository.TaskInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    return customerRepository.findByIdentifier(customerIdentifier)
        .map(taskInstanceRepository::findByCustomer)
        .orElse(Collections.emptyList())
        .stream()
        .filter(taskInstanceEntity -> (includeExecuted ? true : taskInstanceEntity.getExecutedBy() == null))
        .map(taskInstanceEntity -> TaskDefinitionMapper.map(taskInstanceEntity.getTaskDefinition()))
        .collect(Collectors.toList());
  }
}
