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
package org.apache.fineract.cn.customer.catalog.internal.mapper;

import org.apache.fineract.cn.customer.catalog.api.v1.domain.Option;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.OptionEntity;
import java.time.Clock;
import java.time.LocalDateTime;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.lang.DateConverter;

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
