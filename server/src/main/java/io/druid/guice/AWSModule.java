/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.druid.guice;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.metamx.emitter.EmittingLogger;
import io.druid.common.aws.AWSCredentialsConfig;
import io.druid.common.aws.AWSCredentialsUtils;
import io.druid.common.aws.ec2.EC2EnvironmentConfig;

/**
 */
public class AWSModule implements Module
{
  private static final EmittingLogger log = new EmittingLogger(AWSModule.class);

  @Override
  public void configure(Binder binder)
  {
    JsonConfigProvider.bind(binder, "druid.s3", AWSCredentialsConfig.class);
  }

  @Provides
  @LazySingleton
  public AWSCredentialsProvider getAWSCredentialsProvider(final AWSCredentialsConfig config)
  {
    return AWSCredentialsUtils.defaultAWSCredentialsProviderChain(config);
  }

  @Provides
  @LazySingleton
  public AmazonEC2Client getEC2Client(
      AWSCredentialsProvider credentials,
      EC2RegionSetter ec2RegionSetter,
      @JsonProperty("envConfig") EC2EnvironmentConfig envConfig)
  {
    final AmazonEC2Client amazonEC2Client = new AmazonEC2Client(credentials);

    final String regionName = envConfig.getRegion();
    if (regionName != null) {
      try {
        ec2RegionSetter.setRegion(amazonEC2Client, regionName);
      } catch (IllegalArgumentException e) {
        log.warn(e, String.format("Invalid region: %s", regionName));
      }
    }
    return amazonEC2Client;
  }
}
