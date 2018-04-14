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
package org.apache.fineract.cn.customer.catalog.api.v1.client;

import org.apache.fineract.cn.customer.catalog.api.v1.domain.Catalog;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Field;
import java.util.List;
import org.apache.fineract.cn.api.annotation.ThrowsException;
import org.apache.fineract.cn.api.annotation.ThrowsExceptions;
import org.apache.fineract.cn.api.util.CustomFeignClientsConfiguration;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SuppressWarnings("unused")
@FeignClient(name="customer-v1", path="/customer/v1", configuration=CustomFeignClientsConfiguration.class)
public interface CatalogManager {

  @RequestMapping(
      path = "/catalogs",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.CONFLICT, exception = CatalogAlreadyExistsException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = CatalogValidationException.class)
  })
  void createCatalog(@RequestBody final Catalog catalog);

  @RequestMapping(
      path = "/catalogs",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  List<Catalog> fetchCatalogs();

  @RequestMapping(
      path = "/catalogs/{identifier}",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = CatalogNotFoundException.class)
  })
  Catalog findCatalog(@PathVariable("identifier") final String identifier);

  @RequestMapping(
      path = "/catalogs/{identifier}",
      method = RequestMethod.DELETE,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = CatalogNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = CatalogValidationException.class),
      @ThrowsException(status = HttpStatus.CONFLICT, exception = CatalogAlreadyInUseException.class)
  })
  void deleteCatalog(@PathVariable("identifier") final String identifier);

  @RequestMapping(
      path = "/catalogs/{catalogIdentifier}/fields/{fieldIdentifier}",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = CatalogNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = CatalogValidationException.class),
      @ThrowsException(status = HttpStatus.CONFLICT, exception = FieldAlreadyInUseException.class)
  })
  void updateField(@PathVariable("catalogIdentifier") final String catalogIdentifier,
                   @PathVariable("fieldIdentifier") final String fieldIdentifier,
                   @RequestBody Field field);

  @RequestMapping(
      path = "/catalogs/{catalogIdentifier}/fields/{fieldIdentifier}",
      method = RequestMethod.DELETE,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.NOT_FOUND, exception = CatalogNotFoundException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = CatalogValidationException.class),
      @ThrowsException(status = HttpStatus.CONFLICT, exception = FieldAlreadyInUseException.class)
  })
  void deleteField(@PathVariable("catalogIdentifier") final String catalogIdentifier,
                   @PathVariable("fieldIdentifier") final String fieldIdentifier);

}
