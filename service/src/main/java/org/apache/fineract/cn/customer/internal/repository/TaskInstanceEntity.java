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
package org.apache.fineract.cn.customer.internal.repository;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.cn.mariadb.util.LocalDateTimeConverter;

@Entity
@Table(name = "maat_task_instances")
public class TaskInstanceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "task_definition_id")
  private TaskDefinitionEntity taskDefinition;
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id")
  private CustomerEntity customer;
  @Column(name = "a_comment")
  private String comment;
  @Column(name = "executed_on")
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime executedOn;
  @Column(name = "executed_by")
  private String executedBy;

  public TaskInstanceEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public TaskDefinitionEntity getTaskDefinition() {
    return this.taskDefinition;
  }

  public void setTaskDefinition(final TaskDefinitionEntity taskDefinition) {
    this.taskDefinition = taskDefinition;
  }

  public CustomerEntity getCustomer() {
    return this.customer;
  }

  public void setCustomer(final CustomerEntity customer) {
    this.customer = customer;
  }

  public String getComment() {
    return this.comment;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public LocalDateTime getExecutedOn() {
    return this.executedOn;
  }

  public void setExecutedOn(final LocalDateTime executedOn) {
    this.executedOn = executedOn;
  }

  public String getExecutedBy() {
    return this.executedBy;
  }

  public void setExecutedBy(final String executedBy) {
    this.executedBy = executedBy;
  }
}
