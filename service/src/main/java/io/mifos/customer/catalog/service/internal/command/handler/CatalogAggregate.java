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
import io.mifos.customer.catalog.api.v1.domain.Field;
import io.mifos.customer.catalog.service.internal.command.ChangeFieldCommand;
import io.mifos.customer.catalog.service.internal.command.CreateCatalogCommand;
import io.mifos.customer.catalog.service.internal.command.DeleteCatalogCommand;
import io.mifos.customer.catalog.service.internal.command.DeleteFieldCommand;
import io.mifos.customer.catalog.service.internal.mapper.CatalogMapper;
import io.mifos.customer.catalog.service.internal.mapper.FieldMapper;
import io.mifos.customer.catalog.service.internal.mapper.OptionMapper;
import io.mifos.customer.catalog.service.internal.repository.CatalogEntity;
import io.mifos.customer.catalog.service.internal.repository.CatalogRepository;
import io.mifos.customer.catalog.service.internal.repository.FieldEntity;
import io.mifos.customer.catalog.service.internal.repository.FieldRepository;
import io.mifos.customer.catalog.service.internal.repository.OptionRepository;
import io.mifos.customer.service.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Aggregate
public class CatalogAggregate {

  private final Logger logger;
  private final CatalogRepository catalogRepository;
  private final FieldRepository fieldRepository;
  private final OptionRepository optionRepository;

  @Autowired
  public CatalogAggregate(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                          final CatalogRepository catalogRepository,
                          final FieldRepository fieldRepository,
                          final OptionRepository optionRepository) {
    super();
    this.logger = logger;
    this.catalogRepository = catalogRepository;
    this.fieldRepository = fieldRepository;
    this.optionRepository = optionRepository;
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

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CatalogEventConstants.SELECTOR_NAME, selectorValue = CatalogEventConstants.DELETE_CATALOG)
  public String process(final DeleteCatalogCommand deleteCatalogCommand) {
    final Optional<CatalogEntity> optionalCatalog = this.catalogRepository.findByIdentifier(deleteCatalogCommand.identifier());
    if (optionalCatalog.isPresent()) {
      this.catalogRepository.delete(optionalCatalog.get());
      return deleteCatalogCommand.identifier();
    }
    return null;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CatalogEventConstants.SELECTOR_NAME, selectorValue = CatalogEventConstants.DELETE_FIELD)
  public String process(final DeleteFieldCommand deleteFieldCommand) {
    final Optional<CatalogEntity> optionalCatalog = this.catalogRepository.findByIdentifier(deleteFieldCommand.catalogIdentifier());
    if (optionalCatalog.isPresent()) {
      final Optional<FieldEntity> optionalField =
          this.fieldRepository.findByCatalogAndIdentifier(optionalCatalog.get(), deleteFieldCommand.fieldIdentifier());
      if (optionalField.isPresent()) {
        this.fieldRepository.delete(optionalField.get());
        return deleteFieldCommand.fieldIdentifier();
      }
    }
    return null;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CatalogEventConstants.SELECTOR_NAME, selectorValue = CatalogEventConstants.PUT_FIELD)
  public String process(final ChangeFieldCommand changeFieldCommand) {
    final Optional<CatalogEntity> optionalCatalog = this.catalogRepository.findByIdentifier(changeFieldCommand.catalogIdentifier());
    if (optionalCatalog.isPresent()) {
      final Optional<FieldEntity> optionalField =
          this.fieldRepository.findByCatalogAndIdentifier(optionalCatalog.get(), changeFieldCommand.field().getIdentifier());
      if (optionalField.isPresent()) {
        final FieldEntity fieldEntity = optionalField.get();

        fieldEntity.setOptions(null);
        final FieldEntity temporarySavedField = this.fieldRepository.saveAndFlush(fieldEntity);

        this.optionRepository.deleteByField(temporarySavedField);
        this.optionRepository.flush();

        final Field field = changeFieldCommand.field();
        temporarySavedField.setLabel(field.getLabel());
        temporarySavedField.setHint(field.getHint());
        temporarySavedField.setDescription(field.getDescription());
        temporarySavedField.setMandatory(field.getMandatory());
        if (field.getOptions() != null) {
          temporarySavedField.setOptions(
              field.getOptions()
                  .stream()
                  .map(option -> OptionMapper.map(temporarySavedField, option))
                  .collect(Collectors.toList())
          );
        }
        this.fieldRepository.save(temporarySavedField);
        return changeFieldCommand.field().getIdentifier();
      }
    }
    return null;
  }
}
