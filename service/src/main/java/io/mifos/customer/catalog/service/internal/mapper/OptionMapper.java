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
package io.mifos.customer.catalog.service.internal.mapper;

import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.lang.DateConverter;
import io.mifos.customer.catalog.api.v1.domain.Option;
import io.mifos.customer.catalog.service.internal.repository.FieldEntity;
import io.mifos.customer.catalog.service.internal.repository.OptionEntity;

import java.time.Clock;
import java.time.LocalDateTime;

public class OptionMapper {

  private OptionMapper() {
    super();
  }

  public static OptionEntity map(final FieldEntity fieldEntity, final Option option) {
    final OptionEntity optionEntity = new OptionEntity();
    optionEntity.setField(fieldEntity);
    optionEntity.setLabel(option.getLabel());
    optionEntity.setValue(option.getValue());
    optionEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    optionEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));
    return optionEntity;
  }

  public static Option map(final OptionEntity optionEntity) {
    final Option option = new Option();
    option.setLabel(optionEntity.getLabel());
    option.setValue(optionEntity.getValue());
    option.setCreatedBy(optionEntity.getCreatedBy());
    option.setCreatedOn(DateConverter.toIsoString(optionEntity.getCreatedOn()));
    return option;
  }
}
