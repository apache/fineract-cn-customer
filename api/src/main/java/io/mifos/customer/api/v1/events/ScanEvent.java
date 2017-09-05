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
package io.mifos.customer.api.v1.events;

import java.util.Objects;

public class ScanEvent {

  private final String number;

  private final String scanIdentifier;

  public ScanEvent(final String number, final String scanIdentifier) {
    this.number = number;
    this.scanIdentifier = scanIdentifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScanEvent scanEvent = (ScanEvent) o;
    return Objects.equals(number, scanEvent.number) &&
            Objects.equals(scanIdentifier, scanEvent.scanIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number, scanIdentifier);
  }

  @Override
  public String toString() {
    return "ScanEvent{" +
            "number='" + number + '\'' +
            ", scanIdentifier='" + scanIdentifier + '\'' +
            '}';
  }
}
