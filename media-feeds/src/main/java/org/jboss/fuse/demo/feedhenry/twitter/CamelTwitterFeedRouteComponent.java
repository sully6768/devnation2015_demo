/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.fuse.demo.feedhenry.twitter;

import static org.apache.camel.component.amqp.AMQPComponent.amqpComponent;

import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.component.twitter.TwitterComponent;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@org.osgi.service.component.annotations.Component(name = "org.jboss.fuse.demo.feedhenry.twitter")
public class CamelTwitterFeedRouteComponent extends CamelTwitterSupport {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CamelTwitterFeedRouteComponent.class);

	@Activate
	public void activate() {
		LOGGER.info("Spinning up the Camel JMS Producer Route");

		try {
			this.setName("dn-demo-twitter-feeder");


			this.addComponent("amqp",
					amqpComponent("amqp://admin:admin@localhost:5672", false));

			this.addComponent("twitter", new TwitterComponent());

			this.addRoutes(new RouteBuilder() {

				@Override
				public void configure() throws Exception {

					StringBuilder devNation = new StringBuilder();
					devNation.append("twitter://search");
					devNation.append("?type=polling&delay=5");
					devNation.append("&keywords=#DevNation");
					devNation.append(getUriTokens());

					LOGGER.trace("Route 1  : " + devNation.toString());

					StringBuilder redHatSummit = new StringBuilder();
					redHatSummit.append("twitter://search");
					redHatSummit.append("?type=polling&delay=5");
					redHatSummit.append("&keywords=#RHSummit");
					redHatSummit.append(getUriTokens());

					LOGGER.trace("Route 2  : " + redHatSummit.toString());

					JacksonDataFormat prettyPrintDataFormat = new JacksonDataFormat();
					prettyPrintDataFormat.setPrettyPrint(true);

					from(devNation.toString()).from(redHatSummit.toString())
							.routeId("twitter-feed-1")
							.marshal(prettyPrintDataFormat)
							.log("Updates from Twitter: ${body}")
							.to("amqp:queue:org.jboss.fuse.demo.feedhenry");

				}
			});

			this.start();
			this.startAllRoutes();
			if (this.isStarted()) {
				LOGGER.info("CamelContext is Started");
			}

		} catch (Exception e) {
			throw new RuntimeCamelException(
					"Error Adding Route to the Camel Context", e);
		}
		LOGGER.info("Spinning up the Camel JMS Producer Route: SUCCESS");
	}

	@Deactivate
	public void deactivate() {
		LOGGER.info("Shutting down the Camel JMS Producer Route");
		try {
			this.stopRoute("twitter-feed-1");
			this.stop();
		} catch (Exception e) {
			LOGGER.error("TODO Auto-generated catch block: "
					+ e.getLocalizedMessage());
			throw new RuntimeCamelException(
					"Error Adding Route to the Camel Context", e);
		}
	}
}
