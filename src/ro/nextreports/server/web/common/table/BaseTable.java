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
package ro.nextreports.server.web.common.table;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;

/**
 * @author Decebal Suiu
 */
public class BaseTable<T> extends AjaxFallbackDefaultDataTable<T> {

	private static final long serialVersionUID = 1L;

	public BaseTable(String id, List<IColumn<T>> columns, ISortableDataProvider<T> dataProvider, int rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
	}

	/*
	@Override
	protected Item<T> newRowItem(String id, int index, IModel<T> model) {
		return new BaseRowItem(id, index, model); 
	}
	
	class BaseRowItem extends OddEvenItem<T> {
		
		private static final String ON_MOUSE_OVER = "this.className='hover';";
		private static final String ON_MOUSE_OUT_ODD = "this.className='odd';";
        private static final String ON_MOUSE_OUT_EVEN = "this.className='even';";

		public BaseRowItem(String id, int index, IModel<T> model) {
			super(id, index, model);
		}
		
		@Override
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			tag.put("onmouseover", ON_MOUSE_OVER);
			tag.put("onmouseout", (getIndex() % 2 == 0) ? ON_MOUSE_OUT_EVEN :ON_MOUSE_OUT_ODD);
		}
		
	}
	*/

}
