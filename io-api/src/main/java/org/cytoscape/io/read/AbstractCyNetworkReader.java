package org.cytoscape.io.read;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public abstract class AbstractCyNetworkReader extends AbstractTask implements
		CyNetworkReader {

	private static final String CREATE_NEW_COLLECTION_STRING = "Create new network collection";

	private final Map<String, CyRootNetwork> name2RootMap;
	private final Map<Object, CyNode> nodeMap;

	private ListSingleSelection<String> rootNetworkList;
	private ListSingleSelection<String> targetColumnList;

	/**
	 * Data stream for the networks to be created.
	 */
	protected InputStream inputStream;

	/**
	 * Array of networks to be returned.
	 */
	protected CyNetwork[] networks;

	/**
	 * Will be used for creating network views.
	 */
	protected final CyNetworkViewFactory cyNetworkViewFactory;

	/**
	 * Will be used to create new CySubNetwork if this reader needs to create
	 * new CyRootNetwork.
	 */
	protected final CyNetworkFactory cyNetworkFactory;

	@ProvidesTitle
	public String getTitle() {
		return "Import Network";
	}

	@Tunable(description = "Node Identifier Mapping Column:", groups = " ", listenForChange = { "RootNetworkList" })
	public ListSingleSelection<String> getTargetColumnList() {
		return targetColumnList;
	}

	public void setTargetColumnList(ListSingleSelection<String> colList) {
		this.targetColumnList = colList;
		// looks like this does not have any effect, is this a bug?
		this.targetColumnList.setSelectedValue(CyRootNetwork.SHARED_NAME);
	}

	@Tunable(description = "Network Collection:", groups = " ")
	public ListSingleSelection<String> getRootNetworkList() {
		return rootNetworkList;
	}

	public void setRootNetworkList(final ListSingleSelection<String> roots) {
		if (rootNetworkList.getSelectedValue().equalsIgnoreCase(
				CREATE_NEW_COLLECTION_STRING)) {
			// set default
			List<String> colNames = new ArrayList<String>();
			colNames.add(CyRootNetwork.SHARED_NAME);
			targetColumnList = new ListSingleSelection<String>(colNames);
			return;
		}
		targetColumnList = getTargetColumns(name2RootMap.get(rootNetworkList
				.getSelectedValue()));
	}

	private final ListSingleSelection<String> getTargetColumns(
			final CyNetwork network) {
		final CyTable selectedTable = network.getTable(CyNode.class,
				CyRootNetwork.SHARED_ATTRS);
		final List<String> colNames = new ArrayList<String>();

		// Work-around to make the "shared name" the first in the list
		boolean containSharedName = false;
		// check if "shared name" column exist
		if (CyTableUtil.getColumnNames(selectedTable).contains(
				CyRootNetwork.SHARED_NAME)) {
			containSharedName = true;
			colNames.add(CyRootNetwork.SHARED_NAME);
		}

		for (final CyColumn col : selectedTable.getColumns()) {
			// Exclude SUID from the mapping key list
			if (col.getName().equalsIgnoreCase(CyIdentifiable.SUID)) {
				continue;
			}

			if (col.getName().equalsIgnoreCase(CyRootNetwork.SHARED_NAME)
					&& containSharedName) {
				// "shared name" is already added in the first
				continue;
			}
			colNames.add(col.getName());
		}

		return new ListSingleSelection<String>(colNames);
	}

	/**
	 * 
	 * 
	 * @param inputStream
	 * @param cyNetworkViewFactory
	 * @param cyNetworkFactory
	 * @param cyNetworkManager
	 * @param cyRootNetworkManager
	 * @param cyApplicationManager
	 */
	public AbstractCyNetworkReader(final InputStream inputStream,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory,
			final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager) {

		if (inputStream == null)
			throw new NullPointerException("Input stream is null");
		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null");
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null");
		if (cyRootNetworkManager == null)
			throw new NullPointerException("CyRootNetworkManager is null");

		this.inputStream = inputStream;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;

		// initialize the network Collection
		this.name2RootMap = getRootNetworkMap(cyNetworkManager, cyRootNetworkManager);
		this.nodeMap = new HashMap<Object, CyNode>(10000);

		final List<String> rootNames = new ArrayList<String>();
		rootNames.add(CREATE_NEW_COLLECTION_STRING);
		rootNames.addAll(name2RootMap.keySet());
		rootNetworkList = new ListSingleSelection<String>(rootNames);
		rootNetworkList.setSelectedValue(rootNames.get(0));

		// initialize target attribute list
		final List<String> colNames_target = new ArrayList<String>();
		colNames_target.add(CyRootNetwork.SHARED_NAME);
		this.targetColumnList = new ListSingleSelection<String>(colNames_target);
	}

	@Override
	public CyNetwork[] getNetworks() {
		return networks;
	}

	/**
	 * 
	 * Get target network collection, i.e., parent root network for all networks
	 * to be loaded.
	 * 
	 * @return Root network for this network collection. If there is no such
	 *         root, returns null.
	 * 
	 */
	protected final CyRootNetwork getRootNetwork() {
		final String networkCollectionName = this.rootNetworkList
				.getSelectedValue();
		final CyRootNetwork rootNetwork = this.name2RootMap
				.get(networkCollectionName);

		if (rootNetwork != null) {
			// Initialize the map of nodes only when we add network to existing
			// collection.
			this.initNodeMap(rootNetwork);
		}
		return rootNetwork;
	}

	/**
	 * Returns map from key value to existing CyNode.
	 * 
	 * @return
	 */
	protected Map<Object, CyNode> getNodeMap() {
		return this.nodeMap;
	}

	private final void initNodeMap(final CyRootNetwork rootNetwork) {
		final String keyColumnName = this.getTargetColumnList()
				.getSelectedValue();

		final List<CyNode> nodes = rootNetwork.getNodeList();
		for (final CyNode node : nodes) {
			final Object keyValue = rootNetwork.getRow(node).getRaw(
					keyColumnName);
			if (keyValue != null) {
				this.nodeMap.put(keyValue, node);
			}
		}
	}

	private final Map<String, CyRootNetwork> getRootNetworkMap(
			CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager) {

		final Map<String, CyRootNetwork> name2RootMap = new HashMap<String, CyRootNetwork>();

		for (CyNetwork net : cyNetworkManager.getNetworkSet()) {
			final CyRootNetwork rootNet = cyRootNetworkManager.getRootNetwork(net);
			if (!name2RootMap.containsValue(rootNet))
				name2RootMap.put(
						rootNet.getRow(rootNet).get(CyRootNetwork.NAME,
								String.class), rootNet);
		}

		return name2RootMap;
	}

}