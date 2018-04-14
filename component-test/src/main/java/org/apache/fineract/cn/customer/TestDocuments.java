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
package org.apache.fineract.cn.customer;

import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.client.CompletedDocumentCannotBeChangedException;
import org.apache.fineract.cn.customer.api.v1.client.DocumentValidationException;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.api.v1.domain.CustomerDocument;
import org.apache.fineract.cn.customer.api.v1.events.DocumentEvent;
import org.apache.fineract.cn.customer.api.v1.events.DocumentPageEvent;
import org.apache.fineract.cn.customer.util.CustomerDocumentGenerator;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.api.util.NotFoundException;
import org.apache.fineract.cn.test.domain.TimeStampChecker;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

/**
 * @author Myrle Krantz
 */
public class TestDocuments extends AbstractCustomerTest {

  @Test
  public void shouldUploadThenDeleteInCompleteDocument() throws InterruptedException, IOException {
    logger.info("Prepare test");
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customerManager.createCustomer(customer);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier()));

    final CustomerDocument customerDocument = CustomerDocumentGenerator.createRandomCustomerDocument();
    customerDocumentsManager.createDocument(customer.getIdentifier(), customerDocument.getIdentifier(), customerDocument);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.POST_DOCUMENT,
        new DocumentEvent(customer.getIdentifier(), customerDocument.getIdentifier())));

    for (int i = 0; i < 5; i++) {
      createDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), i);
    }

    logger.info("Delete document");
    customerDocumentsManager.deleteDocument(customer.getIdentifier(), customerDocument.getIdentifier());
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.DELETE_DOCUMENT,
        new DocumentEvent(customer.getIdentifier(), customerDocument.getIdentifier())));

    try {
      customerDocumentsManager.getDocument(customer.getIdentifier(), customerDocument.getIdentifier());
      Assert.fail("Deleted document should not be findable");
    }
    catch (final NotFoundException ignored) {}

    final List<CustomerDocument> customersDocuments = customerDocumentsManager.getDocuments(customer.getIdentifier());
    Assert.assertFalse(customersDocuments.contains(customerDocument));
  }

  @Test
  public void shouldUploadEditThenCompleteDocument() throws InterruptedException, IOException {
    logger.info("Prepare test");
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customerManager.createCustomer(customer);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier()));


    logger.info("Check that document \"stub\" can be created");
    final CustomerDocument customerDocument = CustomerDocumentGenerator.createRandomCustomerDocument();
    customerDocumentsManager.createDocument(customer.getIdentifier(), customerDocument.getIdentifier(), customerDocument);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.POST_DOCUMENT,
        new DocumentEvent(customer.getIdentifier(), customerDocument.getIdentifier())));

    final CustomerDocument createdCustomerDocument = customerDocumentsManager.getDocument(
        customer.getIdentifier(), customerDocument.getIdentifier());
    Assert.assertEquals(customerDocument, createdCustomerDocument);


    logger.info("Check that pages can be created");
    for (int i = 0; i < 10; i++) {
      createDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), i);
    }

    List<Integer> pageNumbers = customerDocumentsManager.getDocumentPageNumbers(
        customer.getIdentifier(), customerDocument.getIdentifier());
    final List<Integer> expectedPageNumbers = IntStream.range(0, 10).boxed().collect(Collectors.toList());
    Assert.assertEquals(expectedPageNumbers, pageNumbers);


    logger.info("Check that a page can be deleted");
    customerDocumentsManager.deleteDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), 9);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.DELETE_DOCUMENT_PAGE,
        new DocumentPageEvent(customer.getIdentifier(), customerDocument.getIdentifier(), 9)));

    final List<Integer> changedPageNumbers = customerDocumentsManager.getDocumentPageNumbers(
        customer.getIdentifier(), customerDocument.getIdentifier());
    final List<Integer> changedExpectedPageNumbers = IntStream.range(0, 9).boxed().collect(Collectors.toList());
    Assert.assertEquals(changedExpectedPageNumbers, changedPageNumbers);

    try {
      customerDocumentsManager.getDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), 9);
      Assert.fail("Getting the 9th document page should throw a NotFoundException after the 9th page was removed.");
    }
    catch (final NotFoundException ignored) {}


    logger.info("Check that a document which is missing pages cannot be completed");
    customerDocumentsManager.deleteDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), 2);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.DELETE_DOCUMENT_PAGE,
        new DocumentPageEvent(customer.getIdentifier(), customerDocument.getIdentifier(), 2)));

    try {
      customerDocumentsManager.completeDocument(customer.getIdentifier(), customerDocument.getIdentifier(), true);
      Assert.fail("It shouldn't be possible to complete a document with missing pages.");
    }
    catch (final DocumentValidationException ignored) {}

    createDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), 2);


    logger.info("Check that a document's description can be changed.");
    customerDocument.setDescription("new description");
    customerDocumentsManager.changeDocument(customer.getIdentifier(), customerDocument.getIdentifier(), customerDocument);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.PUT_DOCUMENT,
        new DocumentEvent(customer.getIdentifier(), customerDocument.getIdentifier())));

    {
      final CustomerDocument changedCustomerDocument = customerDocumentsManager.getDocument(customer.getIdentifier(), customerDocument.getIdentifier());
      Assert.assertEquals(customerDocument, changedCustomerDocument);
    }


    logger.info("Check that a valid document can be completed");
    final TimeStampChecker timeStampChecker = TimeStampChecker.roughlyNow();
    customerDocumentsManager.completeDocument(customer.getIdentifier(), customerDocument.getIdentifier(), true);
    Assert.assertTrue(eventRecorder.wait(CustomerEventConstants.POST_DOCUMENT_COMPLETE,
        new DocumentEvent(customer.getIdentifier(), customerDocument.getIdentifier())));

    final CustomerDocument completedDocument = customerDocumentsManager.getDocument(
        customer.getIdentifier(), customerDocument.getIdentifier());
    Assert.assertEquals(true, completedDocument.isCompleted());
    timeStampChecker.assertCorrect(completedDocument.getCreatedOn());


    logger.info("Check that document can't be changed or removed after completion");
    try {
      createDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), 9);
      Assert.fail("Adding another page after the document is completed shouldn't be possible.");
    }
    catch (final CompletedDocumentCannotBeChangedException ignored) {}
    try {
      customerDocumentsManager.deleteDocumentPage(customer.getIdentifier(), customerDocument.getIdentifier(), 8);
      Assert.fail("Deleting a page after the document is completed shouldn't be possible.");
    }
    catch (final CompletedDocumentCannotBeChangedException ignored) {}
    try {
      customerDocumentsManager.changeDocument(customer.getIdentifier(), customerDocument.getIdentifier(), customerDocument);
      Assert.fail("Changing a document after it is completed shouldn't be possible.");
    }
    catch (final CompletedDocumentCannotBeChangedException ignored) {}
    try {
      customerDocumentsManager.deleteDocument(customer.getIdentifier(), customerDocument.getIdentifier());
      Assert.fail("Changing a document after it is completed shouldn't be possible.");
    }
    catch (final CompletedDocumentCannotBeChangedException ignored) {}


    logger.info("Check that document can't be uncompleted");
    try {
      customerDocumentsManager.completeDocument(customer.getIdentifier(), customerDocument.getIdentifier(), false);
      Assert.fail("It shouldn't be possible to change a document from completed to uncompleted.");
    }
    catch (final CompletedDocumentCannotBeChangedException ignored) {}


    logger.info("Check that document is in the list");
    final List<CustomerDocument> documents = customerDocumentsManager.getDocuments(customer.getIdentifier());
    final boolean documentIsInList = documents.stream().anyMatch(x ->
        (x.getIdentifier().equals(customerDocument.getIdentifier())) &&
            (x.isCompleted()));
    Assert.assertTrue("The document we just completed should be in the list", documentIsInList);
  }


  private void createDocumentPage(
      final String customerIdentifier,
      final String documentIdentifier,
      final int pageNumber) throws InterruptedException, IOException {
    final MockMultipartFile page = new MockMultipartFile(
        "page",
        "test.png",
        MediaType.IMAGE_PNG_VALUE,
        RandomStringUtils.randomAlphanumeric(20).getBytes());

    customerDocumentsManager.createDocumentPage(customerIdentifier, documentIdentifier, pageNumber, page);
    eventRecorder.wait(CustomerEventConstants.POST_DOCUMENT_PAGE,
        new DocumentPageEvent(customerIdentifier, documentIdentifier, pageNumber));

    Thread.sleep(100);

    final byte[] uploadedPage = customerDocumentsManager.getDocumentPage(customerIdentifier, documentIdentifier, pageNumber);
    Assert.assertTrue("Page " + pageNumber, Arrays.equals(page.getBytes(), uploadedPage));
  }
}
