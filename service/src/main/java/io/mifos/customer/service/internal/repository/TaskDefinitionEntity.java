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
package io.mifos.customer.service.internal.repository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "maat_task_definitions")
public class TaskDefinitionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @Column(name = "identifier")
  private String identifier;
  @Column(name = "a_type")
  private String type;
  @Column(name = "a_name")
  private String name;
  @Column(name = "description")
  private String description;
  @Column(name = "assigned_commands")
  private String assignedCommands;
  @Column(name = "mandatory")
  private Boolean mandatory;
  @Column(name = "predefined")
  private Boolean predefined;

  public TaskDefinitionEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setIdentifier(final String identifier) {
    this.identifier = identifier;
  }

  public String getType() {
    return this.type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getName() {
    return this.name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public String getAssignedCommands() {
    return this.assignedCommands;
  }

  public void setAssignedCommands(final String assignedCommands) {
    this.assignedCommands = assignedCommands;
  }

  public Boolean isMandatory() {
    return this.mandatory;
  }

  public void setMandatory(final Boolean mandatory) {
    this.mandatory = mandatory;
  }

  public Boolean isPredefined() {
    return this.predefined;
  }

  public void setPredefined(final Boolean predefined) {
    this.predefined = predefined;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TaskDefinitionEntity that = (TaskDefinitionEntity) o;

    return id.equals(that.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
