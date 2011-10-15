package org.apache.tomee.embedded;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.NetworkUtil;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.validation.ValidationException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * @author rmannibucau
 */
public class EmbeddedTomEEContainer extends EJBContainer {
    public static final String TOMEE_EJBCONTAINER_HTTP_PORT = "tomee.ejbcontainer.http.port";
    private static EmbeddedTomEEContainer tomEEContainer;

    private Container container = new Container();
    private String appId;

    private EmbeddedTomEEContainer(String id) {
        appId = id;
    }

    @Override public void close() {
        try {
            if (tomEEContainer.container.getAppContexts(appId) != null) {
                tomEEContainer.container.undeploy(appId);
            }
            tomEEContainer.container.stop();
        } catch (Exception e) {
            throw Exceptions.newEJBException(e);
        }
        tomEEContainer = null;
    }

    @Override public Context getContext() {
        return tomEEContainer.container.getAppContexts(appId).getGlobalJndiContext();
    }

    public static class EmbeddedTomEEContainerProvider implements EJBContainerProvider {
        @Override public EJBContainer createEJBContainer(Map<?, ?> properties) {
            Object provider = properties.get(EJBContainer.PROVIDER);
            if (provider != null && !provider.equals(EmbeddedTomEEContainer.class) && !provider.equals(EmbeddedTomEEContainer.class.getName())) {
                return null;
            }

            if (tomEEContainer != null) {
                return tomEEContainer;
            }

            final String appId = (String) properties.get(EJBContainer.APP_NAME);
            final Object modules = properties.get(EJBContainer.MODULES);

            tomEEContainer = new EmbeddedTomEEContainer(appId);
            Configuration configuration = new Configuration();
            if (properties.containsKey(TOMEE_EJBCONTAINER_HTTP_PORT)) {
                int port;
                Object portValue = properties.get(TOMEE_EJBCONTAINER_HTTP_PORT);
                if (portValue instanceof Integer) {
                    port = (Integer) portValue;
                } else if (portValue instanceof String) {
                    port = Integer.parseInt((String) portValue);
                } else {
                    throw new RuntimeException("port value should be an integer or a string");
                }
                if (port <= 0) {
                    port = NetworkUtil.getNextAvailablePort();
                    System.setProperty(TOMEE_EJBCONTAINER_HTTP_PORT, Integer.toString(port));
                }
                configuration.setHttpPort(port);
            }
            tomEEContainer.container.setup(configuration);
            try {
                tomEEContainer.container.start();

                if (modules instanceof File) {
                    tomEEContainer.container.deploy(appId, ((File) modules));
                } else if (modules instanceof String) {
                    tomEEContainer.container.deploy(appId, new File((String) modules));
                } else {
                    try {
                        tomEEContainer.close();
                    } catch (Exception e) {
                        // no-op
                    }
                    tomEEContainer = null;
                    throw Exceptions.newNoModulesFoundException();
                }

                return tomEEContainer;
            } catch (OpenEJBException e) {
                throw new EJBException(e);
            } catch (MalformedURLException e) {
                throw new EJBException(e);
            } catch (ValidationException ve) {
                throw ve;
            } catch (Exception e) {
                if (e instanceof EJBException) {
                    throw (EJBException) e;
                }
                throw new RuntimeException("initialization exception", e);
            } finally {
                if (tomEEContainer == null) {
                    try {
                        tomEEContainer.close();
                    } catch (Exception e) {
                        // no-op
                    }
                }
            }
        }
    }
}
