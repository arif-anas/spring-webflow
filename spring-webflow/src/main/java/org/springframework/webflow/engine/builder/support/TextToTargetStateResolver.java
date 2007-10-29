/*
 * Copyright 2004-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.webflow.engine.builder.support;

import org.springframework.binding.convert.ConversionContext;
import org.springframework.binding.convert.support.AbstractConverter;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.builder.FlowBuilderContext;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.execution.RequestContext;

/**
 * Converter that takes an encoded string representation and produces a corresponding {@link TargetStateResolver}
 * object.
 * <p>
 * This converter supports the following encoded forms:
 * <ul>
 * <li>"stateId" - will result in a TargetStateResolver that always resolves the same state. </li>
 * <li>"${stateIdExpression} - will result in a TargetStateResolver that resolves the target state by evaluating an
 * expression against the request context.</li>
 * <li>"bean:&lt;id&gt;" - will result in usage of a custom TargetStateResolver bean implementation configured in an
 * external context.</li>
 * </ul>
 * 
 * @author Keith Donald
 * @author Erwin Vervaet
 */
class TextToTargetStateResolver extends AbstractConverter {

	/**
	 * Prefix used when the user wants to use a custom TargetStateResolver implementation managed by a factory.
	 */
	private static final String BEAN_PREFIX = "bean:";

	/**
	 * Context for flow builder services.
	 */
	private FlowBuilderContext flowBuilderContext;

	/**
	 * Create a new converter that converts strings to transition target state resolver objects. The given conversion
	 * service will be used to do all necessary internal conversion (e.g. parsing expression strings).
	 */
	public TextToTargetStateResolver(FlowBuilderContext flowBuilderContext) {
		this.flowBuilderContext = flowBuilderContext;
	}

	public Class[] getSourceClasses() {
		return new Class[] { String.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { TargetStateResolver.class };
	}

	protected Object doConvert(Object source, Class targetClass, ConversionContext context) throws Exception {
		String targetStateId = (String) source;
		ExpressionParser parser = flowBuilderContext.getExpressionParser();
		if (targetStateId.startsWith(BEAN_PREFIX)) {
			return flowBuilderContext.getBeanFactory().getBean(targetStateId.substring(BEAN_PREFIX.length()));
		} else {
			Expression expression = parser.parseExpression(targetStateId, RequestContext.class, String.class, null);
			return new DefaultTargetStateResolver(expression);
		}
	}
}