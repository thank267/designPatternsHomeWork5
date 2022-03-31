/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package thank267.Velocity;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.EasyFactoryConfiguration;
import org.apache.velocity.tools.generic.CollectionTool;
import org.apache.velocity.tools.generic.DisplayTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import thank267.NLP.EmojisService;
import thank267.NLP.MorpherService;
import thank267.NLP.TagsService;
import thank267.Predictor.POSTAPIService;

import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Tags({"Velocity template"})
@CapabilityDescription("Velocity template")
public class StandardVelocityService extends AbstractControllerService implements VelocityService {

	public static final PropertyDescriptor HOST = new PropertyDescriptor.Builder().name("HOST").displayName("Zookeeper host").description("Zookeeper host").required(true).addValidator(StandardValidators.HOSTNAME_PORT_LIST_VALIDATOR).build();

	public static final PropertyDescriptor EMOJIS_SERVICE = new PropertyDescriptor.Builder().name("Emojis Service").displayName("Emojis Service").description("Tags Service").required(true).identifiesControllerService(EmojisService.class).build();

	private static final List<PropertyDescriptor> properties;

	static {
		final List<PropertyDescriptor> props = new ArrayList<>();
		props.add(HOST);
		props.add(EMOJIS_SERVICE);
		properties = Collections.unmodifiableList(props);
	}

	private ToolManager tm;
	private EmojisService emojisService;

	public StandardVelocityService() {
	}

	@Override
	protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
		return properties;
	}

	@OnEnabled
	public void onEnabled(final ConfigurationContext context) throws InitializationException {

		String zkHost = context.getProperty(HOST).getValue();

		this.emojisService = context.getProperty(EMOJIS_SERVICE).asControllerService(EmojisService.class);

		try {
			CuratorFramework curator = CuratorFrameworkFactory.builder().connectString(zkHost).connectionTimeoutMs(6000).retryPolicy(new ExponentialBackoffRetry(2000, 6)).build();

			if (curator.getState() != CuratorFrameworkState.STARTED) {

				curator.start();
				curator.blockUntilConnected(60, TimeUnit.SECONDS);

			}

			VelocityEngine ve = new VelocityEngine();

			ZookeeperResourceLoader rl = new ZookeeperResourceLoader(curator);

			ve.setProperty(Velocity.RESOURCE_LOADERS, "zookeeper");
			ve.addProperty("resource.loader.zookeeper.instance", rl);
			ve.addProperty("emojis", this.emojisService);
			ve.loadDirective("thank267.Velocity.EmojiDirective");
			ve.init();

			tm = new ToolManager();
			EasyFactoryConfiguration config = new EasyFactoryConfiguration();
			config.toolbox("application").tool(CollectionTool.class).tool(DisplayTool.class).tool(MathTool.class).tool(NumberTool.class).property("locale", "ru_RU");

			tm.configure(config);

			tm.setVelocityEngine(ve);

		} catch (InterruptedException e) {

			throw new InitializationException(e);

		}

	}

	public String generate(VelocityContext context, String template) {

		StringWriter writer = new StringWriter();

		Map<String, Object> model = new HashMap<>();

		Arrays.asList(context.getKeys()).forEach(key -> model.put(key, context.get(key)));

		VelocityContext newContext = new VelocityContext(model, tm.createContext());

		tm.getVelocityEngine().getTemplate(template).merge(newContext, writer);

		return writer.toString();

	}

}
