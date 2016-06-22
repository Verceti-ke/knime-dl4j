/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.dl4j.base.nodes.layer;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.DLModelPortObjectSpec;
import org.knime.ext.dl4j.base.settings.impl.LayerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;

/**
 * Abstract superclass for layer node models of Deeplearning4J integration, 
 * contains default implementations for configure method.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public abstract class AbstractDLLayerNodeModel extends AbstractDLNodeModel {

	protected AbstractDLLayerNodeModel(final PortType[] inPortTypes,
            final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);
    }
	
	/** DNNModelPortObjectSpec to exchange specs from configure to execute */
    protected DLModelPortObjectSpec m_outputSpec;
    
    /**
     * Updates the spec with information of the current layer and performs some
     * checks on the spec.
     * 
     * @param inSpecs the spec to extend and check
     * @param dnnTypes the network types the current layer can be part of
     * @param dnnLayerType the type of the current layer
     * @param parameterSettings the settings of the current layer
     * @param logger a logger to log errors
     * @return extended and checked spec
     * @throws InvalidSettingsException 
     */
    protected DLModelPortObjectSpec[] configure(PortObjectSpec[] inSpecs, List<DNNType> dnnTypes, 
    		DNNLayerType dnnLayerType, LayerParameterSettingsModels parameterSettings, 
    		final NodeLogger logger) throws InvalidSettingsException{    	
    	DLModelPortObjectSpec spec = (DLModelPortObjectSpec)inSpecs[0];
    	
    	List<DNNType> newType;
    	/* this is the first layer of the net so we need to set the DNNType. 
    	 * It is assumed that the architecture(DNNType) of the first layer is 
    	 * the architecture of the network. */
    	if (spec.getNeuralNetworkTypes().contains(DNNType.EMPTY)){
    		newType = dnnTypes;
    	} else {    		
    		newType = spec.getNeuralNetworkTypes();
    	}   	
    	
    	//add this layer to list
    	List<DNNLayerType> newLayerTypes = new ArrayList<DNNLayerType>();
    	newLayerTypes.addAll(spec.getLayerTypes());
    	newLayerTypes.add(dnnLayerType);  

    	m_outputSpec = new DLModelPortObjectSpec(newType, newLayerTypes,false);
    	
    	//check for spec sanity
    	logWarnings(logger, ConfigurationUtils.validateSpec(m_outputSpec, dnnTypes));
    	
        return new DLModelPortObjectSpec[]{m_outputSpec};
    }        
}
