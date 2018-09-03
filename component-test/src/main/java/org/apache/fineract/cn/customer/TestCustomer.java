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
import org.apache.fineract.cn.customer.api.v1.client.CustomerAlreadyExistsException;
import org.apache.fineract.cn.customer.api.v1.client.CustomerNotFoundException;
import org.apache.fineract.cn.customer.api.v1.client.CustomerValidationException;
import org.apache.fineract.cn.customer.api.v1.client.DocumentValidationException;
import org.apache.fineract.cn.customer.api.v1.client.PortraitNotFoundException;
import org.apache.fineract.cn.customer.api.v1.domain.Address;
import org.apache.fineract.cn.customer.api.v1.domain.Command;
import org.apache.fineract.cn.customer.api.v1.domain.ContactDetail;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.api.v1.domain.CustomerPage;
import org.apache.fineract.cn.customer.api.v1.domain.ProcessStep;
import org.apache.fineract.cn.customer.util.AddressGenerator;
import org.apache.fineract.cn.customer.util.CommandGenerator;
import org.apache.fineract.cn.customer.util.ContactDetailGenerator;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.lang.DateConverter;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class TestCustomer extends AbstractCustomerTest {

  @Test
  public void shouldCreateCustomer() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    //Pending customer is not in good standing.
    Assert.assertFalse(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    final Customer createdCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertNotNull(createdCustomer);

  }

  @Test
  public void shouldNotCreateCustomerAlreadyExists() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    try {
      this.customerManager.createCustomer(customer);
      Assert.fail();
    } catch (final CustomerAlreadyExistsException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void shouldNotCreateCustomerValidationFailed() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customer.getAddress().setStreet(null);
    customer.setContactDetails(null);

    try {
      this.customerManager.createCustomer(customer);
      Assert.fail();
    } catch (final CustomerValidationException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void shouldFindCustomer() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final Customer foundCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertNotNull(foundCustomer);
    Assert.assertNotNull(foundCustomer.getAddress());
    Assert.assertNotNull(foundCustomer.getContactDetails());
    Assert.assertEquals(2, foundCustomer.getContactDetails().size());
    Assert.assertEquals(customer.getMember(), foundCustomer.getMember());

  }

  @Test
  public void shouldNotFindCustomerNotFound() throws Exception {
    try {
      this.customerManager.findCustomer(RandomStringUtils.randomAlphanumeric(8));
      Assert.fail();
    } catch (final CustomerNotFoundException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void shouldFindNonExistentCustomerIsNotInGoodStanding() throws Exception {
    Assert.assertFalse(this.customerManager.isCustomerInGoodStanding(testEnvironment.generateUniqueIdentifer("don")));
  }

  @Test
  public void shouldFetchCustomers() throws Exception {
    Stream.of(
        CustomerGenerator.createRandomCustomer(),
        CustomerGenerator.createRandomCustomer(),
        CustomerGenerator.createRandomCustomer()
    ).forEach(customer -> {
      this.customerManager.createCustomer(customer);
      try {
        this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
      } catch (final InterruptedException ex) {
        Assert.fail(ex.getMessage());
      }
    });

    final CustomerPage customerPage = this.customerManager.fetchCustomers(null, null, 0, 20, null, null);
    Assert.assertTrue(customerPage.getTotalElements() >= 3);
  }

  @Test
  public void shouldFetchCustomersByTerm() throws Exception {
    final Customer randomCustomer = CustomerGenerator.createRandomCustomer();
    final String randomCustomerIdentifier = randomCustomer.getIdentifier();
    this.customerManager.createCustomer(randomCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, randomCustomerIdentifier);

    final CustomerPage customerPage = this.customerManager.fetchCustomers(randomCustomerIdentifier, Boolean.FALSE, 0, 20, null, null);
    Assert.assertTrue(customerPage.getTotalElements() == 1);
  }

  @Test
  public void shouldUpdateCustomer() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    customer.setSurname(RandomStringUtils.randomAlphanumeric(256));

    this.customerManager.updateCustomer(customer.getIdentifier(), customer);

    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, customer.getIdentifier());

    final Customer updatedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(customer.getSurname(), updatedCustomer.getSurname());
  }

  @Test
  public void shouldNotUpdateCustomerNotFound() throws Exception {
    try {
      this.customerManager.updateCustomer(RandomStringUtils.randomAlphanumeric(8), CustomerGenerator.createRandomCustomer());
      Assert.fail();
    } catch (final CustomerNotFoundException ex) {
      // do nothing, expected
    }
  }

  @Test
  public void shouldActivateClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    final Customer activatedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), activatedCustomer.getCurrentState());
    Assert.assertNotNull(activatedCustomer.getApplicationDate());
  }

  @Test
  public void shouldLockClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.LOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, customer.getIdentifier());

    Assert.assertFalse(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    final Customer lockedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.LOCKED.name(), lockedCustomer.getCurrentState());
  }

  @Test
  public void shouldUnlockClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    final String applicationDate = DateConverter
        .toIsoString(LocalDate.now(Clock.systemUTC())).substring(0, 10);
    customer.setApplicationDate(applicationDate);
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.LOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, customer.getIdentifier());

    Assert.assertFalse(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.UNLOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.UNLOCK_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    final Customer unlockedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), unlockedCustomer.getCurrentState());
    Assert.assertEquals(applicationDate, unlockedCustomer.getApplicationDate());
  }

  @Test
  public void shouldCloseClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.CLOSE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.CLOSE_CUSTOMER, customer.getIdentifier());

    Assert.assertFalse(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    final Customer closedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.CLOSED.name(), closedCustomer.getCurrentState());
  }

  @Test
  public void shouldReopenClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.CLOSE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.CLOSE_CUSTOMER, customer.getIdentifier());

    Assert.assertFalse(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.REOPEN, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.REOPEN_CUSTOMER, customer.getIdentifier());

    Assert.assertTrue(this.customerManager.isCustomerInGoodStanding(customer.getIdentifier()));

    final Customer reopenedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), reopenedCustomer.getCurrentState());

  }

  @Test
  public void shouldFetchCommands() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    final List<Command> commands = this.customerManager.fetchCustomerCommands(customer.getIdentifier());
    Assert.assertTrue(commands.size() == 1);

  }

  @Test
  public void shouldUpdateAddress() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final Address address = AddressGenerator.createRandomAddress();
    this.customerManager.putAddress(customer.getIdentifier(), address);

    this.eventRecorder.wait(CustomerEventConstants.PUT_ADDRESS, customer.getIdentifier());

    final Customer changedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    final Address changedAddress = changedCustomer.getAddress();

    Assert.assertEquals(address.getCity(), changedAddress.getCity());
    Assert.assertEquals(address.getCountryCode(), changedAddress.getCountryCode());
    Assert.assertEquals(address.getPostalCode(), changedAddress.getPostalCode());
    Assert.assertEquals(address.getRegion(), changedAddress.getRegion());
    Assert.assertEquals(address.getStreet(), changedAddress.getStreet());
    Assert.assertEquals(address.getCountry(), changedAddress.getCountry());
  }

  @Test
  public void shouldUpdateContactDetails() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final ContactDetail contactDetail = ContactDetailGenerator.createRandomContactDetail();
    this.customerManager.putContactDetails(customer.getIdentifier(), Collections.singletonList(contactDetail));

    this.eventRecorder.wait(CustomerEventConstants.PUT_CONTACT_DETAILS, customer.getIdentifier());

    final Customer changedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    final List<ContactDetail> changedContactDetails = changedCustomer.getContactDetails();
    Assert.assertEquals(1, changedContactDetails.size());
    final ContactDetail changedContactDetail = changedContactDetails.get(0);
    Assert.assertEquals(contactDetail.getType(), changedContactDetail.getType());
    Assert.assertEquals(contactDetail.getValue(), changedContactDetail.getValue());
    Assert.assertEquals(contactDetail.getValidated(), changedContactDetail.getValidated());
    Assert.assertEquals(contactDetail.getGroup(), changedContactDetail.getGroup());
    Assert.assertEquals(contactDetail.getPreferenceLevel(), changedContactDetail.getPreferenceLevel());

  }

  @Test
  public void shouldUploadPortrait() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.findCustomer(customer.getIdentifier());

    final MockMultipartFile file = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i don't care".getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), file);

    this.eventRecorder.wait(CustomerEventConstants.POST_PORTRAIT, customer.getIdentifier());

    byte[] portrait = this.customerManager.getPortrait(customer.getIdentifier());

    Assert.assertArrayEquals(file.getBytes(), portrait);
  }

  @Test
  public void shouldReplacePortrait() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final MockMultipartFile firstFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i don't care".getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), firstFile);

    this.eventRecorder.wait(CustomerEventConstants.POST_PORTRAIT, customer.getIdentifier());

    final MockMultipartFile secondFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i do care".getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), secondFile);

    this.eventRecorder.wait(CustomerEventConstants.POST_PORTRAIT, customer.getIdentifier());

    // For a unknown reason the wait gets the POST_PORTRAIT event although the the entity has not been written into the database
    Thread.sleep(500);

    final byte[] portrait = this.customerManager.getPortrait(customer.getIdentifier());

    Assert.assertArrayEquals(secondFile.getBytes(), portrait);
  }

  @Test(expected = DocumentValidationException.class)
  public void shouldThrowIfPortraitExceedsMaxSize() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final MockMultipartFile firstFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, RandomStringUtils.randomAlphanumeric(750000).getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), firstFile);
  }

  @Test
  public void shouldDeletePortrait() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier()));

    final MockMultipartFile firstFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i don't care".getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), firstFile);

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.POST_PORTRAIT, customer.getIdentifier()));

    this.customerManager.deletePortrait(customer.getIdentifier());

    Assert.assertTrue(this.eventRecorder.wait(CustomerEventConstants.DELETE_PORTRAIT, customer.getIdentifier()));
  }

  @Test
  public void shouldReturnAvailableProcessSteps() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final List<ProcessStep> pendingProcessSteps = this.customerManager.fetchProcessSteps(customer.getIdentifier());
    Assert.assertEquals(2, pendingProcessSteps.size());
    Assert.assertEquals(Command.Action.ACTIVATE.name(), pendingProcessSteps.get(0).getCommand().getAction());
    Assert.assertEquals(Command.Action.CLOSE.name(), pendingProcessSteps.get(1).getCommand().getAction());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    final List<ProcessStep> activeProcessSteps = this.customerManager.fetchProcessSteps(customer.getIdentifier());
    Assert.assertEquals(2, activeProcessSteps.size());
    Assert.assertEquals(Command.Action.LOCK.name(), activeProcessSteps.get(0).getCommand().getAction());
    Assert.assertEquals(Command.Action.CLOSE.name(), activeProcessSteps.get(1).getCommand().getAction());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.LOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, customer.getIdentifier());

    final List<ProcessStep> lockedProcessSteps = this.customerManager.fetchProcessSteps(customer.getIdentifier());
    Assert.assertEquals(2, lockedProcessSteps.size());
    Assert.assertEquals(Command.Action.UNLOCK.name(), lockedProcessSteps.get(0).getCommand().getAction());
    Assert.assertEquals(Command.Action.CLOSE.name(), lockedProcessSteps.get(1).getCommand().getAction());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.UNLOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.UNLOCK_CUSTOMER, customer.getIdentifier());

    final List<ProcessStep> unlockedProcessSteps = this.customerManager.fetchProcessSteps(customer.getIdentifier());
    Assert.assertEquals(2, unlockedProcessSteps.size());
    Assert.assertEquals(Command.Action.LOCK.name(), unlockedProcessSteps.get(0).getCommand().getAction());
    Assert.assertEquals(Command.Action.CLOSE.name(), unlockedProcessSteps.get(1).getCommand().getAction());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.CLOSE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.CLOSE_CUSTOMER, customer.getIdentifier());

    final List<ProcessStep> closedProcessSteps = this.customerManager.fetchProcessSteps(customer.getIdentifier());
    Assert.assertEquals(1, closedProcessSteps.size());
    Assert.assertEquals(Command.Action.REOPEN.name(), closedProcessSteps.get(0).getCommand().getAction());
  }
}
