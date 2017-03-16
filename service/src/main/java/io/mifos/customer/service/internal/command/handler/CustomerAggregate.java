/*
 * Copyright 2017 The Mifos Initiative
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
package io.mifos.customer.service.internal.command.handler;

import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.annotation.Aggregate;
import io.mifos.core.command.annotation.CommandHandler;
import io.mifos.core.command.annotation.EventEmitter;
import io.mifos.core.lang.ServiceException;
import io.mifos.customer.api.v1.CustomerEventConstants;
import io.mifos.customer.api.v1.domain.Command;
import io.mifos.customer.api.v1.domain.Customer;
import io.mifos.customer.catalog.service.internal.repository.CatalogEntity;
import io.mifos.customer.catalog.service.internal.repository.CatalogRepository;
import io.mifos.customer.catalog.service.internal.repository.FieldEntity;
import io.mifos.customer.catalog.service.internal.repository.FieldRepository;
import io.mifos.customer.catalog.service.internal.repository.FieldValueEntity;
import io.mifos.customer.catalog.service.internal.repository.FieldValueRepository;
import io.mifos.customer.service.internal.command.ActivateCustomerCommand;
import io.mifos.customer.service.internal.command.CloseCustomerCommand;
import io.mifos.customer.service.internal.command.CreateCustomerCommand;
import io.mifos.customer.service.internal.command.LockCustomerCommand;
import io.mifos.customer.service.internal.command.ReopenCustomerCommand;
import io.mifos.customer.service.internal.command.UnlockCustomerCommand;
import io.mifos.customer.service.internal.command.UpdateAddressCommand;
import io.mifos.customer.service.internal.command.UpdateContactDetailsCommand;
import io.mifos.customer.service.internal.command.UpdateCustomerCommand;
import io.mifos.customer.service.internal.command.UpdateIdentificationCardCommand;
import io.mifos.customer.service.internal.mapper.AddressMapper;
import io.mifos.customer.service.internal.mapper.CommandMapper;
import io.mifos.customer.service.internal.mapper.ContactDetailMapper;
import io.mifos.customer.service.internal.mapper.CustomerMapper;
import io.mifos.customer.service.internal.mapper.FieldValueMapper;
import io.mifos.customer.service.internal.mapper.IdentificationCardMapper;
import io.mifos.customer.service.internal.repository.AddressEntity;
import io.mifos.customer.service.internal.repository.AddressRepository;
import io.mifos.customer.service.internal.repository.CommandRepository;
import io.mifos.customer.service.internal.repository.ContactDetailEntity;
import io.mifos.customer.service.internal.repository.ContactDetailRepository;
import io.mifos.customer.service.internal.repository.CustomerEntity;
import io.mifos.customer.service.internal.repository.CustomerRepository;
import io.mifos.customer.service.internal.repository.IdentificationCardEntity;
import io.mifos.customer.service.internal.repository.IdentificationCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Aggregate
public class CustomerAggregate {

  private final AddressRepository addressRepository;
  private final CustomerRepository customerRepository;
  private final IdentificationCardRepository identificationCardRepository;
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

    if (customer.getIdentificationCard() != null) {
      final IdentificationCardEntity identificationCardEntity = IdentificationCardMapper.map(customer.getIdentificationCard());
      identificationCardEntity.setCustomer(savedCustomerEntity);
      this.identificationCardRepository.save(identificationCardEntity);
    }

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

    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(customer.getIdentifier());

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
      this.setCustomValues(customer, customerEntity);
    }

    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    this.customerRepository.save(customerEntity);

    return customer.getIdentifier();
  }

  @Transactional
  @CommandHandler
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.ACTIVATE_CUSTOMER)
  public String activateCustomer(final ActivateCustomerCommand activateCustomerCommand) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(activateCustomerCommand.identifier());

    if (this.taskAggregate.openTasksForCustomerExist(customerEntity)) {
      throw ServiceException.conflict("Open Tasks for customer {0} exists.", activateCustomerCommand.identifier());
    }

    customerEntity.setCurrentState(Customer.State.ACTIVE.name());
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
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(lockCustomerCommand.identifier());
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
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(unlockCustomerCommand.identifier());

    if (this.taskAggregate.openTasksForCustomerExist(customerEntity)) {
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
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(closeCustomerCommand.identifier());
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
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(reopenCustomerCommand.identifier());

    if (this.taskAggregate.openTasksForCustomerExist(customerEntity)) {
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
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(updateAddressCommand.identifier());
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
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(updateContactDetailsCommand.identifier());
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
  @EventEmitter(selectorName = CustomerEventConstants.SELECTOR_NAME, selectorValue = CustomerEventConstants.PUT_IDENTIFICATION_CARD)
  public String updateIdentificationCard(final UpdateIdentificationCardCommand updateIdentificationCardCommand) {
    final CustomerEntity customerEntity = this.customerRepository.findByIdentifier(updateIdentificationCardCommand.identifier());
    customerEntity.setLastModifiedBy(UserContextHolder.checkedGetUser());
    customerEntity.setLastModifiedOn(LocalDateTime.now(Clock.systemUTC()));

    final IdentificationCardEntity oldIdentificationCard = this.identificationCardRepository.findByCustomer(customerEntity);
    if (oldIdentificationCard != null) {
      this.identificationCardRepository.delete(oldIdentificationCard);
    }

    if (updateIdentificationCardCommand.identificationCard() != null) {
      final IdentificationCardEntity identificationCardEntity = IdentificationCardMapper.map(
          updateIdentificationCardCommand.identificationCard());
      identificationCardEntity.setCustomer(customerEntity);
      this.identificationCardRepository.save(identificationCardEntity);
    }

    return updateIdentificationCardCommand.identifier();
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
}
