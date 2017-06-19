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
package io.mifos.customer.util;

import io.mifos.customer.api.v1.domain.TaskDefinition;
import org.apache.commons.lang3.RandomStringUtils;

public final class TaskGenerator {

  private TaskGenerator() {
    super();
  }

  public static TaskDefinition createRandomTask(final TaskDefinition.Type type, final Boolean mandatory, final Boolean predefined) {
    final TaskDefinition taskDefinition = new TaskDefinition();
    taskDefinition.setIdentifier(RandomStringUtils.randomAlphanumeric(8));
    taskDefinition.setType(type.name());
    taskDefinition.setName(RandomStringUtils.randomAlphanumeric(256));
    taskDefinition.setDescription(RandomStringUtils.randomAlphanumeric(2048));
    taskDefinition.setCommands(TaskDefinition.Command.ACTIVATE.name());
    taskDefinition.setMandatory(mandatory);
    taskDefinition.setPredefined(predefined);
    return taskDefinition;
  }
}
