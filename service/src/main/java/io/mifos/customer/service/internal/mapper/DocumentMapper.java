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
import io.mifos.customer.api.v1.domain.CustomerDocument;
import io.mifos.customer.service.internal.repository.CustomerEntity;
import io.mifos.customer.service.internal.repository.DocumentEntity;
import io.mifos.customer.service.internal.repository.DocumentPageEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * @author Myrle Krantz
 */
public class DocumentMapper {
  private DocumentMapper() {
    super();
  }


  public static DocumentPageEntity map(
      final MultipartFile multipartFile,
      final int pageNumber,
      final DocumentEntity documentEntity) throws IOException {
    final DocumentPageEntity ret = new DocumentPageEntity();
    ret.setDocument(documentEntity);
    ret.setPageNumber(pageNumber);
    ret.setImage(multipartFile.getBytes());
    ret.setSize(multipartFile.getSize());
    ret.setContentType(multipartFile.getContentType());
    return ret;
  }

  public static CustomerDocument map(final DocumentEntity documentEntity) {
    final CustomerDocument ret = new CustomerDocument();
    ret.setCompleted(documentEntity.getCompleted());
    ret.setCreatedBy(documentEntity.getCreatedBy());
    ret.setCreatedOn(DateConverter.toIsoString(documentEntity.getCreatedOn()));
    ret.setIdentifier(documentEntity.getIdentifier());
    ret.setDescription(documentEntity.getDescription());
    return ret;
  }

  public static DocumentEntity map(final CustomerDocument customerDocument, final CustomerEntity customerEntity) {
    final DocumentEntity ret = new DocumentEntity();
    ret.setCustomer(customerEntity);
    ret.setCompleted(false);
    ret.setCreatedBy(UserContextHolder.checkedGetUser());
    ret.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));
    ret.setIdentifier(customerDocument.getIdentifier());
    ret.setDescription(customerDocument.getDescription());
    return ret;
  }
}
