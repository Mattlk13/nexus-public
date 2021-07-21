/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.golang.internal.datastore.hosted

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.golang.GolangFormat
import org.sonatype.nexus.repository.golang.internal.datastore.GoRecipeSupport
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet

import static org.sonatype.nexus.common.app.FeatureFlags.FEATURE_GOLANG_HOSTED
import static org.sonatype.nexus.common.property.SystemPropertiesHelper.getBoolean

/**
 * Go hosted recipe.
 *
 * @since 3.next
 */
@Named(GoHostedRecipe.NAME)
@Singleton
class GoHostedRecipe
    extends GoRecipeSupport
{
  protected static final String NAME = 'go-hosted'

  @Inject
  GoHostedHandlers hostedHandlers

  @Inject
  GoHostedRecipe(@Named(HostedType.NAME) final Type type, @Named(GolangFormat.NAME) final Format format)
  {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
    repository.attach(httpClientFacet.get())
    repository.attach(lastAssetMaintenanceFacet.get())
    repository.attach(contentFacet.get())
    repository.attach(searchFacet.get())
    repository.attach(browseFacet.get())
  }

  @Override
  boolean isFeatureEnabled() {
    return super.isFeatureEnabled() && getBoolean(FEATURE_GOLANG_HOSTED, false)
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder()

    addBrowseUnsupportedRoute(builder)

    [infoMatcher(), listMatcher()].each { matcher ->
      builder.route(new Route.Builder().matcher(matcher)
          .handler(timingHandler)
          .handler(securityHandler)
          .handler(exceptionHandler)
          .handler(handlerContributor)
          .handler(partialFetchHandler)
          .handler(contentHeadersHandler)
          .handler(hostedHandlers.get)
          .create())
    }

    [packageMatcher(), moduleMatcher()].each { matcher ->
      builder.route(new Route.Builder().matcher(matcher)
          .handler(timingHandler)
          .handler(securityHandler)
          .handler(exceptionHandler)
          .handler(handlerContributor)
          .handler(partialFetchHandler)
          .handler(contentHeadersHandler)
          .handler(lastDownloadedHandler)
          .handler(hostedHandlers.get)
          .create())
    }

    builder.route(new Route.Builder().matcher(uploadMatcher())
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(handlerContributor)
        .handler(conditionalRequestHandler)
        .handler(partialFetchHandler)
        .handler(contentHeadersHandler)
        .handler(hostedHandlers.upload)
        .create())

    builder.defaultHandlers(HttpHandlers.notFound())

    facet.configure(builder.create())

    return facet
  }
}
