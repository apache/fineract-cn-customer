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
package org.apache.fineract.cn.customer.catalog.internal.service;

import org.apache.fineract.cn.customer.catalog.api.v1.domain.Catalog;
import org.apache.fineract.cn.customer.catalog.internal.mapper.CatalogMapper;
import org.apache.fineract.cn.customer.catalog.internal.mapper.FieldMapper;
import org.apache.fineract.cn.customer.catalog.internal.repository.CatalogEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.CatalogRepository;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldRepository;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldValueRepository;
import org.apache.fineract.cn.customer.ServiceConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

  private final Logger logger;
  private final CatalogRepository catalogRepository;
  private final FieldRepository fieldRepository;
  private final FieldValueRepository fieldValueRepository;

  @Autowired
  public CatalogService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                        final CatalogRepository catalogRepository,
                        final FieldRepository fieldRepository,
                        final FieldValueRepository fieldValueRepository) {
    super();
    this.logger = logger;
    this.catalogRepository = catalogRepository;
    this.fieldRepository = fieldRepository;
    this.fieldValueRepository = fieldValueRepository;
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

  public Boolean catalogInUse(final String identifier) {
    final CatalogEntity catalogEntity = this.catalogRepository.findByIdentifier(identifier).orElseThrow(
        () -> ServiceException.notFound("Catalog {0} not found.", identifier)
    );

    final ArrayList<Boolean> fieldUsageList = new ArrayList<>();
    catalogEntity.getFields().forEach(fieldEntity -> {
      fieldUsageList.add(this.fieldInUse(catalogEntity, fieldEntity.getIdentifier()));
    });

    return fieldUsageList.stream().anyMatch(aBoolean -> aBoolean.equals(Boolean.TRUE));
  }

  public Boolean fieldInUse(final String catalogIdentifier, final String fieldIdentifier) {
    final CatalogEntity catalogEntity = this.catalogRepository.findByIdentifier(catalogIdentifier).orElseThrow(
        () -> ServiceException.notFound("Catalog {0} not found.", catalogIdentifier)
    );
    return this.fieldInUse(catalogEntity, fieldIdentifier);
  }

  private Boolean fieldInUse(final CatalogEntity catalogEntity, final String fieldIdentifier) {
    final FieldEntity fieldEntity = this.fieldRepository.findByCatalogAndIdentifier(catalogEntity, fieldIdentifier).orElseThrow(
        () -> ServiceException.notFound("Field {0} of catalog {1} not found.", catalogEntity.getIdentifier(), fieldIdentifier));
    return this.fieldValueRepository.findByField(fieldEntity).isPresent();
  }
}
