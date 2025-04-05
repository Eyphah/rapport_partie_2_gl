/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.web.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.catalina.connector.Connector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http2.Http2Protocol;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.util.LambdaSafe;
import org.springframework.boot.web.embedded.tomcat.CompressionConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.SslConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl.ServerNameSslBundle;
import org.springframework.util.Assert;
import org.springframework.boot.exceptions.*;
import org.springframework.util.StringUtils;

/**
 * Abstract base class for {@link ConfigurableWebServerFactory} implementations.
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Ivan Sopov
 * @author Eddú Meléndez
 * @author Brian Clozel
 * @author Scott Frederick
 * @since 2.0.0
 */
public abstract class AbstractConfigurableWebServerFactory implements ConfigurableWebServerFactory {

	private static final Log logger = LogFactory.getLog(AbstractConfigurableWebServerFactory.class);

	private int port = 8080;

	protected Set<TomcatConnectorCustomizer> tomcatConnectorCustomizers = new LinkedHashSet<>();

	private InetAddress address;

	private Set<ErrorPage> errorPages = new LinkedHashSet<>();

	private Ssl ssl;

	private SslBundles sslBundles;

	private Http2 http2;

	private Compression compression;

	private String serverHeader;

	private Shutdown shutdown = Shutdown.IMMEDIATE;

	protected Charset uriEncoding = DEFAULT_CHARSET;

	protected Set<TomcatProtocolHandlerCustomizer<?>> tomcatProtocolHandlerCustomizers = new LinkedHashSet<>();

	protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	/**
	 * Create a new {@link AbstractConfigurableWebServerFactory} instance.
	 */
	protected AbstractConfigurableWebServerFactory() {
	}

	/**
	 * Create a new {@link AbstractConfigurableWebServerFactory} instance with the
	 * specified port.
	 * @param port the port number for the web server
	 */
	protected AbstractConfigurableWebServerFactory(int port) {
		this.port = port;
	}

	/**
	 * The port that the web server listens on.
	 * @return the port
	 */
	public int getPort() {
		return this.port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Set the character encoding to use for URL decoding. If not specified 'UTF-8' will
	 * be used.
	 * @param uriEncoding the uri encoding to set
	 */

	public void setUriEncoding(Charset uriEncoding) {
		this.uriEncoding = uriEncoding;
	}

	/**
	 * Returns the character encoding to use for URL decoding.
	 * @return the URI encoding
	 */
	public Charset getUriEncoding() {
		return this.uriEncoding;
	}

	/**
	 * Return the address that the web server binds to.
	 * @return the address
	 */
	public InetAddress getAddress() {
		return this.address;
	}

	@Override
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	// Needs to be protected so it can be used by subclasses
	protected void customizeConnector(Connector connector) {
		int port = Math.max(getPort(), 0);
		connector.setPort(port);
		if (StringUtils.hasText(getServerHeader())) {
			connector.setProperty("server", getServerHeader());
		}
		if (connector.getProtocolHandler() instanceof AbstractProtocol<?> abstractProtocol) {
			customizeProtocol(abstractProtocol);
		}
		invokeProtocolHandlerCustomizers(connector.getProtocolHandler());
		if (getUriEncoding() != null) {
			connector.setURIEncoding(getUriEncoding().name());
		}
		if (getHttp2() != null && getHttp2().isEnabled()) {
			connector.addUpgradeProtocol(new Http2Protocol());
		}
		if (Ssl.isEnabled(getSsl())) {
			customizeSsl(connector);
		}
		TomcatConnectorCustomizer compression = new CompressionConnectorCustomizer(getCompression());
		compression.customize(connector);
		for (TomcatConnectorCustomizer customizer : this.tomcatConnectorCustomizers) {
			customizer.customize(connector);
		}
	}

	private void customizeSsl(Connector connector) {
		SslConnectorCustomizer customizer = new SslConnectorCustomizer(logger, connector, getSsl().getClientAuth());
		customizer.customize(getSslBundle(), getServerNameSslBundles());
		addBundleUpdateHandler(null, getSsl().getBundle(), customizer);
		getSsl().getServerNameBundles()
				.forEach((serverNameSslBundle) -> addBundleUpdateHandler(serverNameSslBundle.serverName(),
						serverNameSslBundle.bundle(), customizer));
	}

	private void addBundleUpdateHandler(String serverName, String sslBundleName, SslConnectorCustomizer customizer) {
		if (StringUtils.hasText(sslBundleName)) {
			getSslBundles().addBundleUpdateHandler(sslBundleName,
					(sslBundle) -> customizer.update(serverName, sslBundle));
		}
	}

	private void customizeProtocol(AbstractProtocol<?> protocol) {
		if (getAddress() != null) {
			protocol.setAddress(getAddress());
		}
	}

	@SuppressWarnings("unchecked")
	private void invokeProtocolHandlerCustomizers(ProtocolHandler protocolHandler) {
		LambdaSafe
				.callbacks(TomcatProtocolHandlerCustomizer.class, this.tomcatProtocolHandlerCustomizers, protocolHandler)
				.invoke((customizer) -> customizer.customize(protocolHandler));
	}

	/**
	 * Returns a mutable set of {@link ErrorPage ErrorPages} that will be used when
	 * handling exceptions.
	 * @return the error pages
	 */
	public Set<ErrorPage> getErrorPages() {
		return this.errorPages;
	}

	@Override
	public void setErrorPages(Set<? extends ErrorPage> errorPages) {
		Assert.notNull(errorPages, "'errorPages' must not be null");
		this.errorPages = new LinkedHashSet<>(errorPages);
	}

	@Override
	public void addErrorPages(ErrorPage... errorPages) {
		Assert.notNull(errorPages, "'errorPages' must not be null");
		this.errorPages.addAll(Arrays.asList(errorPages));
	}

	public Ssl getSsl() {
		return this.ssl;
	}

	@Override
	public void setSsl(Ssl ssl) {
		this.ssl = ssl;
	}

	/**
	 * Return the configured {@link SslBundles}.
	 * @return the {@link SslBundles} or {@code null}
	 * @since 3.2.0
	 */
	public SslBundles getSslBundles() {
		return this.sslBundles;
	}

	@Override
	public void setSslBundles(SslBundles sslBundles) {
		this.sslBundles = sslBundles;
	}

	public Http2 getHttp2() {
		return this.http2;
	}

	@Override
	public void setHttp2(Http2 http2) {
		this.http2 = http2;
	}

	public Compression getCompression() {
		return this.compression;
	}

	@Override
	public void setCompression(Compression compression) {
		this.compression = compression;
	}

	public String getServerHeader() {
		return this.serverHeader;
	}

	@Override
	public void setServerHeader(String serverHeader) {
		this.serverHeader = serverHeader;
	}

	@Override
	public void setShutdown(Shutdown shutdown) {
		this.shutdown = shutdown;
	}

	/**
	 * Returns the shutdown configuration that will be applied to the server.
	 * @return the shutdown configuration
	 * @since 2.3.0
	 */
	public Shutdown getShutdown() {
		return this.shutdown;
	}

	/**
	 * Return the {@link SslBundle} that should be used with this server.
	 * @return the SSL bundle
	 */
	protected final SslBundle getSslBundle() {
		return WebServerSslBundle.get(this.ssl, this.sslBundles);
	}

	protected final Map<String, SslBundle> getServerNameSslBundles() {
		return this.ssl.getServerNameBundles()
			.stream()
			.collect(Collectors.toMap(ServerNameSslBundle::serverName,
					(serverNameSslBundle) -> this.sslBundles.getBundle(serverNameSslBundle.bundle())));
	}

	/**
	 * Return the absolute temp dir for given web server.
	 * @param prefix server name
	 * @return the temp dir for given server.
	 */
	protected final File createTempDir(String prefix) {
		try {
			File tempDir = Files.createTempDirectory(prefix + "." + getPort() + ".").toFile();
			tempDir.deleteOnExit();
			return tempDir;
		}
		catch (IOException ex) {
			throw new WebServerException(
					"Unable to create tempDir. java.io.tmpdir is set to " + System.getProperty("java.io.tmpdir"), ex);
		}
	}

}
