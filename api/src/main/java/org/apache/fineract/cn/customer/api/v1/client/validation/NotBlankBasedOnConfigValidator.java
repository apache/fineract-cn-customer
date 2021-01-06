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
package org.apache.fineract.cn.customer.api.v1.client.validation;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class NotBlankBasedOnConfigValidator implements ConstraintValidator<NotBlankBasedOnConfig, Object> {
    private String fieldName;
    private String expectedFieldValue;

    @Value("${config.bypassNotNull}")
    private Boolean bypassMandatory;

    @Override
    public void initialize(NotBlankBasedOnConfig constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if(bypassMandatory)
            return true;
        return notBlank(value);
    }
    private boolean notBlank(@NotBlank Object vl){
        if(vl == null) return false;
        String value = (String) vl;
        if(StringUtils.isBlank(value)) return false;
        return true;
    }
}
