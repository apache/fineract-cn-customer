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
package io.mifos.customer.api.v1.client;

import io.mifos.core.api.annotation.ThrowsException;
import io.mifos.core.api.annotation.ThrowsExceptions;
import io.mifos.customer.api.v1.config.CustomerFeignClientConfig;
import io.mifos.customer.api.v1.domain.CustomerDocument;
import org.hibernate.validator.constraints.Range;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Myrle Krantz
 */
@FeignClient(name="customer-v1", path="/customer/v1", configuration= CustomerFeignClientConfig.class)
public interface CustomerDocumentsManager {

  @RequestMapping(
      value = "/customers/{customeridentifier}/documents",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  List<CustomerDocument> getDocuments(
      @PathVariable("customeridentifier") final String customerIdentifier);



  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE
  )
  CustomerDocument getDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier);



  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = DocumentValidationException.class)
  })
  void createDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @RequestBody final CustomerDocument customerDocument);


  /**
   * Once a document is "completed" its name and images cannot be changed again.  Only completed
   * documents should be referenced by other services.
   *
   * @param completed once this is set to true it cannot be changed back again.
   */
  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}/completed",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.CONFLICT, exception = CompletedDocumentCannotBeChangedException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = DocumentValidationException.class),
  })
  void completeDocument(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @RequestBody final Boolean completed);



  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}/pages",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  List<Integer> getDocumentPageNumbers(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier);



  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}/pages/{pagenumber}",
      method = RequestMethod.GET,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  byte[] getDocumentPage(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @PathVariable("pagenumber") final Integer pageNumber);



  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}/pages/{pagenumber}",
      method = RequestMethod.POST,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.CONFLICT, exception = CompletedDocumentCannotBeChangedException.class),
      @ThrowsException(status = HttpStatus.BAD_REQUEST, exception = DocumentValidationException.class),
  })
  void createDocumentPage(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @PathVariable("pagenumber") @Range(min=0) final Integer pageNumber,
      @RequestBody final MultipartFile page);


  @RequestMapping(
      value = "/customers/{customeridentifier}/documents/{documentidentifier}/pages/{pagenumber}",
      method = RequestMethod.DELETE,
      produces = MediaType.ALL_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ThrowsExceptions({
      @ThrowsException(status = HttpStatus.CONFLICT, exception = CompletedDocumentCannotBeChangedException.class)
  })
  void deleteDocumentPage(
      @PathVariable("customeridentifier") final String customerIdentifier,
      @PathVariable("documentidentifier") final String documentIdentifier,
      @PathVariable("pagenumber") @Range(min=0) final Integer pageNumber);
}