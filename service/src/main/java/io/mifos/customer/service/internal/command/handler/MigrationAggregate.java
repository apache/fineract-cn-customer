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
package io.mifos.customer.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.mariadb.domain.FlywayFactoryBean;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.service.ServiceConstants;
import io.mifos.customer.service.internal.command.InitializeServiceCommand;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;

@SuppressWarnings({
    "unused"
})
@Aggregate
public class MigrationAggregate {

  private final Logger logger;
  private final DataSource dataSource;
  private final FlywayFactoryBean flywayFactoryBean;

  @Autowired
  public MigrationAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                            final DataSource dataSource,
                            final FlywayFactoryBean flywayFactoryBean) {
    super();
    this.logger = logger;
    this.dataSource = dataSource;
    this.flywayFactoryBean = flywayFactoryBean;
  }

  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.INITIALIZE)
  public String initialize(final InitializeServiceCommand initializeServiceCommand) {
    this.logger.debug("Start service migration.");
    this.flywayFactoryBean.create(this.dataSource).migrate();
    return CustomerEventConstants.INITIALIZE;
  }
}