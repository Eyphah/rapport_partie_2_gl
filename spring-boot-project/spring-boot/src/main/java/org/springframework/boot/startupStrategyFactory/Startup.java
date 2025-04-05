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

package org.springframework.boot.startupStrategyFactory;
import java.time.Duration;

import org.springframework.util.ClassUtils;

public abstract class Startup {
	private Duration timeTakenToStarted;

	// Steps to be implemented by subclasses.
	protected abstract long startTime();
	public abstract Long processUptime();
	public abstract String action();

	// Template method: Common logic using the abstract steps.
	public final Duration started() {
		long now = System.currentTimeMillis();
		this.timeTakenToStarted = Duration.ofMillis(now - startTime());
		return this.timeTakenToStarted;
	}

	public Duration timeTakenToStarted() {
		return this.timeTakenToStarted;
	}

	// Optionally, you can have other common methods here.
	public Duration ready() {
		long now = System.currentTimeMillis();
		return Duration.ofMillis(now - startTime());
	}

	/**
	 * Factory Method pattern to create the proper Startup.
	 */
	public static Startup create() {
		ClassLoader classLoader = Startup.class.getClassLoader();
		if (ClassUtils.isPresent("jdk.crac.management.CRaCMXBean", classLoader)
				&& ClassUtils.isPresent("org.crac.management.CRaCMXBean", classLoader)) {
			return new CoordinatedRestoreAtCheckpointStartup();
		}
		else {
			return new StandardStartup();
		}
	}
}
