/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.cxf.ejb;

import org.apache.cxf.Bus;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.CxfWsContainer;

import java.util.Collection;

public class EjbWsContainer extends CxfWsContainer {
    private final BeanContext beanContext;

    public EjbWsContainer(Bus bus, PortData port, BeanContext beanContext, Collection<ServiceInfo> services) {
        super(bus, port, services);
        if (beanContext == null) throw new NullPointerException("deploymentInfo is null");
        this.beanContext = beanContext;
    }

    protected EjbEndpoint createEndpoint() {
    	return new EjbEndpoint(bus, port, beanContext, httpTransportFactory, availableServices);
    }
}
