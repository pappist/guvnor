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
package org.drools.guvnor.client.decisiontable.widget;

import org.drools.guvnor.client.widgets.drools.decoratedgrid.CellValue;
import org.drools.guvnor.client.widgets.drools.decoratedgrid.SelectedCellValueUpdater;
import org.drools.guvnor.client.widgets.drools.decoratedgrid.events.SelectedCellChangeEvent;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A Vertical Decision Table composed of a VerticalDecoratedGridWidget
 */
public class VerticalDecisionTableWidget extends AbstractDecisionTableWidget {

    public VerticalDecisionTableWidget(DecisionTableControlsWidget ctrls,
                                       SuggestionCompletionEngine sce,
                                       EventBus eventBus) {
        super( ctrls,
               sce,
               eventBus );

        VerticalPanel vp = new VerticalPanel();

        //Callback for cell updates
        //TODO {manstis} This might become an event raised from the UI
        SelectedCellValueUpdater selectedCellValueUpdater = new SelectedCellValueUpdater() {

            public void setSelectedCellsValue(Object value) {
                // TODO Auto-generated method stub
            }

        };

        //Factories for new cell elements
        this.cellFactory = new DecisionTableCellFactory( sce,
                                                         selectedCellValueUpdater,
                                                         eventBus );
        this.cellValueFactory = new DecisionTableCellValueFactory( sce );

        // Construct the widget from which we're composed
        widget = new VerticalDecoratedDecisionTableGridWidget( resources,
                                                               cellFactory,
                                                               cellValueFactory,
                                                               eventBus );
        widget.setHasSystemControlledColumns( this );

        vp.add( widget );
        vp.add( ctrls );
        initWidget( vp );
    }

    public void onSelectedCellChange(SelectedCellChangeEvent event) {
        CellValue< ? > cell = widget.getData().get( event.getCellSelectionDetail().getCoordinate() );
        dtableCtrls.getOtherwiseButton().setEnabled( canAcceptOtherwiseValues( cell ) );
    }

}
