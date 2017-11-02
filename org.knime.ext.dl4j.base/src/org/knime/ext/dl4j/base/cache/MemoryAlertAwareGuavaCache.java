/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   30.05.2017 (David Kolb): created
 */
package org.knime.ext.dl4j.base.cache;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import org.knime.core.data.util.memory.MemoryAlert;
import org.knime.core.data.util.memory.MemoryAlertListener;
import org.knime.core.data.util.memory.MemoryAlertSystem;
import org.knime.core.node.NodeLogger;
import org.knime.ext.dl4j.libs.DL4JPluginActivator;
import org.knime.ext.dl4j.libs.prefs.DL4JPreferencePage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Wrapper for a Guava Cache that listens to memory alerts from {@link MemoryAlertSystem} and cleans the cache if memory
 * gets low.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class MemoryAlertAwareGuavaCache {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(MemoryAlertAwareGuavaCache.class);

    // NB: instantiation as static is OK, as cache is not expensive.
    private final static MemoryAlertAwareGuavaCache CACHE = new MemoryAlertAwareGuavaCache();

    private boolean m_enableVerbose =
        DL4JPluginActivator.getDefault().getPreferenceStore().getBoolean(DL4JPreferencePage.P_ENABLE_VERBOSE_LOGGING);

    private Cache<UUID, Object> m_cache;

    private Semaphore m_gate = new Semaphore(1);

    private final int m_cacheSize = 20;

    private MemoryAlertAwareGuavaCache() {
        if (m_enableVerbose) {
            m_cache = CacheBuilder.newBuilder().maximumSize(m_cacheSize).softValues().recordStats().build();
        } else {
            m_cache = CacheBuilder.newBuilder().maximumSize(m_cacheSize).softValues().build();
        }

        MemoryAlertSystem.getInstance().addListener(new MemoryAlertListener() {
            @Override
            protected boolean memoryAlert(final MemoryAlert alert) {
                if (m_enableVerbose) {
                    LOGGER.debug(getClass().getName() + " released memory for " + m_cache.size() + " models");
                }
                if (m_gate.tryAcquire()) {
                    m_cache.invalidateAll();
                    m_cache.cleanUp();
                    m_gate.release();
                }
                return false;
            }
        });
    }

    /**
     * Associates value with key in this cache. If the cache previously contained a value associated with key, the old
     * value is replaced by value.
     *
     * @param key
     * @param value
     */
    public void put(final UUID key, final Object value) {
        m_cache.put(key, value);
        if (m_enableVerbose) {
            LOGGER.debug("Put: " + m_cache.stats());
            LOGGER.debug("Cache size: " + m_cache.size());
        }
    }

    /**
     * Returns the value associated with key in this cache, or <code>Optional.empty</code> if there is no cached value
     * for key.
     *
     * @param key
     * @return the value associated with key
     */
    public Optional<Object> get(final UUID key) {
        Object o = m_cache.getIfPresent(key);
        if (m_enableVerbose) {
            LOGGER.debug("Get: " + m_cache.stats());
            LOGGER.debug("Cache size: " + m_cache.size());
        }
        return o == null ? Optional.empty() : Optional.of(o);
    }

    /**
     * Returns the value associated with key in this cache, obtaining that value from valueLoader if necessary.
     *
     * @param key
     * @param valueLoader
     * @return the value associated with key
     * @throws ExecutionException
     */
    @SuppressWarnings("unchecked")
    public <V> V get(final UUID key, final Callable<V> valueLoader) throws ExecutionException {
        // NB: guava takes care about synchronization.
        // see: https://google.github.io/guava/releases/21.0/api/docs/com/google/common/cache/Cache.html
        V o = (V)m_cache.get(key, valueLoader);
        if (m_enableVerbose) {
            LOGGER.debug("GetOrLoad: " + m_cache.stats());
            LOGGER.debug("Cache size: " + m_cache.size());
        }
        return o;
    }

    /**
     * Removes the cache entry associated with the specified key.
     *
     * @param key
     */
    public void remove(final UUID key) {
        m_cache.invalidate(key);
        if (m_enableVerbose) {
            LOGGER.debug("Remove: " + m_cache.stats());
            LOGGER.debug("Cache size: " + m_cache.size());
        }
    }

    /**
     * Cleans up the cache, i.e. removes all invalidated objects.
     */
    public synchronized void cleanUp() {
        // NB: semaphore to avoid redundant cleanUps
        if (m_gate.tryAcquire()) {
            m_cache.cleanUp();
            m_gate.release();
        }
    }

    /**
     * @return singleton instance of this cache
     */
    public static MemoryAlertAwareGuavaCache getInstance() {
        return CACHE;
    }
}
