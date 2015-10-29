/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Jan S. Rellermeyer, IBM Research - initial API and implementation
 *******************************************************************************/

package org.osgi.impl.service.rest.client;

import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import org.json.JSONObject;
import org.osgi.framework.dto.BundleDTO;
import org.osgi.framework.dto.ServiceReferenceDTO;
import org.osgi.framework.startlevel.dto.BundleStartLevelDTO;
import org.osgi.framework.startlevel.dto.FrameworkStartLevelDTO;
import org.osgi.service.rest.client.RestClient;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.util.Series;

/**
 * Implementation of the (Java) REST client
 * 
 * @author Jan S. Rellermeyer, IBM Research
 */
public class RestClientImpl implements RestClient {

	private static final String	MT_FRAMEWORK_STARTLEVEL		= "application/org.osgi.framework.startlevel";

	private static final String	MT_BUNDLE					= "application/org.osgi.bundle";

	private static final String	MT_BUNDLES					= "application/org.osgi.bundles";

	private static final String	MT_BUNDLES_REPRESENTATIONS	= "application/org.osgi.bundles.representations";

	private static final String	MT_BUNDLE_STATE				= "application/org.osgi.bundle.state";

	private static final String	MT_BUNDLE_HEADER			= "application/org.osgi.bundle.header";

	private static final String	MT_BUNDLE_STARTLEVEL		= "application/org.osgi.bundle.startlevel";

	private static final String	MT_SERVICE					= "application/org.osgi.service";

	private static final String	MT_SERVICES					= "application/org.osgi.services";

	private static final String	MT_SERVICES_REPRESENTATIONS	= "application/org.osgi.services.representations";

	private static final String	MT_JSON_EXT					= "+json";

	private static final String	MT_XML_EXT					= "+xml";

	private final MediaType		FRAMEWORK_STARTLEVEL;

	private final MediaType		BUNDLE;

	private final MediaType		BUNDLES;

	private final MediaType		BUNDLES_REPRESENTATIONS;

	private final MediaType		BUNDLE_STATE;

	private final MediaType		BUNDLE_HEADER;

	private final MediaType		BUNDLE_STARTLEVEL;

	private final MediaType		SERVICE;

	private final MediaType		SERVICES;

	private final MediaType		SERVICES_REPRESENTATIONS;

	private final URI			baseUri;

	protected RestClientImpl(final URI uri, final boolean useXml) {
		this.baseUri = uri.normalize().resolve("/");
		final String ext = useXml ? MT_XML_EXT : MT_JSON_EXT;
		FRAMEWORK_STARTLEVEL = new MediaType(MT_FRAMEWORK_STARTLEVEL + ext);
		BUNDLE = new MediaType(MT_BUNDLE + ext);
		BUNDLES = new MediaType(MT_BUNDLES + ext);
		BUNDLES_REPRESENTATIONS = new MediaType(MT_BUNDLES_REPRESENTATIONS + ext);
		BUNDLE_STATE = new MediaType(MT_BUNDLE_STATE + ext);
		BUNDLE_HEADER = new MediaType(MT_BUNDLE_HEADER + ext);
		BUNDLE_STARTLEVEL = new MediaType(MT_BUNDLE_STARTLEVEL + ext);
		SERVICE = new MediaType(MT_SERVICE + ext);
		SERVICES = new MediaType(MT_SERVICES + ext);
		SERVICES_REPRESENTATIONS = new MediaType(MT_SERVICES_REPRESENTATIONS + ext);

	}

	/**
	 * @see org.osgi.rest.client.RestClient#getFrameworkStartLevel()
	 */
	public FrameworkStartLevelDTO getFrameworkStartLevel() throws Exception {
		final Representation repr = new ClientResource(Method.GET,
				baseUri.resolve("framework/startlevel"))
				.get(FRAMEWORK_STARTLEVEL);

		return DTOReflector.getDTO(FrameworkStartLevelDTO.class, repr);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#setFrameworkStartLevel(org.osgi.dto.framework
	 *      .startlevel.FrameworkStartLevelDTO)
	 */
	public void setFrameworkStartLevel(final FrameworkStartLevelDTO startLevel)
			throws Exception {
		new ClientResource(Method.PUT, baseUri.resolve("framework/startlevel")).put(
				DTOReflector.getJson(FrameworkStartLevelDTO.class, startLevel),
				FRAMEWORK_STARTLEVEL);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundles()
	 */
	public Collection<String> getBundlePaths() throws Exception {
		final ClientResource res = new ClientResource(Method.GET,
				baseUri.resolve("framework/bundles"));
		final Representation repr = res.get(BUNDLES);

		System.err.println("HAVING MEDIA TYPE " + repr.getMediaType());

		return DTOReflector.getStrings(repr);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleRepresentations()
	 */
	public Collection<BundleDTO> getBundles() throws Exception {
		try {
			final Representation repr = new ClientResource(Method.GET,
					baseUri.resolve("framework/bundles/representations"))
					.get(BUNDLES_REPRESENTATIONS);

			return DTOReflector.getDTOs(BundleDTO.class, repr);
		} catch (final ResourceException e) {
			if (Status.CLIENT_ERROR_NOT_FOUND.equals(e.getStatus())) {
				return null;
			}
			throw e;
		}
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundle(long)
	 */
	public BundleDTO getBundle(final long id) throws Exception {
		return getBundle("framework/bundle/" + id);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundle(java.lang.String)
	 */
	public BundleDTO getBundle(final String bundlePath) throws Exception {
		try {
			final Representation repr = new ClientResource(Method.GET,
					baseUri.resolve(bundlePath)).get(BUNDLE);
			return DTOReflector.getDTO(BundleDTO.class, repr);
		} catch (final ResourceException e) {
			if (Status.CLIENT_ERROR_NOT_FOUND.equals(e.getStatus())) {
				return null;
			}
			throw e;
		}
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleState(long)
	 */
	public int getBundleState(final long id) throws Exception {
		return getBundleState("framework/bundle/" + id);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleState(java.lang.String)
	 */
	public int getBundleState(final String bundlePath) throws Exception {
		final Representation repr = new ClientResource(Method.GET,
				baseUri.resolve(bundlePath + "/state")).get(BUNDLE_STATE);

		// FIXME: hardcoded to JSON
		final JSONObject obj = new JsonRepresentation(repr).getJsonObject();
		return obj.getInt("state");
	}

	/**
	 * @see org.osgi.rest.client.RestClient#startBundle(long)
	 */
	public void startBundle(final long id) throws Exception {
		startBundle("framework/bundle/" + id, 0);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#startBundle(long)
	 */
	public void startBundle(final long id, final int options) throws Exception {
		startBundle("framework/bundle/" + id, options);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#startBundle(java.lang.String)
	 */
	public void startBundle(final String bundlePath) throws Exception {
		startBundle(bundlePath, 0);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#startBundle(java.lang.String)
	 */
	public void startBundle(final String bundlePath, final int options)
			throws Exception {
		// FIXME: hardcoded to JSON
		final JSONObject state = new JSONObject();
		state.put("state", 32);
		state.put("options", options);
		new ClientResource(Method.PUT, baseUri.resolve(bundlePath + "/state"))
				.put(state, BUNDLE_STATE);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#stopBundle(long)
	 */
	public void stopBundle(final long id) throws Exception {
		stopBundle("framework/bundle/" + id, 0);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#stopBundle(long)
	 */
	public void stopBundle(final long id, final int options) throws Exception {
		stopBundle("framework/bundle/" + id, options);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#stopBundle(java.lang.String)
	 */
	public void stopBundle(final String bundlePath) throws Exception {
		stopBundle(bundlePath, 0);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#stopBundle(java.lang.String)
	 */
	public void stopBundle(final String bundlePath, final int options)
			throws Exception {
		final JSONObject state = new JSONObject();
		state.put("state", 4);
		state.put("options", options);
		new ClientResource(Method.PUT, baseUri.resolve(bundlePath + "/state"))
				.put(state, BUNDLE_STATE);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleHeaders(long)
	 */
	public Map<String, String> getBundleHeaders(final long id) throws Exception {
		return getBundleHeaders("framework/bundle/" + id);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleHeaders(java.lang.String)
	 */
	public Map<String, String> getBundleHeaders(final String bundlePath)
			throws Exception {
		final Representation repr = new ClientResource(Method.GET,
				baseUri.resolve(bundlePath + "/header"))
				.get(BUNDLE_HEADER);

		return DTOReflector.getMap(repr);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleStartLevel(long)
	 */
	public BundleStartLevelDTO getBundleStartLevel(final long id)
			throws Exception {
		return getBundleStartLevel("framework/bundle/" + id);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getBundleStartLevel(java.lang.String)
	 */
	public BundleStartLevelDTO getBundleStartLevel(final String bundlePath)
			throws Exception {
		final Representation repr = new ClientResource(Method.GET,
				baseUri.resolve(bundlePath + "/startlevel"))
				.get(BUNDLE_STARTLEVEL);

		return DTOReflector.getDTO(BundleStartLevelDTO.class, repr);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#setBundleStartLevel(long,
	 *      org.osgi.dto.framework.startlevel.BundleStartLevelDTO)
	 */
	public void setBundleStartLevel(final long id,
			final int startLevel) throws Exception {
		setBundleStartLevel("framework/bundle/" + id, startLevel);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#setBundleStartLevel(java.lang.String,
	 *      org.osgi.dto.framework.startlevel.BundleStartLevelDTO)
	 */
	public void setBundleStartLevel(final String bundlePath,
			final int startLevel) throws Exception {
		BundleStartLevelDTO bsl = new BundleStartLevelDTO();
		bsl.startLevel = startLevel;
		new ClientResource(Method.PUT, baseUri.resolve(bundlePath
				+ "/startlevel")).put(
				DTOReflector.getJson(BundleStartLevelDTO.class, bsl),
				BUNDLE_STARTLEVEL);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#installBundle(java.net.URL)
	 */
	public BundleDTO installBundle(final String url) throws Exception {
		final ClientResource res = new ClientResource(Method.POST,
				baseUri.resolve("framework/bundles"));
		final Representation repr = res.post(url, MediaType.TEXT_PLAIN);

		return getBundle(repr.getText());
	}

	/**
	 * @see org.osgi.rest.client.RestClient#installBundle(java.io.InputStream)
	 */
	public BundleDTO installBundle(final String location, final InputStream in)
			throws Exception {
		final ClientResource res = new ClientResource(Method.POST,
				baseUri.resolve("framework/bundles"));
		@SuppressWarnings("unchecked")
		Series<Header> headers = (Series<Header>) res.getRequestAttributes().get("org.restlet.http.headers");
		if (headers == null) {
			headers = new Series<Header>(Header.class);
			res.getRequestAttributes().put("org.restlet.http.headers", headers);
		}
		headers.add("Content-Location", location);

		/*
		 * does not work in the current RESTLET version:
		 * res.getRequest().getAttributes() .put("message.entity.locationRef",
		 * new Reference(location));
		 */
		final Representation repr = res.post(in);

		return getBundle(repr.getText());
	}

	/**
	 * @see org.osgi.rest.client.RestClient#updateBundle(long)
	 */
	public BundleDTO updateBundle(final long id) throws Exception {
		new ClientResource(Method.PUT, baseUri.resolve("framework/bundle/"
				+ id)).put("", MediaType.TEXT_PLAIN);
		return null; // TODO return a BundleDTO
	}

	/**
	 * @see org.osgi.rest.client.RestClient#updateBundle(long, java.net.URL)
	 */
	public BundleDTO updateBundle(final long id, final String url) throws Exception {
		new ClientResource(Method.PUT, baseUri.resolve("framework/bundle/"
				+ id)).put(url, MediaType.TEXT_PLAIN);
		return null; // TODO return a BundleDTO
	}

	/**
	 * @see org.osgi.rest.client.RestClient#updateBundle(long,
	 *      java.io.InputStream)
	 */
	public BundleDTO updateBundle(final long id, final InputStream in)
			throws Exception {
		new ClientResource(Method.PUT, baseUri.resolve("framework/bundle/"
				+ id)).put(in);
		return null; // TODO return a BundleDTO
	}

	/**
	 * @see org.osgi.rest.client.RestClient#uninstallBundle(long)
	 */
	public BundleDTO uninstallBundle(final long id) throws Exception {
		return uninstallBundle("framework/bundle/" + id);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#uninstallBundle(java.lang.String)
	 */
	public BundleDTO uninstallBundle(final String bundlePath) throws Exception {
		final ClientResource res = new ClientResource(Method.DELETE,
				baseUri.resolve(bundlePath));
		res.delete();
		return null; // TODO return a BundleDTO
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getServices()
	 */
	public Collection<String> getServicePaths() throws Exception {
		return getServicePaths(null);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getServices(java.lang.String)
	 */
	public Collection<String> getServicePaths(final String filter) throws Exception {
		final ClientResource res = new ClientResource(Method.GET,
				baseUri.resolve("framework/services"));

		if (filter != null) {
			res.addQueryParameter("filter", filter);
		}

		final Representation repr = res.get(SERVICES);

		return DTOReflector.getStrings(repr);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getServiceRepresentations()
	 */
	public Collection<ServiceReferenceDTO> getServiceReferences()
			throws Exception {
		return getServiceReferences(null);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getServiceRepresentations(java.lang.String
	 *      )
	 */
	public Collection<ServiceReferenceDTO> getServiceReferences(
			final String filter) throws Exception {
		final ClientResource res = new ClientResource(Method.GET,
				baseUri.resolve("framework/services/representations"));
		if (filter != null) {
			res.addQueryParameter("filter", filter);
		}
		final Representation repr = res.get(SERVICES_REPRESENTATIONS);

		return DTOReflector.getDTOs(ServiceReferenceDTO.class, repr);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getServiceReference(long)
	 */
	public ServiceReferenceDTO getServiceReference(final long id)
			throws Exception {
		return getServiceReference("framework/service/" + id);
	}

	/**
	 * @see org.osgi.rest.client.RestClient#getServiceReference(java.lang.String)
	 */
	public ServiceReferenceDTO getServiceReference(final String servicePath)
			throws Exception {
		final Representation repr = new ClientResource(Method.GET,
				baseUri.resolve(servicePath)).get(SERVICE);

		return DTOReflector.getDTO(ServiceReferenceDTO.class,
				repr);
	}


}
