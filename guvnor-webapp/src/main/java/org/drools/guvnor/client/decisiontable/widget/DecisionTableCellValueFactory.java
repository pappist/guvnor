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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.drools.guvnor.client.asseteditor.drools.modeldriven.ui.RuleAttributeWidget;
import org.drools.guvnor.client.widgets.drools.decoratedgrid.AbstractCellValueFactory;
import org.drools.guvnor.client.widgets.drools.decoratedgrid.CellValue;
import org.drools.guvnor.client.widgets.drools.decoratedgrid.CellValue.CellState;
import org.drools.guvnor.client.widgets.drools.decoratedgrid.data.DynamicDataRow;
import org.drools.ide.common.client.modeldriven.SuggestionCompletionEngine;
import org.drools.ide.common.client.modeldriven.dt52.ActionWorkItemCol52;
import org.drools.ide.common.client.modeldriven.dt52.ActionWorkItemInsertFactCol52;
import org.drools.ide.common.client.modeldriven.dt52.ActionWorkItemSetFieldCol52;
import org.drools.ide.common.client.modeldriven.dt52.Analysis;
import org.drools.ide.common.client.modeldriven.dt52.AttributeCol52;
import org.drools.ide.common.client.modeldriven.dt52.ConditionCol52;
import org.drools.ide.common.client.modeldriven.dt52.DTCellValue52;
import org.drools.ide.common.client.modeldriven.dt52.DTColumnConfig52;
import org.drools.ide.common.client.modeldriven.dt52.DTDataTypes52;
import org.drools.ide.common.client.modeldriven.dt52.GuidedDecisionTable52;
import org.drools.ide.common.client.modeldriven.dt52.LimitedEntryCol;
import org.drools.ide.common.client.modeldriven.dt52.RowNumberCol52;

/**
 * A Factory to create CellValues applicable to given columns.
 */
public class DecisionTableCellValueFactory extends AbstractCellValueFactory<DTColumnConfig52, DTCellValue52> {

    // Model used to determine data-types etc for cells
    private GuidedDecisionTable52 model;

    /**
     * Construct a Cell Value Factory for a specific Decision Table
     * 
     * @param sce
     *            SuggestionCompletionEngine to assist with drop-downs
     */
    public DecisionTableCellValueFactory(SuggestionCompletionEngine sce) {
        super( sce );
    }

    /**
     * Set the model for which CellValues will be created
     * 
     * @param model
     */
    public void setModel(GuidedDecisionTable52 model) {
        if ( model == null ) {
            throw new IllegalArgumentException( "model cannot be null" );
        }
        this.model = model;
    }

    /**
     * Construct a new row of data for the underlying model
     * 
     * @return
     */
    public List<DTCellValue52> makeRowData() {
        List<DTCellValue52> data = new ArrayList<DTCellValue52>();
        List<DTColumnConfig52> columns = model.getAllColumns();
        for ( DTColumnConfig52 column : columns ) {
            DTCellValue52 cell = makeModelCellValue( column );
            data.add( cell );
        }
        return data;
    }

    /**
     * Construct a new row of data for the MergableGridWidget
     * 
     * @param cell
     * @return
     */
    @Override
    public DynamicDataRow makeUIRowData() {
        DynamicDataRow data = new DynamicDataRow();
        List<DTColumnConfig52> columns = model.getAllColumns();
        for ( DTColumnConfig52 column : columns ) {
            DTCellValue52 dcv = makeModelCellValue( column );
            DTDataTypes52 dataType = getDataType( column );
            assertDTCellValue( dataType,
                               dcv );
            CellValue< ? extends Comparable< ? >> cell = convertModelCellValue( column,
                                                                                dcv );
            data.add( cell );
        }

        return data;
    }

    /**
     * Make a Model cell for the given column
     * 
     * @param column
     * @return
     */
    @Override
    public DTCellValue52 makeModelCellValue(DTColumnConfig52 column) {
        DTDataTypes52 dataType = getDataType( column );
        DTCellValue52 dcv = new DTCellValue52( column.getDefaultValue() );
        assertDTCellValue( dataType,
                           dcv );
        return dcv;
    }

    /**
     * Convert a Model cell to one that can be used in the UI
     * 
     * @param cell
     * @return
     */
    @Override
    public CellValue< ? extends Comparable< ? >> convertModelCellValue(DTColumnConfig52 column,
                                                                       DTCellValue52 dcv) {
        DTDataTypes52 dataType = getDataType( column );
        assertDTCellValue( dataType,
                           dcv );

        CellValue< ? extends Comparable< ? >> cell = null;
        switch ( dataType ) {
            case BOOLEAN :
                cell = makeNewBooleanCellValue( dcv.getBooleanValue() );
                break;
            case DATE :
                cell = makeNewDateCellValue( dcv.getDateValue() );
                break;
            case NUMERIC :
                if ( column instanceof RowNumberCol52 ) {
                    cell = makeNewRowNumberCellValue( dcv.getNumericValue() );
                } else {
                    cell = makeNewNumericCellValue( dcv.getNumericValue() );
                    if ( column instanceof AttributeCol52 ) {
                        AttributeCol52 at = (AttributeCol52) column;
                        if ( at.getAttribute().equals( RuleAttributeWidget.SALIENCE_ATTR ) ) {
                            if ( at.isUseRowNumber() ) {
                                cell = makeNewRowNumberCellValue( dcv.getNumericValue() );
                            }
                        }
                    }
                }
                break;
            default :
                cell = makeNewStringCellValue( dcv.getStringValue() );
                if ( column instanceof AttributeCol52 ) {
                    AttributeCol52 ac = (AttributeCol52) column;
                    if ( ac.getAttribute().equals( RuleAttributeWidget.DIALECT_ATTR ) ) {
                        cell = makeNewDialectCellValue( dcv.getStringValue() );
                    }
                }
        }

        if ( dcv.isOtherwise() ) {
            cell.addState( CellState.OTHERWISE );
        }

        return cell;
    }

    // Get the Data Type corresponding to a given column
    protected DTDataTypes52 getDataType(DTColumnConfig52 column) {

        //Limited Entry are simply boolean
        if ( column instanceof LimitedEntryCol ) {
            return DTDataTypes52.BOOLEAN;
        }

        //Action Work Items are always boolean
        if ( column instanceof ActionWorkItemCol52 ) {
            return DTDataTypes52.BOOLEAN;
        }

        //Actions setting Field Values from Work Item Result Parameters are always boolean
        if ( column instanceof ActionWorkItemSetFieldCol52 || column instanceof ActionWorkItemInsertFactCol52 ) {
            return DTDataTypes52.BOOLEAN;
        }

        //Operators "is null" and "is not null" require a boolean cell
        if ( column instanceof ConditionCol52 ) {
            ConditionCol52 cc = (ConditionCol52) column;
            if ( cc.getOperator() != null && (cc.getOperator().equals( "== null" ) || cc.getOperator().equals( "!= null" )) ) {
                return DTDataTypes52.BOOLEAN;
            }
        }

        //Extended Entry...
        return model.getTypeSafeType( column,
                                      sce );
    }

    //If the Decision Table model has been converted from the legacy text based
    //class then all values are held in the DTCellValue's StringValue. This
    //function attempts to set the correct DTCellValue property based on
    //the DTCellValue's data type.
    private void assertDTCellValue(DTDataTypes52 dataType,
                                   DTCellValue52 dcv) {
        //If already converted exit
        if ( dcv.getDataType().equals( dataType ) ) {
            return;
        }

        String text = dcv.getStringValue();
        switch ( dataType ) {
            case BOOLEAN :
                dcv.setBooleanValue( (text == null ? null : Boolean.valueOf( text )) );
                break;
            case DATE :
                Date d = null;
                try {
                    if ( text != null ) {
                        if ( DATE_CONVERTOR == null ) {
                            throw new IllegalArgumentException( "DATE_CONVERTOR has not been initialised." );
                        }
                        d = DATE_CONVERTOR.parse( text );
                    }
                } catch ( IllegalArgumentException e ) {
                }
                dcv.setDateValue( d );
                break;
            case NUMERIC :
                BigDecimal bd = null;
                try {
                    if ( text != null ) {
                        bd = new BigDecimal( text );
                    }
                } catch ( NumberFormatException e ) {
                }
                dcv.setNumericValue( bd );
                break;
        }

    }

    public CellValue<BigDecimal> makeNewRowNumberCellValue(BigDecimal initialValue) {
        // Rows are 0-based internally but 1-based in the UI
        CellValue<BigDecimal> cv = makeNewNumericCellValue();
        if ( initialValue != null ) {
            cv.setValue( initialValue );
        }
        return cv;
    }

    public CellValue<Analysis> makeNewAnalysisCellValue() {
        Analysis analysis = new Analysis();
        return new CellValue<Analysis>( analysis );
    }

}
