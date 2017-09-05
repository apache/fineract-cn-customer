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

import io.mifos.core.lang.validation.constraints.ValidIdentifier;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

public class PayrollDistribution {

  @ValidIdentifier
  private String mainAccountNumber;
  @Valid
  private Set<PayrollAllocation> payrollAllocations = new HashSet<>();
  private String createdBy;
  private String createdOn;
  private String lastModifiedBy;
  private String lastModifiedOn;

  public PayrollDistribution() {
    super();
  }

  public String getMainAccountNumber() {
    return this.mainAccountNumber;
  }

  public void setMainAccountNumber(final String mainAccountNumber) {
    this.mainAccountNumber = mainAccountNumber;
  }

  public Set<PayrollAllocation> getPayrollAllocations() {
    return this.payrollAllocations;
  }

  public void setPayrollAllocations(final Set<PayrollAllocation> payrollAllocations) {
    this.payrollAllocations = payrollAllocations;
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
