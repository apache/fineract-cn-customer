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
package org.apache.fineract.cn.customer.internal.command.handler;

import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.domain.Command;
import org.apache.fineract.cn.customer.api.v1.domain.Customer;
import org.apache.fineract.cn.customer.api.v1.events.ScanEvent;
import org.apache.fineract.cn.customer.catalog.internal.repository.CatalogEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.CatalogRepository;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldRepository;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldValueEntity;
import org.apache.fineract.cn.customer.catalog.internal.repository.FieldValueRepository;
import org.apache.fineract.cn.customer.internal.command.ActivateCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.CloseCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.CreateCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.CreateIdentificationCardCommand;
import org.apache.fineract.cn.customer.internal.command.CreateIdentificationCardScanCommand;
import org.apache.fineract.cn.customer.internal.command.CreatePortraitCommand;
import org.apache.fineract.cn.customer.internal.command.DeleteIdentificationCardCommand;
import org.apache.fineract.cn.customer.internal.command.DeleteIdentificationCardScanCommand;
import org.apache.fineract.cn.customer.internal.command.DeletePortraitCommand;
import org.apache.fineract.cn.customer.internal.command.LockCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.ReopenCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.UnlockCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.UpdateAddressCommand;
import org.apache.fineract.cn.customer.internal.command.UpdateContactDetailsCommand;
import org.apache.fineract.cn.customer.internal.command.UpdateCustomerCommand;
import org.apache.fineract.cn.customer.internal.command.UpdateIdentificationCardCommand;
import org.apache.fineract.cn.customer.internal.mapper.AddressMapper;
import org.apache.fineract.cn.customer.internal.mapper.CommandMapper;
import org.apache.fineract.cn.customer.internal.mapper.ContactDetailMapper;
import org.apache.fineract.cn.customer.internal.mapper.CustomerMapper;
import org.apache.fineract.cn.customer.internal.mapper.FieldValueMapper;
import org.apache.fineract.cn.customer.internal.mapper.IdentificationCardMapper;
import org.apache.fineract.cn.customer.internal.mapper.IdentificationCardScanMapper;
import org.apache.fineract.cn.customer.internal.mapper.PortraitMapper;
import org.apache.fineract.cn.customer.internal.repository.AddressEntity;
import org.apache.fineract.cn.customer.internal.repository.AddressRepository;
import org.apache.fineract.cn.customer.internal.repository.CommandRepository;
import org.apache.fineract.cn.customer.internal.repository.ContactDetailEntity;
import org.apache.fineract.cn.customer.internal.repository.ContactDetailRepository;
import org.apache.fineract.cn.customer.internal.repository.CustomerEntity;
import org.apache.fineract.cn.customer.internal.repository.CustomerRepository;
import org.apache.fineract.cn.customer.internal.repository.IdentificationCardEntity;
import org.apache.fineract.cn.customer.internal.repository.IdentificationCardRepository;
import org.apache.fineract.cn.customer.internal.repository.IdentificationCardScanEntity;
import org.apache.fineract.cn.customer.internal.repository.IdentificationCardScanRepository;
import org.apache.fineract.cn.customer.internal.repository.PortraitEntity;
import org.apache.fineract.cn.customer.internal.repository.PortraitRepository;
import java.io.IOException;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.fineract.cn.api.util.UserContextHolder;
import org.apache.fineract.cn.command.annotation.Aggregate;
import org.apache.fineract.cn.command.annotation.CommandHandler;
import org.apache.fineract.cn.command.annotation.CommandLogLevel;
import org.apache.fineract.cn.command.annotation.EventEmitter;
import org.apache.fineract.cn.lang.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@Aggregate
public class CustomerAggregate {
  private final AddressRepository addressRepository;
  private final CustomerRepository customerRepository;
  private final IdentificationCardRepository identificationCardRepository;
  private final IdentificationCardScanRepository identificationCardScanRepository;
  private final PortraitRepository portraitRepository;
  private final ContactDetailRepository contactDetailRepository;
  private final FieldValueRepository fieldValueRepository;
  private final CatalogRepository catalogRepository;
  private final FieldRepository fieldRepository;
  private final CommandRepository commandRepository;
  private final TaskAggregate taskAggregate;

  @Autowired
  public CustomerAggregate(final AddressRepository addressRepository,
                           final CustomerRepository customerRepository,
                           final IdentificationCardRepository identificationCardRepository,
                           final IdentificationCardScanRepository identificationCardScanRepository,
                           final PortraitRepository portraitRepository,
                           final ContactDetailRepository contactDetailRepository,
                           final FieldValueRepository fieldValueRepository,
                           final CatalogRepository catalogRepository,
                           final FieldRepository fieldRepository,
                           final CommandRepository commandRepository,
                           final TaskAggregate taskAggregate) {
    super();
    this.addressRepository = addressRepository;
    this.customerRepository = customerRepository;
    this.identificationCardRepository = identificationCardRepository;
    this.identificationCardScanRepository = identificationCardScanRepository;
    this.portraitRepository = portraitRepository;
    this.contactDetailRepository = contactDetailRepository;
    this.fieldValueRepository = fieldValueRepository;
    this.catalogRepository = catalogRepository;
    this.fieldRepository = fieldRepository;
    this.commandRepository = commandRepository;
    this.taskAggregate = taskAggregate;
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_CUSTOMER)
  public String createCustomer(final CreateCustomerCommand createCustomerCommand) {
    final Customer customer = createCustomerCommand.customer();

    final AddressEntity savedAddress = this.addressRepository.save(AddressMapper.map(customer.getAddress()));

    final CustomerEntity customerEntity = CustomerMapper.map(customer);
    customerEntity.setCurrentState(Customer.State.PENDING.name());
    customerEntity.setAddress(savedAddress);
    final CustomerEntity savedCustomerEntity = this.customerRepository.save(customerEntity);

    if (customer.getContactDetails() != null) {
      this.contactDetailRepository.save(
          customer.getContactDetails()
              .stream()
              .map(contact -> {
                final ContactDetailEntity contactDetailEntity = ContactDetailMapper.map(contact);
                contactDetailEntity.setCustomer(savedCustomerEntity);
                return contactDetailEntity;
              })
              .collect(Collectors.toList())
      );
    }

    if (customer.getCustomValues() != null) {
      this.setCustomValues(customer, savedCustomerEntity);
    }

    this.taskAggregate.onCustomerCommand(savedCustomerEntity, Command.Action.ACTIVATE);

    return customer.getIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_CUSTOMER)
  public String updateCustomer(final UpdateCustomerCommand updateCustomerCommand) {
    final Customer customer = updateCustomerCommand.customer();

    final CustomerEntity customerEntity = findCustomerEntityOrThrow(customer.getIdentifier());

    customerEntity.setGivenName(customer.getGivenName());
    customerEntity.setMiddleName(customer.getMiddleName());
    customerEntity.setSurname(customer.getSurname());
    customerEntity.setAccountBeneficiary(customer.getAccountBeneficiary());
    customerEntity.setReferenceCustomer(customer.getReferenceCustomer());
    customerEntity.setAssignedOffice(customer.getAssignedOffice());
    customerEntity.setAssignedEmployee(customer.getAssignedEmployee());

    if (customer.getDateOfBirth() != null ) {
      final LocalDate newDateOfBirth = customer.getDateOfBirth().toLocalDate();
      if (customerEntity.getDateOfBirth() == null) {
        customerEntity.setDateOfBirth(Date.valueOf(newDateOfBirth));
      } else {
        final LocalDate dateOfBirth = customerEntity.getDateOfBirth().toLocalDate();
        if (!dateOfBirth.isEqual(newDateOfBirth)) {
          customerEntity.setDateOfBirth(Date.valueOf(newDateOfBirth));
        }
      }
    } else {
      customerEntity.setDateOfBirth(null);
    }

    if (customer.getCustomValues() != null) {
      this.fieldValueRepository.deleteByCustomer(customerEntity);
      this.fieldValueRepository.flush();
      this.setCustomValues(customer, customerEntity);
    }

    if (customer.getAddress() != null) {
      this.updateAddress(new UpdateAddressCommand(customer.getIdentifier(), customer.getAddress()));
    }

    this.updateContactDetails(new UpdateContactDetailsCommand(customer.getIdentifier(), customer.getContactDetails()));

    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    this.customerRepository.save(customerEntity);

    return customer.getIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.ACTIVATE_CUSTOMER)
  public String activateCustomer(final ActivateCustomerCommand activateCustomerCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(activateCustomerCommand.identifier());

    if (this.taskAggregate.openTasksForCustomerExist(customerEntity, Command.Action.ACTIVATE.name())) {
      throw ServiceException.conflict("Open Tasks for customer {0} exists.", activateCustomerCommand.identifier());
    }

    customerEntity.setCurrentState(Customer.State.ACTIVE.name());
    if (customerEntity.getApplicationDate() == null) {
      customerEntity.setApplicationDate(LocalDate.now(Clock.systemUTC()));
    }
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final CustomerEntity savedCustomerEntity = this.customerRepository.save(customerEntity);

    this.commandRepository.save(
        CommandMapper.create(savedCustomerEntity, Command.Action.ACTIVATE.name(), activateCustomerCommand.comment())
    );

    return activateCustomerCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.LOCK_CUSTOMER)
  public String lockCustomer(final LockCustomerCommand lockCustomerCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(lockCustomerCommand.identifier());

    customerEntity.setCurrentState(Customer.State.LOCKED.name());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final CustomerEntity savedCustomerEntity = this.customerRepository.save(customerEntity);

    this.commandRepository.save(
        CommandMapper.create(savedCustomerEntity, Command.Action.LOCK.name(), lockCustomerCommand.comment())
    );

    this.taskAggregate.onCustomerCommand(savedCustomerEntity, Command.Action.UNLOCK);

    return lockCustomerCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.UNLOCK_CUSTOMER)
  public String unlockCustomer(final UnlockCustomerCommand unlockCustomerCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(unlockCustomerCommand.identifier());

    if (this.taskAggregate.openTasksForCustomerExist(customerEntity, Command.Action.UNLOCK.name())) {
      throw ServiceException.conflict("Open Tasks for customer {0} exists.", unlockCustomerCommand.identifier());
    }

    customerEntity.setCurrentState(Customer.State.ACTIVE.name());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final CustomerEntity savedCustomerEntity = this.customerRepository.save(customerEntity);

    this.commandRepository.save(
        CommandMapper.create(savedCustomerEntity, Command.Action.UNLOCK.name(), unlockCustomerCommand.comment())
    );

    return unlockCustomerCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.CLOSE_CUSTOMER)
  public String closeCustomer(final CloseCustomerCommand closeCustomerCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(closeCustomerCommand.identifier());

    customerEntity.setCurrentState(Customer.State.CLOSED.name());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final CustomerEntity savedCustomerEntity = this.customerRepository.save(customerEntity);

    this.commandRepository.save(
        CommandMapper.create(savedCustomerEntity, Command.Action.CLOSE.name(), closeCustomerCommand.comment())
    );

    this.taskAggregate.onCustomerCommand(savedCustomerEntity, Command.Action.REOPEN);

    return closeCustomerCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.REOPEN_CUSTOMER)
  public String reopenCustomer(final ReopenCustomerCommand reopenCustomerCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(reopenCustomerCommand.identifier());

    if (this.taskAggregate.openTasksForCustomerExist(customerEntity, Command.Action.REOPEN.name())) {
      throw ServiceException.conflict("Open Tasks for customer {0} exists.", reopenCustomerCommand.identifier());
    }

    customerEntity.setCurrentState(Customer.State.ACTIVE.name());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final CustomerEntity savedCustomerEntity = this.customerRepository.save(customerEntity);

    this.commandRepository.save(
        CommandMapper.create(savedCustomerEntity, Command.Action.REOPEN.name(), reopenCustomerCommand.comment())
    );

    return reopenCustomerCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_ADDRESS)
  public String updateAddress(final UpdateAddressCommand updateAddressCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(updateAddressCommand.identifier());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final AddressEntity oldAddressEntity = customerEntity.getAddress();

    final AddressEntity newAddressEntity = this.addressRepository.save(AddressMapper.map(updateAddressCommand.address()));

    customerEntity.setAddress(newAddressEntity);
    this.customerRepository.save(customerEntity);

    this.addressRepository.delete(oldAddressEntity);

    return updateAddressCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_CONTACT_DETAILS)
  public String updateContactDetails(final UpdateContactDetailsCommand updateContactDetailsCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(updateContactDetailsCommand.identifier());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final List<ContactDetailEntity> oldContactDetails = this.contactDetailRepository.findByCustomer(customerEntity);
    this.contactDetailRepository.delete(oldContactDetails);

    if (updateContactDetailsCommand.contactDetails() != null) {
      this.contactDetailRepository.save(
          updateContactDetailsCommand.contactDetails()
              .stream()
              .map(contact -> {
                final ContactDetailEntity newContactDetail = ContactDetailMapper.map(contact);
                newContactDetail.setCustomer(customerEntity);
                return newContactDetail;
              })
              .collect(Collectors.toList())
      );
    }

    return updateContactDetailsCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_IDENTIFICATION_CARD)
  public String createIdentificationCard(final CreateIdentificationCardCommand createIdentificationCardCommand) {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(createIdentificationCardCommand.identifier());

    final IdentificationCardEntity identificationCardEntity = IdentificationCardMapper.map(createIdentificationCardCommand.identificationCard());

    identificationCardEntity.setCustomer(customerEntity);
    identificationCardEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    identificationCardEntity.setCreatedOn(LocalDateTime.now(Clock.systemUTC()));

    this.identificationCardRepository.save(identificationCardEntity);

    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    this.customerRepository.save(customerEntity);

    return identificationCardEntity.getNumber();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_IDENTIFICATION_CARD)
  public String updateIdentificationCard(final UpdateIdentificationCardCommand updateIdentificationCardCommand) {
    final Optional<IdentificationCardEntity> optionalIdentificationCardEntity = this.identificationCardRepository.findByNumber(updateIdentificationCardCommand.number());

    final IdentificationCardEntity identificationCard = IdentificationCardMapper.map(updateIdentificationCardCommand.identificationCard());

    optionalIdentificationCardEntity.ifPresent(identificationCardEntity -> {
      identificationCardEntity.setIssuer(identificationCard.getIssuer());
      identificationCardEntity.setType(identificationCard.getType());
      identificationCardEntity.setExpirationDate(identificationCard.getExpirationDate());
      identificationCardEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
      identificationCardEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

      this.identificationCardRepository.save(identificationCardEntity);

      final CustomerEntity customerEntity = identificationCardEntity.getCustomer();

      customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
      customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

      this.customerRepository.save(customerEntity);
    });

    return updateIdentificationCardCommand.number();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.DELETE_IDENTIFICATION_CARD)
  public String deleteIdentificationCard(final DeleteIdentificationCardCommand deleteIdentificationCardCommand) throws IOException {
    final Optional<IdentificationCardEntity> optionalIdentificationCardEntity = this.identificationCardRepository.findByNumber(deleteIdentificationCardCommand.number());

    optionalIdentificationCardEntity.ifPresent(identificationCardEntity -> {

      final List<IdentificationCardScanEntity> cardScanEntities = this.identificationCardScanRepository.findByIdentificationCard(identificationCardEntity);

      this.identificationCardScanRepository.delete(cardScanEntities);

      this.identificationCardRepository.delete(identificationCardEntity);

      final CustomerEntity customerEntity = identificationCardEntity.getCustomer();

      customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
      customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

      this.customerRepository.save(customerEntity);
    });

    return deleteIdentificationCardCommand.number();
  }

  @Transactional
  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_IDENTIFICATION_CARD_SCAN)
  public ScanEvent createIdentificationCardScan(final CreateIdentificationCardScanCommand command) throws Exception {
    final Optional<IdentificationCardEntity> identificationCardEntity = this.identificationCardRepository.findByNumber(command.number());

    final IdentificationCardEntity cardEntity = identificationCardEntity.orElseThrow(() -> ServiceException.notFound("Identification card {0} not found.", command.number()));

    final IdentificationCardScanEntity identificationCardScanEntity = IdentificationCardScanMapper.map(command.scan());

    final MultipartFile image = command.image();

    final LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

    identificationCardScanEntity.setImage(image.getBytes());
    identificationCardScanEntity.setContentType(image.getContentType());
    identificationCardScanEntity.setSize(image.getSize());
    identificationCardScanEntity.setIdentificationCard(cardEntity);
    identificationCardScanEntity.setCreatedBy(UserContextHolder.checkedGetUser());
    identificationCardScanEntity.setCreatedOn(now);

    identificationCardScanRepository.save(identificationCardScanEntity);

    cardEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    cardEntity.setLastModifiedOn(now);

    identificationCardRepository.save(cardEntity);

    return new ScanEvent(command.number(), command.scan().getIdentifier());
  }

  @Transactional
  @CommandHandler(logStart = CommandLogLevel.INFO, logFinish = CommandLogLevel.INFO)
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.DELETE_IDENTIFICATION_CARD_SCAN)
  public ScanEvent deleteIdentificationCardScan(final DeleteIdentificationCardScanCommand command) {
    final Optional<IdentificationCardEntity> cardEntity = this.identificationCardRepository.findByNumber(command.number());
    final Optional<IdentificationCardScanEntity> scanEntity = cardEntity
            .flatMap(entity -> this.identificationCardScanRepository.findByIdentifierAndIdentificationCard(command.scanIdentifier(), entity));

    scanEntity.ifPresent(identificationCardScanEntity -> {

      this.identificationCardScanRepository.delete(identificationCardScanEntity);

      final IdentificationCardEntity identificationCard = identificationCardScanEntity.getIdentificationCard();

      identificationCard.setLastModifiedBy(UserContextHolder.checkedGetUser());
      identificationCard.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

      this.identificationCardRepository.save(identificationCard);
    });

    return new ScanEvent(command.number(), command.scanIdentifier());
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.POST_PORTRAIT)
  public String createPortrait(final CreatePortraitCommand createPortraitCommand) throws IOException {
    if(createPortraitCommand.portrait() == null) {
      return null;
    }

    final CustomerEntity customerEntity = findCustomerEntityOrThrow(createPortraitCommand.identifier());

    final PortraitEntity portraitEntity = PortraitMapper.map(createPortraitCommand.portrait());
    portraitEntity.setCustomer(customerEntity);
    this.portraitRepository.save(portraitEntity);

    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    this.customerRepository.save(customerEntity);

    return createPortraitCommand.identifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.DELETE_PORTRAIT)
  public String deletePortrait(final DeletePortraitCommand deletePortraitCommand) throws IOException {
    final CustomerEntity customerEntity = findCustomerEntityOrThrow(deletePortraitCommand.identifier());

    this.portraitRepository.deleteByCustomer(customerEntity);

    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    this.customerRepository.save(customerEntity);

    return deletePortraitCommand.identifier();
  }

  private void setCustomValues(final Customer customer, final CustomerEntity savedCustomerEntity) {
    this.fieldValueRepository.save(
        customer.getCustomValues()
            .stream()
            .map(value -> {
              final Optional<CatalogEntity> catalog =
                  this.catalogRepository.findByIdentifier(value.getCatalogIdentifier());
              final Optional<FieldEntity> field =
                  this.fieldRepository.findByCatalogAndIdentifier(
                      catalog.orElseThrow(() -> ServiceException.notFound("Catalog {0} not found.", value.getCatalogIdentifier())),
                      value.getFieldIdentifier());
              final FieldValueEntity fieldValueEntity = FieldValueMapper.map(value);
              fieldValueEntity.setCustomer(savedCustomerEntity);
              fieldValueEntity.setField(
                  field.orElseThrow(() -> ServiceException.notFound("Field {0} not found.", value.getFieldIdentifier())));
              return fieldValueEntity;
            })
            .collect(Collectors.toList())
    );
  }

  private CustomerEntity findCustomerEntityOrThrow(String identifier) {
    return this.customerRepository.findByIdentifier(identifier)
        .orElseThrow(() -> ServiceException.notFound("Customer ''{0}'' not found", identifier));
  }
}
