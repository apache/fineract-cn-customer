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
package org.apache.fineract.cn.customer.internal.service;

import org.apache.fineract.cn.customer.api.v1.domain.CustomerDocument;
import org.apache.fineract.cn.customer.internal.mapper.DocumentMapper;
import org.apache.fineract.cn.customer.internal.repository.DocumentEntity;
import org.apache.fineract.cn.customer.internal.repository.DocumentPageEntity;
import org.apache.fineract.cn.customer.internal.repository.DocumentPageRepository;
import org.apache.fineract.cn.customer.internal.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Myrle Krantz
 */
@Service
public class DocumentService {
  private final DocumentRepository documentRepository;
  private final DocumentPageRepository documentPageRepository;

  @Autowired
  public DocumentService(
      final DocumentRepository documentRepository,
      final DocumentPageRepository documentPageRepository) {
    this.documentRepository = documentRepository;
    this.documentPageRepository = documentPageRepository;
  }

  public Optional<DocumentPageEntity> findPage(
      final String customerIdentifier,
      final String documentIdentifier,
      final Integer pageNumber) {
    return this.documentPageRepository.findByCustomerIdAndDocumentIdentifierAndPageNumber(
        customerIdentifier,
        documentIdentifier,
        pageNumber);
  }

  public Stream<CustomerDocument> find(final String customerIdentifier) {
    final Stream<DocumentEntity> preMappedRet = this.documentRepository.findByCustomerId(customerIdentifier);
    return preMappedRet.map(DocumentMapper::map);
  }

  public Optional<CustomerDocument> findDocument(
      final String customerIdentifier,
      final String documentIdentifier) {
    return this.documentRepository.findByCustomerIdAndDocumentIdentifier(customerIdentifier, documentIdentifier)
        .map(DocumentMapper::map);
  }

  public boolean documentExists(
      final String customerIdentifier,
      final String documentIdentifier) {
    return findDocument(customerIdentifier, documentIdentifier).isPresent();
  }

  public Stream<Integer> findPageNumbers(
      final String customerIdentifier,
      final String documentIdentifier) {
    return documentPageRepository.findByCustomerIdAndDocumentIdentifier(customerIdentifier, documentIdentifier)
        .map(DocumentPageEntity::getPageNumber);
  }

  public boolean isDocumentCompleted(
      final String customerIdentifier,
      final String documentIdentifier) {
    return documentRepository.findByCustomerIdAndDocumentIdentifier(customerIdentifier, documentIdentifier)
        .map(DocumentEntity::getCompleted).orElse(true);
  }

  public boolean isDocumentMissingPages(
      final String customerIdentifier,
      final String documentIdentifier) {
    final List<Integer> pageNumbers = findPageNumbers(customerIdentifier, documentIdentifier)
        .sorted(Integer::compareTo)
        .collect(Collectors.toList());
    for (int i = 0; i < pageNumbers.size(); i++) {
      if (i != pageNumbers.get(i))
        return true;
    }

    return false;
  }
}