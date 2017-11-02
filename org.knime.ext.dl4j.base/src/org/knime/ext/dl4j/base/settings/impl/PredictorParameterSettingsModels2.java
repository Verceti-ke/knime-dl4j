/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
 * Website: http://www.knime.com; Email: contact@knime.com
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
package org.knime.ext.dl4j.base.settings.impl;

import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.dl4j.base.settings.IParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.enumerate.PredictorPrameter;

/**
 * Implementation of {@link IParameterSettingsModels} to store and create {@link SettingsModel}s for
 * {@link PredictorPrameter}s.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class PredictorParameterSettingsModels2 extends AbstractMapSetParameterSettingsModels<PredictorPrameter> {

    static final boolean DEFAULT_BOOLEAN = false;

    @Override
    public SettingsModel createParameter(final PredictorPrameter enumerate) throws IllegalArgumentException {
        switch (enumerate) {
            case APPEND_PREDICTION:
                return new SettingsModelBoolean("append_prediction", DEFAULT_BOOLEAN);
            case PREDICT_STEPS:
                return new SettingsModelBoolean("predict_steps", DEFAULT_BOOLEAN);
            case APPEND_SCORE:
                return new SettingsModelBoolean("append_score", DEFAULT_BOOLEAN);
            case APPEND_PROBABILITY:
                return new SettingsModelBoolean("append_probability", DEFAULT_BOOLEAN);
            case CHANGE_PREDICTION_COLUMN_NAME:
                return new SettingsModelBoolean("change_prediction_column_name", DEFAULT_BOOLEAN);
            case NEW_PREDICTION_COLUMN_NAME:
                return new SettingsModelString("new_prediction_column_name", "");
            case PROBABILITY_COLUMN_SUFFIX:
                return new SettingsModelString("probability_column_suffix", "");
            case PREDICTION_COLUMN_SUFFIX:
                return new SettingsModelString("prediction_column_suffix", "");
            case LAYER_SELECTION:
                return new SettingsModelString("layer_to_activate", "");
            default:
                throw new IllegalArgumentException("No case defined for Predictor Parameter: " + enumerate);
        }
    }
}
