/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Open Source Version is distributed with Sencha Ext JS pursuant to a FLOSS Exception agreed upon
 * between Sonatype, Inc. and Sencha Inc. Sencha Ext JS is licensed under GPL v3 and cannot be redistributed as part of a
 * closed source work.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/*global Ext*/

/**
 *  **{@link Ext.direct.RemotingProvider}** overrides.
 *
 *  @since 3.0
 */
Ext.define('NX.ext.direct.RemotingProvider', {
  override: 'Ext.direct.RemotingProvider',

  /**
   * Avoid buffering if "enableBuffer" option is false.
   * Ensure timeout is set on requests.
   *
   * @override
   */
  queueTransaction: function(transaction) {
    // NEXUS-18220, NEXUS-18494 - Usages of NX.direct.* may not have set the user specified connection timeout
    transaction.timeout = transaction.timeout || Ext.Ajax.getTimeout();

    if (transaction.callbackOptions && transaction.callbackOptions.enableBuffer === false) {
      this.sendTransaction(transaction);
      return;
    }
    this.callParent(arguments);
  },

  /**
   * Ensure timeout is set on requests.
   *
   * @since 3.15
   * @override
   */
  sendTransaction: function(transaction) {
    // NEXUS-18220, NEXUS-18494 - Usages of NX.direct.* may not have set the user specified connection timeout
    transaction.timeout = transaction.timeout || Ext.Ajax.getTimeout();
    this.callParent(arguments);
  }
});
