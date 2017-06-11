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
package io.mifos.customer.listener;

import io.mifos.core.lang.config.TenantHeaderFilter;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.customer.api.v1.CustomerEventConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventListener {

  private final EventRecorder eventRecorder;

  @Autowired
  public CustomerEventListener(final EventRecorder eventRecorder) {
    super();
    this.eventRecorder = eventRecorder;
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_POST_CUSTOMER
  )
  public void customerCreatedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                   final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.POST_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_PUT_CUSTOMER
  )
  public void customerUpdatedEvents(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                    final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.PUT_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_ACTIVATE_CUSTOMER
  )
  public void customerActivatedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                     final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.ACTIVATE_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_LOCK_CUSTOMER
  )
  public void customerLockedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                  final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.LOCK_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_UNLOCK_CUSTOMER
  )
  public void customerUnlockedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                    final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.UNLOCK_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_CLOSE_CUSTOMER
  )
  public void customerClosedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                  final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.CLOSE_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_REOPEN_CUSTOMER
  )
  public void customerReopenedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                    final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.REOPEN_CUSTOMER, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_PUT_ADDRESS
  )
  public void addressChangedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                  final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.PUT_ADDRESS, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_PUT_CONTACT_DETAILS
  )
  public void contactDetailsChangedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                  final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.PUT_CONTACT_DETAILS, payload, String.class);
  }

  @JmsListener(
          destination = CustomerEventConstants.DESTINATION,
          selector = CustomerEventConstants.SELECTOR_POST_IDENTIFICATION_CARD
  )
  public void identificationCardCreateEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                             final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.POST_IDENTIFICATION_CARD, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_PUT_IDENTIFICATION_CARD
  )
  public void identificationCardChangedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                             final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.PUT_IDENTIFICATION_CARD, payload, String.class);
  }

  @JmsListener(
          destination = CustomerEventConstants.DESTINATION,
          selector = CustomerEventConstants.SELECTOR_DELETE_IDENTIFICATION_CARD
  )
  public void identificationCardDeletedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                             final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.DELETE_IDENTIFICATION_CARD, payload, String.class);
  }

  @JmsListener(
          destination = CustomerEventConstants.DESTINATION,
          selector = CustomerEventConstants.SELECTOR_PUT_PORTRAIT
  )
  public void portraitPutEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                             final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.POST_PORTRAIT, payload, String.class);
  }

  @JmsListener(
          destination = CustomerEventConstants.DESTINATION,
          selector = CustomerEventConstants.SELECTOR_DELETE_PORTRAIT
  )
  public void portraitDeleteEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                               final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.DELETE_PORTRAIT, payload, String.class);
  }
}
