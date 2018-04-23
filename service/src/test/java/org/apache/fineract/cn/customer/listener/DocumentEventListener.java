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
package org.apache.fineract.cn.customer.listener;

import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.api.v1.events.DocumentEvent;
import org.apache.fineract.cn.customer.api.v1.events.DocumentPageEvent;
import org.apache.fineract.cn.lang.config.TenantHeaderFilter;
import org.apache.fineract.cn.test.listener.EventRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Myrle Krantz
 */
@Component
public class DocumentEventListener {

  private final EventRecorder eventRecorder;

  @Autowired
  public DocumentEventListener(final EventRecorder eventRecorder) {
    super();
    this.eventRecorder = eventRecorder;
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_POST_DOCUMENT
  )
  public void postDocumentEvent(
      @Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
      final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.POST_DOCUMENT, payload, DocumentEvent.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_PUT_DOCUMENT
  )
  public void putDocumentEvent(
      @Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
      final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.PUT_DOCUMENT, payload, DocumentEvent.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_DELETE_DOCUMENT
  )
  public void deleteDocumentEvent(
      @Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
      final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.DELETE_DOCUMENT, payload, DocumentEvent.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_POST_DOCUMENT_PAGE
  )
  public void postDocumentPageEvent(
      @Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
      final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.POST_DOCUMENT_PAGE, payload, DocumentPageEvent.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_DELETE_DOCUMENT_PAGE
  )
  public void deleteDocumentPageEvent(
      @Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
      final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.DELETE_DOCUMENT_PAGE, payload, DocumentPageEvent.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_POST_DOCUMENT_COMPLETE
  )
  public void postDocumentComplete(
      @Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
      final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.POST_DOCUMENT_COMPLETE, payload, DocumentEvent.class);
  }
}
