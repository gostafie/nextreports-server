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
package ro.nextreports.server.web.security;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;

import ro.nextreports.server.domain.AclEntry;
import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.web.security.PermissionPanel;


/**
 * @author Decebal Suiu
 */
public class UserAclEntryPanel extends Panel {

	public UserAclEntryPanel(String id, Entity entity, AclEntry aclEntry, List<String> notGrantedUsers) {
		super(id);
		
        add(new UserAclEntryForm("form", entity, aclEntry, notGrantedUsers));
        add(new FeedbackPanel("feedback"));
	}

	public void onUserAclEntry(AjaxRequestTarget target, AclEntry aclEntry, boolean recursive) {
		// override
	}

    public void onCancel(AjaxRequestTarget target) {
        // override
    }

	class UserAclEntryForm extends Form<AclEntry> {

		private boolean recursive;
		
		public UserAclEntryForm(String id, final Entity entity, AclEntry aclEntry, List<String> notGrantedUsers) {
			super(id, new CompoundPropertyModel<AclEntry>(aclEntry));
			
			boolean modify = aclEntry.getName() != null;
			
            if (aclEntry.getName() == null) {
            	aclEntry.setName(notGrantedUsers.get(0));
            }
            ChoiceRenderer<String> choiceRenderer = new ChoiceRenderer<String>();
            DropDownChoice<String> choice = new DropDownChoice<String>("name", notGrantedUsers, choiceRenderer);
            if (modify) {
            	choice.setEnabled(false);
            }
            add(choice);
            
            add(new PermissionPanel("permissionPanel", aclEntry).setRenderBodyOnly(true));
            
            add(new CheckBox("recursively", new PropertyModel<Boolean>(this, "recursive")));
            
            AjaxSubmitLink addLink = new AjaxSubmitLink("add") {

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					AclEntry aclEntry = UserAclEntryForm.this.getModelObject();
					onUserAclEntry(target, aclEntry, recursive);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					// TODO Auto-generated method stub
				}

            };
            if (modify) {
                addLink.add(new SimpleAttributeModifier("rawValue", "Modify"));
            }

            add(addLink);
            
            add(new AjaxLink("cancel") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					onCancel(target);
				}
            	
            });
		}
					    
	}
	        
}
