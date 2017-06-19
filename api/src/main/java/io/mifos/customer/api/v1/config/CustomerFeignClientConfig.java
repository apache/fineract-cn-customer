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
package io.mifos.customer.api.v1.config;/*
 * Copyright 2016 The Mifos Initiative.
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

import feign.Feign;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import io.mifos.core.api.util.AnnotatedErrorDecoder;
import io.mifos.core.api.util.TenantedTargetInterceptor;
import io.mifos.core.api.util.TokenedTargetInterceptor;
import io.mifos.customer.api.v1.config.encoder.CustomDecoder;
import io.mifos.customer.api.v1.config.encoder.CustomEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

public class CustomerFeignClientConfig extends FeignClientsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TenantedTargetInterceptor tenantedTargetInterceptor() {
        return new TenantedTargetInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenedTargetInterceptor tokenedTargetInterceptor() {
        return new TokenedTargetInterceptor();
    }

    @Bean(
            name = {"api-logger"}
    )
    public Logger logger() {
        return LoggerFactory.getLogger("api-logger");
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public Feign.Builder feignBuilder(@Qualifier("api-logger") Logger logger) {
        return new CustomerFeignClientConfig.AnnotatedErrorDecoderFeignBuilder(logger);
    }

    private static class AnnotatedErrorDecoderFeignBuilder extends Feign.Builder {
        private final Logger logger;

        AnnotatedErrorDecoderFeignBuilder(Logger logger) {
            this.logger = logger;
        }

        public <T> T target(Target<T> target) {
            this.errorDecoder(new AnnotatedErrorDecoder(this.logger, target.type()));
            return this.build().newInstance(target);
        }
    }

    @Bean
    @Primary
    @Scope("prototype")
    public Encoder feignEncoder() {
      return new CustomEncoder(new GsonEncoder(), new SpringFormEncoder());
    }

    @Bean
    @Primary
    @Scope("prototype")
    public Decoder feignDecoder() {
        return new CustomDecoder(new GsonDecoder());
    }

}
