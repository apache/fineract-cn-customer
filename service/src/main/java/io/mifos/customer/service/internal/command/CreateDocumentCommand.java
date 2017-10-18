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
package io.mifos.customer.service.internal.command;

import io.mifos.customer.api.v1.domain.CustomerDocument;

/**
 * @author Myrle Krantz
 */
public class CreateDocumentCommand {
  private final String customerIdentifier;
  private final CustomerDocument customerDocument;

  public CreateDocumentCommand(
      final String customerIdentifier,
      final CustomerDocument customerDocument) {
    this.customerIdentifier = customerIdentifier;
    this.customerDocument = customerDocument;
  }

  public String getCustomerIdentifier() {
    return customerIdentifier;
  }

  public CustomerDocument getCustomerDocument() {
    return customerDocument;
  }

  @Override
  public String toString() {
    return "CreateDocumentCommand{" +
        "customerIdentifier='" + customerIdentifier + '\'' +
        ", customerDocument=" + customerDocument.getIdentifier() +
        '}';
  }
}
