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

@SuppressWarnings({"WeakerAccess", "unused"})
@Component
public class MigrationEventListener {

  private final EventRecorder eventRecorder;

  @Autowired
  public MigrationEventListener(final EventRecorder eventRecorder) {
    super();
    this.eventRecorder = eventRecorder;
  }

  @JmsListener(
      destination = CustomerEventConstants.DESTINATION,
      selector = CustomerEventConstants.SELECTOR_INITIALIZE
  )
  public void initialized(@Header(TenantHeaderFilter.TENANT_HEADER) final String tenant,
                          final String payload) {
    this.eventRecorder.event(tenant, CustomerEventConstants.INITIALIZE, payload, String.class);
  }
}
