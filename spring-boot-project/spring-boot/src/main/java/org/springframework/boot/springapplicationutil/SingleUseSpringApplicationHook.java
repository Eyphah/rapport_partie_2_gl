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

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationHook;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * {@link SpringApplicationHook} decorator that ensures the hook is only used once.
 */
public final class SingleUseSpringApplicationHook implements SpringApplicationHook {

	private final AtomicBoolean used = new AtomicBoolean();

	private final SpringApplicationHook delegate;

	SingleUseSpringApplicationHook(SpringApplicationHook delegate) {
		this.delegate = delegate;
	}

	@Override
	public SpringApplicationRunListener getRunListener(SpringApplication springApplication) {
		return this.used.compareAndSet(false, true) ? this.delegate.getRunListener(springApplication) : null;
	}

}
