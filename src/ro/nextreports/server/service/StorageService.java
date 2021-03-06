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
package ro.nextreports.server.service;

import java.util.List;

import ro.nextreports.server.domain.Entity;
import ro.nextreports.server.domain.Settings;
import ro.nextreports.server.domain.VersionInfo;
import ro.nextreports.server.exception.DuplicationException;
import ro.nextreports.server.exception.NotFoundException;
import ro.nextreports.server.exception.ReferenceException;
import ro.nextreports.server.search.SearchEntry;


/**
 * @author Decebal Suiu
 */
public interface StorageService {

	public Entity getEntity(String path) throws NotFoundException;
	
	public String getDashboardId(String widgetId) throws NotFoundException;

	public Entity getEntityById(String id) throws NotFoundException;

	public Entity[] getEntityChildren(String path) throws NotFoundException;

	public Entity[] getBaseEntityChildren(String path) throws NotFoundException;

	public Entity[] getEntityChildrenById(String id) throws NotFoundException;
	
	public Entity[] getEntityChildrenById(String id, int firstResult, int maxResults) throws NotFoundException;

	public Entity[] getEntitiesByClassName(String path, String className) throws NotFoundException;
	
	public Entity[] getEntitiesByClassNameWithoutSecurity(String path, String className) throws NotFoundException;

    public String addEntity(Entity entity) throws DuplicationException;
    
    public String addEntity(Entity entity, boolean keepId) throws DuplicationException;

	public void removeEntity(String path) throws ReferenceException;

	public void removeEntityById(String id) throws NotFoundException;

    public void removeEntitiesById(List<String> ids) throws NotFoundException;

    public void renameEntity(String path, String newName) throws NotFoundException, DuplicationException;

    public void modifyEntity(Entity entity);
    
    public void modifyEntity(Entity entity, String excludeChildrenName);
    
    public void modifyEntities(List<Entity> entities);

    public void copyEntity(String sourcePath, String destPath) throws NotFoundException, DuplicationException;

    public void copyEntities(List<String> sourcePaths, String destPath) throws NotFoundException, DuplicationException;

    public void moveEntity(String sourcePath, String destPath) throws NotFoundException, DuplicationException;

    public void moveEntities(List<String> sourcePaths, String destPath) throws NotFoundException, DuplicationException;

    public boolean isEntityReferenced(String path) throws NotFoundException;

    public boolean entityExists(String path);
    
    public VersionInfo[] getVersionInfos(String id) throws NotFoundException;

    public Entity getVersion(String id, String versionName) throws NotFoundException;

	public void restoreVersion(String path, String versionName) throws NotFoundException;

    public Entity[] search(List<SearchEntry> searchEntries, String searchKey);

    public void stopSearch(String searchKey);

    public String addOrModifyEntity(Entity entity);

    public String getEntityPath(String id) throws NotFoundException;
    
    public int countEntityChildrenById(String id) throws NotFoundException;
    
    public void clearEntityCache(String id);
    
    public void clearCache();
    
    public void setDefaultProperty(String path, String defaultValue);
    
    public String getDefaultProperty(String path) throws NotFoundException;
    
    public byte[] getLogoImage();
    
    public void personalizeSettings(String fileName, byte[] content, String theme, String language);
    
    public void personalizeTheme(String theme);
    
    public Settings getSettings();
    
    public void createFolders(String path) throws DuplicationException;
    
    public void clearUserWidgetData(String widgetId);
    
}
