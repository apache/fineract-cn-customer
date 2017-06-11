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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class IdentificationCard {

  @NotBlank
  private String type;
  @NotBlank
  private String number;
  @NotNull
  @Valid
  private ExpirationDate expirationDate;
  private String issuer;
  private String createdBy;
  private String createdOn;
  private String lastModifiedBy;
  private String lastModifiedOn;

  public IdentificationCard() {
    super();
  }

  public String getType() {
    return this.type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public String getNumber() {
    return this.number;
  }

  public void setNumber(final String number) {
    this.number = number;
  }

  public ExpirationDate getExpirationDate() {
    return this.expirationDate;
  }

  public void setExpirationDate(final ExpirationDate expirationDate) {
    this.expirationDate = expirationDate;
  }

  public String getIssuer() {
    return this.issuer;
  }

  public void setIssuer(final String issuer) {
    this.issuer = issuer;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public String getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(String createdOn) {
    this.createdOn = createdOn;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public String getLastModifiedOn() {
    return lastModifiedOn;
  }

  public void setLastModifiedOn(String lastModifiedOn) {
    this.lastModifiedOn = lastModifiedOn;
  }
}
