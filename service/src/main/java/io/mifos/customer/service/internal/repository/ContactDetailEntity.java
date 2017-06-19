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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "maat_contact_details")
public class ContactDetailEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "customer_id")
  private CustomerEntity customer;
  @Column(name = "a_type")
  private String type;
  @Column(name = "a_group")
  private String group;
  @Column(name = "a_value")
  private String value;
  @Column(name = "preference_level")
  private Integer preferenceLevel;
  @Column(name = "validated")
  private Boolean valid;

  public ContactDetailEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public CustomerEntity getCustomer() {
    return this.customer;
  }

  public void setCustomer(final CustomerEntity customer) {
    this.customer = customer;
  }

  public String getType() {
    return this.type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getGroup() {
    return this.group;
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public String getValue() {
    return this.value;
  }

  public Integer getPreferenceLevel() {
    return this.preferenceLevel;
  }

  public void setPreferenceLevel(final Integer preferenceLevel) {
    this.preferenceLevel = preferenceLevel;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public Boolean getValid() {
    return this.valid;
  }

  public void setValid(final Boolean valid) {
    this.valid = valid;
  }
}
