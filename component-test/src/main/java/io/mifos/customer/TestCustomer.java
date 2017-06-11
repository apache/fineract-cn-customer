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
package io.mifos.customer;

import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.fixture.mariadb.MariaDBInitializer;
import io.mifos.core.test.listener.EnableEventRecording;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.client.*;
import io.mifos.customer.api.v1.domain.*;
import io.mifos.customer.service.rest.config.CustomerRestConfiguration;
import io.mifos.customer.util.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestCustomer {

  private static final String APP_NAME = "customer-v1";

  @Configuration
  @EnableEventRecording
  @EnableFeignClients(basePackages = {"io.mifos.customer.api.v1.client"})
  @RibbonClient(name = APP_NAME)
  @ComponentScan(
      basePackages = {
            "io.mifos.customer.listener"
      }
  )
  @Import({CustomerRestConfiguration.class})
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }
  }
  private static final String TEST_USER = "maatkare";
  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static MariaDBInitializer mariaDBInitializer = new MariaDBInitializer();
  private final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer, mariaDBInitializer);

  @ClassRule
  public static TestRule orderClassRules = RuleChain
          .outerRule(testEnvironment)
          .around(cassandraInitializer)
          .around(mariaDBInitializer)
          .around(tenantDataStoreContext);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
          = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment, this::waitForInitialize);

  @Autowired
  private CustomerManager customerManager;

  @Autowired
  private EventRecorder eventRecorder;

  private AutoUserContext userContext;

  public TestCustomer() {
    super();
  }

  @Before
  public void prepareTest() {
    userContext = tenantApplicationSecurityEnvironment.createAutoUserContext(TEST_USER);
  }

  @After
  public void cleanupTest() {
    userContext.close();
  }

  public boolean waitForInitialize() {
    try {
      return this.eventRecorder.wait(CustomerEventConstants.INITIALIZE, CustomerEventConstants.INITIALIZE);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Test
  public void shouldCreateCustomer() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

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

    final Customer activatedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), activatedCustomer.getCurrentState());
  }

  @Test
  public void shouldLockClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.LOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, customer.getIdentifier());

    final Customer lockedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.LOCKED.name(), lockedCustomer.getCurrentState());
  }

  @Test
  public void shouldUnlockClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.LOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.UNLOCK, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.UNLOCK_CUSTOMER, customer.getIdentifier());

    final Customer unlockedCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertEquals(Customer.State.ACTIVE.name(), unlockedCustomer.getCurrentState());
  }

  @Test
  public void shouldCloseClient() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.CLOSE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.CLOSE_CUSTOMER, customer.getIdentifier());

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

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.CLOSE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.CLOSE_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.REOPEN, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.REOPEN_CUSTOMER, customer.getIdentifier());

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
  public void shouldFetchIdentificationCards() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    Stream.of(
            IdentificationCardGenerator.createRandomIdentificationCard(),
            IdentificationCardGenerator.createRandomIdentificationCard(),
            IdentificationCardGenerator.createRandomIdentificationCard()
    ).forEach(identificationCard -> {
      this.customerManager.createIdentificationCard(customer.getIdentifier(), identificationCard);
      try {
        this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
      } catch (final InterruptedException ex) {
        Assert.fail(ex.getMessage());
      }
    });

    final List<IdentificationCard> result = this.customerManager.fetchIdentificationCards(customer.getIdentifier());

    Assert.assertTrue(result.size() == 3);
  }

  @Test
  public void shouldCreateIdentificationCard() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final IdentificationCard newIdentificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

    this.customerManager.createIdentificationCard(customer.getIdentifier(), newIdentificationCard);

    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, newIdentificationCard.getNumber());

    final IdentificationCard identificationCard = this.customerManager.findIdentificationCard(customer.getIdentifier(), newIdentificationCard.getNumber());

    Assert.assertNotNull(identificationCard);

    Assert.assertEquals(identificationCard.getCreatedBy(), TEST_USER);
  }

  @Test
  public void shouldUpdateIdentificationCard() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final IdentificationCard newIdentificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

    this.customerManager.createIdentificationCard(customer.getIdentifier(), newIdentificationCard);

    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, newIdentificationCard.getNumber());

    final IdentificationCard identificationCard = this.customerManager.findIdentificationCard(customer.getIdentifier(), newIdentificationCard.getNumber());

    final IdentificationCard updatedIdentificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

    updatedIdentificationCard.setNumber(newIdentificationCard.getNumber());

    this.customerManager.updateIdentificationCard(customer.getIdentifier(), updatedIdentificationCard.getNumber(), updatedIdentificationCard);

    this.eventRecorder.wait(CustomerEventConstants.PUT_IDENTIFICATION_CARD, updatedIdentificationCard.getNumber());

    final IdentificationCard changedIdentificationCard = this.customerManager.findIdentificationCard(customer.getIdentifier(), identificationCard.getNumber());

    Assert.assertEquals(updatedIdentificationCard.getType(), changedIdentificationCard.getType());
    Assert.assertEquals(updatedIdentificationCard.getIssuer(), changedIdentificationCard.getIssuer());
    Assert.assertEquals(updatedIdentificationCard.getNumber(), changedIdentificationCard.getNumber());
    Assert.assertEquals(TEST_USER, changedIdentificationCard.getLastModifiedBy());
  }

  @Test(expected = IdentificationCardNotFoundException.class)
  public void shouldDeleteIdentificationCard() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

    this.customerManager.createIdentificationCard(customer.getIdentifier(), identificationCard);

    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());

    this.customerManager.deleteIdentificationCard(customer.getIdentifier(), identificationCard.getNumber());

    this.eventRecorder.wait(CustomerEventConstants.DELETE_IDENTIFICATION_CARD, identificationCard.getNumber());

    this.customerManager.findIdentificationCard(customer.getIdentifier(), identificationCard.getNumber());
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

  @Test(expected = PortraitValidationException.class)
  public void shouldThrowIfPortraitExceedsMaxSize() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final MockMultipartFile firstFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, RandomStringUtils.randomAlphanumeric(750000).getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), firstFile);
  }

  @Test(expected = PortraitNotFoundException.class)
  public void shouldDeletePortrait() throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);

    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final MockMultipartFile firstFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i don't care".getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), firstFile);

    this.eventRecorder.wait(CustomerEventConstants.POST_PORTRAIT, customer.getIdentifier());

    this.customerManager.deletePortrait(customer.getIdentifier());

    this.eventRecorder.wait(CustomerEventConstants.DELETE_PORTRAIT, customer.getIdentifier());

    this.customerManager.getPortrait(customer.getIdentifier());
  }
}
