/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.springapplicationutil;


import org.apache.commons.logging.Log;

import org.springframework.boot.ApplicationProperties;
import org.springframework.boot.Banner;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplicationBannerPrinter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static org.springframework.boot.SpringApplication.logger;

public class SpringApplicationBannerUtil {
	private Banner banner;
	private static Class<?> mainApplicationClass;
	private ResourceLoader resourceLoader;
	private ApplicationProperties properties;

	public SpringApplicationBannerUtil(Banner banner, Class<?> mainApplicationClass, ResourceLoader resourceLoader,ApplicationProperties properties) {
		this.banner = banner;
		this.mainApplicationClass = mainApplicationClass;
		this.resourceLoader = resourceLoader;
		this.properties = properties;
	}

	public Banner printBanner(ConfigurableEnvironment environment) {
		if (this.properties.getBannerMode(environment) == Banner.Mode.OFF) {
			return null;
		}
		ResourceLoader resourceLoader = (this.resourceLoader != null) ? this.resourceLoader
				: new DefaultResourceLoader(null);
		SpringApplicationBannerPrinter bannerPrinter = new SpringApplicationBannerPrinter(resourceLoader, this.banner);
		if (this.properties.getBannerMode(environment) == Mode.LOG) {
			return bannerPrinter.print(environment, this.mainApplicationClass, logger);
		}
		return bannerPrinter.print(environment, this.mainApplicationClass, (Log)System.out);
	}
}
