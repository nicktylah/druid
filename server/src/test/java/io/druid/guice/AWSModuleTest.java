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

import com.amazonaws.services.ec2.AmazonEC2Client;
import io.druid.common.aws.AWSCredentialsConfig;
import io.druid.common.aws.AWSCredentialsUtils;
import io.druid.common.aws.ec2.EC2EnvironmentConfig;
import org.easymock.EasyMock;
import org.junit.Test;


public class AWSModuleTest
{

  @Test
  public void testGetEC2ClientSetsRegion()
  {
    EC2RegionSetter ec2RegionSetter = EasyMock.createMock(EC2RegionSetter.class);
    AWSModule awsModule = new AWSModule();

    ec2RegionSetter.setRegion(
        EasyMock.anyObject(AmazonEC2Client.class),
        EasyMock.matches("us-west-2"));
    EasyMock.expectLastCall().once();
    EasyMock.replay(ec2RegionSetter);

    awsModule.getEC2Client(
        AWSCredentialsUtils.defaultAWSCredentialsProviderChain(new AWSCredentialsConfig()),
        ec2RegionSetter,
        new EC2EnvironmentConfig("us-west-2", "", null, null));
    EasyMock.verify(ec2RegionSetter);
  }

  @Test
  public void testGetEC2ClientDoesNotSetRegionIfNull()
  {
    EC2RegionSetter ec2RegionSetter = EasyMock.createMock(EC2RegionSetter.class);
    AWSModule awsModule = new AWSModule();

    EasyMock.replay(ec2RegionSetter);

    awsModule.getEC2Client(
        AWSCredentialsUtils.defaultAWSCredentialsProviderChain(new AWSCredentialsConfig()),
        ec2RegionSetter,
        new EC2EnvironmentConfig(null, "", null, null));
    EasyMock.verify(ec2RegionSetter);
  }
}
