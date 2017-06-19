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
package io.mifos.customer.catalog.service.internal.service;

import io.mifos.customer.catalog.api.v1.domain.Catalog;
import io.mifos.customer.catalog.service.internal.mapper.CatalogMapper;
import io.mifos.customer.catalog.service.internal.mapper.FieldMapper;
import io.mifos.customer.catalog.service.internal.repository.CatalogRepository;
import io.mifos.customer.service.ServiceConstants;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CatalogService {

  private final Logger logger;
  private final CatalogRepository catalogRepository;

  @Autowired
  public CatalogService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                        final CatalogRepository catalogRepository) {
    super();
    this.logger = logger;
    this.catalogRepository = catalogRepository;
  }

  public Boolean catalogExists(final String identifier) {
    return this.catalogRepository.findByIdentifier(identifier).isPresent();
  }

  public List<Catalog> fetchAllCatalogs() {
    return this.catalogRepository.findAll()
        .stream()
        .map(catalogEntity -> {
          final Catalog catalog = CatalogMapper.map(catalogEntity);
          catalog.setFields(
              catalogEntity.getFields()
                  .stream()
                  .map(FieldMapper::map)
                  .collect(Collectors.toList())
          );
          return catalog;
        })
        .collect(Collectors.toList());
  }

  public Optional<Catalog> findCatalog(final String identifier) {
    return this.catalogRepository.findByIdentifier(identifier)
        .map(catalogEntity -> {
          final Catalog catalog = CatalogMapper.map(catalogEntity);
          catalog.setFields(
              catalogEntity.getFields()
                  .stream()
                  .map(FieldMapper::map)
                  .collect(Collectors.toList())
          );
          return catalog;
        });
  }
}
