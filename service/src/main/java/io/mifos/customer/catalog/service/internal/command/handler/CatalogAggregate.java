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
package io.mifos.customer.catalog.service.internal.command.handler;

import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.customer.catalog.api.v1.CatalogEventConstants;
import io.mifos.customer.catalog.api.v1.domain.Catalog;
import io.mifos.customer.catalog.service.internal.repository.CatalogEntity;
import io.mifos.customer.catalog.service.internal.repository.CatalogRepository;
import io.mifos.customer.catalog.service.internal.command.CreateCatalogCommand;
import io.mifos.customer.catalog.service.internal.mapper.CatalogMapper;
import io.mifos.customer.catalog.service.internal.mapper.FieldMapper;
import io.mifos.customer.service.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Aggregate
public class CatalogAggregate {

  private final Logger logger;
  private final CatalogRepository catalogRepository;

  @Autowired
  public CatalogAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                          final CatalogRepository catalogRepository) {
    super();
    this.logger = logger;
    this.catalogRepository = catalogRepository;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CatalogEventConstants.SELECTOR_NAME, selectorValue = CatalogEventConstants.POST_CATALOG)
  public String createCatalog(final CreateCatalogCommand createCatalogCommand) {
    final Catalog catalog = createCatalogCommand.catalog();
    final CatalogEntity catalogEntity = CatalogMapper.map(catalog);
    catalogEntity.setFields(catalog.getFields()
        .stream()
        .map(field -> FieldMapper.map(catalogEntity, field))
        .collect(Collectors.toList())
    );
    this.catalogRepository.save(catalogEntity);
    return catalog.getIdentifier();
  }
}
