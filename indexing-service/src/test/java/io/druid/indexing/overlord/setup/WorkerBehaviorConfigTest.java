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

package io.druid.indexing.overlord.setup;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.druid.indexing.overlord.autoscaling.ec2.EC2AutoScaler;
import io.druid.indexing.overlord.autoscaling.ec2.EC2EnvironmentConfig;
import io.druid.indexing.overlord.autoscaling.ec2.EC2NodeData;
import io.druid.indexing.overlord.autoscaling.ec2.StringEC2UserData;
import io.druid.jackson.DefaultObjectMapper;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkerBehaviorConfigTest
{

  private AmazonEC2Client amazonEC2Client;
  private InjectableValues mockInjectableValues;
  private static final Map<String, String> tagMap;
  static
  {
    tagMap = new HashMap<>();
    tagMap.put("role", "production");
  }
  private static final List<Map<String, String>> TAGS = Collections.singletonList(tagMap);

  @Before
  public void setUp() throws Exception
  {
    amazonEC2Client = EasyMock.createMock(AmazonEC2Client.class);
    mockInjectableValues = new InjectableValues()
    {
      @Override
      public Object findInjectableValue(
          Object valueId, DeserializationContext ctxt, BeanProperty forProperty, Object beanInstance
      )
      {
        if (valueId == "com.amazonaws.services.ec2.AmazonEC2") {
          return amazonEC2Client;
        } else {
          return null;
        }
      }
    };
  }

  @Test
  public void testSerde() throws Exception
  {
    WorkerBehaviorConfig config = new WorkerBehaviorConfig(
        new FillCapacityWithAffinityWorkerSelectStrategy(
            new FillCapacityWithAffinityConfig(
                ImmutableMap.of("foo", Arrays.asList("localhost"))
            )
        ),
        new EC2AutoScaler(
            7,
            11,
            new EC2EnvironmentConfig(
                "us-east-1",
                "us-east-1a",
                new EC2NodeData(
                    "amiid",
                    "instanceType",
                    3,
                    5,
                    Arrays.asList("securityGroupIds"),
                    "keyNames",
                    "subnetId",
                    null,
                    null,
                    TAGS
                ),
                new StringEC2UserData(
                    "availZone",
                    "replace",
                    "version"
                )
            ),
            amazonEC2Client,
            null
        )
    );

    final ObjectMapper mapper = new DefaultObjectMapper();
    mapper.setInjectableValues(
        mockInjectableValues
    );
    Assert.assertEquals(config, mapper.readValue(mapper.writeValueAsBytes(config), WorkerBehaviorConfig.class));
    EasyMock.expectLastCall().once();
  }

  @Test
  public void testSerdeLegacyConfig() throws Exception
  {
    WorkerBehaviorConfig config = new WorkerBehaviorConfig(
        new FillCapacityWithAffinityWorkerSelectStrategy(
            new FillCapacityWithAffinityConfig(
                ImmutableMap.of("foo", Arrays.asList("localhost"))
            )
        ),
        new EC2AutoScaler(
            7,
            11,
            new EC2EnvironmentConfig(
                null,
                "us-east-1a",
                new EC2NodeData(
                    "amiid",
                    "instanceType",
                    3,
                    5,
                    Arrays.asList("securityGroupIds"),
                    "keyNames",
                    "subnetId",
                    null,
                    null,
                    null
                ),
                new StringEC2UserData(
                    "availZone",
                    "replace",
                    "version"
                )
            ),
            amazonEC2Client,
            null
        )
    );

    final ObjectMapper mapper = new DefaultObjectMapper();
    mapper.setInjectableValues(
        mockInjectableValues
    );
    Assert.assertEquals(config, mapper.readValue(mapper.writeValueAsBytes(config), WorkerBehaviorConfig.class));
  }
}
