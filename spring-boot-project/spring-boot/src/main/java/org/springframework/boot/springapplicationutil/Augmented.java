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
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.runner.notification.RunListener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationHook;
import org.springframework.context.ApplicationContext;
import org.springframework.util.function.ThrowingConsumer;

import static org.springframework.boot.SpringApplication.withHook;

/**
 * Used to configure and run an augmented {@link SpringApplication} where additional
 * configuration should be applied.
 *
 * @since 3.1.0
 */
public class Augmented {

	private final ThrowingConsumer<String[]> main;

	private final Set<Class<?>> sources;

	private final Set<String> additionalProfiles;

	public Augmented(ThrowingConsumer<String[]> main, Set<Class<?>> sources, Set<String> additionalProfiles) {
		this.main = main;
		this.sources = Set.copyOf(sources);
		this.additionalProfiles = additionalProfiles;
	}

	/**
	 * Return a new {@link Augmented} instance with additional
	 * sources that should be applied when the application runs.
	 * @param sources the sources that should be applied
	 * @return a new {@link Augmented} instance
	 */
	public Augmented with(Class<?>... sources) {
		LinkedHashSet<Class<?>> merged = new LinkedHashSet<>(this.sources);
		merged.addAll(Arrays.asList(sources));
		return new Augmented(this.main, merged, this.additionalProfiles);
	}

	/**
	 * Return a new {@link Augmented} instance with additional
	 * profiles that should be applied when the application runs.
	 * @param profiles the profiles that should be applied
	 * @return a new {@link Augmented} instance
	 * @since 3.4.0
	 */
	public Augmented withAdditionalProfiles(String... profiles) {
		Set<String> merged = new LinkedHashSet<>(this.additionalProfiles);
		merged.addAll(Arrays.asList(profiles));
		return new Augmented(this.main, this.sources, merged);
	}

	/**
	 * Run the application using the given args.
	 * @param args the main method args
	 * @return the running {@link ApplicationContext}
	 */
	public Running run(String... args) {
		RunListener runListener = new RunListener();
		SpringApplicationHook hook = new SingleUseSpringApplicationHook((springApplication) -> {
			springApplication.addPrimarySources(this.sources);
			springApplication.setAdditionalProfiles(this.additionalProfiles.toArray(String[]::new));
			return (org.springframework.boot.SpringApplicationRunListener) runListener;
		});
		withHook(hook, () -> this.main.accept(args));
		return (Running) runListener;
	}
}