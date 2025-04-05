/*
 * Copyright 2012-2019 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.springframework.boot.exceptions.JsonParseException;
import org.springframework.util.ReflectionUtils;

/**
 * Base class for parsers wrapped or implemented in this package. This class provides
 * common methods to parse JSON data into various data structures, such as Maps and Lists,
 * and handles parsing errors through custom exceptions.
 *
 * @author Anton Telechev
 * @author Phillip Webb
 * @since 2.0.1
 */
public abstract class AbstractJsonParser implements JsonParser {

	/**
	 * Parses given JSON string into a Map, using the provided parser method to process
	 * the data. The JSON String is expected to represent a JSON object
	 * @param json The JSON String to parse.
	 * @param parser The method used to parse the JSON String into a Map.
	 * @return A Map representing the JSON object that has been parsed.
	 * @throws JsonParseException If the JSON string cannot be parsed as a Map.
	 */
	protected final Map<String, Object> parseMap(String json, Function<String, Map<String, Object>> parser) {
		return trimParse(json, "{", parser);
	}

	/**
	 * Parses the given JSON object into a List, using the provided parser method to
	 * process the data. The JSON String is expected to represent a JSON array.
	 * @param json The JSON string to parse.
	 * @param parser The function used to parse the JSON string into a list.
	 * @return A list representing the JSON data parsed.
	 * @throws JsonParseException if the JSON string cannot be parsed.
	 */
	protected final List<Object> parseList(String json, Function<String, List<Object>> parser) {
		return trimParse(json, "[", parser);
	}

	/**
	 * Utility method that trims the spaces of given JSON String and if the JSON String
	 * starts with the given prefix, then it applies the given parser to it.
	 * @param json The JSON string to trim.
	 * @param prefix The string prefix.
	 * @param parser The function used to parse the trimmed string.
	 * @return The result of the parser method applied to the trimmed json string.
	 * @throws JsonParseException if the JSON string does not start with the given prefix.
	 */
	protected final <T> T trimParse(String json, String prefix, Function<String, T> parser) {
		String trimmed = (json != null) ? json.trim() : "";
		if (trimmed.startsWith(prefix)) {
			return parser.apply(trimmed);
		}
		throw new JsonParseException();
	}

	/**
	 * Method that applies a parser securely and handles exceptions.
	 * @param parser The parser method.
	 * @param check The expected exception type.
	 * @return The result of parser.call() if successful
	 * @throws JsonParseException if caught exception is from check type.
	 * @throws RuntimeException if caught exception comes from runtime.
	 * @throws IllegalStateException if caught exception is other than the 2 previous
	 * cases.
	 */
	protected final <T> T tryParse(Callable<T> parser, Class<? extends Exception> check) {
		try {
			return parser.call();
		}
		catch (Exception ex) {
			if (check.isAssignableFrom(ex.getClass())) {
				throw new JsonParseException(ex);
			}
			ReflectionUtils.rethrowRuntimeException(ex);
			throw new IllegalStateException(ex);
		}
	}

}
