/*
 * File: InfrastructureAssemblyBuilderTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.bedrock.runtime;

/**
 * Tests for the {@link InfrastructureAssemblyBuilder} class.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class InfrastructureAssemblyBuilderTest
{
//  TODO: refactor to use options and remove schema
//      @Test
//      @SuppressWarnings("unchecked")
//      public void shouldRealizeAssemblyWithConsoleOverride() throws Exception
//      {
//          Platform           platform1  = mock(Platform.class, "P-1");
//          Platform           platform2  = mock(Platform.class, "P-2");
//
//          ApplicationConsole console1   = mock(ApplicationConsole.class, "C1");
//          ApplicationConsole console2   = mock(ApplicationConsole.class, "C2");
//          ApplicationConsole console3   = mock(ApplicationConsole.class, "C3");
//
//          Predicate          predicate1 = mock(Predicate.class, "PR-1");
//          ApplicationSchema  schema1    = mock(ApplicationSchema.class, "S-1");
//          Predicate          predicate2 = mock(Predicate.class, "PR-2");
//          ApplicationSchema  schema2    = mock(ApplicationSchema.class, "S-2");
//          Predicate          predicate3 = mock(Predicate.class, "PR-3");
//          ApplicationSchema  schema3    = mock(ApplicationSchema.class, "S-3");
//
//          when(platform1.getName()).thenReturn("P-1");
//          when(platform1.build(anyString(), any(ApplicationSchema.class),
//                                 any(ApplicationConsole.class))).thenAnswer(new ApplicationAnswer("P-1"));
//
//          when(platform2.getName()).thenReturn("P-2");
//          when(platform2.build(anyString(), any(ApplicationSchema.class),
//                                 any(ApplicationConsole.class))).thenAnswer(new ApplicationAnswer("P-2"));
//
//          when(predicate1.evaluate(platform1)).thenReturn(true);
//          when(predicate1.evaluate(platform2)).thenReturn(true);
//          when(predicate2.evaluate(platform1)).thenReturn(false);
//          when(predicate2.evaluate(platform2)).thenReturn(false);
//          when(predicate3.evaluate(platform1)).thenReturn(true);
//          when(predicate3.evaluate(platform2)).thenReturn(false);
//
//          InfrastructureAssemblyBuilder builder = new InfrastructureAssemblyBuilder();
//
//          builder.addApplication(predicate1, "A", schema1, 2, console1);
//          builder.addApplication(predicate2, "B", schema2, 1);
//          builder.addApplication(predicate3, "C", schema3, 3, console2);
//
//          Infrastructure<Platform> infrastructure = new Infrastructure<>();
//
//          infrastructure.addPlatform(platform1);
//          infrastructure.addPlatform(platform2);
//
//          SimpleAssembly<ApplicationStub> result = (SimpleAssembly<ApplicationStub>) builder.build(infrastructure,
//                                                                                                     console3);
//
//          assertThat(result.size(), is(7));
//          assertThat(result,
//                     containsInAnyOrder(new ApplicationStub("P-1",
//                                                            "A-1@P-1",
//                                                            console3,
//                                                            schema1), new ApplicationStub("P-1",
//                                                                                          "A-2@P-1",
//                                                                                          console3,
//                                                                                          schema1), new ApplicationStub("P-2",
//              "A-1@P-2",
//              console3,
//              schema1), new ApplicationStub("P-2",
//                                            "A-2@P-2",
//                                            console3,
//                                            schema1), new ApplicationStub("P-1",
//                                                                          "C-1@P-1",
//                                                                          console3,
//                                                                          schema3), new ApplicationStub("P-1",
//                                                                                                        "C-2@P-1",
//                                                                                                        console3,
//                                                                                                        schema3), new ApplicationStub("P-1",
//              "C-3@P-1",
//              console3,
//              schema3)));
//      }
//
//
//      @Test
//      @SuppressWarnings("unchecked")
//      public void shouldRealizeAssemblyWithoutConsoleOverride() throws Exception
//      {
//          Platform           platform1  = mock(Platform.class, "P-1");
//          Platform           platform2  = mock(Platform.class, "P-2");
//
//          ApplicationConsole console1   = mock(ApplicationConsole.class, "C1");
//          ApplicationConsole console2   = mock(ApplicationConsole.class, "C2");
//
//          Predicate          predicate1 = mock(Predicate.class, "PR-1");
//          ApplicationSchema  schema1    = mock(ApplicationSchema.class, "S-1");
//          Predicate          predicate2 = mock(Predicate.class, "PR-2");
//          ApplicationSchema  schema2    = mock(ApplicationSchema.class, "S-2");
//          Predicate          predicate3 = mock(Predicate.class, "PR-3");
//          ApplicationSchema  schema3    = mock(ApplicationSchema.class, "S-3");
//
//          when(platform1.getName()).thenReturn("P-1");
//          when(platform1.build(anyString(), any(ApplicationSchema.class),
//                                 any(ApplicationConsole.class))).thenAnswer(new ApplicationAnswer("P-1"));
//
//          when(platform2.getName()).thenReturn("P-2");
//          when(platform2.build(anyString(), any(ApplicationSchema.class),
//                                 any(ApplicationConsole.class))).thenAnswer(new ApplicationAnswer("P-2"));
//
//          when(predicate1.evaluate(platform1)).thenReturn(true);
//          when(predicate1.evaluate(platform2)).thenReturn(true);
//          when(predicate2.evaluate(platform1)).thenReturn(false);
//          when(predicate2.evaluate(platform2)).thenReturn(false);
//          when(predicate3.evaluate(platform1)).thenReturn(true);
//          when(predicate3.evaluate(platform2)).thenReturn(false);
//
//          InfrastructureAssemblyBuilder builder = new InfrastructureAssemblyBuilder();
//
//          builder.addApplication(predicate1, "A", schema1, 2, console1);
//          builder.addApplication(predicate2, "B", schema2, 1);
//          builder.addApplication(predicate3, "C", schema3, 3, console2);
//
//          Infrastructure<Platform> infrastructure = new Infrastructure<>();
//
//          infrastructure.addPlatform(platform1);
//          infrastructure.addPlatform(platform2);
//
//          SimpleAssembly<ApplicationStub> result = (SimpleAssembly<ApplicationStub>) builder.build(infrastructure);
//
//          assertThat(result.size(), is(7));
//          assertThat(result,
//                     containsInAnyOrder(new ApplicationStub("P-1",
//                                                            "A-1@P-1",
//                                                            console1,
//                                                            schema1), new ApplicationStub("P-1",
//                                                                                          "A-2@P-1",
//                                                                                          console1,
//                                                                                          schema1), new ApplicationStub("P-2",
//              "A-1@P-2",
//              console1,
//              schema1), new ApplicationStub("P-2",
//                                            "A-2@P-2",
//                                            console1,
//                                            schema1), new ApplicationStub("P-1",
//                                                                          "C-1@P-1",
//                                                                          console2,
//                                                                          schema3), new ApplicationStub("P-1",
//                                                                                                        "C-2@P-1",
//                                                                                                        console2,
//                                                                                                        schema3), new ApplicationStub("P-1",
//              "C-3@P-1",
//              console2,
//              schema3)));
//      }
//
//
//      /**
//       * A Mockito {@link Answer} class to provide a stub {@link Application}
//       * when a mock {@link Platform}'s build method is called.
//       */
//      public static class ApplicationAnswer implements Answer<Application>
//      {
//          private String platformName;
//
//
//          /**
//           * Constructs ...
//           *
//           *
//           * @param platformName
//           */
//          public ApplicationAnswer(String platformName)
//          {
//              this.platformName = platformName;
//          }
//
//
//          @Override
//          public Application answer(InvocationOnMock invocationOnMock) throws Throwable
//          {
//              Object[] args = invocationOnMock.getArguments();
//
//              return new ApplicationStub(platformName,
//                                         (String) args[0],
//                                         (ApplicationConsole) args[2],
//                                         (ApplicationSchema) args[1]);
//          }
//      }
//
//
//      /**
//       * A stub {@link Application} class to use to assert
//       * that the expected {@link Application}s are realized.
//       */
//      public static class ApplicationStub implements Application
//      {
//          private final String             platform;
//          private final String             name;
//          private final ApplicationConsole console;
//          private final ApplicationSchema  schema;
//
//
//          /**
//           * Constructs ...
//           *
//           *
//           * @param platform
//           * @param name
//           * @param console
//           * @param schema
//           */
//          public ApplicationStub(String             platform,
//                                 String             name,
//                                 ApplicationConsole console,
//                                 ApplicationSchema  schema)
//          {
//              this.platform = platform;
//              this.name     = name;
//              this.console  = console;
//              this.schema   = schema;
//          }
//
//
//          @Override
//          public boolean equals(Object o)
//          {
//              if (this == o)
//              {
//                  return true;
//              }
//
//              if (o == null || getClass() != o.getClass())
//              {
//                  return false;
//              }
//
//              ApplicationStub that = (ApplicationStub) o;
//
//              if (console != null ? !console.equals(that.console) : that.console != null)
//              {
//                  return false;
//              }
//
//              if (name != null ? !name.equals(that.name) : that.name != null)
//              {
//                  return false;
//              }
//
//              if (platform != null ? !platform.equals(that.platform) : that.platform != null)
//              {
//                  return false;
//              }
//
//              if (schema != null ? !schema.equals(that.schema) : that.schema != null)
//              {
//                  return false;
//              }
//
//              return true;
//          }
//
//
//          @Override
//          public int hashCode()
//          {
//              int result = platform != null ? platform.hashCode() : 0;
//
//              result = 31 * result + (name != null ? name.hashCode() : 0);
//              result = 31 * result + (console != null ? console.hashCode() : 0);
//              result = 31 * result + (schema != null ? schema.hashCode() : 0);
//
//              return result;
//          }
//
//
//          @Override
//          public String toString()
//          {
//              return "ApplicationStub{" + "platform='" + platform + '\'' + ", name='" + name + '\'' + ", console="
//                     + console + ", schema=" + schema + '}';
//          }
//
//
//          @Override
//          public Properties getEnvironmentVariables()
//          {
//              return null;
//          }
//
//
//          @Override
//          public String getName()
//          {
//              return null;
//          }
//
//
//          @Override
//          public Platform getPlatform()
//          {
//              return null;
//          }
//
//
//          @Override
//          public Options getOptions()
//          {
//              return new Options();
//          }
//
//
//          @Override
//          public void close()
//          {
//          }
//
//
//          @Override
//          public void close(Option... options)
//          {
//          }
//
//
//          @Override
//          public int waitFor(Option... options)
//          {
//              return 0;
//          }
//
//
//          @Override
//          public int exitValue()
//          {
//              return 0;
//          }
//
//
//          @Override
//          public long getId()
//          {
//              return 0;
//          }
//
//
//          @Override
//          public Timeout getDefaultTimeout()
//          {
//              return Timeout.autoDetect();
//          }
//      }
}
