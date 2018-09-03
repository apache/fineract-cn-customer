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

import com.google.gson.Gson;
import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.domain.*;
import org.apache.fineract.cn.customer.util.AddressGenerator;
import org.apache.fineract.cn.customer.util.CommandGenerator;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import org.apache.fineract.cn.lang.DateOfBirth;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CustomerApiDocumentation extends AbstractCustomerTest {
  @Rule
  public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/doc/generated-snippets/test-customer");

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @Before
  public void setUp ( ) {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
            .apply(documentationConfiguration(this.restDocumentation))
            .build();
  }

  @Test
  public void documentCreateCustomer ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    Gson gson = new Gson();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(2000));
    dateOfBirth.setMonth(Integer.valueOf(6));
    dateOfBirth.setDay(Integer.valueOf(6));

    Address address = new Address();
    address.setStreet("Hospital");
    address.setCity("Muyuka");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail contactDetailOne = new ContactDetail();
    contactDetailOne.setType(ContactDetail.Type.MOBILE.name());
    contactDetailOne.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetailOne.setValue("677777777");
    contactDetailOne.setPreferenceLevel(Integer.valueOf(1));
    contactDetailOne.setValidated(Boolean.FALSE);

    ContactDetail contactDetailTwo = new ContactDetail();
    contactDetailTwo.setType(ContactDetail.Type.PHONE.name());
    contactDetailTwo.setGroup(ContactDetail.Group.BUSINESS.name());
    contactDetailTwo.setValue("233363640");
    contactDetailTwo.setPreferenceLevel(Integer.valueOf(2));
    contactDetailTwo.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(contactDetailOne);
    contactDetails.add(contactDetailTwo);

    customer.setIdentifier("idOne");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Kima");
    customer.setMiddleName("Bessem");
    customer.setSurname("Ray");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Oweh ViB");
    customer.setAssignedEmployee("Che Godwin");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Spouse");
    customer.setReferenceCustomer("mate");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 200).toString());
    customer.setLastModifiedBy("Nakuve");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 4).toString());

    this.mockMvc.perform(post("/customers")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(customer))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-create-customer", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("identifier").description("The customer's identifier"),
                            fieldWithPath("type").description("The type of legal entity  +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("givenName").description("The customer's given name"),
                            fieldWithPath("middleName").description("The customer's middle name"),
                            fieldWithPath("surname").description("The customer's surname"),
                            fieldWithPath("dateOfBirth").type("DateOfBirth").description("The customer's date of birth +\n" +
                                    " +\n" +
                                    " _DateOfBirth_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("member").description("Is customer a member of the MFI ?"),
                            fieldWithPath("assignedOffice").description("The customer's assigned office"),
                            fieldWithPath("assignedEmployee").description("The customer's assigned employee"),
                            fieldWithPath("address").type("Address").description("The customer's physical address +\n" +
                                    " +\n" +
                                    "_Address_ { +\n" +
                                    "*String* street, +\n" +
                                    "*String* city, +\n" +
                                    "*String* region, +\n" +
                                    "*String* postalCode, +\n" +
                                    "*String* countryCode, +\n" +
                                    "*String* country  } +"),
                            fieldWithPath("contactDetails").type("List<ContactDetail>").description("The customer's contact details +\n" +
                                    " +\n" +
                                    "_ContactDetail_ { +\n" +
                                    "  *enum* _Type_ { +\n" +
                                    "     EMAIL, +\n" +
                                    "     PHONE, +\n" +
                                    "     MOBILE +\n" +
                                    "   } type, +\n" +
                                    "   *enum* _Group_ { +\n" +
                                    "     BUSINESS, +\n" +
                                    "     PRIVATE +\n" +
                                    "   } group, +\n" +
                                    "   *String* value +\n" +
                                    " } +"),
                            fieldWithPath("currentState").type("State").description("The customer's current state +\n" +
                                    " +\n" +
                                    "*enum* _State_ { +\n" +
                                    "     PENDING, +\n" +
                                    "     ACTIVE, +\n" +
                                    "     LOCKED, +\n" +
                                    "     CLOSED +\n" +
                                    "   } +"),
                            fieldWithPath("accountBeneficiary").type("String").description("account beneficiary"),
                            fieldWithPath("applicationDate").type("String").description("date customer applied for account"),
                            fieldWithPath("lastModifiedBy").type("String").description("employee who last modified account"),
                            fieldWithPath("lastModifiedOn").type("String").description("last time account was modified"),
                            fieldWithPath("referenceCustomer").type("String").description("fellow customer to refer to")
                    )));
  }

  @Test
  public void documentFindCustomer ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1997));
    dateOfBirth.setMonth(Integer.valueOf(6));
    dateOfBirth.setDay(Integer.valueOf(5));

    Address address = new Address();
    address.setStreet("Che St");
    address.setCity("Bali");
    address.setRegion("WC");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail contactDetailOne = new ContactDetail();
    contactDetailOne.setType(ContactDetail.Type.MOBILE.name());
    contactDetailOne.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetailOne.setValue("675673477");
    contactDetailOne.setPreferenceLevel(Integer.valueOf(1));
    contactDetailOne.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(contactDetailOne);

    customer.setIdentifier("eniruth");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Eni");
    customer.setMiddleName("Ruth");
    customer.setSurname("Tah");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Bali ViB");
    customer.setAssignedEmployee("Shu Dion");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Hubby");
    customer.setReferenceCustomer("friend");
    customer.setApplicationDate(LocalDate.ofYearDay(2018, 4).toString());
    customer.setLastModifiedBy("Asah Aaron");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 100).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final Customer foundCustomer = this.customerManager.findCustomer(customer.getIdentifier());
    Assert.assertNotNull(foundCustomer);

    this.mockMvc.perform(get("/customers/" + foundCustomer.getIdentifier())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-find-customer", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("identifier").description("The customer's identifier"),
                            fieldWithPath("type").description("The type of legal entity  +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("givenName").description("The customer's given name"),
                            fieldWithPath("middleName").description("The customer's middle name"),
                            fieldWithPath("surname").description("The customer's surName"),
                            fieldWithPath("dateOfBirth").type("DateOfBirth").description("The customer's date of birth +\n" +
                                    " +\n" +
                                    " _DateOfBirth_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("member").description("Is customer a member of the MFI ?"),
                            fieldWithPath("assignedOffice").description("The customer's assigned office"),
                            fieldWithPath("assignedEmployee").description("The customer's assigned employee"),
                            fieldWithPath("address").type("Address").description("The customer's physical address +\n" +
                                    " +\n" +
                                    "_Address_ { +\n" +
                                    "*String* street, +\n" +
                                    "*String* city, +\n" +
                                    "*String* region, +\n" +
                                    "*String* postalCode, +\n" +
                                    "*String* countryCode, +\n" +
                                    "*String* country  } +"),
                            fieldWithPath("contactDetails").type("List<ContactDetail>").description("The customer's contact details +\n" +
                                    " +\n" +
                                    "_ContactDetail_ { +\n" +
                                    "  *enum* _Type_ { +\n" +
                                    "     EMAIL, +\n" +
                                    "     PHONE, +\n" +
                                    "     MOBILE +\n" +
                                    "   } type, +\n" +
                                    "   *enum* _Group_ { +\n" +
                                    "     BUSINESS, +\n" +
                                    "     PRIVATE +\n" +
                                    "   } group, +\n" +
                                    "   *String* value +\n" +
                                    " } +"),
                            fieldWithPath("currentState").type("State").description("The customer's current state +\n" +
                                    " +\n" +
                                    "*enum* _State_ { +\n" +
                                    "     PENDING, +\n" +
                                    "     ACTIVE, +\n" +
                                    "     LOCKED, +\n" +
                                    "     CLOSED +\n" +
                                    "   } +"),
                            fieldWithPath("accountBeneficiary").type("String").description("(Optional) The customer beneficiary").optional(),
                            fieldWithPath("referenceCustomer").type("String").description("(Optional) The customer's reference").optional(),
                            fieldWithPath("applicationDate").type("String").description("(Optional) The date customer applied for account"),
                            fieldWithPath("customValues").type("List<Value>").description("Some custom values"),
                            fieldWithPath("createdBy").description("User who created account"),
                            fieldWithPath("createdOn").description("Date and time when account was created"),
                            fieldWithPath("lastModifiedBy").type("String").description("(Optional) Employee who last modified account"),
                            fieldWithPath("lastModifiedOn").type("String").description("(Optional) Date and time account was last modified")
                    )));
  }

  @Test
  public void documentFetchCustomers ( ) throws Exception {
    final Customer customerOne = CustomerGenerator.createRandomCustomer();
    final Customer customerTwo = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirthOne = new DateOfBirth();
    dateOfBirthOne.setYear(Integer.valueOf(1997));
    dateOfBirthOne.setMonth(Integer.valueOf(6));
    dateOfBirthOne.setDay(Integer.valueOf(5));

    Address addressOne = new Address();
    addressOne.setStreet("Che St");
    addressOne.setCity("Bali");
    addressOne.setRegion("WC");
    addressOne.setPostalCode("8050");
    addressOne.setCountryCode("CM");
    addressOne.setCountry("Cameroon");

    ContactDetail contactDetailOne = new ContactDetail();
    contactDetailOne.setType(ContactDetail.Type.MOBILE.name());
    contactDetailOne.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetailOne.setValue("675673477");
    contactDetailOne.setPreferenceLevel(Integer.valueOf(1));
    contactDetailOne.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetailsOne = new ArrayList <>();
    contactDetailsOne.add(contactDetailOne);

    customerOne.setIdentifier("meni");
    customerOne.setType(Customer.Type.PERSON.name());
    customerOne.setGivenName("Meni");
    customerOne.setMiddleName("Richmond");
    customerOne.setSurname("Akom");
    customerOne.setDateOfBirth(dateOfBirthOne);
    customerOne.setMember(Boolean.TRUE);
    customerOne.setAssignedOffice("Buea ViB");
    customerOne.setAssignedEmployee("Itoh Mih");
    customerOne.setAddress(addressOne);
    customerOne.setContactDetails(contactDetailsOne);
    customerOne.setCurrentState(Customer.State.PENDING.name());
    customerOne.setAccountBeneficiary("Spouse");
    customerOne.setReferenceCustomer("friend");
    customerOne.setApplicationDate(LocalDate.ofYearDay(2018, 49).toString());

    DateOfBirth dateOfBirthTwo = new DateOfBirth();
    dateOfBirthTwo.setYear(Integer.valueOf(2000));
    dateOfBirthTwo.setMonth(Integer.valueOf(6));
    dateOfBirthTwo.setDay(Integer.valueOf(6));

    Address addressTwo = new Address();
    addressTwo.setStreet("Mile 16");
    addressTwo.setCity("Buea");
    addressTwo.setRegion("SWR");
    addressTwo.setPostalCode("8050");
    addressTwo.setCountryCode("CM");
    addressTwo.setCountry("Cameroon");

    ContactDetail contactDetailTwo = new ContactDetail();
    contactDetailTwo.setType(ContactDetail.Type.MOBILE.name());
    contactDetailTwo.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetailTwo.setValue("677784712");
    contactDetailTwo.setPreferenceLevel(Integer.valueOf(1));
    contactDetailTwo.setValidated(Boolean.FALSE);

    ContactDetail contactDetailThree = new ContactDetail();
    contactDetailThree.setType(ContactDetail.Type.PHONE.name());
    contactDetailThree.setGroup(ContactDetail.Group.BUSINESS.name());
    contactDetailThree.setValue("237463690");
    contactDetailThree.setPreferenceLevel(Integer.valueOf(2));
    contactDetailThree.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetailsTwo = new ArrayList <>();
    contactDetailsTwo.add(contactDetailTwo);
    contactDetailsTwo.add(contactDetailThree);

    customerTwo.setIdentifier("bencho");
    customerTwo.setType(Customer.Type.PERSON.name());
    customerTwo.setGivenName("Bencho");
    customerTwo.setMiddleName("Etah");
    customerTwo.setSurname("Obi");
    customerTwo.setDateOfBirth(dateOfBirthTwo);
    customerTwo.setMember(Boolean.TRUE);
    customerTwo.setAssignedOffice("Kah ViB");
    customerTwo.setAssignedEmployee("Ebot Tabi");
    customerTwo.setAddress(addressTwo);
    customerTwo.setContactDetails(contactDetailsTwo);
    customerTwo.setCurrentState(Customer.State.PENDING.name());
    customerTwo.setAccountBeneficiary("Spouse");
    customerTwo.setReferenceCustomer("mate");
    customerTwo.setApplicationDate(LocalDate.ofYearDay(2017, 100).toString());
    customerTwo.setLastModifiedBy("Nalowa");
    customerTwo.setLastModifiedOn(LocalDate.ofYearDay(2018, 40).toString());

    Stream.of(customerOne, customerTwo)
            .forEach(customer -> {
              this.customerManager.createCustomer(customer);
              try {
                this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
              } catch (final InterruptedException ex) {
                Assert.fail(ex.getMessage());
              }
            });

    final CustomerPage customerPage = this.customerManager.fetchCustomers(null, null, 0, 20, null, null);
    Assert.assertTrue(customerPage.getTotalElements() >= 2);

    this.mockMvc.perform(get("/customers")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-customers", preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("customers").type("List<Customer>").description("The List of Customers +\n"),
                            fieldWithPath("totalPages").type("Integer").description("Number of pages"),
                            fieldWithPath("totalElements").type("Long").description("Number of customers in page"),
                            fieldWithPath("customers[].identifier").type("String").description("First customer's identifier"),
                            fieldWithPath("customers[].type").type("Type").description("The type of first customer +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("customers[].givenName").description("first customer's given name"),
                            fieldWithPath("customers[].middleName").description("first customer's middle name"),
                            fieldWithPath("customers[].surname").description("first customer's surName"),
                            fieldWithPath("customers[].dateOfBirth").type("DateOfBirth").description("first customer's date of birth +\n" +
                                    " +\n" +
                                    " _DateOfBirth_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("customers[].member").description("Is customer a member of the MFI ?"),
                            fieldWithPath("customers[].assignedOffice").description("first customer's assigned office"),
                            fieldWithPath("customers[].assignedEmployee").description("first customer's assigned employee"),
                            fieldWithPath("customers[].address").type("Address").description("first customer's physical address +\n" +
                                    " +\n" +
                                    "_Address_ { +\n" +
                                    "*String* street, +\n" +
                                    "*String* city, +\n" +
                                    "*String* region, +\n" +
                                    "*String* postalCode, +\n" +
                                    "*String* countryCode, +\n" +
                                    "*String* country  } +"),
                            fieldWithPath("customers[].contactDetails").type("List<ContactDetail>").description("first customer's contact details +\n" +
                                    " +\n" +
                                    "_ContactDetail_ { +\n" +
                                    "  *enum* _Type_ { +\n" +
                                    "     EMAIL, +\n" +
                                    "     PHONE, +\n" +
                                    "     MOBILE +\n" +
                                    "   } type, +\n" +
                                    "   *enum* _Group_ { +\n" +
                                    "     BUSINESS, +\n" +
                                    "     PRIVATE +\n" +
                                    "   } group, +\n" +
                                    "   *String* value +\n" +
                                    " } +"),
                            fieldWithPath("customers[].currentState").type("State").description("first customer's current state +\n" +
                                    " +\n" +
                                    "*enum* _State_ { +\n" +
                                    "     PENDING, +\n" +
                                    "     ACTIVE, +\n" +
                                    "     LOCKED, +\n" +
                                    "     CLOSED +\n" +
                                    "   } +"),
                            fieldWithPath("customers[].accountBeneficiary").description("first customer's beneficiary"),
                            fieldWithPath("customers[].referenceCustomer").description("first customer's reference"),
                            fieldWithPath("customers[].applicationDate").description("The date first customer applied for account"),
                            fieldWithPath("customers[].customValues").type("List<Value>").description("first customer's custom values"),
                            fieldWithPath("customers[].createdBy").description("employee who created first customer's account"),
                            fieldWithPath("customers[].createdOn").description("Date and time when first customer's account was created"),
                            fieldWithPath("customers[].lastModifiedBy").type("String").description("Employee who last modified first customer's account"),
                            fieldWithPath("customers[].lastModifiedOn").type("String").description("Date and time first customer's account was last modified"),
                            fieldWithPath("customers[1].identifier").description("Second customer's identifier"),
                            fieldWithPath("customers[1].type").type("Type").description("The type of the second customer +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("customers[1].givenName").description("The second customer's given name"),
                            fieldWithPath("customers[1].middleName").description("The second customer's middle name"),
                            fieldWithPath("customers[1].surname").description("The second customer's surName"),
                            fieldWithPath("customers[1].dateOfBirth").type("DateOfBirth").description("The second customer's date of birth +\n" +
                                    " +\n" +
                                    " _DateOfBirth_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("customers[1].member").description("Is second customer a member of the MFI ?"),
                            fieldWithPath("customers[1].assignedOffice").description("The second customer's assigned office"),
                            fieldWithPath("customers[1].assignedEmployee").description("The second customer's assigned employee"),
                            fieldWithPath("customers[1].address").type("Address").description("second customer's physical address +\n" +
                                    " +\n" +
                                    "_Address_ { +\n" +
                                    "*String* street, +\n" +
                                    "*String* city, +\n" +
                                    "*String* region, +\n" +
                                    "*String* postalCode, +\n" +
                                    "*String* countryCode, +\n" +
                                    "*String* country  } +"),
                            fieldWithPath("customers[1].contactDetails").type("List<ContactDetail>").description("second customer's contact details +\n" +
                                    " +\n" +
                                    "_ContactDetail_ { +\n" +
                                    "  *enum* _Type_ { +\n" +
                                    "     EMAIL, +\n" +
                                    "     PHONE, +\n" +
                                    "     MOBILE +\n" +
                                    "   } type, +\n" +
                                    "   *enum* _Group_ { +\n" +
                                    "     BUSINESS, +\n" +
                                    "     PRIVATE +\n" +
                                    "   } group, +\n" +
                                    "   *String* value +\n" +
                                    " } +"),
                            fieldWithPath("customers[1].currentState").type("State").description("The second customer's current state +\n" +
                                    " +\n" +
                                    "*enum* _State_ { +\n" +
                                    "     PENDING, +\n" +
                                    "     ACTIVE, +\n" +
                                    "     LOCKED, +\n" +
                                    "     CLOSED +\n" +
                                    "   } +"),
                            fieldWithPath("customers[1].accountBeneficiary").description("The second customer's beneficiary"),
                            fieldWithPath("customers[1].referenceCustomer").description("The second customer's reference"),
                            fieldWithPath("customers[1].applicationDate").description("The date second customer applied for account"),
                            fieldWithPath("customers[1].customValues").type("List<Value>").description("Second customer's custom values"),
                            fieldWithPath("customers[1].createdBy").description("User who created second customer's account"),
                            fieldWithPath("customers[1].createdOn").description("Date and time when second customer's account was created"),
                            fieldWithPath("customers[1].lastModifiedBy").type("String").description("Employee who last modified second customer's account"),
                            fieldWithPath("customers[1].lastModifiedOn").type("String").description("Date and time second customer's account was last modified"))));
  }

  @Test
  public void documentUpdateCustomer ( ) throws Exception {
    final Customer originalCustomer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1990));
    dateOfBirth.setMonth(Integer.valueOf(4));
    dateOfBirth.setDay(Integer.valueOf(3));

    Address address = new Address();
    address.setStreet("Sonac");
    address.setCity("Bamenda");
    address.setRegion("NWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail contactDetailOne = new ContactDetail();
    contactDetailOne.setType(ContactDetail.Type.MOBILE.name());
    contactDetailOne.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetailOne.setValue("677665544");
    contactDetailOne.setPreferenceLevel(Integer.valueOf(1));
    contactDetailOne.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(contactDetailOne);

    originalCustomer.setIdentifier("tifuh");
    originalCustomer.setType(Customer.Type.PERSON.name());
    originalCustomer.setGivenName("Tifuh");
    originalCustomer.setMiddleName("Ndah");
    originalCustomer.setSurname("Tah");
    originalCustomer.setDateOfBirth(dateOfBirth);
    originalCustomer.setMember(Boolean.TRUE);
    originalCustomer.setAssignedOffice("Nkwen");
    originalCustomer.setAssignedEmployee("Chi Tih");
    originalCustomer.setAddress(address);
    originalCustomer.setContactDetails(contactDetails);
    originalCustomer.setCurrentState(Customer.State.PENDING.name());
    originalCustomer.setAccountBeneficiary("Wife");
    originalCustomer.setReferenceCustomer("sister");
    originalCustomer.setApplicationDate(LocalDate.ofYearDay(2017, 249).toString());
    originalCustomer.setLastModifiedBy("Nah Toh");
    originalCustomer.setLastModifiedOn(LocalDate.ofYearDay(2018, 153).toString());

    this.customerManager.createCustomer(originalCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, originalCustomer.getIdentifier());

    originalCustomer.setSurname("Sih");

    this.customerManager.updateCustomer(originalCustomer.getIdentifier(), originalCustomer);
    this.eventRecorder.wait(CustomerEventConstants.PUT_CUSTOMER, originalCustomer.getIdentifier());

    final Customer updatedCustomer = this.customerManager.findCustomer(originalCustomer.getIdentifier());

    Gson gson = new Gson();
    this.mockMvc.perform(put("/customers/" + originalCustomer.getIdentifier())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(updatedCustomer))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-update-customer", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("identifier").description("The customer's identifier"),
                            fieldWithPath("type").description("The type of legal entity  +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("givenName").description("The customer's given name"),
                            fieldWithPath("middleName").description("The customer's middle name"),
                            fieldWithPath("surname").description("The customer's surname"),
                            fieldWithPath("dateOfBirth").type("DateOfBirth").description("The customer's date of birth +\n" +
                                    " +\n" +
                                    " _DateOfBirth_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("member").description("Is customer a member of the MFI ?"),
                            fieldWithPath("assignedOffice").description("The customer's assigned office"),
                            fieldWithPath("assignedEmployee").description("The customer's assigned employee"),
                            fieldWithPath("address").type("Address").description("The customer's physical address +\n" +
                                    " +\n" +
                                    "_Address_ { +\n" +
                                    "*String* street, +\n" +
                                    "*String* city, +\n" +
                                    "*String* region, +\n" +
                                    "*String* postalCode, +\n" +
                                    "*String* countryCode, +\n" +
                                    "*String* country  } +"),
                            fieldWithPath("contactDetails").type("List<ContactDetail>").description("The customer's contact details +\n" +
                                    " +\n" +
                                    "_ContactDetail_ { +\n" +
                                    "  *enum* _Type_ { +\n" +
                                    "     EMAIL, +\n" +
                                    "     PHONE, +\n" +
                                    "     MOBILE +\n" +
                                    "   } type, +\n" +
                                    "   *enum* _Group_ { +\n" +
                                    "     BUSINESS, +\n" +
                                    "     PRIVATE +\n" +
                                    "   } group, +\n" +
                                    "   *String* value +\n" +
                                    " } +"),
                            fieldWithPath("currentState").type("State").description("The customer's current state +\n" +
                                    " +\n" +
                                    "*enum* _State_ { +\n" +
                                    "     PENDING, +\n" +
                                    "     ACTIVE, +\n" +
                                    "     LOCKED, +\n" +
                                    "     CLOSED +\n" +
                                    "   } +"),
                            fieldWithPath("accountBeneficiary").type("String").description("account beneficiary"),
                            fieldWithPath("applicationDate").type("String").description("date customer applied for account"),
                            fieldWithPath("customValues").type("List<Value>").description("Second customer's custom values"),
                            fieldWithPath("createdBy").description("User who created second customer's account"),
                            fieldWithPath("createdOn").description("Date and time when second customer's account was created"),
                            fieldWithPath("lastModifiedBy").type("String").description("employee who last modified account"),
                            fieldWithPath("lastModifiedOn").type("String").description("last time account was modified"),
                            fieldWithPath("referenceCustomer").type("String").description("fellow customer to refer to"))));
  }

  @Test
  public void documentActivateClient ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1990));
    dateOfBirth.setMonth(Integer.valueOf(4));
    dateOfBirth.setDay(Integer.valueOf(3));

    Address address = new Address();
    address.setStreet("Cow Str");
    address.setCity("Bamenda");
    address.setRegion("NWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail contactDetailOne = new ContactDetail();
    contactDetailOne.setType(ContactDetail.Type.MOBILE.name());
    contactDetailOne.setGroup(ContactDetail.Group.PRIVATE.name());
    contactDetailOne.setValue("678695104");
    contactDetailOne.setPreferenceLevel(Integer.valueOf(1));
    contactDetailOne.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(contactDetailOne);

    customer.setIdentifier("bihade");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Bih");
    customer.setMiddleName("Ade");
    customer.setSurname("Njang");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Mankon");
    customer.setAssignedEmployee("Cho Sa'ah");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Wife");
    customer.setReferenceCustomer("Sister");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 269).toString());
    customer.setLastModifiedBy("Cho Sa'ah");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 69).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    Command activateCustomer = CommandGenerator.create(Command.Action.ACTIVATE, "Client Activated");

    Gson gson = new Gson();
    this.mockMvc.perform(post("/customers/" + customer.getIdentifier() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(activateCustomer)))
            .andExpect(status().isAccepted())
            .andDo(document("document-activate-client", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action ACTIVATE " +
                                    " +\n" +
                                    " *enum* _Action_ { +\n" +
                                    "    ACTIVATE, +\n" +
                                    "    LOCK, +\n" +
                                    "    UNLOCK, +\n" +
                                    "    CLOSE, +\n" +
                                    "    REOPEN +\n" +
                                    "  }"),
                            fieldWithPath("comment").description("Activate comment"))));
  }

  @Test
  public void documentLockClient ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1990));
    dateOfBirth.setMonth(Integer.valueOf(1));
    dateOfBirth.setDay(Integer.valueOf(1));

    Address address = new Address();
    address.setStreet("Ghana St");
    address.setCity("Bamenda");
    address.setRegion("NWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("670696104");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("669876543");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(mtnContact);
    contactDetails.add(nextellContact);

    customer.setIdentifier("bahtende");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Bah");
    customer.setMiddleName("Tende");
    customer.setSurname("Njuh");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Nkwen");
    customer.setAssignedEmployee("Nchang Shu");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Wife");
    customer.setReferenceCustomer("bihade");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 14).toString());
    customer.setLastModifiedBy("Cho Sa'ah");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 56).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    Command activateClient = CommandGenerator.create(Command.Action.ACTIVATE, "Client Activated");
    this.customerManager.customerCommand(customer.getIdentifier(), activateClient);
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Command lockClient = CommandGenerator.create(Command.Action.LOCK, "Locked Client");

    Gson gson = new Gson();
    this.mockMvc.perform(post("/customers/" + customer.getIdentifier() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(lockClient)))
            .andExpect(status().isAccepted())
            .andDo(document("document-lock-client", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action LOCK " +
                                    " +\n" +
                                    " *enum* _Action_ { +\n" +
                                    "    ACTIVATE, +\n" +
                                    "    LOCK, +\n" +
                                    "    UNLOCK, +\n" +
                                    "    CLOSE, +\n" +
                                    "    REOPEN +\n" +
                                    "  }"),
                            fieldWithPath("comment").description("Lock comment"))));
  }

  @Test
  public void documentUnlockClient ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1991));
    dateOfBirth.setMonth(Integer.valueOf(2));
    dateOfBirth.setDay(Integer.valueOf(2));

    Address address = new Address();
    address.setStreet("UpStatn");
    address.setCity("Bamenda");
    address.setRegion("NWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("677696184");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("669676443");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(mtnContact);
    contactDetails.add(nextellContact);

    customer.setIdentifier("pobum");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Pobum");
    customer.setMiddleName("Rebe");
    customer.setSurname("Dob");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Nkwen");
    customer.setAssignedEmployee("Ntsang");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Daughter");
    customer.setReferenceCustomer("bahtende");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 90).toString());
    customer.setLastModifiedBy("Awa Sum");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 150).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    Command activateClient = CommandGenerator.create(Command.Action.ACTIVATE, "Client Activated");
    this.customerManager.customerCommand(customer.getIdentifier(), activateClient);
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    Command lockClient = CommandGenerator.create(Command.Action.LOCK, "Client Locked");
    this.customerManager.customerCommand(customer.getIdentifier(), lockClient);
    this.eventRecorder.wait(CustomerEventConstants.LOCK_CUSTOMER, customer.getIdentifier());

    Command unlockClient = CommandGenerator.create(Command.Action.UNLOCK, "Client Unlocked");

    Gson gson = new Gson();
    this.mockMvc.perform(post("/customers/" + customer.getIdentifier() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(unlockClient)))
            .andExpect(status().isAccepted())
            .andDo(document("document-unlock-client", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action UNLOCK " +
                                    " +\n" +
                                    " *enum* _Action_ { +\n" +
                                    "    ACTIVATE, +\n" +
                                    "    LOCK, +\n" +
                                    "    UNLOCK, +\n" +
                                    "    CLOSE, +\n" +
                                    "    REOPEN +\n" +
                                    "  }"),
                            fieldWithPath("comment").description("Unlock comment"))));
  }

  @Test
  public void documentCloseClient ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1992));
    dateOfBirth.setMonth(Integer.valueOf(3));
    dateOfBirth.setDay(Integer.valueOf(3));

    Address address = new Address();
    address.setStreet("Soppo");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("677223344");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("668877553");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(mtnContact);
    contactDetails.add(nextellContact);

    customer.setIdentifier("epolle");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Epolle");
    customer.setMiddleName("E.");
    customer.setSurname("Makoge");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Muea");
    customer.setAssignedEmployee("Epie N.");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Relative");
    customer.setReferenceCustomer("pobum");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 32).toString());
    customer.setLastModifiedBy("Epie N.");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 80).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    Command closeClient = CommandGenerator.create(Command.Action.CLOSE, "Client Closed");

    Gson gson = new Gson();
    this.mockMvc.perform(post("/customers/" + customer.getIdentifier() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(closeClient)))
            .andExpect(status().isAccepted())
            .andDo(document("document-close-client", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action CLOSE " +
                                    " +\n" +
                                    " *enum* _Action_ { +\n" +
                                    "    ACTIVATE, +\n" +
                                    "    LOCK, +\n" +
                                    "    UNLOCK, +\n" +
                                    "    CLOSE, +\n" +
                                    "    REOPEN +\n" +
                                    "  }"),
                            fieldWithPath("comment").description("Close comment"))));
  }

  @Test
  public void documentReopenClient ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1993));
    dateOfBirth.setMonth(Integer.valueOf(4));
    dateOfBirth.setDay(Integer.valueOf(4));

    Address address = new Address();
    address.setStreet("Molyko");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("677429344");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("666817553");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(mtnContact);
    contactDetails.add(nextellContact);

    customer.setIdentifier("eyolle");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Eyolle");
    customer.setMiddleName("E.");
    customer.setSurname("Mola");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Bova");
    customer.setAssignedEmployee("Etonde I.");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Brother");
    customer.setReferenceCustomer("epolle");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 3).toString());
    customer.setLastModifiedBy("Epie N.");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 8).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    Command closeClient = CommandGenerator.create(Command.Action.CLOSE, "Closed");
    this.customerManager.customerCommand(customer.getIdentifier(), closeClient);
    this.eventRecorder.wait(CustomerEventConstants.CLOSE_CUSTOMER, customer.getIdentifier());

    Command reopenClient = CommandGenerator.create(Command.Action.REOPEN, "Reopened Client");

    Gson gson = new Gson();
    this.mockMvc.perform(post("/customers/" + customer.getIdentifier() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(reopenClient)))
            .andExpect(status().isAccepted())
            .andDo(document("document-reopen-client", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("action").description("Action REOPEN " +
                                    " +\n" +
                                    " *enum* _Action_ { +\n" +
                                    "    ACTIVATE, +\n" +
                                    "    LOCK, +\n" +
                                    "    UNLOCK, +\n" +
                                    "    CLOSE, +\n" +
                                    "    REOPEN +\n" +
                                    "  }"),
                            fieldWithPath("comment").description("Reopen comment"))));
  }

  @Test
  public void documentFetchCommands ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1993));
    dateOfBirth.setMonth(Integer.valueOf(4));
    dateOfBirth.setDay(Integer.valueOf(4));

    Address address = new Address();
    address.setStreet("Wokoko");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("677429343");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("666737653");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(mtnContact);
    contactDetails.add(nextellContact);

    customer.setIdentifier("kumba");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Kumba");
    customer.setMiddleName("Ebere");
    customer.setSurname("Besong");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Mile 16");
    customer.setAssignedEmployee("Malafa I.");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Mola");
    customer.setReferenceCustomer("eyolle");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 23).toString());
    customer.setLastModifiedBy("Epie Ngome");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 82).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.customerManager.customerCommand(customer.getIdentifier(), CommandGenerator.create(Command.Action.ACTIVATE, "Test"));
    this.eventRecorder.wait(CustomerEventConstants.ACTIVATE_CUSTOMER, customer.getIdentifier());

    final List <Command> commands = this.customerManager.fetchCustomerCommands(customer.getIdentifier());

    this.mockMvc.perform(get("/customers/" + customer.getIdentifier() + "/commands")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-commands", preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("[].action").description("A List of commands"),
                            fieldWithPath("[].comment").description("A comment"),
                            fieldWithPath("[].createdOn").description("Date and time customer was created"),
                            fieldWithPath("[].createdBy").description("Employee who created customer"))));
  }

  @Test
  public void documentUpdateAddress ( ) throws Exception {
    final Customer originalCustomer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1993));
    dateOfBirth.setMonth(Integer.valueOf(4));
    dateOfBirth.setDay(Integer.valueOf(4));

    Address address = new Address();
    address.setStreet("Bolifamba");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("677429377");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("666767693");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(mtnContact);
    contactDetails.add(nextellContact);

    originalCustomer.setIdentifier("likumba");
    originalCustomer.setType(Customer.Type.PERSON.name());
    originalCustomer.setGivenName("Ikome");
    originalCustomer.setMiddleName("Esong");
    originalCustomer.setSurname("Ikome");
    originalCustomer.setDateOfBirth(dateOfBirth);
    originalCustomer.setMember(Boolean.TRUE);
    originalCustomer.setAssignedOffice("Bolifamba");
    originalCustomer.setAssignedEmployee("Malafa Ikome");
    originalCustomer.setAddress(address);
    originalCustomer.setContactDetails(contactDetails);
    originalCustomer.setCurrentState(Customer.State.PENDING.name());
    originalCustomer.setAccountBeneficiary("Mola Kola");
    originalCustomer.setReferenceCustomer("likumba");
    originalCustomer.setApplicationDate(LocalDate.ofYearDay(2017, 239).toString());
    originalCustomer.setLastModifiedBy("Malafa Ikome");
    originalCustomer.setLastModifiedOn(LocalDate.ofYearDay(2018, 97).toString());

    this.customerManager.createCustomer(originalCustomer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, originalCustomer.getIdentifier());

    final Address updatedAddress = AddressGenerator.createRandomAddress();
    updatedAddress.setStreet("Ombe Rd");
    updatedAddress.setCity("Mutengene");
    updatedAddress.setRegion("SWR");
    updatedAddress.setPostalCode("8050");
    updatedAddress.setCountryCode("CM");
    updatedAddress.setCountry("Cameroon");

    Gson gson = new Gson();
    this.mockMvc.perform(put("/customers/" + originalCustomer.getIdentifier() + "/address")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(updatedAddress))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-update-address", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("street").description("street"),
                            fieldWithPath("city").description("city"),
                            fieldWithPath("region").description("region"),
                            fieldWithPath("postalCode").description("postal code"),
                            fieldWithPath("countryCode").description("country code"),
                            fieldWithPath("country").description("country"))));
  }

  @Test
  public void documentUpdateCustomerDetails ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1994));
    dateOfBirth.setMonth(Integer.valueOf(5));
    dateOfBirth.setDay(Integer.valueOf(5));

    Address address = new Address();
    address.setStreet("Bokwango");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail orangeContact = new ContactDetail();
    orangeContact.setType(ContactDetail.Type.MOBILE.name());
    orangeContact.setGroup(ContactDetail.Group.PRIVATE.name());
    orangeContact.setValue("699429343");
    orangeContact.setPreferenceLevel(Integer.valueOf(1));
    orangeContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("666737666");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(nextellContact);
    contactDetails.add(orangeContact);

    customer.setIdentifier("kome");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Kome");
    customer.setMiddleName("Ngome");
    customer.setSurname("B.");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Bongo Sq");
    customer.setAssignedEmployee("Malafa E.");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Wife");
    customer.setReferenceCustomer("likumba");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 230).toString());
    customer.setLastModifiedBy("Malafa E.");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 21).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
    Assert.assertTrue(contactDetails.size() == 2);

    contactDetails.remove(1);
    Assert.assertTrue(contactDetails.size() == 1);

    ContactDetail mtnContact = new ContactDetail();
    mtnContact.setType(ContactDetail.Type.MOBILE.name());
    mtnContact.setGroup(ContactDetail.Group.PRIVATE.name());
    mtnContact.setValue("677757564");
    mtnContact.setPreferenceLevel(Integer.valueOf(1));
    mtnContact.setValidated(Boolean.TRUE);

    contactDetails.add(mtnContact);
    Assert.assertTrue(contactDetails.size() == 2);

    Gson gson = new Gson();
    this.mockMvc.perform(put("/customers/" + customer.getIdentifier() + "/contact")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(contactDetails))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-update-customer-details", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("[].type").type("Type").description("The type of the first contact +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("[].group").type("Group").description("The group of the first contact +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    BUSINESS, +\n" +
                                    "    PRIVATE +\n" +
                                    "  } +"),
                            fieldWithPath("[].value").description("first contact's value"),
                            fieldWithPath("[].preferenceLevel").description("Preference Level"),
                            fieldWithPath("[].validated").description("Is first contact validated ?"),
                            fieldWithPath("[1].type").type("Type").description("The type of the second contact +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    PERSON, +\n" +
                                    "    BUSINESS +\n" +
                                    "  } +"),
                            fieldWithPath("[1].group").type("Group").description("The Group of the second contact +\n" +
                                    " +\n" +
                                    "*enum* _Type_ { +\n" +
                                    "    BUSINESS, +\n" +
                                    "    PRIVATE +\n" +
                                    "  } +"),
                            fieldWithPath("[1].value").description("second contact's value"),
                            fieldWithPath("[1].preferenceLevel").description("Preference Level"),
                            fieldWithPath("[1].validated").description("Is second contact validated ?"))));
  }

  @Test
  public void documentUploadPortrait ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1995));
    dateOfBirth.setMonth(Integer.valueOf(6));
    dateOfBirth.setDay(Integer.valueOf(6));

    Address address = new Address();
    address.setStreet("Soppo");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail orangeContact = new ContactDetail();
    orangeContact.setType(ContactDetail.Type.MOBILE.name());
    orangeContact.setGroup(ContactDetail.Group.PRIVATE.name());
    orangeContact.setValue("699499349");
    orangeContact.setPreferenceLevel(Integer.valueOf(1));
    orangeContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(orangeContact);

    customer.setIdentifier("ojong");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Ojong");
    customer.setMiddleName("Arrey");
    customer.setSurname("Bessong");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Bonduma");
    customer.setAssignedEmployee("Egbe E");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("Wife");
    customer.setReferenceCustomer("kumba");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 301).toString());
    customer.setLastModifiedBy("Egbe E.");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 101).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final MockMultipartFile portrait = new MockMultipartFile("portrait", "portrait.png", MediaType.IMAGE_PNG_VALUE, "i don't care".getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.fileUpload("/customers/" + customer.getIdentifier() + "/portrait")
            .file(portrait))
            .andExpect(status().isAccepted())
            .andDo(document("document-upload-portrait"));
  }

  @Test
  public void documentReplacePortrait ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1996));
    dateOfBirth.setMonth(Integer.valueOf(7));
    dateOfBirth.setDay(Integer.valueOf(7));

    Address address = new Address();
    address.setStreet("Bona");
    address.setCity("Buea");
    address.setRegion("SWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail orangeContact = new ContactDetail();
    orangeContact.setType(ContactDetail.Type.MOBILE.name());
    orangeContact.setGroup(ContactDetail.Group.PRIVATE.name());
    orangeContact.setValue("699411349");
    orangeContact.setPreferenceLevel(Integer.valueOf(1));
    orangeContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(orangeContact);

    customer.setIdentifier("osong");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Osong");
    customer.setMiddleName("Arrey");
    customer.setSurname("Besong");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Bona");
    customer.setAssignedEmployee("Egbereke E");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("wife");
    customer.setReferenceCustomer("mianda");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 290).toString());
    customer.setLastModifiedBy("Ashu Arrey");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 11).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final MockMultipartFile firstFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i don't care".getBytes());

    this.customerManager.postPortrait(customer.getIdentifier(), firstFile);

    this.eventRecorder.wait(CustomerEventConstants.POST_PORTRAIT, customer.getIdentifier());

    final MockMultipartFile secondFile = new MockMultipartFile("portrait", "test.png", MediaType.IMAGE_PNG_VALUE, "i care".getBytes());

    this.mockMvc.perform(MockMvcRequestBuilders.fileUpload("/customers/" + customer.getIdentifier() + "/portrait")
            .file(secondFile))
            .andExpect(status().isAccepted())
            .andDo(document("document-replace-portrait"));
  }

  @Test
  public void documentDeletePortrait ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    DateOfBirth dateOfBirth = new DateOfBirth();
    dateOfBirth.setYear(Integer.valueOf(1994));
    dateOfBirth.setMonth(Integer.valueOf(5));
    dateOfBirth.setDay(Integer.valueOf(5));

    Address address = new Address();
    address.setStreet("Nwah");
    address.setCity("Nkambe");
    address.setRegion("NWR");
    address.setPostalCode("8050");
    address.setCountryCode("CM");
    address.setCountry("Cameroon");

    ContactDetail orangeContact = new ContactDetail();
    orangeContact.setType(ContactDetail.Type.MOBILE.name());
    orangeContact.setGroup(ContactDetail.Group.PRIVATE.name());
    orangeContact.setValue("699420043");
    orangeContact.setPreferenceLevel(Integer.valueOf(1));
    orangeContact.setValidated(Boolean.FALSE);

    ContactDetail nextellContact = new ContactDetail();
    nextellContact.setType(ContactDetail.Type.MOBILE.name());
    nextellContact.setGroup(ContactDetail.Group.PRIVATE.name());
    nextellContact.setValue("666737226");
    nextellContact.setPreferenceLevel(Integer.valueOf(2));
    nextellContact.setValidated(Boolean.FALSE);

    List <ContactDetail> contactDetails = new ArrayList <>();
    contactDetails.add(nextellContact);
    contactDetails.add(orangeContact);

    customer.setIdentifier("shey");
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName("Shey");
    customer.setMiddleName("Sembe");
    customer.setSurname("Waba.");
    customer.setDateOfBirth(dateOfBirth);
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice("Nwah Sq");
    customer.setAssignedEmployee("Waba Mala");
    customer.setAddress(address);
    customer.setContactDetails(contactDetails);
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAccountBeneficiary("wife");
    customer.setReferenceCustomer("wirba");
    customer.setApplicationDate(LocalDate.ofYearDay(2017, 98).toString());
    customer.setLastModifiedBy("Waba Mala");
    customer.setLastModifiedOn(LocalDate.ofYearDay(2018, 12).toString());

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    this.mockMvc.perform(delete("/customers/" + customer.getIdentifier() + "/portrait")
            .accept(MediaType.ALL_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-delete-portrait", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
  }
}