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
import org.apache.fineract.cn.customer.api.v1.client.IdentificationCardNotFoundException;
import org.apache.fineract.cn.customer.api.v1.client.ScanAlreadyExistsException;
import org.apache.fineract.cn.customer.api.v1.client.ScanNotFoundException;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.api.v1.domain.IdentificationCard;
import org.apache.fineract.cn.customer.api.v1.domain.IdentificationCardScan;
import org.apache.fineract.cn.customer.api.v1.events.ScanEvent;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import org.apache.fineract.cn.customer.util.IdentificationCardGenerator;
import org.apache.fineract.cn.customer.util.ScanGenerator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.stream.Stream;

public class TestIdentificationCards extends AbstractCustomerTest {

    @Test
    public void shouldFetchIdentificationCards() throws Exception {
        final String customerIdentifier = this.createCustomer();

        Stream.of(
                IdentificationCardGenerator.createRandomIdentificationCard(),
                IdentificationCardGenerator.createRandomIdentificationCard(),
                IdentificationCardGenerator.createRandomIdentificationCard()
        ).forEach(identificationCard -> {
            this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
            try {
                this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
            } catch (final InterruptedException ex) {
                Assert.fail(ex.getMessage());
            }
        });

        final List<IdentificationCard> result = this.customerManager.fetchIdentificationCards(customerIdentifier);

        Assert.assertTrue(result.size() == 3);
    }

    @Test
    public void shouldCreateIdentificationCard() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        final IdentificationCard identificationCard = this.customerManager.findIdentificationCard(customerIdentifier, identificationCardNumber);

        Assert.assertNotNull(identificationCard);

        Assert.assertEquals(identificationCard.getCreatedBy(), TEST_USER);
    }

    @Test
    public void shouldUpdateIdentificationCard() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        final IdentificationCard identificationCard = this.customerManager.findIdentificationCard(customerIdentifier, identificationCardNumber);

        final IdentificationCard updatedIdentificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

        updatedIdentificationCard.setNumber(identificationCardNumber);

        this.customerManager.updateIdentificationCard(customerIdentifier, updatedIdentificationCard.getNumber(), updatedIdentificationCard);

        this.eventRecorder.wait(CustomerEventConstants.PUT_IDENTIFICATION_CARD, updatedIdentificationCard.getNumber());

        final IdentificationCard changedIdentificationCard = this.customerManager.findIdentificationCard(customerIdentifier, identificationCard.getNumber());

        Assert.assertEquals(updatedIdentificationCard.getType(), changedIdentificationCard.getType());
        Assert.assertEquals(updatedIdentificationCard.getIssuer(), changedIdentificationCard.getIssuer());
        Assert.assertEquals(updatedIdentificationCard.getNumber(), changedIdentificationCard.getNumber());
        Assert.assertEquals(TEST_USER, changedIdentificationCard.getLastModifiedBy());
    }

    @Test(expected = IdentificationCardNotFoundException.class)
    public void shouldDeleteIdentificationCard() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        this.customerManager.deleteIdentificationCard(customerIdentifier, identificationCardNumber);

        this.eventRecorder.wait(CustomerEventConstants.DELETE_IDENTIFICATION_CARD, identificationCardNumber);

        this.customerManager.findIdentificationCard(customerIdentifier, identificationCardNumber);
    }

    @Test(expected = IdentificationCardNotFoundException.class)
    public void shouldDeleteIdentificationCardWithScan() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        this.createScan(customerIdentifier, identificationCardNumber);

        this.customerManager.deleteIdentificationCard(customerIdentifier, identificationCardNumber);

        this.eventRecorder.wait(CustomerEventConstants.DELETE_IDENTIFICATION_CARD, identificationCardNumber);

        this.customerManager.findIdentificationCard(customerIdentifier, identificationCardNumber);
    }

    @Test
    public void shouldFetchScans() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        this.createScan(customerIdentifier, identificationCardNumber);
        this.createScan(customerIdentifier, identificationCardNumber);
        this.createScan(customerIdentifier, identificationCardNumber);

        final List<IdentificationCardScan> result = this.customerManager.fetchIdentificationCardScans(customerIdentifier, identificationCardNumber);

        Assert.assertTrue(result.size() == 3);
    }

    @Test
    public void shouldFindScan() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumberOne = this.createIdentificationCard(customerIdentifier);
        final String identificationCardNumberTwo = this.createIdentificationCard(customerIdentifier);
        this.createScan(customerIdentifier, identificationCardNumberOne, "sameIdentifier");
        final IdentificationCardScan createdScan = this.createScan(customerIdentifier, identificationCardNumberTwo, "sameIdentifier");

        final IdentificationCardScan scan = this.customerManager.findIdentificationCardScan(customerIdentifier, identificationCardNumberTwo, createdScan.getIdentifier());

        Assert.assertNotNull(scan);

        Assert.assertEquals(scan.getIdentifier(), createdScan.getIdentifier());
        Assert.assertEquals(scan.getDescription(), createdScan.getDescription());
    }

    @Test
    public void shouldFindScanWithImage() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        final IdentificationCardScan scan = ScanGenerator.createRandomScan(null);

        final byte[] imageInBytes = RandomStringUtils.randomAlphanumeric(20).getBytes();

        final MockMultipartFile image = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, imageInBytes);

        this.customerManager.postIdentificationCardScan(customerIdentifier, identificationCardNumber, scan.getIdentifier(), scan.getDescription(), image);

        this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD_SCAN, new ScanEvent(identificationCardNumber, scan.getIdentifier()));

        final byte[] persistedImageInBytes = this.customerManager.fetchIdentificationCardScanImage(customerIdentifier, identificationCardNumber, scan.getIdentifier());

        Assert.assertArrayEquals(imageInBytes, persistedImageInBytes);
    }

    @Test(expected = ScanAlreadyExistsException.class)
    public void shouldThrowIfScanAlreadyExists() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        final IdentificationCardScan scan = this.createScan(customerIdentifier, identificationCardNumber);

        final MockMultipartFile image = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, RandomStringUtils.randomAlphanumeric(20).getBytes());

        this.customerManager.postIdentificationCardScan(customerIdentifier, identificationCardNumber, scan.getIdentifier(), scan.getDescription(), image);
    }

    @Test(expected = ScanNotFoundException.class)
    public void shouldDeleteScan() throws Exception {
        final String customerIdentifier = this.createCustomer();

        final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

        final IdentificationCardScan createdScan = this.createScan(customerIdentifier, identificationCardNumber);

        this.customerManager.deleteScan(customerIdentifier, identificationCardNumber, createdScan.getIdentifier());

        this.eventRecorder.wait(CustomerEventConstants.DELETE_IDENTIFICATION_CARD_SCAN, new ScanEvent(identificationCardNumber, createdScan.getIdentifier()));

        this.customerManager.findIdentificationCardScan(customerIdentifier, identificationCardNumber, createdScan.getIdentifier());
    }

    private String createCustomer() throws Exception {
        final Customer customer = CustomerGenerator.createRandomCustomer();

        this.customerManager.createCustomer(customer);

        this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

        return customer.getIdentifier();
    }

    private String createIdentificationCard(final String customerIdentifier) throws Exception {
        final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

        this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);

        this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());

        return identificationCard.getNumber();
    }

    private IdentificationCardScan createScan(final String customerIdentifier, final String cardNumber) throws Exception {
        final IdentificationCardScan scan = ScanGenerator.createRandomScan(null);

        this.postIdentificationCardScan(customerIdentifier, cardNumber, scan);

        return scan;
    }

    private IdentificationCardScan createScan(final String customerIdentifier, final String cardNumber, final String identifier) throws Exception {
        final IdentificationCardScan scan = ScanGenerator.createRandomScan(identifier);

        this.postIdentificationCardScan(customerIdentifier, cardNumber, scan);

        return scan;
    }

    private void postIdentificationCardScan(final String customerIdentifier, final String cardNumber, final IdentificationCardScan scan) throws InterruptedException {
        final MockMultipartFile image = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, RandomStringUtils.randomAlphanumeric(20).getBytes());

        this.customerManager.postIdentificationCardScan(customerIdentifier, cardNumber, scan.getIdentifier(), scan.getDescription(), image);

        this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD_SCAN, new ScanEvent(cardNumber, scan.getIdentifier()));
    }

}