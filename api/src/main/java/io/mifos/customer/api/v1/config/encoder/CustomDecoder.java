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

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import feign.gson.GsonDecoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;

@Component
public class CustomDecoder implements Decoder {

  private final Decoder defaultDecoder;
  private final GsonDecoder gsonDecoder;

  public CustomDecoder(final GsonDecoder gsonDecoder) {
    this.gsonDecoder = gsonDecoder;
    this.defaultDecoder = new Decoder.Default();
  }

  @Override
  public Object decode(Response response, Type type) throws IOException, FeignException {
    if (byte[].class.equals(type)) {
      return this.defaultDecoder.decode(response, type);
    }

    return this.gsonDecoder.decode(response, type);
  }

}
