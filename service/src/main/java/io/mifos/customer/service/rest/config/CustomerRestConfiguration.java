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
package io.mifos.customer.service.rest.config;

import io.mifos.anubis.config.EnableAnubis;
import io.mifos.core.async.config.EnableAsync;
import io.mifos.core.cassandra.config.EnableCassandra;
import io.mifos.core.command.config.EnableCommandProcessing;
import io.mifos.core.lang.ApplicationName;
import io.mifos.core.lang.config.EnableApplicationName;
import io.mifos.core.lang.config.EnableServiceException;
import io.mifos.core.lang.config.EnableTenantContext;
import io.mifos.customer.catalog.service.rest.config.CatalogRestConfiguration;
import io.mifos.customer.service.ServiceConstants;
import io.mifos.customer.service.internal.config.CustomerServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@EnableAsync
@EnableTenantContext
@EnableCassandra
@EnableCommandProcessing
@EnableAnubis
@EnableServiceException
@EnableApplicationName
@ComponentScan({
    "io.mifos.customer.service.rest.controller"
})
@Import({
    CatalogRestConfiguration.class,
    CustomerServiceConfiguration.class
})
public class CustomerRestConfiguration extends WebMvcConfigurerAdapter {

  public CustomerRestConfiguration() {
    super();
  }

  @Bean(name = ServiceConstants.LOGGER_NAME)
  public Logger logger(final ApplicationName applicationName) {
    return LoggerFactory.getLogger(applicationName.getServiceName());
  }

  @Override
  public void configurePathMatch(final PathMatchConfigurer configurer) {
    configurer.setUseSuffixPatternMatch(Boolean.FALSE);
  }
}
