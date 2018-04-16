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
package org.apache.fineract.cn.customer.internal.service;

import org.apache.fineract.cn.customer.api.v1.domain.*;
import org.apache.fineract.cn.customer.catalog.api.v1.domain.Value;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldValueEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldValueRepository;
import org.apache.fineract.cn.customer.internal.mapper.*;
import org.apache.fineract.cn.customer.internal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CustomerService {

  private final CustomerRepository customerRepository;
  private final IdentificationCardRepository identificationCardRepository;
  private final IdentificationCardScanRepository identificationCardScanRepository;
  private final PortraitRepository portraitRepository;
  private final ContactDetailRepository contactDetailRepository;
  private final FieldValueRepository fieldValueRepository;
  private final CommandRepository commandRepository;
  private final TaskDefinitionRepository taskDefinitionRepository;
  private final TaskInstanceRepository taskInstanceRepository;

  @Autowired
  public CustomerService(final CustomerRepository customerRepository,
                         final IdentificationCardRepository identificationCardRepository,
                         final IdentificationCardScanRepository identificationCardScanRepository,
                         final PortraitRepository portraitRepository,
                         final ContactDetailRepository contactDetailRepository,
                         final FieldValueRepository fieldValueRepository,
                         final CommandRepository commandRepository,
                         final TaskDefinitionRepository taskDefinitionRepository,
                         final TaskInstanceRepository taskInstanceRepository) {
    super();
    this.customerRepository = customerRepository;
    this.identificationCardRepository = identificationCardRepository;
    this.identificationCardScanRepository = identificationCardScanRepository;
    this.portraitRepository = portraitRepository;
    this.contactDetailRepository = contactDetailRepository;
    this.fieldValueRepository = fieldValueRepository;
    this.commandRepository = commandRepository;
    this.taskDefinitionRepository = taskDefinitionRepository;
    this.taskInstanceRepository = taskInstanceRepository;
  }

  public Boolean customerExists(final String identifier) {
    return this.customerRepository.existsByIdentifier(identifier);
  }

  public Boolean identificationCardExists(final String number) {
    return this.identificationCardRepository.existsByNumber(number);
  }

  public Boolean identificationCardScanExists(final String number, final String identifier) {
    return this.identificationCardRepository.findByNumber(number)
            .map(cardEntity -> this.identificationCardScanRepository.existsByIdentifierAndIdentificationCard(identifier, cardEntity))
            .orElse(false);
  }

  public Optional<Customer> findCustomer(final String identifier) {
    return customerRepository.findByIdentifier(identifier)
        .map(customerEntity -> {
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

          return customer;
        });
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

  public final Stream<Command> fetchCommandsByCustomer(final String identifier) {
    return customerRepository.findByIdentifier(identifier)
        .map(commandRepository::findByCustomer)
        .orElse(Stream.empty())
        .map(CommandMapper::map);
  }

  public final Optional<PortraitEntity> findPortrait(final String identifier) {
    return customerRepository.findByIdentifier(identifier)
        .map(portraitRepository::findByCustomer);
  }

  public final Stream<IdentificationCard> fetchIdentificationCardsByCustomer(final String identifier) {
    return customerRepository.findByIdentifier(identifier)
        .map(identificationCardRepository::findByCustomer)
        .orElse(Stream.empty())
        .map(IdentificationCardMapper::map);
  }

  public Optional<IdentificationCard> findIdentificationCard(final String number) {
    final Optional<IdentificationCardEntity> identificationCardEntity = this.identificationCardRepository.findByNumber(number);

    return identificationCardEntity.map(IdentificationCardMapper::map);
  }

  public final List<IdentificationCardScan> fetchScansByIdentificationCard(final String number) {
    final Optional<IdentificationCardEntity> identificationCard = this.identificationCardRepository.findByNumber(number);

    return identificationCard.map(this.identificationCardScanRepository::findByIdentificationCard)
            .map(x -> x.stream().map(IdentificationCardScanMapper::map).collect(Collectors.toList()))
            .orElseGet(Collections::emptyList);
  }

  private Optional<IdentificationCardScanEntity> findIdentificationCardEntity(final String number, final String identifier) {
    final Optional<IdentificationCardEntity> cardEntity = this.identificationCardRepository.findByNumber(number);
    return cardEntity.flatMap(card -> this.identificationCardScanRepository.findByIdentifierAndIdentificationCard(identifier, card));
  }

  public Optional<IdentificationCardScan> findIdentificationCardScan(final String number, final String identifier) {
    return this.findIdentificationCardEntity(number, identifier).map(IdentificationCardScanMapper::map);
  }

  public Optional<byte[]> findIdentificationCardScanImage(final String number, final String identifier) {
    return this.findIdentificationCardEntity(number, identifier).map(IdentificationCardScanEntity::getImage);
  }

  public List<ProcessStep> getProcessSteps(final String customerIdentifier) {
    return customerRepository.findByIdentifier(customerIdentifier)
        .map(customerEntity -> {
          final List<ProcessStep> processSteps = new ArrayList<>();

          final Customer.State state = Customer.State.valueOf(customerEntity.getCurrentState());
          switch (state) {
            case PENDING:
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.ACTIVATE));
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.CLOSE));
              break;
            case ACTIVE:
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.LOCK));
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.CLOSE));
              break;
            case LOCKED:
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.UNLOCK));
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.CLOSE));
              break;
            case CLOSED:
              processSteps.add(this.buildProcessStep(customerEntity, Command.Action.REOPEN));
              break;
          }

          return processSteps;
        })
        .orElse(Collections.emptyList());
  }

  private ProcessStep buildProcessStep(final CustomerEntity customerEntity, final Command.Action action) {
    final ProcessStep processStep = new ProcessStep();

    final Command command = new Command();
    command.setAction(action.name());
    processStep.setCommand(command);

    final ArrayList<TaskDefinition> taskDefinitions = new ArrayList<>();
    this.taskDefinitionRepository.findByAssignedCommandsContaining(action.name())
        .forEach(taskDefinitionEntity ->
            this.taskInstanceRepository.findByCustomerAndTaskDefinition(customerEntity, taskDefinitionEntity)
            .forEach(taskInstanceEntity -> {
              if (taskInstanceEntity.getExecutedBy() == null) {
                taskDefinitions.add(TaskDefinitionMapper.map(taskDefinitionEntity));
              }
            }));
    processStep.setTaskDefinitions(taskDefinitions);

    return processStep;
  }
}
