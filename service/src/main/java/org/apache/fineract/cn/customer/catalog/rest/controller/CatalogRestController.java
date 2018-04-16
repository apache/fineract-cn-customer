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
package org.apache.fineract.cn.customer.catalog.rest.controller;

import org.apache.fineract.cn.customer.PermittableGroupIds;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Catalog;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Field;
import org.apache.fineract.cn.customer.catalog.internal.command.ChangeFieldCommand;
import org.apache.fineract.cn.customer.catalog.internal.command.CreateCatalogCommand;
import org.apache.fineract.cn.customer.catalog.internal.command.DeleteCatalogCommand;
import org.apache.fineract.cn.customer.catalog.internal.command.DeleteFieldCommand;
import org.apache.fineract.cn.customer.catalog.internal.service.CatalogService;
import org.apache.fineract.cn.customer.ServiceConstants;
import java.util.List;
import javax.validation.Valid;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalogs")
public class CatalogRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final CatalogService catalogService;

  @Autowired
  public CatalogRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                               final CommandGateway commandGateway,
                               final CatalogService catalogService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.catalogService = catalogService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CATALOG)
  @RequestMapping(
      path = "",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> createCatalog(@RequestBody final Catalog catalog) {
    if (this.catalogService.catalogExists(catalog.getIdentifier())) {
      throw ServiceException.conflict("Catalog {0} already exists.", catalog.getIdentifier());
    }
    this.commandGateway.process(new CreateCatalogCommand(catalog));
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CATALOG)
  @RequestMapping(
      path = "",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<List<Catalog>> fetchCatalogs() {
    return ResponseEntity.ok(this.catalogService.fetchAllCatalogs());
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CATALOG)
  @RequestMapping(
      path = "/{identifier}",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Catalog> findCatalog(@PathVariable("identifier") final String identifier) {
    return ResponseEntity.ok(
        this.catalogService.findCatalog(identifier)
            .orElseThrow(() -> ServiceException.notFound("Catalog {0} not found.", identifier))
    );
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CATALOG)
  @RequestMapping(
      path = "/{identifier}",
      method = RequestMethod.DELETE,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> deleteCatalog(@PathVariable("identifier") final String identifier) {
    if (this.catalogService.catalogInUse(identifier)) {
      throw ServiceException.conflict("Catalog {0} in use.", identifier);
    }

    this.commandGateway.process(new DeleteCatalogCommand(identifier));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CATALOG)
  @RequestMapping(
      path = "/{catalogIdentifier}/fields/{fieldIdentifier}",
      method = RequestMethod.PUT,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> updateField(@PathVariable("catalogIdentifier") final String catalogIdentifier,
                                   @PathVariable("fieldIdentifier") final String fieldIdentifier,
                                   @RequestBody @Valid Field field) {
    if (this.catalogService.fieldInUse(catalogIdentifier, fieldIdentifier)) {
      throw ServiceException.conflict("Field {0} in use.", fieldIdentifier);
    }

    this.commandGateway.process(new ChangeFieldCommand(catalogIdentifier, field));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CATALOG)
  @RequestMapping(
      path = "/{catalogIdentifier}/fields/{fieldIdentifier}",
      method = RequestMethod.DELETE,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> deleteField(@PathVariable("catalogIdentifier") final String catalogIdentifier,
                                     @PathVariable("fieldIdentifier") final String fieldIdentifier) {
    if (this.catalogService.fieldInUse(catalogIdentifier, fieldIdentifier)) {
      throw ServiceException.conflict("Field {0} in use.", fieldIdentifier);
    }

    this.commandGateway.process(new DeleteFieldCommand(catalogIdentifier, fieldIdentifier));

    return ResponseEntity.accepted().build();
  }
}
