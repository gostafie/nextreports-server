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
package ro.nextreports.server.web.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ro.nextreports.server.domain.VersionInfo;
import ro.nextreports.server.service.StorageService;


//
public class EntityVersionDataProvider extends SortableDataProvider<VersionInfo> {

	private static final long serialVersionUID = 1L;
	
	private String entityId;
    private transient List<VersionInfo> versions;

    @SpringBean
    private StorageService storageService;

    public EntityVersionDataProvider(String entityId) {
    	this.entityId = entityId;
    	
    	Injector.get().inject(this);
    }

	public Iterator<? extends VersionInfo> iterator(int first, int count) {
		return getVersions().iterator();
	}

	public IModel<VersionInfo> model(VersionInfo version) {
		return new Model<VersionInfo>(version);
	}

	public int size() {
		return getVersions().size();
	}

	public void detach() {
		versions = null;
	}

    private List<VersionInfo> getVersions() {
        if (versions == null) {
        	try {
				versions = getReportVersions();
			} catch (Exception e) {
				// TODO
				throw new RuntimeException(e);
			}
        }

        System.out.println("get " + versions.size() + " versions");

        return versions;
    }

   private List<VersionInfo> getReportVersions() throws Exception {
        VersionInfo[] array = storageService.getVersionInfos(entityId);
        return Arrays.asList(array);
    }



}

