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

import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.lang.DateConverter;
import io.mifos.customer.api.v1.domain.Command;
import io.mifos.customer.service.internal.repository.CommandEntity;
import io.mifos.customer.service.internal.repository.CustomerEntity;

import java.time.Clock;
import java.time.LocalDateTime;

public final class CommandMapper {

  private CommandMapper() {
    super();
  }

  public static CommandEntity create(final CustomerEntity customer, final String action, final String comment) {
    final CommandEntity commandEntity = new CommandEntity();
    commandEntity.setCustomer(customer);
    commandEntity.setType(action);
    commandEntity.setComment(comment);
    commandEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    commandEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));
    return commandEntity;
  }

  public static Command map(final CommandEntity commandEntity) {
    final Command command = new Command();
    command.setAction(commandEntity.getType());
    command.setComment(commandEntity.getComment());
    command.setCreatedBy(commandEntity.getCreatedBy());
    command.setCreatedOn(DateConverter.toIsoString(commandEntity.getCreatedOn()));
    return command;
  }
}
