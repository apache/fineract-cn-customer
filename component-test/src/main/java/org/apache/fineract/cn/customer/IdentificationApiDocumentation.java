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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.api.v1.domain.ExpirationDate;
import org.apache.fineract.cn.customer.api.v1.domain.IdentificationCard;
import org.apache.fineract.cn.customer.api.v1.domain.IdentificationCardScan;
import org.apache.fineract.cn.customer.api.v1.events.ScanEvent;
import org.apache.fineract.cn.customer.util.CustomerGenerator;
import org.apache.fineract.cn.customer.util.IdentificationCardGenerator;
import org.apache.fineract.cn.customer.util.ScanGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
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

public class IdentificationApiDocumentation extends AbstractCustomerTest {
  @Rule
  public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build/doc/generated-snippets/test-identification-cards");

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
  public void documentFetchIdCards ( ) throws Exception {
    final String customerIdentifier = this.createCustomer();

    IdentificationCard NiDCard = IdentificationCardGenerator.createRandomIdentificationCard();

    ExpirationDate exp = new ExpirationDate();
    exp.setYear(2028);
    exp.setMonth(5);
    exp.setDay(23);

    NiDCard.setType("National");
    NiDCard.setNumber("07235388");
    NiDCard.setExpirationDate(exp);
    NiDCard.setIssuer("NDNS");
    NiDCard.setCreatedBy("Nakuve L.");
    NiDCard.setCreatedOn("2018/5/23");
    NiDCard.setLastModifiedBy("Chi Tih");
    NiDCard.setLastModifiedOn(LocalDate.now().toString());

    IdentificationCard SchoolId = IdentificationCardGenerator.createRandomIdentificationCard();

    ExpirationDate ex = new ExpirationDate();
    ex.setYear(2019);
    ex.setMonth(10);
    ex.setDay(7);

    SchoolId.setType("University");
    SchoolId.setNumber("SC10A123");
    SchoolId.setExpirationDate(ex);
    SchoolId.setIssuer("UB");
    SchoolId.setCreatedBy("Ndip Ndip");
    SchoolId.setCreatedOn("2010/12/10");
    SchoolId.setLastModifiedBy("Chi Tih");
    SchoolId.setLastModifiedOn(LocalDate.now().toString());

    Stream.of(NiDCard, SchoolId)
            .forEach(identificationCard -> {
              this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
              try {
                this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
              } catch (final InterruptedException exception) {
                Assert.fail(exception.getMessage());
              }
            });

    final List <IdentificationCard> cards = this.customerManager.fetchIdentificationCards(customerIdentifier);
    System.out.println("Number of cards is " + cards.size());
    Assert.assertTrue(cards.size() == 2);

    this.mockMvc.perform(get("/customers/" + customerIdentifier + "/identifications")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-id-cards", preprocessRequest(prettyPrint()),
                    preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("[].type").description("type of first card"),
                            fieldWithPath("[].number").description("Number on first card"),
                            fieldWithPath("[].expirationDate").type("ExpirationDate").description("expiry date of first card +\n" +
                                    " +\n" +
                                    " *class* _ExpirationDate_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("[].issuer").description("issuer of first card"),
                            fieldWithPath("[].createdBy").description("creator of first card"),
                            fieldWithPath("[].createdOn").description("date of creation of first card "),
                            fieldWithPath("[].lastModifiedBy").type("String").description("employee who last modified first card"),
                            fieldWithPath("[].lastModifiedOn").type("String").description("date when first card was lastly modified"),
                            fieldWithPath("[1].type").description("type of second card"),
                            fieldWithPath("[1].number").description("Number of second card"),
                            fieldWithPath("[1].expirationDate").type("ExpirationDate").description("expiry date of second card +\n" +
                                    " +\n" +
                                    " *class* _ExpirationDate_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("[1].issuer").description("issuer of second card"),
                            fieldWithPath("[1].createdBy").description("creator of second card"),
                            fieldWithPath("[1].createdOn").description("date of creation of second card"),
                            fieldWithPath("[1].lastModifiedBy").type("String").description("employee who last modified second card"),
                            fieldWithPath("[1].lastModifiedOn").type("String").description("date when second cards was lastly modified"))));
  }

  @Test
  public void documentCreateIdCard ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    customer.setIdentifier("ejiks");
    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    final String customerIdentifier = customer.getIdentifier();
    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

    ExpirationDate expirationDate = new ExpirationDate();
    expirationDate.setYear(2029);
    expirationDate.setMonth(10);
    expirationDate.setDay(10);

    identificationCard.setType("National");
    identificationCard.setNumber("SC10A1234567");
    identificationCard.setExpirationDate(expirationDate);
    identificationCard.setIssuer("National Security");
    identificationCard.setCreatedBy("Ndop Ndop");
    identificationCard.setCreatedOn("2018/10/10");
    identificationCard.setLastModifiedBy("Cho Toh");
    identificationCard.setLastModifiedOn(LocalDate.now().toString());

    Gson gson = new Gson();
    this.mockMvc.perform(post("/customers/" + customerIdentifier + "/identifications")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(identificationCard))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-create-id-card", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("type").description("Type of identity card"),
                            fieldWithPath("number").description("Identity card number"),
                            fieldWithPath("expirationDate").type("ExpirationDate").description("expiry date of card +\n" +
                                    " +\n" +
                                    " *class* _ExpirationDate_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("issuer").description("Issuer of identity Card"),
                            fieldWithPath("createdBy").description("employee who created card"),
                            fieldWithPath("createdOn").description("date when card was created"),
                            fieldWithPath("lastModifiedBy").description("employee who last modified card"),
                            fieldWithPath("lastModifiedOn").description("last time card was modified"))));
  }

  @Test
  public void documentUpdateIdCard ( ) throws Exception {
    String customerIdentifier = this.createCustomer();
    final String identificationCardNumber = this.createIdentificationCard(customerIdentifier);

    final IdentificationCard updatedIdentificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    updatedIdentificationCard.setNumber(identificationCardNumber);

    ExpirationDate expee = new ExpirationDate();
    expee.setYear(2020);
    expee.setMonth(1);
    expee.setDay(1);

    updatedIdentificationCard.setType("University");
    updatedIdentificationCard.setExpirationDate(expee);
    updatedIdentificationCard.setIssuer("UBuea");

    Gson gson = new Gson();
    this.mockMvc.perform(put("/customers/" + customerIdentifier + "/identifications/" + identificationCardNumber)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(updatedIdentificationCard))
            .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-update-id-card", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    requestFields(
                            fieldWithPath("type").description("Type of Identity card"),
                            fieldWithPath("number").description("Identity card number"),
                            fieldWithPath("expirationDate").type("ExpirationDate").description("expiry date of card +\n" +
                                    " +\n" +
                                    " *class* _ExpirationDate_ { +\n" +
                                    "    *Integer* year, +\n" +
                                    "    *Integer* month, +\n" +
                                    "    *Integer* day, +\n" +
                                    "} +"),
                            fieldWithPath("issuer").description("Issuer of identity Card"))));
  }

  @Test
  public void documentDeleteIdCard ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customer.setIdentifier("ekolle");

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
    final String customerIdentifier = customer.getIdentifier();

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    identificationCard.setNumber("FET12345");
    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
    final String identificationCardNumber = identificationCard.getNumber();

    this.mockMvc.perform(delete("/customers/" + customerIdentifier + "/identifications/" + identificationCardNumber)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-delete-id-card", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
  }

  @Test
  public void documentDeleteIdCardWithScan ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customer.setIdentifier("ana");

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
    final String customerIdentifier = customer.getIdentifier();

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    identificationCard.setNumber("FET20Z234");
    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
    final String identificationCardNumber = identificationCard.getNumber();

    final IdentificationCardScan scan = ScanGenerator.createRandomScan(null);
    scan.setIdentifier("myScan");
    scan.setDescription("My Own Scan");
    this.postIdentificationCardScan(customerIdentifier, identificationCardNumber, scan);
    final String scanIdentifier = scan.getIdentifier();

    this.mockMvc.perform(delete("/customers/" + customerIdentifier + "/identifications/"
            + identificationCardNumber + "/scans/" + scanIdentifier)
            .accept(MediaType.ALL_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-delete-id-card-with-scan", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
  }

  @Test
  public void documentFetchScans ( ) throws Exception {
    final Customer customerOne = CustomerGenerator.createRandomCustomer();
    customerOne.setIdentifier("ato'oh");
    final String customerIdentifier = customerOne.getIdentifier();

    this.customerManager.createCustomer(customerOne);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customerIdentifier);

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    identificationCard.setNumber("CT13B0987");
    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
    final String identificationCardNumber = identificationCard.getNumber();

    IdentificationCardScan iDCardOne = this.createScan(customerIdentifier, identificationCardNumber, "ScanOne", "First Scan");
    IdentificationCardScan iDCardTwo = this.createScan(customerIdentifier, identificationCardNumber, "ScanTwo", "Second Scan");
    IdentificationCardScan iDCardThree = this.createScan(customerIdentifier, identificationCardNumber, "ScanThree", "Three Scan");

    final List <IdentificationCardScan> cardScans = this.customerManager.fetchIdentificationCardScans(customerIdentifier, identificationCardNumber);

    Gson gson = new Gson();
    this.mockMvc.perform(get("/customers/" + customerIdentifier + "/identifications/" + identificationCardNumber + "/scans")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(cardScans))
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-fetch-scans", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("[].identifier").description("First scan's identifier"),
                            fieldWithPath("[].description").description("First scan's description"),
                            fieldWithPath("[1].identifier").description("Second scan's identifier"),
                            fieldWithPath("[1].description").description("Second scan's description"),
                            fieldWithPath("[2].identifier").description("Third scan's identifier"),
                            fieldWithPath("[2].description").description("Third scan's description"))));
  }

  @Test
  public void documentFindScan ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customer.setIdentifier("checko");

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
    final String customerIdentifier = customer.getIdentifier();

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    identificationCard.setNumber("SM23A4321");
    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
    final String identificationCardNumber = identificationCard.getNumber();

    final IdentificationCardScan scan = this.createScan(customerIdentifier, identificationCardNumber, "soughtOut", "Found Scan");

    Gson gson = new Gson();
    this.mockMvc.perform(get("/customers/" + customerIdentifier
            + "/identifications/" + identificationCardNumber + "/scans/" + scan.getIdentifier())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(gson.toJson(scan))
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-find-scan", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()),
                    responseFields(
                            fieldWithPath("identifier").description("scan's identifier"),
                            fieldWithPath("description").description("scan's description"))));
  }

  @Test
  public void documentFindScanWithImage ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customer.setIdentifier("akong");

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
    final String customerIdentifier = customer.getIdentifier();

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    identificationCard.setNumber("SC18C0999");
    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
    final String identificationCardNumber = identificationCard.getNumber();

    final IdentificationCardScan scan = ScanGenerator.createRandomScan(null);
    scan.setIdentifier("scanIdentity");
    scan.setDescription("scanDescription");
    final byte[] imageInBytes = "iCareAboutImage".getBytes();

    final MockMultipartFile image = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, imageInBytes);
    this.customerManager.postIdentificationCardScan(customerIdentifier, identificationCardNumber, scan.getIdentifier(), scan.getDescription(), image);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD_SCAN, new ScanEvent(identificationCardNumber, scan.getIdentifier()));

    this.mockMvc.perform(get("/customers/" + customerIdentifier + "/identifications/"
            + identificationCardNumber + "/scans/" + scan.getIdentifier() + "/image")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(imageInBytes)
            .accept(MediaType.ALL_VALUE))
            .andExpect(status().isOk())
            .andDo(document("document-find-scan-with-image", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
  }

  @Test
  public void documentDeleteIdCardScan ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();
    customer.setIdentifier("wabah");

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());
    final String customerIdentifier = customer.getIdentifier();

    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();
    identificationCard.setNumber("SC012A001");
    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());
    final String identificationCardNumber = identificationCard.getNumber();

    final IdentificationCardScan createdScan = ScanGenerator.createRandomScan(null);
    this.postIdentificationCardScan(customerIdentifier, identificationCardNumber, createdScan);

    createdScan.setIdentifier("justScan");
    createdScan.setDescription("Just Scan");
    Assert.assertNotNull(createdScan);

    this.mockMvc.perform(delete("/customers/" + customerIdentifier + "/identifications/"
            + identificationCardNumber + "/scans/" + createdScan.getIdentifier())
            .accept(MediaType.ALL_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isAccepted())
            .andDo(document("document-delete-id-card-scan", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())));
  }

  private String createCustomer ( ) throws Exception {
    final Customer customer = CustomerGenerator.createRandomCustomer();

    this.customerManager.createCustomer(customer);
    this.eventRecorder.wait(CustomerEventConstants.POST_CUSTOMER, customer.getIdentifier());

    return customer.getIdentifier();
  }

  private String createIdentificationCard (final String customerIdentifier) throws Exception {
    final IdentificationCard identificationCard = IdentificationCardGenerator.createRandomIdentificationCard();

    this.customerManager.createIdentificationCard(customerIdentifier, identificationCard);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD, identificationCard.getNumber());

    return identificationCard.getNumber();
  }

  private IdentificationCardScan createScan (final String customerIdentifier, final String cardNumber, String scanIdentifier, String scanDescription) throws Exception {
    final IdentificationCardScan scan = ScanGenerator.createRandomScan(null);
    scan.setIdentifier(scanIdentifier);
    scan.setDescription(scanDescription);

    this.postIdentificationCardScan(customerIdentifier, cardNumber, scan);

    return scan;
  }

  private IdentificationCardScan createScan (final String customerIdentifier, final String cardNumber) throws Exception {
    final IdentificationCardScan scan = ScanGenerator.createRandomScan(null);

    this.postIdentificationCardScan(customerIdentifier, cardNumber, scan);

    return scan;
  }

  private IdentificationCardScan createScan (final String customerIdentifier, final String cardNumber, final String identifier) throws Exception {
    final IdentificationCardScan scan = ScanGenerator.createRandomScan(identifier);

    this.postIdentificationCardScan(customerIdentifier, cardNumber, scan);

    return scan;
  }

  private void postIdentificationCardScan (final String customerIdentifier, final String cardNumber, final IdentificationCardScan scan) throws InterruptedException {
    final MockMultipartFile image = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, RandomStringUtils.randomAlphanumeric(20).getBytes());

    this.customerManager.postIdentificationCardScan(customerIdentifier, cardNumber, scan.getIdentifier(), scan.getDescription(), image);
    this.eventRecorder.wait(CustomerEventConstants.POST_IDENTIFICATION_CARD_SCAN, new ScanEvent(cardNumber, scan.getIdentifier()));
  }
}
