/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.client.widgets.drools.decoratedgrid.events;

import org.drools.guvnor.client.widgets.drools.decoratedgrid.AbstractMergableGridWidget.CellSelectionDetail;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents a change in the selected cell.
 */
public class SelectedCellChangeEvent extends GwtEvent<SelectedCellChangeEvent.Handler> {

    public static interface Handler
        extends
        EventHandler {

        void onSelectedCellChange(SelectedCellChangeEvent event);
    }

    public static Type<SelectedCellChangeEvent.Handler> TYPE = new Type<SelectedCellChangeEvent.Handler>();

    private final CellSelectionDetail                    cellDetails;

    /**
     * Creates a value change event.
     * 
     * @param cellExtents
     *            details of selected cell
     */
    public SelectedCellChangeEvent(CellSelectionDetail cellDetails) {
        if ( cellDetails == null ) {
            throw new IllegalArgumentException( "cellDetails cannot be null" );
        }
        this.cellDetails = cellDetails;
    }

    /**
     * Gets the details of the selected cell
     * 
     * @return the details
     */
    public CellSelectionDetail getCellSelectionDetail() {
        return this.cellDetails;
    }

    @Override
    public final Type<SelectedCellChangeEvent.Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SelectedCellChangeEvent.Handler handler) {
        handler.onSelectedCellChange( this );
    }
}
