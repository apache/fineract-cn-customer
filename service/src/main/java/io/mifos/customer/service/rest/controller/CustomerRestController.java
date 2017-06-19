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
package io.mifos.customer.service.rest.controller;

import io.mifos.anubis.annotation.AcceptedTokenType;
import io.mifos.anubis.annotation.Permittable;
import io.mifos.core.api.util.UserContextHolder;
import io.mifos.core.command.gateway.CommandGateway;
import io.mifos.core.lang.ServiceException;
import io.mifos.customer.PermittableGroupIds;
import io.mifos.customer.api.v1.domain.*;
import io.mifos.customer.catalog.service.internal.service.FieldValueValidator;
import io.mifos.customer.service.ServiceConstants;
import io.mifos.customer.service.internal.command.*;
import io.mifos.customer.service.internal.repository.PortraitEntity;
import io.mifos.customer.service.internal.service.CustomerService;
import io.mifos.customer.service.internal.service.TaskService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class CustomerRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final CustomerService customerService;
  private final FieldValueValidator fieldValueValidator;
  private final TaskService taskService;
  private final Environment environment;

  @Autowired
  public CustomerRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                final CommandGateway commandGateway,
                                final CustomerService customerService,
                                final FieldValueValidator fieldValueValidator,
                                final TaskService taskService,
                                final Environment environment) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.customerService = customerService;
    this.fieldValueValidator = fieldValueValidator;
    this.taskService = taskService;
    this.environment = environment;
  }

  @Permittable(value = AcceptedTokenType.SYSTEM)
  @RequestMapping(
      value = "/initialize",
      method = RequestMethod.POST,
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public
  ResponseEntity<Void>
  initialize() throws InterruptedException {
    this.commandGateway.process(new InitializeServiceCommand());
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> createCustomer(@RequestBody @Valid final Customer customer) throws InterruptedException {
    if (this.customerService.customerExists(customer.getIdentifier())) {
      throw ServiceException.conflict("Customer {0} already exists.", customer.getIdentifier());
    }

    if (customer.getCustomValues() != null) {
      this.fieldValueValidator.validateValues(customer.getCustomValues());
    }

    this.commandGateway.process(new CreateCustomerCommand(customer));
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<CustomerPage> fetchCustomers(@RequestParam(value = "term", required = false) final String term,
                                              @RequestParam(value = "includeClosed", required = false) final Boolean includeClosed,
                                              @RequestParam(value = "pageIndex", required = false) final Integer pageIndex,
                                              @RequestParam(value = "size", required = false) final Integer size,
                                              @RequestParam(value = "sortColumn", required = false) final String sortColumn,
                                              @RequestParam(value = "sortDirection", required = false) final String sortDirection) {
    return ResponseEntity.ok(this.customerService.fetchCustomer(
        term, (includeClosed != null ? includeClosed : Boolean.FALSE),
        this.createPageRequest(pageIndex, size, sortColumn, sortDirection)));
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Customer> findCustomer(@PathVariable("identifier") final String identifier) {
    final Optional<Customer> customer = this.customerService.findCustomer(identifier);
    if (customer.isPresent()) {
      return ResponseEntity.ok(customer.get());
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> updateCustomer(@PathVariable("identifier") final String identifier,
                                      @RequestBody final Customer customer) {
    if (this.customerService.customerExists(identifier)) {
      if (customer.getCustomValues() != null) {
        this.fieldValueValidator.validateValues(customer.getCustomValues());
      }
      this.commandGateway.process(new UpdateCustomerCommand(customer));
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/commands",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> customerCommand(@PathVariable("identifier") final String identifier,
                                       @RequestBody final Command command) {
    final Optional<Customer> customerOptional = this.customerService.findCustomer(identifier);
    if (customerOptional.isPresent()) {
      final Customer customer = customerOptional.get();
      final Command.Action action = Command.Action.valueOf(command.getAction());
      switch (action) {
        case ACTIVATE:
          if (!customer.getCurrentState().equals(Customer.State.PENDING.name())) {
            throw ServiceException.badRequest(
                "Customer {0} can not be activated, current state is {1}.",
                identifier,
                customer.getCurrentState());
          }
          this.commandGateway.process(new ActivateCustomerCommand(identifier, command.getComment()));
          break;
        case LOCK:
          if (!customer.getCurrentState().equals(Customer.State.ACTIVE.name())) {
            throw ServiceException.badRequest(
                "Customer {0} can not be locked, current state is {1}.",
                identifier,
                customer.getCurrentState());
          }
          this.commandGateway.process(new LockCustomerCommand(identifier, command.getComment()));
          break;
        case UNLOCK:
          if (!customer.getCurrentState().equals(Customer.State.LOCKED.name())) {
            throw ServiceException.badRequest(
                "Customer {0} can not be unlocked, current state is {1}.",
                identifier,
                customer.getCurrentState());
          }
          this.commandGateway.process(new UnlockCustomerCommand(identifier, command.getComment()));
          break;
        case CLOSE:
          if (!customer.getCurrentState().equals(Customer.State.ACTIVE.name())
              && !customer.getCurrentState().equals(Customer.State.LOCKED.name())) {
            throw ServiceException.badRequest(
                "Customer {0} can not be closed, current state is {1}.",
                identifier,
                customer.getCurrentState());
          }
          this.commandGateway.process(new CloseCustomerCommand(identifier, command.getComment()));
          break;
        case REOPEN:
          if (!customer.getCurrentState().equals(Customer.State.CLOSED.name())) {
            throw ServiceException.badRequest(
                "Customer {0} can not be reopened, current state is {1}.",
                identifier,
                customer.getCurrentState());
          }
          this.commandGateway.process(new ReopenCustomerCommand(identifier, command.getComment()));
          break;
        default:
          throw ServiceException.badRequest("Unsupported action {0}.", command.getAction());
      }
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/commands",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<List<Command>> fetchCustomerCommands(@PathVariable("identifier") final String identifier) {
    if (this.customerService.customerExists(identifier)) {
      return ResponseEntity.ok(this.customerService.fetchCommandsByCustomer(identifier));
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/tasks/{taskIdentifier}",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> addTaskToCustomer(@PathVariable("identifier") final String identifier,
                                         @PathVariable("taskIdentifier") final String taskIdentifier) {
    if (this.customerService.customerExists(identifier)) {
      if (this.taskService.taskDefinitionExists(taskIdentifier)) {
        this.commandGateway.process(new AddTaskDefinitionToCustomerCommand(identifier, taskIdentifier));
      } else {
        throw ServiceException.notFound("Task definition {0} not found.", taskIdentifier);
      }
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/tasks/{taskIdentifier}",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> taskForCustomerExecuted(@PathVariable("identifier") final String identifier,
                                               @PathVariable("taskIdentifier") final String taskIdentifier) {
    if (this.customerService.customerExists(identifier)) {
      if (this.taskService.taskDefinitionExists(taskIdentifier)) {
        final TaskDefinition taskDefinition = this.taskService.findByIdentifier(taskIdentifier).get();
        final Customer customer;
        switch (TaskDefinition.Type.valueOf(taskDefinition.getType())) {
          case ID_CARD:
            final List<IdentificationCard> identificationCards = this.customerService.fetchIdentificationCardsByCustomer(identifier);
            if (identificationCards.isEmpty()) {
              throw ServiceException.conflict("No identification cards for customer found.");
            }
            break;
          case FOUR_EYES:
            customer = this.customerService.findCustomer(identifier).get();
            if (customer.getAssignedEmployee().equals(UserContextHolder.checkedGetUser())) {
              throw ServiceException.conflict("Signing user must be different than creator.");
            }
            break;
        }
        this.commandGateway.process(new ExecuteTaskForCustomerCommand(identifier, taskIdentifier));
      } else {
        throw ServiceException.notFound("Task definition {0} not found.", taskIdentifier);
      }
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/tasks",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<List<TaskDefinition>> findTasksForCustomer(@PathVariable("identifier") final String identifier,
                                                            @RequestParam(value = "includeExecuted", required = false) final Boolean includeExecuted) {
    if (this.customerService.customerExists(identifier)) {
      return ResponseEntity.ok(this.taskService.findTasksByCustomer(identifier, (includeExecuted != null ? includeExecuted : Boolean.FALSE)));
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/address",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> putAddress(@PathVariable("identifier") final String identifier,
                                  @RequestBody @Valid final Address address) {
    if (this.customerService.customerExists(identifier)) {
      this.commandGateway.process(new UpdateAddressCommand(identifier, address));
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CUSTOMER)
  @RequestMapping(
      value = "/customers/{identifier}/contact",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> putContactDetails(@PathVariable("identifier") final String identifier,
                                         @RequestBody final List<ContactDetail> contactDetails) {
    if (this.customerService.customerExists(identifier)) {
      this.commandGateway.process(new UpdateContactDetailsCommand(identifier, contactDetails));
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTIFICATIONS)
  @RequestMapping(
          value = "/customers/{identifier}/identifications",
          method = RequestMethod.GET,
          produces = MediaType.APPLICATION_JSON_VALUE,
          consumes = MediaType.ALL_VALUE
  )
  public @ResponseBody ResponseEntity<List<IdentificationCard>> fetchIdentificationCards(@PathVariable("identifier") final String identifier) {
    this.throwIfCustomerNotExists(identifier);
    return ResponseEntity.ok(this.customerService.fetchIdentificationCardsByCustomer(identifier));
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTIFICATIONS)
  @RequestMapping(
          value = "/customers/{identifier}/identifications/{number}",
          method = RequestMethod.GET,
          produces = MediaType.APPLICATION_JSON_VALUE,
          consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<IdentificationCard> findIdentificationCard(@PathVariable("identifier") final String identifier,
                                            @PathVariable("number") final String number) {
    this.throwIfCustomerNotExists(identifier);

    final Optional<IdentificationCard> identificationCard = this.customerService.findIdentificationCard(number);
    if (identificationCard.isPresent()) {
      return ResponseEntity.ok(identificationCard.get());
    } else {
      throw ServiceException.notFound("Identification card {0} not found.", number);
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTIFICATIONS)
  @RequestMapping(
          value = "/customers/{identifier}/identifications",
          method = RequestMethod.POST,
          produces = MediaType.APPLICATION_JSON_VALUE,
          consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> createIdentificationCard(@PathVariable("identifier") final String identifier,
                                @RequestBody @Valid final IdentificationCard identificationCard) {
    if (this.customerService.customerExists(identifier)) {
      if (this.customerService.identificationCardExists(identificationCard.getNumber())) {
        throw ServiceException.conflict("IdentificationCard {0} already exists.", identificationCard.getNumber());
      }

      this.commandGateway.process(new CreateIdentificationCardCommand(identifier, identificationCard));
    } else {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTIFICATIONS)
  @RequestMapping(
      value = "/customers/{identifier}/identifications/{number}",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> updateIdentificationCard(@PathVariable("identifier") final String identifier,
                                                @PathVariable("number") final String number,
                                                @RequestBody @Valid final IdentificationCard identificationCard) {
    this.throwIfCustomerNotExists(identifier);
    this.throwIfIdentificationCardNotExists(number);

    if(!number.equals(identificationCard.getNumber())) {
      throw ServiceException.badRequest("Number in path is different from number in request body");
    }

    this.commandGateway.process(new UpdateIdentificationCardCommand(identifier, identificationCard.getNumber(), identificationCard));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.IDENTIFICATIONS)
  @RequestMapping(
          value = "/customers/{identifier}/identifications/{number}",
          method = RequestMethod.DELETE,
          produces = MediaType.APPLICATION_JSON_VALUE,
          consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> deleteIdentificationCard(@PathVariable("identifier") final String identifier,
                                @PathVariable("number") final String number) {
    this.throwIfCustomerNotExists(identifier);

    this.commandGateway.process(new DeleteIdentificationCardCommand(number));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.PORTRAIT)
  @RequestMapping(
      value = "/customers/{identifier}/portrait",
      method = RequestMethod.GET,
      consumes = MediaType.ALL_VALUE
  )
  public ResponseEntity<byte[]> getPortrait(@PathVariable("identifier") final String identifier) {
    this.throwIfPortraitNotExists(identifier);

    final PortraitEntity portrait = this.customerService.findPortrait(identifier);

    return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(portrait.getContentType()))
            .contentLength(portrait.getImage().length)
            .body(portrait.getImage());
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.PORTRAIT)
  @RequestMapping(
      value = "/customers/{identifier}/portrait",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public @ResponseBody ResponseEntity<Void> postPortrait(@PathVariable("identifier") final String identifier,
                                          @RequestBody final MultipartFile portrait) {
    if(portrait == null) {
      throw ServiceException.badRequest("Portrait not found");
    }

    this.throwIfCustomerNotExists(identifier);

    final Long maxSize = this.environment.getProperty("upload.image.max-size", Long.class);

    if(portrait.getSize() > maxSize) {
      throw ServiceException.badRequest("Portrait can't exceed size of {0}", maxSize);
    }

    if(!portrait.getContentType().contains(MediaType.IMAGE_JPEG_VALUE)
            && !portrait.getContentType().contains(MediaType.IMAGE_PNG_VALUE)) {
      throw ServiceException.badRequest("Only content type {0} and {1} allowed", MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE);
    }

    try {
      this.commandGateway.process(new DeletePortraitCommand(identifier), String.class).get();
    } catch (Throwable e) {
      logger.warn("Could not delete portrait: {0}", e.getMessage());
    }

    this.commandGateway.process(new CreatePortraitCommand(identifier, portrait));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.PORTRAIT)
  @RequestMapping(
      value = "/customers/{identifier}/portrait",
      method = RequestMethod.DELETE,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public @ResponseBody ResponseEntity<Void> deletePortrait(@PathVariable("identifier") final String identifier) {
    this.commandGateway.process(new DeletePortraitCommand(identifier));

    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TASK)
  @RequestMapping(
      value = "/tasks",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> createTask(@RequestBody final TaskDefinition taskDefinition) {
    if (this.taskService.taskDefinitionExists(taskDefinition.getIdentifier())) {
      throw ServiceException.conflict("Task definition {0} already exists.", taskDefinition.getIdentifier());
    } else {
      this.commandGateway.process(new CreateTaskDefinitionCommand(taskDefinition));
    }
    return ResponseEntity.accepted().build();
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TASK)
  @RequestMapping(
      value = "/tasks",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<List<TaskDefinition>> fetchAllTasks() {
    return ResponseEntity.ok(this.taskService.fetchAll());
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TASK)
  @RequestMapping(
      value = "/tasks/{identifier}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.ALL_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<TaskDefinition> findTask(@PathVariable("identifier") final String identifier) {
    final Optional<TaskDefinition> taskDefinitionOptional = this.taskService.findByIdentifier(identifier);
    if (taskDefinitionOptional.isPresent()) {
      return ResponseEntity.ok(taskDefinitionOptional.get());
    } else {
      throw ServiceException.notFound("Task {0} not found.", identifier);
    }
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.TASK)
  @RequestMapping(
      value = "/tasks/{identifier}",
      method = RequestMethod.PUT,
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public
  @ResponseBody
  ResponseEntity<Void> updateTask(@PathVariable("identifier") final String identifier, @RequestBody final TaskDefinition taskDefinition) {
    if (this.taskService.taskDefinitionExists(identifier)) {
      this.commandGateway.process(new UpdateTaskDefinitionCommand(identifier, taskDefinition));
    } else {
      throw ServiceException.notFound("Task {0} not found.", identifier);
    }
    return ResponseEntity.accepted().build();
  }

  private Pageable createPageRequest(final Integer pageIndex, final Integer size, final String sortColumn, final String sortDirection) {
    final Integer pageIndexToUse = pageIndex != null ? pageIndex : 0;
    final Integer sizeToUse = size != null ? size : 20;
    final String sortColumnToUse = sortColumn != null ? sortColumn : "identifier";
    final Sort.Direction direction = sortDirection != null ? Sort.Direction.valueOf(sortDirection.toUpperCase()) : Sort.Direction.ASC;
    return new PageRequest(pageIndexToUse, sizeToUse, direction, sortColumnToUse);
  }

  private void throwIfCustomerNotExists(final String identifier) {
    if (!this.customerService.customerExists(identifier)) {
      throw ServiceException.notFound("Customer {0} not found.", identifier);
    }
  }

  private void throwIfPortraitNotExists(final String identifier) {
    if (!this.customerService.portraitExists(identifier)) {
      throw ServiceException.notFound("Portrait for Customer {0} not found.", identifier);
    }
  }

  private void throwIfIdentificationCardNotExists(final String number) {
    if (!this.customerService.identificationCardExists(number)) {
      throw ServiceException.notFound("Identification card {0} not found.", number);
    }
  }

}
