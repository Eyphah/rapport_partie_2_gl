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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.StartupInfoLogger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import static org.springframework.boot.SpringApplication.logger;

public class SpringApplicationLog {

	private static Class<?> mainApplicationClass;

	public SpringApplicationLog(Class<?> mainApplicationClass) {
		this.mainApplicationClass = mainApplicationClass;
	}
	/**
	 * Called to log startup information, subclasses may override to add additional
	 * logging.
	 * @param context the application context
	 * @since 3.4.0
	 */
	public void logStartupInfo(ConfigurableApplicationContext context) {
		boolean isRoot = context.getParent() == null;
		if (isRoot) {
			new StartupInfoLogger(this.mainApplicationClass, context.getEnvironment()).logStarting(getApplicationLog());
		}
	}

	private List<String> quoteProfiles(String[] profiles) {
		return Arrays.stream(profiles).map((profile) -> "\"" + profile + "\"").toList();
	}

	/**
	 * Called to log active profile information.
	 * @param context the application context
	 */
	public void logStartupProfileInfo(ConfigurableApplicationContext context) {
		Log log = getApplicationLog();
		if (log.isInfoEnabled()) {
			List<String> activeProfiles = quoteProfiles(context.getEnvironment().getActiveProfiles());
			if (ObjectUtils.isEmpty(activeProfiles)) {
				List<String> defaultProfiles = quoteProfiles(context.getEnvironment().getDefaultProfiles());
				String message = String.format("%s default %s: ", defaultProfiles.size(),
						(defaultProfiles.size() <= 1) ? "profile" : "profiles");
				log.info("No active profile set, falling back to " + message
						+ StringUtils.collectionToDelimitedString(defaultProfiles, ", "));
			}
			else {
				String message = (activeProfiles.size() == 1) ? "1 profile is active: "
						: activeProfiles.size() + " profiles are active: ";
				log.info("The following " + message + StringUtils.collectionToDelimitedString(activeProfiles, ", "));
			}
		}
	}

	/**
	 * Returns the {@link Log} for the application. By default will be deduced.
	 * @return the application log
	 */
	public static Log getApplicationLog() {
		if (mainApplicationClass == null) {
			return logger;
		}
		return LogFactory.getLog(mainApplicationClass);
	}
}
