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
package io.mifos.customer.service.internal.mapper;

import io.mifos.core.lang.DateConverter;
import io.mifos.customer.api.v1.domain.ExpirationDate;
import io.mifos.customer.api.v1.domain.IdentificationCard;
import io.mifos.customer.service.internal.repository.IdentificationCardEntity;

public final class IdentificationCardMapper {

  private IdentificationCardMapper() {
    super();
  }

  public static IdentificationCardEntity map(final IdentificationCard identificationCard) {
    final IdentificationCardEntity identificationCardEntity = new IdentificationCardEntity();
    identificationCardEntity.setType(identificationCard.getType());
    identificationCardEntity.setNumber(identificationCard.getNumber());
    identificationCardEntity.setExpirationDate(identificationCard.getExpirationDate().toLocalDate());
    identificationCardEntity.setIssuer(identificationCard.getIssuer());
    return identificationCardEntity;
  }

  public static IdentificationCard map(final IdentificationCardEntity identificationCardEntity) {
    final IdentificationCard identificationCard = new IdentificationCard();
    identificationCard.setType(identificationCardEntity.getType());
    identificationCard.setNumber(identificationCardEntity.getNumber());
    identificationCard.setExpirationDate(ExpirationDate.fromLocalDate(identificationCardEntity.getExpirationDate()));
    identificationCard.setIssuer(identificationCardEntity.getIssuer());

    if (identificationCardEntity.getCreatedBy() != null ) {
      identificationCard.setCreatedBy(identificationCardEntity.getCreatedBy());
      identificationCard.setCreatedOn(DateConverter.toIsoString(identificationCardEntity.getCreatedOn()));
    }

    if (identificationCardEntity.getLastModifiedBy() != null) {
      identificationCard.setLastModifiedBy(identificationCardEntity.getLastModifiedBy());
      identificationCard.setLastModifiedOn(DateConverter.toIsoString(identificationCardEntity.getLastModifiedOn()));
    }

    return identificationCard;
  }
}
