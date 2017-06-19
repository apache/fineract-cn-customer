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
package io.mifos.customer.catalog.util;

import io.mifos.customer.catalog.api.v1.domain.Catalog;
import io.mifos.customer.catalog.api.v1.domain.Field;
import io.mifos.customer.catalog.api.v1.domain.Option;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;

public class CatalogGenerator {

  private CatalogGenerator() {
    super();
  }

  public static Catalog createRandomCatalog() {
    final Catalog catalog = new Catalog();
    catalog.setIdentifier(RandomStringUtils.randomAlphanumeric(32));
    catalog.setName(RandomStringUtils.randomAlphanumeric(256));
    catalog.setDescription(RandomStringUtils.randomAlphanumeric(4096));

    final Field simpleField = new Field();
    simpleField.setDataType(Field.DataType.NUMBER.name());
    simpleField.setIdentifier(RandomStringUtils.randomAlphanumeric(32));
    simpleField.setLabel(RandomStringUtils.randomAlphanumeric(256));
    simpleField.setHint(RandomStringUtils.randomAlphanumeric(512));
    simpleField.setDescription(RandomStringUtils.randomAlphanumeric(4096));
    simpleField.setMandatory(Boolean.FALSE);
    simpleField.setLength(10);
    simpleField.setPrecision(2);
    simpleField.setMinValue(0.00D);
    simpleField.setMaxValue(99999999.99D);

    final Field optionField = new Field();
    optionField.setDataType(Field.DataType.SINGLE_SELECTION.name());
    optionField.setIdentifier(RandomStringUtils.randomAlphanumeric(32));
    optionField.setLabel(RandomStringUtils.randomAlphanumeric(256));
    optionField.setHint(RandomStringUtils.randomAlphanumeric(512));
    optionField.setDescription(RandomStringUtils.randomAlphanumeric(4096));
    optionField.setMandatory(Boolean.FALSE);
    final Option option = new Option();
    option.setLabel(RandomStringUtils.randomAlphanumeric(256));
    option.setValue(1);
    optionField.setOptions(Arrays.asList(option));

    catalog.setFields(Arrays.asList(simpleField, optionField));
    return catalog;
  }
}
