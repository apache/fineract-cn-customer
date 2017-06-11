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
package io.mifos.customer.service.internal.command;

import io.mifos.customer.api.v1.domain.IdentificationCard;

public class UpdateIdentificationCardCommand {

  private final String identifier;
  private final String number;
  private final IdentificationCard identificationCard;

  public UpdateIdentificationCardCommand(final String identifier, final String number, final IdentificationCard identificationCard) {
    super();
    this.identifier = identifier;
    this.number = number;
    this.identificationCard = identificationCard;
  }

  public String identifier() {
    return this.identifier;
  }

  public String number() {
    return this.number;
  }

  public IdentificationCard identificationCard() {
    return this.identificationCard;
  }
}
