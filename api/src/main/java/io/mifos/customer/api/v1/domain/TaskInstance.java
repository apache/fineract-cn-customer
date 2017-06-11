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
package io.mifos.customer.api.v1.domain;

import org.hibernate.validator.constraints.NotBlank;

public final class TaskInstance {

  @NotBlank
  private String taskIdentifier;
  private String comment;
  private String executedOn;
  private String executedBy;

  public TaskInstance() {
    super();
  }

  public String getTaskIdentifier() {
    return taskIdentifier;
  }

  public void setTaskIdentifier(final String taskIdentifier) {
    this.taskIdentifier = taskIdentifier;
  }

  public String getComment() {
    return this.comment;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public String getExecutedOn() {
    return this.executedOn;
  }

  public void setExecutedOn(final String executedOn) {
    this.executedOn = executedOn;
  }

  public String getExecutedBy() {
    return this.executedBy;
  }

  public void setExecutedBy(final String executedBy) {
    this.executedBy = executedBy;
  }
}
