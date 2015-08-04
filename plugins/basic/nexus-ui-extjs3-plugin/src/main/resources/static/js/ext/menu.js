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
/*global define*/
define('ext/menu', ['extjs', 'Nexus/error/ErrorHandler'], function(Ext, ErrorHandler){
  // HACK we cannot replace the menu class to check cfg.id, because initComponent has a static reference to Ext.menu.Menu -> recursion
  Ext.override(Ext.menu.Menu, {
    initComponentOrig : Ext.menu.Menu.prototype.initComponent,
    initComponent : function() {
      // HACK check if id was generated by Ext
      if (this.id.indexOf('ext-') !== 0 &&
            // weed out legitimate, ExtJS-internal use
            (this.id.indexOf('-hctx') !== this.id.length - 5 && this.id.indexOf('-hcols-menu') !== this.id.length - 11 )) {
        ErrorHandler.handleError(new Error('Menu config with fixed id leads to duplicate menu items: ' + this.id));
      }
      this.initComponentOrig.apply(this, arguments);
    }
  });

  return Ext.menu.Menu;
});