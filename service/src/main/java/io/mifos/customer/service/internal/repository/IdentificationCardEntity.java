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

import io.mifos.core.mariadb.util.LocalDateConverter;
import io.mifos.core.mariadb.util.LocalDateTimeConverter;

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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "maat_identification_cards")
public class IdentificationCardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  @Column(name = "a_type")
  private String type;
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn(name = "customer_id")
  private CustomerEntity customer;
  @Column(name = "a_number")
  private String number;
  @Column(name = "expiration_date")
  @Convert(converter = LocalDateConverter.class)
  private LocalDate expirationDate;
  @Column(name = "issuer")
  private String issuer;
  @Column(name = "created_by")
  private String createdBy;
  @Column(name = "created_on")
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime createdOn;
  @Column(name = "last_modified_by")
  private String lastModifiedBy;
  @Column(name = "last_modified_on")
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime lastModifiedOn;

  public IdentificationCardEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public String getType() {
    return this.type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public CustomerEntity getCustomer() {
    return this.customer;
  }

  public void setCustomer(final CustomerEntity customer) {
    this.customer = customer;
  }

  public String getNumber() {
    return this.number;
  }

  public void setNumber(final String number) {
    this.number = number;
  }

  public LocalDate getExpirationDate() {
    return this.expirationDate;
  }

  public void setExpirationDate(final LocalDate expirationDate) {
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

  public LocalDateTime getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public LocalDateTime getLastModifiedOn() {
    return lastModifiedOn;
  }

  public void setLastModifiedOn(LocalDateTime lastModifiedOn) {
    this.lastModifiedOn = lastModifiedOn;
  }
}
