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

import org.apache.fineract.cn.customer.api.v1.domain.IdentificationCardScan;
import org.springframework.web.multipart.MultipartFile;

public class CreateIdentificationCardScanCommand {

  private final String number;

  private final IdentificationCardScan scan;

  private final MultipartFile image;

  public CreateIdentificationCardScanCommand(final String number, final IdentificationCardScan scan, final MultipartFile image) {
    this.number = number;
    this.scan = scan;
    this.image = image;
  }

  public String number () {
    return number;
  }

  public IdentificationCardScan scan() {
    return scan;
  }

  public MultipartFile image() { return image; }

  @Override
  public String toString() {
    return "CreateIdentificationCardScanCommand{" +
            "number='" + number + '\'' +
            ", scan=" + scan +
            '}';
  }
}
