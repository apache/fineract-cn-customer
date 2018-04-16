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
package org.apache.fineract.cn.customer.internal.command;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Myrle Krantz
 */
public class CreateDocumentPageCommand {
  private final String customerIdentifier;
  private final String documentIdentifier;
  private final Integer pageNumber;
  private final MultipartFile document;

  public CreateDocumentPageCommand(
      final String customerIdentifier,
      final String documentIdentifier,
      final int pageNumber,
      final MultipartFile document) {
    this.customerIdentifier = customerIdentifier;
    this.documentIdentifier = documentIdentifier;
    this.pageNumber = pageNumber;
    this.document = document;
  }

  public String getCustomerIdentifier() {
    return customerIdentifier;
  }

  public String getDocumentIdentifier() {
    return documentIdentifier;
  }

  public Integer getPageNumber() {
    return pageNumber;
  }

  public MultipartFile getDocument() {
    return document;
  }
}
