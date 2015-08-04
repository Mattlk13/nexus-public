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
package org.sonatype.security.realms.validator;

import java.util.Set;

import org.sonatype.configuration.validation.ValidationRequest;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.security.model.CPrivilege;
import org.sonatype.security.model.CRole;
import org.sonatype.security.model.CUser;
import org.sonatype.security.model.CUserRoleMapping;
import org.sonatype.security.model.Configuration;

public interface SecurityConfigurationValidator
{
  ValidationResponse validateModel(ValidationRequest<Configuration> request);

  ValidationResponse validatePrivilege(SecurityValidationContext ctx, CPrivilege privilege, boolean update);

  ValidationResponse validateRoleContainment(SecurityValidationContext ctx);

  ValidationResponse validateRole(SecurityValidationContext ctx, CRole role, boolean update);

  ValidationResponse validateUser(SecurityValidationContext ctx, CUser user, Set<String> roles, boolean update);

  ValidationResponse validateUserRoleMapping(SecurityValidationContext ctx, CUserRoleMapping userRoleMapping,
                                             boolean update);
}
