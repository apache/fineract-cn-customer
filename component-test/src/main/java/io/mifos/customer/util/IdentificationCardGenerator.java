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
package io.mifos.customer.util;

import io.mifos.customer.api.v1.domain.ExpirationDate;
import io.mifos.customer.api.v1.domain.IdentificationCard;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Clock;
import java.time.LocalDate;

public class IdentificationCardGenerator {

  private IdentificationCardGenerator() {
    super();
  }

  public static IdentificationCard createRandomIdentificationCard() {
    final IdentificationCard identificationCard = new IdentificationCard();
    identificationCard.setType(RandomStringUtils.randomAlphanumeric(128));
    identificationCard.setNumber(RandomStringUtils.randomAlphanumeric(32));
    identificationCard.setExpirationDate(ExpirationDate.fromLocalDate(LocalDate.now(Clock.systemUTC()).plusYears(2L)));
    identificationCard.setIssuer(RandomStringUtils.randomAlphanumeric(256));
    return identificationCard;
  }
}
