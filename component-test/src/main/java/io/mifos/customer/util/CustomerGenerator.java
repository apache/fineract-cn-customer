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
package io.mifos.customer.util;

import io.mifos.core.lang.DateOfBirth;
import io.mifos.customer.api.v1.domain.Customer;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;

public final class CustomerGenerator {

  private CustomerGenerator() {
    super();
  }

  public static Customer createRandomCustomer() {
    final Customer customer = new Customer();
    customer.setIdentifier(RandomStringUtils.randomAlphanumeric(8));
    customer.setType(Customer.Type.PERSON.name());
    customer.setGivenName(RandomStringUtils.randomAlphanumeric(256));
    customer.setMiddleName(RandomStringUtils.randomAlphanumeric(256));
    customer.setSurname(RandomStringUtils.randomAlphanumeric(256));
    customer.setDateOfBirth(DateOfBirth.fromLocalDate(LocalDate.now(Clock.systemUTC())));
    customer.setMember(Boolean.TRUE);
    customer.setAssignedOffice(RandomStringUtils.randomAlphanumeric(8));
    customer.setAssignedEmployee(RandomStringUtils.randomAlphanumeric(8));
    customer.setCurrentState(Customer.State.PENDING.name());
    customer.setAddress(AddressGenerator.createRandomAddress());
    customer.setContactDetails(Arrays.asList(ContactDetailGenerator.createRandomContactDetail(),
        ContactDetailGenerator.createRandomContactDetail()));
    return customer;
  }
}
