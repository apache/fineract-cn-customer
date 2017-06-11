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
package io.mifos.customer.service.internal.mapper;

import io.mifos.core.lang.DateConverter;
import io.mifos.customer.api.v1.domain.TaskInstance;
import io.mifos.customer.service.internal.repository.CustomerEntity;
import io.mifos.customer.service.internal.repository.TaskInstanceEntity;
import io.mifos.customer.service.internal.repository.TaskDefinitionEntity;

public class TaskInstanceMapper {

  public TaskInstanceMapper() {
    super();
  }

  public static TaskInstanceEntity create(final TaskDefinitionEntity taskDefinition, final CustomerEntity customer) {
    final TaskInstanceEntity taskInstanceEntity = new TaskInstanceEntity();
    taskInstanceEntity.setTaskDefinition(taskDefinition);
    taskInstanceEntity.setCustomer(customer);
    return taskInstanceEntity;
  }

  public static TaskInstance map(final TaskInstanceEntity taskInstanceEntity) {
    final TaskInstance taskInstance = new TaskInstance();
    taskInstance.setTaskIdentifier(taskInstanceEntity.getTaskDefinition().getIdentifier());
    taskInstance.setComment(taskInstanceEntity.getComment());
    if (taskInstanceEntity.getExecutedOn() != null) {
      taskInstance.setExecutedOn(DateConverter.toIsoString(taskInstanceEntity.getExecutedOn()));
    }
    taskInstance.setExecutedBy(taskInstanceEntity.getExecutedBy());
    return taskInstance;
  }
}
