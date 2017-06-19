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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public final class ContactDetail {

  public enum Type {
    EMAIL,
    PHONE,
    MOBILE
  }

  public enum Group {
    BUSINESS,
    PRIVATE
  }

  @NotNull
  private Type type;
  @NotNull
  private Group group;
  @NotBlank
  private String value;
  @Min(1)
  @Max(127)
  private Integer preferenceLevel;  private Boolean validated;

  public ContactDetail() {
    super();
  }

  public String getType() {
    return this.type.name();
  }

  public void setType(final String type) {
    this.type = Type.valueOf(type.toUpperCase());
  }

  public String getValue() {
    return this.value;
  }

  public String getGroup() {
    return this.group.name();
  }

  public void setGroup(final String group) {
    this.group = Group.valueOf(group);
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public Boolean getValidated() {
    return this.validated;
  }

  public void setValidated(final Boolean validated) {
    this.validated = validated;
  }

  public Integer getPreferenceLevel() {
    return this.preferenceLevel;
  }

  public void setPreferenceLevel(final Integer preferenceLevel) {
    this.preferenceLevel = preferenceLevel;
  }
}
