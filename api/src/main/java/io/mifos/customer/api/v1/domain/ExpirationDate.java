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

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class ExpirationDate {

  @NotNull
  private Integer year;
  @NotNull
  private Integer month;
  @NotNull
  private Integer day;

  public ExpirationDate() {
    super();
  }

  public Integer getYear() {
    return this.year;
  }

  public void setYear(final Integer year) {
    this.year = year;
  }

  public Integer getMonth() {
    return this.month;
  }

  public void setMonth(final Integer month) {
    this.month = month;
  }

  public Integer getDay() {
    return this.day;
  }

  public void setDay(final Integer day) {
    this.day = day;
  }

  public LocalDate toLocalDate() {
    return LocalDate.of(this.year, this.month, this.day);
  }

  public static ExpirationDate fromLocalDate(LocalDate localDate) {
    ExpirationDate dateOfBirth = new ExpirationDate();
    dateOfBirth.setYear(localDate.getYear());
    dateOfBirth.setMonth(localDate.getMonthValue());
    dateOfBirth.setDay(localDate.getDayOfMonth());
    return dateOfBirth;
  }
}
