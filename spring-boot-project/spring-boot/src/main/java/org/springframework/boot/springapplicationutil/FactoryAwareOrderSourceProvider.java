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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.OrderComparator.OrderSourceProvider;

/**
 * {@link OrderSourceProvider} used to obtain factory method and target type order
 * sources. Based on internal {@link DefaultListableBeanFactory} code.
 */
public class FactoryAwareOrderSourceProvider implements OrderSourceProvider {

	private final ConfigurableBeanFactory beanFactory;

	private final Map<?, String> instancesToBeanNames;

	public FactoryAwareOrderSourceProvider(ConfigurableBeanFactory beanFactory, Map<?, String> instancesToBeanNames) {
		this.beanFactory = beanFactory;
		this.instancesToBeanNames = instancesToBeanNames;
	}

	@Override
	public Object getOrderSource(Object obj) {
		String beanName = this.instancesToBeanNames.get(obj);
		return (beanName != null) ? getOrderSource(beanName, obj.getClass()) : null;
	}

	private Object getOrderSource(String beanName, Class<?> instanceType) {
		try {
			RootBeanDefinition beanDefinition = (RootBeanDefinition) this.beanFactory
					.getMergedBeanDefinition(beanName);
			Method factoryMethod = beanDefinition.getResolvedFactoryMethod();
			Class<?> targetType = beanDefinition.getTargetType();
			targetType = (targetType != instanceType) ? targetType : null;
			return Stream.of(factoryMethod, targetType).filter(Objects::nonNull).toArray();
		}
		catch (NoSuchBeanDefinitionException ex) {
			return null;
		}
	}

}
