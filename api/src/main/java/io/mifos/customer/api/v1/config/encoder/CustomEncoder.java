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
package io.mifos.customer.api.v1.config.encoder;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.gson.GsonEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;

@Component
public class CustomEncoder implements Encoder {

  private final Encoder defaultEncoder;
  private final GsonEncoder gsonEncoder;
  private final SpringFormEncoder springFormEncoder;

  public CustomEncoder(final GsonEncoder gsonEncoder, final SpringFormEncoder springFormEncoder) {
    this.gsonEncoder = gsonEncoder;
    this.springFormEncoder = springFormEncoder;
    this.defaultEncoder = new Encoder.Default();
  }

  @Override
  public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
    if (bodyType.equals(MultipartFile.class)) {
      this.springFormEncoder.encode(object, bodyType, template);
    }else {
      this.gsonEncoder.encode(object, bodyType, template);
    }
  }
}
