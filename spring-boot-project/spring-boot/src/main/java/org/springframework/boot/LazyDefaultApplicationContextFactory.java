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

package org.springframework.boot;

import java.util.function.Supplier;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

public class LazyDefaultApplicationContextFactory implements ApplicationContextFactory {

	private static final Supplier<ApplicationContextFactory> delegateSupplier = LazySupplier
		.of(DefaultApplicationContextFactory::new);

	private static ApplicationContextFactory getDelegate() {
		return delegateSupplier.get();
	}

	@Override
	public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
		return getDelegate().create(webApplicationType);
	}

	@Override
	public Class<? extends ConfigurableEnvironment> getEnvironmentType(WebApplicationType webApplicationType) {
		return getDelegate().getEnvironmentType(webApplicationType);
	}

	@Override
	public ConfigurableEnvironment createEnvironment(WebApplicationType webApplicationType) {
		return getDelegate().createEnvironment(webApplicationType);
	}

	private static class LazySupplier implements Supplier<ApplicationContextFactory> {

		private volatile ApplicationContextFactory instance;

		private final Supplier<ApplicationContextFactory> supplier;

		private LazySupplier(Supplier<ApplicationContextFactory> supplier) {
			this.supplier = supplier;
		}

		public static LazySupplier of(Supplier<ApplicationContextFactory> supplier) {
			return new LazySupplier(supplier);
		}

		@Override
		public ApplicationContextFactory get() {
			if (this.instance == null) {
				synchronized (this) {
					if (this.instance == null) {
						this.instance = this.supplier.get();
					}
				}
			}
			return this.instance;
		}

	}

}
