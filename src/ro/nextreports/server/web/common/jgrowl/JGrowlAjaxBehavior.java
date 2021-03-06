/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.web.common.jgrowl;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.resource.PackageResourceReference;

/**
 * @author Decebal Suiu
 */
public class JGrowlAjaxBehavior extends AbstractDefaultAjaxBehavior {

	private static final long serialVersionUID = 1L;

	/**
	 * Displays an info message that is sticky. The default is non-sticky. Sample usage:
	 * session.getFeedbackMessages().add(new FeedbackMessage(null, "my message", JGrowlBehavior.INFO_STICKY));
	 */
	public static final int INFO_FADE = 111;
	public static final int INFO_STICKY = 250;
	public static final int ERROR_STICKY = 251;

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
//		System.out.println("JGrowl render head");
		response.renderJavaScriptReference(new PackageResourceReference(JGrowlAjaxBehavior.class, "jquery.jgrowl.js"));
        response.renderCSSReference(new PackageResourceReference(JGrowlAjaxBehavior.class, "jquery.jgrowl.css"));
        response.renderCSSReference(new PackageResourceReference(JGrowlAjaxBehavior.class, "jgrowl.css"));

		String feedback = renderFeedback();
		if (!StringUtils.isEmpty(feedback)) {
//			System.out.println("rendering");
			response.renderOnDomReadyJavaScript(feedback);
		}
	}
	
	@Override
	protected void respond(AjaxRequestTarget target) {
//		System.out.println("JGrowl respond");
		String feedback = renderFeedback();
		if (!StringUtils.isEmpty(feedback)) {
			target.appendJavaScript(feedback);
		}

	}

	/*
	@Override
	protected void onBind() {
		// Add the jgrowl css so the components that use the behaviour don't have to do it.
		getComponent().add((CSSPackageResource.getHeaderContribution(this.getClass(), "jquery.jgrowl.css")));
	}
	*/

	private String renderFeedback() {
		//	this.getComponent().getFeedbackMessage();
		FeedbackMessages fm = Session.get().getFeedbackMessages();
		
		Iterator<FeedbackMessage> iter = fm.iterator();
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext()) {
			FeedbackMessage message = iter.next();
			if ((message.getReporter() != null) || message.isRendered()) {
				// If a component-level message, don't show it
				continue;
			}
			
			// if we are info stick set to info else set to message level
			String cssClassSuffix = "";
			switch (message.getLevel()) {
				case INFO_STICKY: 
					cssClassSuffix = "INFO";
					break;
				case ERROR_STICKY:
					cssClassSuffix = "ERROR";
					break;
				default:
					cssClassSuffix = message.getLevelAsString();
					break;
			}			
			Serializable serializable = message.getMessage();
			
			// grab the message, if it's null use an empty string
			String msg = (serializable == null) ? StringUtils.EMPTY : serializable.toString();
			
			sb.append("$.jGrowl(\"").append(escape(msg)).append('\"');
			sb.append(", {");
			// set the css style, i.e. the theme
			sb.append("theme: \'jgrowl-").append(cssClassSuffix).append("\'");
			// set sticky
			if (message.getLevel() > FeedbackMessage.INFO) {
				sb.append(", sticky: true");
			} else {
				// default is 3000 (3sec)
				sb.append(", life: 5000");
			}

			sb.append("}");
			sb.append(");");
			message.markRendered();
		}
		
		return sb.toString();
	}

	 private String escape(String text) {
		 return text.replaceAll("\n", "\\r").replaceAll("\"", "\\\\\"");
	 }
	 
}
