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

package org.springframework.boot.exceptions;

import org.springframework.context.ConfigurableApplicationContext;

public class AbandonedRunException extends RuntimeException {
	private ConfigurableApplicationContext applicationContext;

	/**
	 * Create a new {@link AbandonedRunException} instance.
	 */
	public AbandonedRunException() {
		this(null);
	}

	/**
	 * Create a new {@link AbandonedRunException} instance with the given application
	 * context.
	 * @param applicationContext the application context that was available when the
	 * run was abandoned
	 */
	public AbandonedRunException(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Return the application context that was available when the run was abandoned or
	 * {@code null} if no context was available.
	 * @return the application context
	 */
	public ConfigurableApplicationContext getApplicationContext() {
		return this.applicationContext;
	}
}
