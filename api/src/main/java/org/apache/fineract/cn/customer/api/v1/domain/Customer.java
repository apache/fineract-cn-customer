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
package org.apache.fineract.cn.customer.api.v1.domain;

import org.apache.fineract.cn.customer.catalog.api.v1.domain.Value;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.apache.fineract.cn.lang.DateOfBirth;
import org.hibernate.validator.constraints.NotBlank;

public final class Customer {

  public enum Type {
    PERSON,
    BUSINESS
  }

  public enum State {
    PENDING,
    ACTIVE,
    LOCKED,
    CLOSED
  }

  @NotBlank
  private String identifier;
  @NotNull
  private Type type;
  @NotBlank
  private String givenName;
  private String middleName;
  @NotBlank
  private String surname;
  @NotNull
  private DateOfBirth dateOfBirth;
  @NotNull
  private Boolean member;
  private String accountBeneficiary;
  private String referenceCustomer;
  private String assignedOffice;
  private String assignedEmployee;
  @NotNull
  @Valid
  private Address address;
  @Valid
  private List<ContactDetail> contactDetails;
  private State currentState;
  private String applicationDate;
  private List<Value> customValues;
  private String createdBy;
  private String createdOn;
  private String lastModifiedBy;
  private String lastModifiedOn;

  public Customer() {
    super();
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setIdentifier(final String identifier) {
    this.identifier = identifier;
  }

  public String getType() {
    return this.type.name();
  }

  public void setType(final String type) {
    this.type = Type.valueOf(type);
  }

  public String getGivenName() {
    return this.givenName;
  }

  public void setGivenName(final String givenName) {
    this.givenName = givenName;
  }

  public String getMiddleName() {
    return this.middleName;
  }

  public void setMiddleName(final String middleName) {
    this.middleName = middleName;
  }

  public String getSurname() {
    return this.surname;
  }

  public void setSurname(final String surname) {
    this.surname = surname;
  }

  public DateOfBirth getDateOfBirth() {
    return this.dateOfBirth;
  }

  public void setDateOfBirth(final DateOfBirth dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public Boolean getMember() {
    return this.member;
  }

  public void setMember(final Boolean member) {
    this.member = member;
  }

  public String getAccountBeneficiary() {
    return this.accountBeneficiary;
  }

  public void setAccountBeneficiary(final String accountBeneficiary) {
    this.accountBeneficiary = accountBeneficiary;
  }

  public String getReferenceCustomer() {
    return this.referenceCustomer;
  }

  public void setReferenceCustomer(final String referenceCustomer) {
    this.referenceCustomer = referenceCustomer;
  }

  public String getAssignedOffice() {
    return this.assignedOffice;
  }

  public void setAssignedOffice(final String assignedOffice) {
    this.assignedOffice = assignedOffice;
  }

  public String getAssignedEmployee() {
    return this.assignedEmployee;
  }

  public void setAssignedEmployee(final String assignedEmployee) {
    this.assignedEmployee = assignedEmployee;
  }

  public Address getAddress() {
    return this.address;
  }

  public void setAddress(final Address address) {
    this.address = address;
  }

  public List<ContactDetail> getContactDetails() {
    return this.contactDetails;
  }

  public void setContactDetails(final List<ContactDetail> contactDetails) {
    this.contactDetails = contactDetails;
  }

  public String getCurrentState() {
    return this.currentState != null ? this.currentState.name() : null;
  }

  public void setCurrentState(final String currentState) {
    this.currentState = State.valueOf(currentState);
  }

  public String getApplicationDate() {
    return this.applicationDate;
  }

  public void setApplicationDate(final String applicationDate) {
    this.applicationDate = applicationDate;
  }

  public List<Value> getCustomValues() {
    return this.customValues;
  }

  public void setCustomValues(final List<Value> customValues) {
    this.customValues = customValues;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public String getCreatedOn() {
    return this.createdOn;
  }

  public void setCreatedOn(final String createdOn) {
    this.createdOn = createdOn;
  }

  public String getLastModifiedBy() {
    return this.lastModifiedBy;
  }

  public void setLastModifiedBy(final String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public String getLastModifiedOn() {
    return this.lastModifiedOn;
  }

  public void setLastModifiedOn(final String lastModifiedOn) {
    this.lastModifiedOn = lastModifiedOn;
  }
}
