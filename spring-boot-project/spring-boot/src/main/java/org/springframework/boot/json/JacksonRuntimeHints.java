/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.json;

import com.fasterxml.jackson.databind.ser.std.ClassSerializer;
import com.fasterxml.jackson.databind.ser.std.FileSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers.AtomicBooleanSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers.AtomicIntegerSerializer;
import com.fasterxml.jackson.databind.ser.std.StdJdkSerializers.AtomicLongSerializer;
import com.fasterxml.jackson.databind.ser.std.TokenBufferSerializer;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;
import org.springframework.util.ClassUtils;

/**
 * {@link RuntimeHintsRegistrar} implementation for Jackson.
 *
 * @author Moritz Halbritter
 */
class JacksonRuntimeHints implements RuntimeHintsRegistrar {

	/**
	 * Registers runtime hints for Jackson serializer classes to support native image
	 * compilation (e.g., GraalVM). This implementation conditionally registers serializer
	 * classes if the core Jackson databind infrastructure is present in the classpath.
	 *
	 * The method performs two main operations: 1. Checks for the presence of Jackson's
	 * {@code BasicSerializerFactory} class to verify Jackson availability</li> 2.
	 * Registers specific serializer classes for reflection if Jackson is present</li>
	 *
	 * The following serializers are registered with {@code INVOKE_PUBLIC_CONSTRUCTORS}
	 * access: 1.{@code AtomicBooleanSerializer} 2.{@code AtomicIntegerSerializer}
	 * 3.{@code AtomicLongSerializer} 4.{@code FileSerializer} 5.{@code ClassSerializer}
	 * 6.{@code TokenBufferSerializer}
	 * @param hints the {@link RuntimeHints} instance to register with
	 * @param classLoader the classloader to use for checking class presence
	 *
	 * @see RuntimeHints
	 * @see ReflectionHints
	 */
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (!ClassUtils.isPresent("com.fasterxml.jackson.databind.ser.BasicSerializerFactory", classLoader)) {
			return;
		}
		registerSerializers(hints.reflection());
	}

	/**
	 * Helper method that registers specific Jackson serializer classes for reflection
	 * support. The registered serializers will have their public constructors made
	 * available at runtime.
	 * @param hints the {@link ReflectionHints} instance to register serializer types with
	 */
	private void registerSerializers(ReflectionHints hints) {
		hints.registerTypes(TypeReference.listOf(AtomicBooleanSerializer.class, AtomicIntegerSerializer.class,
				AtomicLongSerializer.class, FileSerializer.class, ClassSerializer.class, TokenBufferSerializer.class),
				TypeHint.builtWith(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
	}

}
