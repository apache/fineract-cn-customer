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
package org.apache.fineract.cn.customer.rest.controller;

import org.apache.fineract.cn.customer.PermittableGroupIds;
import org.apache.fineract.cn.customer.api.v1.domain.CustomerDocument;
import org.apache.fineract.cn.customer.internal.command.ChangeDocumentCommand;
import org.apache.fineract.cn.customer.internal.command.CompleteDocumentCommand;
import org.apache.fineract.cn.customer.internal.command.CreateDocumentCommand;
import org.apache.fineract.cn.customer.internal.command.CreateDocumentPageCommand;
import org.apache.fineract.cn.customer.internal.command.DeleteDocumentCommand;
import org.apache.fineract.cn.customer.internal.command.DeleteDocumentPageCommand;
import org.apache.fineract.cn.customer.internal.repository.DocumentPageEntity;
import org.apache.fineract.cn.customer.internal.service.CustomerService;
import org.apache.fineract.cn.customer.internal.service.DocumentService;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.lang.ServiceException;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Myrle Krantz
 */
@RestController
@RequestMapping("/customers/{customeridentifier}/documents")
public class DocumentsRestController {
  private final CommandGateway commandGateway;
  private final CustomerService customerService;
  private final DocumentService documentService;

  @Autowired
  public DocumentsRestController(
      final CommandGateway commandGateway,
      final CustomerService customerService,
      final DocumentService documentService) {
    this.commandGateway = commandGateway;
    this.customerService = customerService;
    this.documentService = documentService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public ResponseEntity<List<CustomerDocument>> getDocuments(
      @PathVariable("customeridentifier") final String customerIdentifier) {
    throwIfCustomerNotExists(customerIdentifier);

    return ResponseEntity.ok(documentService.find(customerIdentifier).collect(Collectors.toList()));
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public ResponseEntity<CustomerDocument> getDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier) {
    return ResponseEntity
        .ok(documentService.findDocument(customerIdentifier, documentIdentifier)
            .orElseThrow(() -> ServiceException.notFound("Document ''{0}'' for customer ''{1}'' not found.",
                documentIdentifier, customerIdentifier)));
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public @ResponseBody
  ResponseEntity<Void> createDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @RequestBody final @Valid CustomerDocument instance) {
    throwIfCustomerNotExists(customerIdentifier);

    if (!instance.getIdentifier().equals(documentIdentifier))
      throw ServiceException.badRequest("Document identifier in request body must match document identifier in request path.");

    commandGateway.process(new CreateDocumentCommand(customerIdentifier, instance));

    return ResponseEntity.accepted().build();
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public @ResponseBody
  ResponseEntity<Void> changeDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @RequestBody final @Valid CustomerDocument instance) {
    throwIfCustomerNotExists(customerIdentifier);
    throwIfCustomerDocumentNotExists(customerIdentifier, documentIdentifier);

    throwIfDocumentCompleted(customerIdentifier, documentIdentifier);

    if (!instance.getIdentifier().equals(documentIdentifier))
      throw ServiceException.badRequest("Document identifier in request body must match document identifier in request path.");

    commandGateway.process(new ChangeDocumentCommand(customerIdentifier, instance));

    return ResponseEntity.accepted().build();
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}",
      method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public @ResponseBody
  ResponseEntity<Void> deleteDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier) {
    throwIfCustomerNotExists(customerIdentifier);
    throwIfCustomerDocumentNotExists(customerIdentifier, documentIdentifier);

    throwIfDocumentCompleted(customerIdentifier, documentIdentifier);

    commandGateway.process(new DeleteDocumentCommand(customerIdentifier, documentIdentifier));

    return ResponseEntity.accepted().build();
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}/completed",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public @ResponseBody
  ResponseEntity<Void> completeDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @RequestBody final @Valid Boolean completed) {
    throwIfCustomerDocumentNotExists(customerIdentifier, documentIdentifier);

    if (!completed)
      throwIfDocumentCompleted(customerIdentifier, documentIdentifier);

    throwIfPagesMissing(customerIdentifier, documentIdentifier);

    if (completed)
      commandGateway.process(new CompleteDocumentCommand(customerIdentifier, documentIdentifier));

    return ResponseEntity.accepted().build();
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}/pages",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public @ResponseBody
  ResponseEntity<List<Integer>> getDocumentPageNumbers(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier) {
    throwIfCustomerDocumentNotExists(customerIdentifier, documentIdentifier);

    return ResponseEntity.ok(documentService.findPageNumbers(customerIdentifier, documentIdentifier).collect(Collectors.toList()));
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}/pages/{pagenumber}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public ResponseEntity<byte[]> getDocumentPage(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @PathVariable("pagenumber") final Integer pageNumber) {
    final DocumentPageEntity documentPageEntity = documentService.findPage(customerIdentifier, documentIdentifier, pageNumber)
        .orElseThrow(() -> ServiceException.notFound("Page ''{0}'' of document ''{1}'' for customer ''{2}'' not found.",
            pageNumber, documentIdentifier, customerIdentifier));

    return ResponseEntity
        .ok()
        .contentType(MediaType.parseMediaType(documentPageEntity.getContentType()))
        .contentLength(documentPageEntity.getImage().length)
        .body(documentPageEntity.getImage());
  }


  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}/pages/{pagenumber}",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public @ResponseBody
  ResponseEntity<Void> createDocumentPage(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @PathVariable("pagenumber") @Range(min=0) final Integer pageNumber,
      @RequestBody final MultipartFile page) {
    if(page == null) {
      throw ServiceException.badRequest("Document not found");
    }

    throwIfCustomerNotExists(customerIdentifier);
    throwIfDocumentCompleted(customerIdentifier, documentIdentifier);
    throwIfInvalidContentType(page.getContentType());

    commandGateway.process(new CreateDocumentPageCommand(customerIdentifier, documentIdentifier, pageNumber, page));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.DOCUMENTS)
  @RequestMapping(
      value = "/{documentidentifier}/pages/{pagenumber}",
      method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public @ResponseBody
  ResponseEntity<Void>  deleteDocumentPage(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @PathVariable("pagenumber") final Integer pageNumber) {
    throwIfCustomerDocumentNotExists(customerIdentifier, documentIdentifier);

    throwIfDocumentCompleted(customerIdentifier, documentIdentifier);

    commandGateway.process(new DeleteDocumentPageCommand(customerIdentifier, documentIdentifier, pageNumber));

    return ResponseEntity.accepted().build();
  }

  private void throwIfCustomerNotExists(final String customerIdentifier) {
    if (!this.customerService.customerExists(customerIdentifier)) {
      throw ServiceException.notFound("Customer ''{0}'' not found.", customerIdentifier);
    }
  }

  private void throwIfCustomerDocumentNotExists(final String customerIdentifier, final String documentIdentifier) {
    if (!this.documentService.documentExists(customerIdentifier, documentIdentifier)) {
      throw ServiceException.notFound("Customer ''{0}'' not found.", customerIdentifier);
    }
  }

  private void throwIfInvalidContentType(final String contentType) {
    if(!contentType.contains(MediaType.IMAGE_JPEG_VALUE)
        && !contentType.contains(MediaType.IMAGE_PNG_VALUE)) {
      throw ServiceException.badRequest("Image has contentType ''{0}'', but only content types ''{1}'' and ''{2}'' allowed.",
          contentType, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);
    }
  }

  private void throwIfDocumentCompleted(final String customerIdentifier, final String documentIdentifier) {
    if (documentService.isDocumentCompleted(customerIdentifier, documentIdentifier))
      throw ServiceException.conflict("The document ''{0}'' for customer ''{1}'' is completed and cannot be uncompleted.",
          documentIdentifier, customerIdentifier);
  }

  private void throwIfPagesMissing(final String customerIdentifier, final String documentIdentifier) {
    if (documentService.isDocumentMissingPages(customerIdentifier, documentIdentifier))
      throw ServiceException.badRequest("The document ''{0}'' for customer ''{1}'' is missing pages.",
          documentIdentifier, customerIdentifier);
  }
}
