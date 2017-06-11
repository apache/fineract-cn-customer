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
package io.mifos.customer.service.internal.service;

import io.mifos.customer.api.v1.domain.Command;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.customer.api.v1.domain.CustomerPage;
import io.mifos.customer.api.v1.domain.IdentificationCard;
import io.mifos.customer.catalog.api.v1.domain.Value;
import io.mifos.customer.catalog.service.internal.repository.FieldEntity;
import io.mifos.customer.catalog.service.internal.repository.FieldValueEntity;
import io.mifos.customer.catalog.service.internal.repository.FieldValueRepository;
import io.mifos.customer.service.ServiceConstants;
import io.mifos.customer.service.internal.mapper.CommandMapper;
import io.mifos.customer.service.internal.mapper.ContactDetailMapper;
import io.mifos.customer.service.internal.mapper.CustomerMapper;
import io.mifos.customer.service.internal.mapper.IdentificationCardMapper;
import io.mifos.customer.service.internal.repository.*;
import io.mifos.customer.service.internal.mapper.AddressMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

  private final Logger logger;
  private final CustomerRepository customerRepository;
  private final IdentificationCardRepository identificationCardRepository;
  private final PortraitRepository portraitRepository;
  private final ContactDetailRepository contactDetailRepository;
  private final FieldValueRepository fieldValueRepository;
  private final CommandRepository commandRepository;

  @Autowired
  public CustomerService(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                         final CustomerRepository customerRepository,
                         final IdentificationCardRepository identificationCardRepository,
                         final PortraitRepository portraitRepository,
                         final ContactDetailRepository contactDetailRepository,
                         final FieldValueRepository fieldValueRepository,
                         final CommandRepository commandRepository) {
    super();
    this.logger = logger;
    this.customerRepository = customerRepository;
    this.identificationCardRepository = identificationCardRepository;
    this.portraitRepository = portraitRepository;
    this.contactDetailRepository = contactDetailRepository;
    this.fieldValueRepository = fieldValueRepository;
    this.commandRepository = commandRepository;
  }

  public Boolean customerExists(final String identifier) {
    return this.customerRepository.existsByIdentifier(identifier);
  }

  public Boolean portraitExists(final String identifier) {
    return this.portraitRepository.existsByIdentifier(identifier);
  }

  public Boolean identificationCardExists(final String number) {
    return this.identificationCardRepository.existsByNumber(number);
  }

  public Optional<Customer> findCustomer(final String identifier) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(identifier);
    if (customerEntity != null) {
      final Customer customer = CustomerMapper.map(customerEntity);
      customer.setAddress(AddressMapper.map(customerEntity.getAddress()));

      final List<ContactDetailEntity> contactDetailEntities = this.contactDetailRepository.findByCustomer(customerEntity);
      if (contactDetailEntities != null) {
        customer.setContactDetails(
            contactDetailEntities
                .stream()
                  .map(ContactDetailMapper::map)
                  .collect(Collectors.toList())
        );
      }

      final List<FieldValueEntity> fieldValueEntities = this.fieldValueRepository.findByCustomer(customerEntity);
      if (fieldValueEntities != null) {
        customer.setCustomValues(
            fieldValueEntities
                .stream()
                .map(fieldValueEntity -> {
                  final Value value = new Value();
                  value.setValue(fieldValueEntity.getValue());
                  final FieldEntity fieldEntity = fieldValueEntity.getField();
                  value.setCatalogIdentifier(fieldEntity.getCatalog().getIdentifier());
                  value.setFieldIdentifier(fieldEntity.getIdentifier());
                  return value;
                }).collect(Collectors.toList())
        );
      }

      return Optional.of(customer);
    } else {
      return Optional.empty();
    }
  }

  public CustomerPage fetchCustomer(final String term, final Boolean includeClosed, final Pageable pageable) {
    final Page<CustomerEntity> customerEntities;
    if (includeClosed) {
      if (term != null) {
        customerEntities =
            this.customerRepository.findByIdentifierContainingOrGivenNameContainingOrSurnameContaining(term, term, term, pageable);
      } else {
        customerEntities = this.customerRepository.findAll(pageable);
      }
    } else {
      if (term != null) {
        customerEntities =
            this.customerRepository.findByCurrentStateNotAndIdentifierContainingOrGivenNameContainingOrSurnameContaining(
                Customer.State.CLOSED.name(), term, term, term, pageable);
      } else {
        customerEntities = this.customerRepository.findByCurrentStateNot(Customer.State.CLOSED.name(), pageable);
      }
    }

    final CustomerPage customerPage = new CustomerPage();
    customerPage.setTotalPages(customerEntities.getTotalPages());
    customerPage.setTotalElements(customerEntities.getTotalElements());
    if (customerEntities.getSize() > 0) {
      final ArrayList<Customer> customers = new ArrayList<>(customerEntities.getSize());
      customerPage.setCustomers(customers);
      customerEntities.forEach(customerEntity -> customers.add(CustomerMapper.map(customerEntity)));
    }

    return customerPage;
  }

  public final List<Command> fetchCommandsByCustomer(final String identifier) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(identifier);
    final List<CommandEntity> commands = this.commandRepository.findByCustomer(customerEntity);
    if (commands != null) {
      return commands.stream().map(CommandMapper::map).collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  public final PortraitEntity findPortrait(final String identifier) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(identifier);

    return this.portraitRepository.findByCustomer(customerEntity);
  }

  public final List<IdentificationCard> fetchIdentificationCardsByCustomer(final String identifier) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(identifier);
    final List<IdentificationCardEntity> identificationCards = this.identificationCardRepository.findByCustomer(customerEntity);

    if (identificationCards != null) {
      return identificationCards.stream().map(IdentificationCardMapper::map).collect(Collectors.toList());
    } else {
      return Collections.emptyList();
    }
  }

  public Optional<IdentificationCard> findIdentificationCard(final String number) {
    final Optional<IdentificationCardEntity> identificationCardEntity = this.identificationCardRepository.findByNumber(number);

    return identificationCardEntity.map(IdentificationCardMapper::map);
  }
}
