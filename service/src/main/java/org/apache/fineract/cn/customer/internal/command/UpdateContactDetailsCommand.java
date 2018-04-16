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
package org.apache.fineract.cn.customer.internal.command;

import org.apache.fineract.cn.customer.api.v1.domain.ContactDetail;

import java.util.List;

public class UpdateContactDetailsCommand {

  private final String identifier;
  private final List<ContactDetail> contactDetails;

  public UpdateContactDetailsCommand(final String identifier, final List<ContactDetail> contactDetails) {
    super();
    this.identifier = identifier;
    this.contactDetails = contactDetails;
  }

  public String identifier() {
    return this.identifier;
  }

  public List<ContactDetail> contactDetails() {
    return this.contactDetails;
  }
}
