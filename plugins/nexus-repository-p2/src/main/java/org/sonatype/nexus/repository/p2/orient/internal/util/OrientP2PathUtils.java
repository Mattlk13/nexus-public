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
package org.sonatype.nexus.repository.p2.orient.internal.util;

import java.io.IOException;

import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.repository.p2.internal.AssetKind;
import org.sonatype.nexus.repository.p2.internal.metadata.P2Attributes;
import org.sonatype.nexus.repository.p2.internal.util.P2PathUtils;
import org.sonatype.nexus.repository.p2.internal.util.P2TempBlobUtils;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;
import org.sonatype.nexus.repository.view.payloads.TempBlob;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.p2.orient.internal.OrientP2Facet.HASH_ALGORITHMS;

/**
 * Utility methods for working with P2 routes and paths.
 */
public final class OrientP2PathUtils
{
  public static final String PLUGIN_NAME = "pluginName";

  private static final String NAME_VERSION_SEPARATOR = "_";

  private static final String EXTENSION = "extension";

  private OrientP2PathUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Utility method encapsulating getting a particular token by name from a matcher, including preconditions.
   */
  private static String match(final State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  /**
   * Returns the name from a {@link State}.
   */
  public static String name(final State state) {
    return match(state, "name");
  }

  /**
   * Returns the name and extension from a {@link State}.
   */
  public static String filename(final State state) {
    return name(state) + '.' + extension(state);
  }

  public static String version(final State state) {
    return match(state, "version");
  }

  /**
   * Returns the Component Name from the name as a default from a {@link State}.
   *
   * @see #name(State)
   */
  public static String componentName(final State state) {
    return name(state).split(NAME_VERSION_SEPARATOR)[0];
  }

  /**
   * Returns the Version from the name as a default from a {@link State}.
   *
   * @see #name(State)
   */
  public static String componentVersion(final State state) {
    return name(state).split(NAME_VERSION_SEPARATOR)[1];
  }

  /**
   * Returns the extension from a {@link State}.
   */
  public static String extension(final State state) {
    return match(state, EXTENSION);
  }

  /**
   * Returns the {@link State} for the content.
   */
  public static State matcherState(final Context context) {
    return context.getAttributes().require(State.class);
  }

  public static P2Attributes toP2Attributes(final String path, final State state) {
    return P2Attributes.builder()
        .componentName(componentName(state))
        .componentVersion(version(state))
        .extension(extension(state))
        .fileName(filename(state))
        .path(path)
        .build();
  }

  public static P2Attributes toP2AttributesBinary(final String path, final State state) {
    return P2Attributes.builder()
        .pluginName(name(state))
        .componentName(name(state))
        .componentVersion(version(state))
        .path(path)
        .build();
  }

  public static P2Attributes getBinaryAttributesFromBlobName(final String blobName) {
    P2Attributes.Builder attributes = P2Attributes.builder();
    //https/download.eclipse.org/technology/epp/packages/2019-12/binary/epp.package.java.executable.cocoa.macosx.x86_64_4.14.0.20191212-1200
    String version = getBinaryVersionFromBlobName(blobName);
    String name = getBinaryNameFromBlobName(blobName, version);
    AssetKind assetKind = P2PathUtils.getAssetKind(blobName);

    attributes.componentName(name);
    attributes.componentVersion(version);
    attributes.pluginName(name);
    attributes.assetKind(assetKind);

    return attributes.build();
  }

  public static P2Attributes getPackageAttributesFromBlob(final StorageFacet storageFacet,
                                                          final P2TempBlobUtils p2TempBlobUtils,
                                                          final Blob blob,
                                                          final String blobName)
      throws IOException
  {
    P2Attributes.Builder attributes = P2Attributes.builder();

    try (TempBlob tempBlob = storageFacet.createTempBlob(blob.getInputStream(), HASH_ALGORITHMS)) {
      String extension = getPackageExtensionFromBlobName(blobName);
      P2Attributes p2Attributes = P2Attributes.builder().extension(extension).build();
      P2Attributes mergedAttributes = p2TempBlobUtils.mergeAttributesFromTempBlob(tempBlob, p2Attributes);
      AssetKind assetKind = P2PathUtils.getAssetKind(blobName);

      attributes.componentName(mergedAttributes.getComponentName());
      attributes.componentVersion(mergedAttributes.getComponentVersion());
      attributes.pluginName(mergedAttributes.getPluginName());
      attributes.assetKind(assetKind);
    }

    return attributes.build();
  }

  private static String getBinaryNameFromBlobName(final String blobName, final String version) {
    String[] namePaths = blobName.split("/");
    return namePaths[namePaths.length - 1].replace("_" + version, "");
  }

  private static String getBinaryVersionFromBlobName(final String blobName) {
    String[] versionPaths = blobName.split("_");
    return versionPaths[versionPaths.length - 1];
  }

  private static String getPackageExtensionFromBlobName(final String blobName) {
    String[] paths = blobName.split("\\.");
    return paths[paths.length - 1];
  }
}
