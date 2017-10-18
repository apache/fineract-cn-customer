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

import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.ServiceException;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.events.DocumentEvent;
import io.mifos.customer.api.v1.events.DocumentPageEvent;
import io.mifos.customer.service.internal.command.CompleteDocumentCommand;
import io.mifos.customer.service.internal.command.CreateDocumentCommand;
import io.mifos.customer.service.internal.command.CreateDocumentPageCommand;
import io.mifos.customer.service.internal.command.DeleteDocumentPageCommand;
import io.mifos.customer.service.internal.mapper.DocumentMapper;
import io.mifos.customer.service.internal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * @author Myrle Krantz
 */
@Aggregate
public class DocumentCommandHandler {
  private final DocumentRepository documentRepository;
  private final DocumentPageRepository documentPageRepository;
  private final CustomerRepository customerRepository;

  @Autowired
  public DocumentCommandHandler(
      final DocumentRepository documentRepository,
      final DocumentPageRepository documentPageRepository,
      final CustomerRepository customerRepository) {
    this.documentRepository = documentRepository;
    this.documentPageRepository = documentPageRepository;
    this.customerRepository = customerRepository;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_DOCUMENT_PAGE)
  public DocumentPageEvent process(final CreateDocumentPageCommand command) throws IOException {
    final DocumentEntity documentEntity = documentRepository.findByCustomerIdAndDocumentIdentifier(
        command.getCustomerIdentifier(),
        command.getDocumentIdentifier())
        .orElseThrow(() -> ServiceException.badRequest("Document not found"));

    final DocumentPageEntity documentPageEntity = DocumentMapper.map(command.getDocument(), command.getPageNumber(), documentEntity);
    documentPageRepository.save(documentPageEntity);

    return new DocumentPageEvent(command.getCustomerIdentifier(), command.getDocumentIdentifier(), command.getPageNumber());
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_DOCUMENT)
  public DocumentEvent process(final CreateDocumentCommand command) throws IOException {


    final CustomerEntity customerEntity = customerRepository.findByIdentifier(command.getCustomerIdentifier());
    final DocumentEntity documentEntity = DocumentMapper.map(command.getCustomerDocument(), customerEntity);
    documentRepository.save(documentEntity);

    return new DocumentEvent(command.getCustomerIdentifier(), command.getCustomerDocument().getIdentifier());
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_DOCUMENT_COMPLETE)
  public DocumentEvent process(final CompleteDocumentCommand command) throws IOException {
    final DocumentEntity documentEntity = documentRepository.findByCustomerIdAndDocumentIdentifier(
        command.getCustomerIdentifier(),
        command.getDocumentIdentifier())
        .orElseThrow(() -> ServiceException.badRequest("Document not found"));

    documentEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));
    documentEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    documentEntity.setCompleted(true);
    documentRepository.save(documentEntity);


    return new DocumentEvent(command.getCustomerIdentifier(), command.getDocumentIdentifier());
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.DELETE_DOCUMENT_PAGE)
  public DocumentPageEvent process(final DeleteDocumentPageCommand command) throws IOException {
    documentPageRepository.findByCustomerIdAndDocumentIdentifierAndPageNumber(
        command.getCustomerIdentifier(),
        command.getDocumentIdentifier(),
        command.getPageNumber())
        .ifPresent(documentPageRepository::delete);

    //No exception if it's not present, because why bother.  It's not present.  That was the goal.

    return new DocumentPageEvent(command.getCustomerIdentifier(), command.getDocumentIdentifier(), command.getPageNumber());
  }
}
