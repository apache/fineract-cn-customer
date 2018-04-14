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
package org.apache.fineract.cn.customer.catalog.listener;

import org.apache.fineract.cn.customer.api.v1.CustomerEventConstants;
import org.apache.fineract.cn.customer.catalog.api.v1.CatalogEventConstants;
import org.apache.fineract.cn.lang.config.TenantHeaderFilter;
import org.apache.fineract.cn.test.listener.EventRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class CatalogEventListener {
  private final EventRecorder eventRecorder;

  @Autowired
  public CatalogEventListener(final EventRecorder eventRecorder) {
    super();
    this.eventRecorder = eventRecorder;
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CatalogEventConstants.SELECTOR_POST_CATALOG
  )
  public void customerCreatedEvent(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                                   final String payload) {
    this.eventRecorder.event(tenant, CatalogEventConstants.POST_CATALOG, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CatalogEventConstants.SELECTOR_DELETE_CATALOG
  )
  public void onDeleteCatalog(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                              final String payload) {
    this.eventRecorder.event(tenant, CatalogEventConstants.DELETE_CATALOG, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CatalogEventConstants.SELECTOR_DELETE_FIELD
  )
  public void onDeleteField(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                            final String payload) {
    this.eventRecorder.event(tenant, CatalogEventConstants.DELETE_FIELD, payload, String.class);
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CatalogEventConstants.SELECTOR_PUT_FIELD
  )
  public void onChangeField(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                            final String payload) {
    this.eventRecorder.event(tenant, CatalogEventConstants.PUT_FIELD, payload, String.class);
  }
}
