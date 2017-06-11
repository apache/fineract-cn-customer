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
package io.mifos.customer.catalog.service.internal.repository;

import io.mifos.core.mariadb.util.LocalDateTimeConverter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "nun_fields")
public class FieldEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @ManyToOne
  @JoinColumn(name = "catalog_id")
  private CatalogEntity catalog;
  @Column(name = "identifier", length = 32, nullable = false)
  private String identifier;
  @Column(name = "data_type", length = 256, nullable = false)
  private String dataType;
  @Column(name = "a_label", length = 256, nullable = false)
  private String label;
  @Column(name = "a_hint", length = 512)
  private String hint;
  @Column(name = "description", length = 4096)
  @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<OptionEntity> options;
  private String description;
  @Column(name = "mandatory")
  private Boolean mandatory;
  @Column(name = "a_length")
  private Integer length;
  @Column(name = "a_precision")
  private Integer precision;
  @Column(name = "min_value")
  private Double minValue;
  @Column(name = "max_value")
  private Double maxValue;
  @Column(name = "created_by")
  private String createdBy;
  @Column(name = "created_on")
  @Convert(converter = LocalDateTimeConverter.class)
  private LocalDateTime createdOn;

  public FieldEntity() {
    super();
  }

  public Long getId() {
    return this.id;
  }

  public void setId(final Long id) {
    this.id = id;
  }

  public CatalogEntity getCatalog() {
    return this.catalog;
  }

  public void setCatalog(final CatalogEntity catalog) {
    this.catalog = catalog;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setIdentifier(final String identifier) {
    this.identifier = identifier;
  }

  public String getDataType() {
    return this.dataType;
  }

  public void setDataType(final String dataType) {
    this.dataType = dataType;
  }

  public String getLabel() {
    return this.label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getHint() {
    return this.hint;
  }

  public void setHint(final String hint) {
    this.hint = hint;
  }

  public List<OptionEntity> getOptions() {
    return this.options;
  }

  public void setOptions(final List<OptionEntity> options) {
    this.options = options;
  }

  public String getDescription() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public Boolean getMandatory() {
    return this.mandatory;
  }

  public void setMandatory(final Boolean mandatory) {
    this.mandatory = mandatory;
  }

  public Integer getLength() {
    return this.length;
  }

  public void setLength(final Integer length) {
    this.length = length;
  }

  public Integer getPrecision() {
    return this.precision;
  }

  public void setPrecision(final Integer precision) {
    this.precision = precision;
  }

  public Double getMinValue() {
    return this.minValue;
  }

  public void setMinValue(final Double minValue) {
    this.minValue = minValue;
  }

  public Double getMaxValue() {
    return this.maxValue;
  }

  public void setMaxValue(final Double maxValue) {
    this.maxValue = maxValue;
  }

  public String getCreatedBy() {
    return this.createdBy;
  }

  public void setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedOn() {
    return this.createdOn;
  }

  public void setCreatedOn(final LocalDateTime createdOn) {
    this.createdOn = createdOn;
  }
}
