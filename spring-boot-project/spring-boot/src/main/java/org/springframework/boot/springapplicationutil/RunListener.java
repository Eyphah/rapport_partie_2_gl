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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * {@link SpringApplicationRunListener} to capture {@link Running} application
 * details.
 */
public final class RunListener implements SpringApplicationRunListener, Running {

	private final List<ConfigurableApplicationContext> contexts = Collections
			.synchronizedList(new ArrayList<>());

	@Override
	public void contextLoaded(ConfigurableApplicationContext context) {
		this.contexts.add(context);
	}

	@Override
	public ConfigurableApplicationContext getApplicationContext() {
		List<ConfigurableApplicationContext> rootContexts = this.contexts.stream()
				.filter((context) -> context.getParent() == null)
				.toList();
		Assert.state(!rootContexts.isEmpty(), "No root application context located");
		Assert.state(rootContexts.size() == 1, "No unique root application context located");
		return rootContexts.get(0);
	}

}
