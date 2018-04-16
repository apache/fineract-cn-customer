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
package org.apache.fineract.cn.customer.internal.mapper;

import org.apache.fineract.cn.customer.api.v1.domain.TaskDefinition;
import org.apache.fineract.cn.customer.internal.repository.TaskDefinitionEntity;
import org.apache.commons.lang.StringUtils;

public final class TaskDefinitionMapper {

  private TaskDefinitionMapper() {
    super();
  }

  public static TaskDefinitionEntity map(final TaskDefinition taskDefinition) {
    final TaskDefinitionEntity taskDefinitionEntity = new TaskDefinitionEntity();
    taskDefinitionEntity.setIdentifier(taskDefinition.getIdentifier());
    taskDefinitionEntity.setType(taskDefinition.getType());
    taskDefinitionEntity.setName(taskDefinition.getName());
    taskDefinitionEntity.setDescription(taskDefinition.getDescription());
    taskDefinitionEntity.setAssignedCommands(StringUtils.join(taskDefinition.getCommands(), ";"));
    taskDefinitionEntity.setMandatory(taskDefinition.getMandatory());
    taskDefinitionEntity.setPredefined(taskDefinition.getPredefined());
    return taskDefinitionEntity;
  }

  public static TaskDefinition map(final TaskDefinitionEntity taskDefinitionEntity) {
    final TaskDefinition taskDefinition = new TaskDefinition();
    taskDefinition.setIdentifier(taskDefinitionEntity.getIdentifier());
    taskDefinition.setType(taskDefinitionEntity.getType());
    taskDefinition.setName(taskDefinitionEntity.getName());
    taskDefinition.setDescription(taskDefinitionEntity.getDescription());
    taskDefinition.setCommands(taskDefinitionEntity.getAssignedCommands().split(";"));
    taskDefinition.setMandatory(taskDefinitionEntity.isMandatory());
    taskDefinition.setPredefined(taskDefinitionEntity.isPredefined());
    return taskDefinition;
  }
}
