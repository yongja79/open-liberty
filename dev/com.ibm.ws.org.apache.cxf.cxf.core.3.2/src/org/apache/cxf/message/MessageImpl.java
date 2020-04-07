/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.message;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.apache.cxf.Bus;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.continuations.ContinuationProvider;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.security.SecurityContext;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.https.CertConstraints;

public class MessageImpl extends StringMapImpl implements Message {
    private static final long serialVersionUID = -3020763696429459865L;

    private Exchange exchange;
    private String id;
    private InterceptorChain interceptorChain;

    // array of Class<T>/T pairs for contents
    private Object[] contents = new Object[20];
    private int index;

    //Liberty code change start
    private static int protoHeaders = 0;
    private static int opStack = 1;
    private static int contentType = 2;
    private static int destination = 3;
    private static int queryString = 4;
    private static int httpRequest = 5;
    private static int httpResponse = 6;
    private static int pathToMatchSlash = 7;
    private static int httpRequestMethod = 8;
    private static int interceptorProviders = 9;
    private static int templateParameters = 10;
    private static int accept = 11;
    private static int continuationProvider = 12;
    private static int wsdlDescription = 13;
    private static int wsdlInterface = 14;
    private static int wsdlOperation = 15;
    private static int wsdlPort = 16;
    private static int wsdlService = 17;
    private static int requestUrl = 18;
    private static int requestUri = 19;
    private static int pathInfo = 20;
    private static int basePath = 21;
    private static int fixedParamOrder = 22;
    private static int inInterceptors = 23;
    private static int outInterceptors = 24;
    private static int responseCode = 25;
    private static int attachments = 26;
    private static int encoding = 27;
    private static int httpContext = 28;
    private static int httpConfig = 29;
    private static int httpContextMatchStrategy = 30;
    private static int httpBasePath = 31;
    private static int asyncPostDispatch = 32;
    private static int securityContext = 33;
    private static int authorizationPolicy = 34;
    private static int certConstraints = 35;
    private static int serviceRedirection = 36;
    private static int httpServletResponse = 37;
    private static int resourceMethod = 38;
    private static int oneWayRequest = 39;
    private static int asyncResponse = 40;
    private static int threadContextSwitched = 41;
    private static int cacheInputProperty = 42;
    private static int previousMessage = 43;
    private static int responseHeadersCopied = 44;
    private static int sseEventSink = 45;
    private static int requestorRole = 46;
    private static int partialResponse = 47;
    private static int emptyPartialResponse = 48;
    private static int endpointAddress = 49;
    private static int inboundMessage = 50;
    private static int TOTAL = 51;
    private Object[] propertyValues = new Object[TOTAL];
    
    private static final String REQUEST_PATH_TO_MATCH_SLASH = "path_to_match_slash";
    private static final String TEMPLATE_PARAMETERS = "jaxrs.template.parameters";
    private static final String CONTINUATION_PROVIDER = ContinuationProvider.class.getName();
    private static final String DESTINATION = Destination.class.getName();
    private static final String OP_RES_INFO_STACK = "org.apache.cxf.jaxrs.model.OperationResourceInfoStack";
    private static final String HTTP_BASE_PATH = "http.base.path";
    private static final String SECURITY_CONTEXT = SecurityContext.class.getName();
    private static final String AUTHORIZATION_POLICY = AuthorizationPolicy.class.getName();
    private static final String CERT_CONSTRAINTS = CertConstraints.class.getName();
    private static final String HTTP_SERVLET_RESPONSE = HttpServletResponse.class.getName();
    private static final String RESOURCE_METHOD = "org.apache.cxf.resource.method";
    private static final String ASYNC_RESPONSE = "javax.ws.rs.container.AsyncResponse";
    private static final String SSE_EVENT_SINK = "javax.ws.rs.sse.SseEventSink";
    private static final Map<String, Integer> KEYMAP;
    private static String[] propertyNames = new String[TOTAL];

    private static final Object NOT_FOUND = new Object();
    private static final Integer KEY_NOT_FOUND = Integer.valueOf(-1);
    
    static {
        Map<String, Integer> keymap = new HashMap<String, Integer>(TOTAL);
        propertyNames[contentType] = CONTENT_TYPE;
        keymap.put(CONTENT_TYPE, contentType);
        propertyNames[protoHeaders] = PROTOCOL_HEADERS;
        keymap.put(PROTOCOL_HEADERS, protoHeaders);
        propertyNames[queryString] = QUERY_STRING;
        keymap.put(QUERY_STRING, queryString);
        propertyNames[httpRequest] = AbstractHTTPDestination.HTTP_REQUEST;
        keymap.put(AbstractHTTPDestination.HTTP_REQUEST, httpRequest);
        propertyNames[httpResponse] = AbstractHTTPDestination.HTTP_RESPONSE;
        keymap.put(AbstractHTTPDestination.HTTP_RESPONSE, httpResponse);
        propertyNames[pathToMatchSlash] = REQUEST_PATH_TO_MATCH_SLASH;
        keymap.put(REQUEST_PATH_TO_MATCH_SLASH, pathToMatchSlash);
        propertyNames[httpRequestMethod] = HTTP_REQUEST_METHOD;
        keymap.put(HTTP_REQUEST_METHOD, httpRequestMethod);
        propertyNames[interceptorProviders] = INTERCEPTOR_PROVIDERS;
        keymap.put(INTERCEPTOR_PROVIDERS, interceptorProviders);
        propertyNames[templateParameters] = TEMPLATE_PARAMETERS;
        keymap.put(TEMPLATE_PARAMETERS, templateParameters);
        propertyNames[accept] = ACCEPT_CONTENT_TYPE;
        keymap.put(ACCEPT_CONTENT_TYPE, accept);
        propertyNames[continuationProvider] = CONTINUATION_PROVIDER;
        keymap.put(CONTINUATION_PROVIDER, continuationProvider);
        propertyNames[destination] = DESTINATION;
        keymap.put(DESTINATION, destination);
        propertyNames[opStack] = OP_RES_INFO_STACK;
        keymap.put(OP_RES_INFO_STACK, opStack);
        propertyNames[wsdlDescription] = WSDL_DESCRIPTION;
        keymap.put(WSDL_DESCRIPTION, wsdlDescription);
        propertyNames[wsdlInterface] = WSDL_INTERFACE;
        keymap.put(WSDL_INTERFACE, wsdlInterface);
        propertyNames[wsdlOperation] = WSDL_OPERATION;
        keymap.put(WSDL_OPERATION, wsdlOperation);
        propertyNames[wsdlPort] = WSDL_PORT;
        keymap.put(WSDL_PORT, wsdlPort);
        propertyNames[wsdlService] = WSDL_SERVICE;
        keymap.put(WSDL_SERVICE, wsdlService);
        propertyNames[requestUrl] = REQUEST_URL;
        keymap.put(REQUEST_URL, requestUrl);
        propertyNames[requestUri] = REQUEST_URI;
        keymap.put(REQUEST_URI, requestUri);
        propertyNames[pathInfo] = PATH_INFO;
        keymap.put(PATH_INFO, pathInfo);
        propertyNames[basePath] = BASE_PATH;
        keymap.put(BASE_PATH, basePath);
        propertyNames[fixedParamOrder] = FIXED_PARAMETER_ORDER;
        keymap.put(FIXED_PARAMETER_ORDER, fixedParamOrder);
        propertyNames[inInterceptors] = IN_INTERCEPTORS;
        keymap.put(IN_INTERCEPTORS, inInterceptors);
        propertyNames[outInterceptors] = OUT_INTERCEPTORS;
        keymap.put(OUT_INTERCEPTORS, outInterceptors);
        propertyNames[responseCode] = RESPONSE_CODE;
        keymap.put(RESPONSE_CODE, responseCode);
        propertyNames[attachments] = ATTACHMENTS;
        keymap.put(ATTACHMENTS, attachments);
        propertyNames[encoding] = ENCODING;
        keymap.put(ENCODING, encoding);
        propertyNames[httpContext] = AbstractHTTPDestination.HTTP_CONTEXT;
        keymap.put(AbstractHTTPDestination.HTTP_CONTEXT, httpContext);
        propertyNames[httpConfig] = AbstractHTTPDestination.HTTP_CONFIG;
        keymap.put(AbstractHTTPDestination.HTTP_CONFIG, httpConfig);
        propertyNames[httpContextMatchStrategy] = AbstractHTTPDestination.HTTP_CONTEXT_MATCH_STRATEGY;
        keymap.put(AbstractHTTPDestination.HTTP_CONTEXT_MATCH_STRATEGY, httpContextMatchStrategy);
        propertyNames[httpBasePath] = HTTP_BASE_PATH;
        keymap.put(HTTP_BASE_PATH, httpBasePath);
        propertyNames[asyncPostDispatch] = ASYNC_POST_RESPONSE_DISPATCH;
        keymap.put(ASYNC_POST_RESPONSE_DISPATCH, asyncPostDispatch);
        propertyNames[securityContext] = SECURITY_CONTEXT;
        keymap.put(SECURITY_CONTEXT, securityContext);
        propertyNames[authorizationPolicy] = AUTHORIZATION_POLICY;
        keymap.put(AUTHORIZATION_POLICY, authorizationPolicy);
        propertyNames[certConstraints] = CERT_CONSTRAINTS;
        keymap.put(CERT_CONSTRAINTS, certConstraints);
        propertyNames[serviceRedirection] = AbstractHTTPDestination.SERVICE_REDIRECTION;
        keymap.put(AbstractHTTPDestination.SERVICE_REDIRECTION, serviceRedirection);
        propertyNames[httpServletResponse] = HTTP_SERVLET_RESPONSE;
        keymap.put(HTTP_SERVLET_RESPONSE, httpServletResponse);
        propertyNames[resourceMethod] = RESOURCE_METHOD;
        keymap.put(RESOURCE_METHOD, resourceMethod);
        propertyNames[oneWayRequest] = ONE_WAY_REQUEST;
        keymap.put(ONE_WAY_REQUEST, oneWayRequest);
        propertyNames[asyncResponse] = ASYNC_RESPONSE;
        keymap.put(ASYNC_RESPONSE, asyncResponse);
        propertyNames[threadContextSwitched] = THREAD_CONTEXT_SWITCHED;
        keymap.put(THREAD_CONTEXT_SWITCHED, threadContextSwitched);
        propertyNames[cacheInputProperty] = OutgoingChainInterceptor.CACHE_INPUT_PROPERTY;
        keymap.put(OutgoingChainInterceptor.CACHE_INPUT_PROPERTY, cacheInputProperty);
        propertyNames[previousMessage] = PhaseInterceptorChain.PREVIOUS_MESSAGE;
        keymap.put(PhaseInterceptorChain.PREVIOUS_MESSAGE, previousMessage);
        propertyNames[responseHeadersCopied] = AbstractHTTPDestination.RESPONSE_HEADERS_COPIED;
        keymap.put(AbstractHTTPDestination.RESPONSE_HEADERS_COPIED, responseHeadersCopied);
        propertyNames[sseEventSink] = SSE_EVENT_SINK;
        keymap.put(SSE_EVENT_SINK, sseEventSink);
        propertyNames[requestorRole] = REQUESTOR_ROLE;
        keymap.put(REQUESTOR_ROLE, requestorRole);
        propertyNames[partialResponse] = PARTIAL_RESPONSE_MESSAGE;
        keymap.put(PARTIAL_RESPONSE_MESSAGE, partialResponse);
        propertyNames[emptyPartialResponse] = EMPTY_PARTIAL_RESPONSE_MESSAGE;
        keymap.put(EMPTY_PARTIAL_RESPONSE_MESSAGE, emptyPartialResponse);
        propertyNames[endpointAddress] = ENDPOINT_ADDRESS;
        keymap.put(ENDPOINT_ADDRESS, endpointAddress);
        propertyNames[inboundMessage] = INBOUND_MESSAGE;
        keymap.put(INBOUND_MESSAGE, inboundMessage);
        KEYMAP = Collections.unmodifiableMap(keymap);
    }
    //Liberty code change end

    // Liberty change - used to avoid resize
    public MessageImpl(int isize, float factor) {
        super(isize, factor);
        for (int i = 0; i < TOTAL; i++) {
            propertyValues[i] = NOT_FOUND;
        }
    }

    public MessageImpl() {
        //nothing
        for (int i = 0; i < TOTAL; i++) {
            propertyValues[i] = NOT_FOUND;
        }
    }

    public MessageImpl(Message m) {
        super(m);
        if (m instanceof MessageImpl) {
            MessageImpl impl = (MessageImpl) m;
            exchange = impl.getExchange();
            id = impl.id;
            interceptorChain = impl.interceptorChain;
            contents = impl.contents;
            index = impl.index;
            for (int i = 0; i < TOTAL; i++) {
                propertyValues[i] = NOT_FOUND;
            }
        } else {
            throw new RuntimeException("Not a MessageImpl! " + m.getClass());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Attachment> getAttachments() {
        //Liberty code change start
        return (Collection<Attachment>) getFromPropertyArray(attachments);
        //Liberty code change end
    }

    @Override
    public void setAttachments(Collection<Attachment> a) {
        //Liberty code change start
        propertyValues[attachments] = a;
        //Liberty code change end
    }

    public String getAttachmentMimeType() {
        //for sub class overriding
        return null;
    }

    @Override
    public Destination getDestination() {
        //Liberty code change start
        return (Destination) getFromPropertyArray(destination);
        //Liberty code change start
    }

    @Override
    public Exchange getExchange() {
        return exchange;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public InterceptorChain getInterceptorChain() {
        return this.interceptorChain;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getContent(Class<T> format) {
        for (int x = 0; x < index; x += 2) {
            if (contents[x] == format) {
                return (T) contents[x + 1];
            }
        }
        return null;
    }

    @Override
    public <T> void setContent(Class<T> format, Object content) {
        for (int x = 0; x < index; x += 2) {
            if (contents[x] == format) {
                contents[x + 1] = content;
                return;
            }
        }
        if (index >= contents.length) {
            //very unlikely to happen.   Haven't seen more than about 6,
            //but just in case we'll add a few more
            Object[] tmp = new Object[contents.length + 10];
            System.arraycopy(contents, 0, tmp, 0, contents.length);
            contents = tmp;
        }
        contents[index] = format;
        contents[index + 1] = content;
        index += 2;
    }

    @Override
    public <T> void removeContent(Class<T> format) {
        for (int x = 0; x < index; x += 2) {
            if (contents[x] == format) {
                index -= 2;
                if (x != index) {
                    contents[x] = contents[index];
                    contents[x + 1] = contents[index + 1];
                }
                contents[index] = null;
                contents[index + 1] = null;
                return;
            }
        }
    }

    @Override
    public Set<Class<?>> getContentFormats() {

        Set<Class<?>> c = new HashSet<>();
        for (int x = 0; x < index; x += 2) {
            c.add((Class<?>) contents[x]);
        }
        return c;
    }

    public void setDestination(Destination d) {
        //Liberty code change start
    	propertyValues[destination] = d;
        //Liberty code change end
    }

    @Override
    public void setExchange(Exchange e) {
        this.exchange = e;
    }

    @Override
    public void setId(String i) {
        this.id = i;
    }

    @Override
    public void setInterceptorChain(InterceptorChain ic) {
        this.interceptorChain = ic;
    }

    //Liberty code change start
    // Since these maps can have null value, use the getOrDefault API
    // to prevent calling get twice under the covers
    @Override
    public Object getContextualProperty(String key) {
        //Liberty code change start
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            Object value = propertyValues[index];
            return value == NOT_FOUND ? null : value;
        }
        //Liberty code change end

        Object o = getOrDefault(key, NOT_FOUND);
        if (o != NOT_FOUND) {
            return o;
        }
        return getFromExchange(key);
    }

    private Object getFromExchange(String key) {
        Exchange ex = getExchange();
        if (ex != null) {
            Object o = ex.getOrDefault(key, NOT_FOUND);
            if (o != NOT_FOUND) {
                return o;
            }
            
            Map<String, Object> p;
            Endpoint ep = ex.getEndpoint();
            if (ep != null) {
                o = ep.getOrDefault(key, NOT_FOUND);
                if (o != NOT_FOUND) {
                    return o;
                }

                EndpointInfo ei = ep.getEndpointInfo();
                if (ei != null) {
                    if ((p = ei.getProperties()) != null && (o = p.getOrDefault(key, NOT_FOUND)) != NOT_FOUND) {
                        return o;
                    }
                    if ((p = ei.getBinding().getProperties()) != null && (o = p.getOrDefault(key, NOT_FOUND)) != NOT_FOUND) {
                        return o;
                    }
                }
            }
            Service sv = ex.getService();
            if (sv != null && (o = sv.getOrDefault(key, NOT_FOUND)) != NOT_FOUND) {
                return o;
            }
            Bus b = ex.getBus();
            if (b != null && (p = b.getProperties()) != null) {
                if ((o = p.getOrDefault(key, NOT_FOUND)) != NOT_FOUND) {
                    return o;
                }
            }
        }
        return null;
    }

    private Set<String> getExchangeKeySet() {
        HashSet<String> keys = new HashSet<>();
        Exchange ex = getExchange();
        if (ex != null) {
            Bus b = ex.getBus();
            Map<String, Object> p;
            if (b != null && (p = b.getProperties()) != null) {
                if (!p.isEmpty()) {
                    keys.addAll(p.keySet());
                }
            }
            Service sv = ex.getService();
            if (sv != null && !sv.isEmpty()) {
                keys.addAll(sv.keySet());
            }
            Endpoint ep = ex.getEndpoint();
            if (ep != null) {
                EndpointInfo ei = ep.getEndpointInfo();
                if (ei != null) {
                    if ((p = ei.getBinding().getProperties()) != null) {
                        if (!p.isEmpty()) {
                            keys.addAll(p.keySet());
                        }
                    }
                    if ((p = ei.getProperties()) != null) {
                        if (!p.isEmpty()) {
                            keys.addAll(p.keySet());
                        }
                    }
                }
                
                if (!ep.isEmpty()) {
                    keys.addAll(ep.keySet());
                }
            }
            if (!ex.isEmpty()) {
                keys.addAll(ex.keySet());
            }
        }
        return keys;
    }

    @Override
    public Set<String> getContextualPropertyKeys() {
        Set<String> s = getExchangeKeySet();
        s.addAll(keySet());
        return s;
    }
    //Liberty code change end
    
    public static void copyContent(Message m1, Message m2) {
        for (Class<?> c : m1.getContentFormats()) {
            m2.setContent(c, m1.getContent(c));
        }
    }

    //Liberty code change start
    @Override
    public void resetContextCache() {
    }

    void setContextualProperty(String key, Object v) {
        if (!containsKey(key)) {
            put(key, v);
        }
    }
    
    @SuppressWarnings("rawtypes")
    public Map getProtocolHeaders() {
        return (Map) getFromPropertyArray(protoHeaders);
    }
    
    @SuppressWarnings("rawtypes")
    public void setProtocolHeaders(Map p) {
        propertyValues[protoHeaders] = p;
    }
    
    @Override
    public Object remove(Object key) {
        return remove((String) key);
    }
    
    public Object remove(String key) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            Object ret = getFromPropertyArray(index);
            propertyValues[index] = NOT_FOUND;
            return ret;
        }
        return super.remove(key);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> key) {
        return (T) get(key.getName());
    }

    @Override
    public <T> void put(Class<T> key, T value) {
        put(key.getName(), value);
    }

    @Override
    public Object get(Object key) {
        return get((String) key);
    }

    public Object get(String key) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            return getFromPropertyArray(index);
        }
        
        return super.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            Object ret = getFromPropertyArray(index);
            propertyValues[index] = value;
            return ret;
        }

        return super.put(key, value);
    }

    @Override
    public Set<String> keySet() {
        Set<String> myKeys = new HashSet<String>(super.keySet());
        for (int i = 0; i < TOTAL; i++) {
            if (propertyValues[i] != NOT_FOUND) {
                myKeys.add(propertyNames[i]);
            }
        }

        return myKeys;
    }
    
    @Override
    public Set<Map.Entry<String,Object>> entrySet() {
        HashSet<Map.Entry<String,Object>> myEntrySet = new HashSet<Map.Entry<String,Object>>(super.entrySet());
        for (int i = 0; i < TOTAL; i++) {
            if (propertyValues[i] != NOT_FOUND) {
                myEntrySet.add(new AbstractMap.SimpleEntry<String,Object>(propertyNames[i], propertyValues[i]));
            }
        }
        return myEntrySet;
    }
    
    @Override
    public boolean containsKey(Object key) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            return propertyValues[index] != NOT_FOUND;
        }
        return super.containsKey(key);
    }
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }
    @Override
    public Collection<Object> values() {
        Collection<Object> myValues = new ArrayList<Object>(super.values());
        for (Object o : propertyValues) {
            if (o != NOT_FOUND) {
                myValues.add(o);
            }
        }
        return myValues;
    }

    public Object getAuthorizationPolicy() {
        return getFromPropertyArray(authorizationPolicy);
    }

    public void setAuthorizationPolicy(Object a) {
        propertyValues[authorizationPolicy] = a;
    }

    public Object getCertConstraints() {
        return getFromPropertyArray(certConstraints);
    }

    public void setCertConstraints(Object c) {
        propertyValues[certConstraints] = c;
    }

    public Object getServiceRedirection() {
        return getFromPropertyArray(serviceRedirection);
    }

    public void setServiceRedirection(Object s) {
        propertyValues[serviceRedirection] = s;
    }

    public Object getHttpServletResponse() {
        return getFromPropertyArray(httpServletResponse);
    }

    public void setHttpServletResponse(Object h) {
        propertyValues[httpServletResponse] = h;
    }

    public Object getResourceMethod() {
        return getFromPropertyArray(resourceMethod);
    }

    public void setResourceMethod(Object r) {
        propertyValues[resourceMethod] = r;
    }

    public Object getOneWayRequest() {
        return getFromPropertyArray(oneWayRequest);
    }

    public void setOneWayRequest(Object o) {
        propertyValues[oneWayRequest] = o;
    }

    public Object getAsyncResponse() {
        return getFromPropertyArray(asyncResponse);
    }

    public void setAsyncResponse(Object a) {
        propertyValues[asyncResponse] = a;
    }

    public Object getThreadContextSwitched() {
        return getFromPropertyArray(threadContextSwitched);
    }

    public void setThreadContextSwitched(Object t) {
        propertyValues[threadContextSwitched] = t;
    }

    public Object getPreviousMessage() {
        return getFromPropertyArray(previousMessage);
    }

    public boolean containsPreviousMessage() {
        return propertyValues[previousMessage] != NOT_FOUND;
    }

    public void setPreviousMessage(Object p) {
        propertyValues[previousMessage] = p;
    }

    public Object getCacheInputProperty() {
        return getFromPropertyArray(cacheInputProperty);
    }

    public void setCacheInputProperty(Object c) {
        propertyValues[cacheInputProperty] = c;
    }

    public Object getSseEventSink() {
        return getFromPropertyArray(sseEventSink);
    }

    public void setSseEventSink(Object s) {
        propertyValues[sseEventSink] = s;
    }

    public Object getResponseHeadersCopied() {
        return getFromPropertyArray(responseHeadersCopied);
    }

    public void setResponseHeadersCopied(Object r) {
        propertyValues[responseHeadersCopied] = r;
    }

    public Object getRequestorRole() {
        return getFromPropertyArray(requestorRole);
    }

    public void setRequestorRole(Object r) {
        propertyValues[requestorRole] = r;
    }

    public Object getEmptyPartialResponse() {
        return getFromPropertyArray(emptyPartialResponse);
    }

    public void setEmptyPartialResponse(Object e) {
        propertyValues[emptyPartialResponse] = e;
    }

    public Object getPartialResponse() {
        return getFromPropertyArray(partialResponse);
    }

    public void setPartialResponse(Object p) {
        propertyValues[partialResponse] = p;
    }

    public Object getEndpointAddress() {
        return getFromPropertyArray(endpointAddress);
    }

    public void setEndpointAddress(Object e) {
        propertyValues[endpointAddress] = e;
    }
    
    public Object getInboundMessage() {
        return getFromPropertyArray(inboundMessage);
    }

    public void setInboundMessage(Object i) {
        propertyValues[inboundMessage] = i;
    }
    public String getPathToMatchSlash() {
        return (String) getFromPropertyArray(pathToMatchSlash);
    }
    
    public void setPathToMatchSlash(String p) {
        propertyValues[pathToMatchSlash] = p;
    }
    
    public String getHttpRequestMethod() {
        return (String) getFromPropertyArray(httpRequestMethod);
    }
    
    public void setHttpRequestMethod(String h) {
        propertyValues[httpRequestMethod] = h;
    }

    public void removePathToMatchSlash() {
        propertyValues[pathToMatchSlash] = NOT_FOUND;
    }
    public String getQueryString() {
        return (String) getFromPropertyArray(queryString);
    }
    
    public void setQueryString(String q) {
        propertyValues[queryString] = q;
    }
    public Object getOperationResourceInfoStack() {
        return getFromPropertyArray(opStack);
    }
    
    public void setOperationResourceInfoStack(Object o) {
        propertyValues[opStack] = o;
    }

    public String getContentType() {
        return (String) getFromPropertyArray(contentType);
    }
    
    public boolean containsContentType() {
        return propertyValues[contentType] != NOT_FOUND;
    }
    
    public void setContentType(String c) {
        propertyValues[contentType] = c;
    }

    public Object getHttpRequest() {
        return getFromPropertyArray(httpRequest);
    }
    
    public boolean containsHttpRequest() {
        return propertyValues[httpRequest] != NOT_FOUND;
    }
    
    public void setHttpRequest(Object h) {
        propertyValues[httpRequest] = h;
    }
    
    public Object getHttpResponse() {
        return getFromPropertyArray(httpResponse);
    }
    
    public void setHttpResponse(Object h) {
        propertyValues[httpResponse] = h;
    }

    public Object getAccept() {
        return getFromPropertyArray(accept);
    }
    
    public void setAccept(Object a) {
        propertyValues[accept] = a;
    }

    public Object getContinuationProvider() {
        return getFromPropertyArray(continuationProvider);
    }
    
    public void setContinuationProvider(Object c) {
        propertyValues[continuationProvider] = c;
    }

    public Object getWsdlDescription() {
        return getFromPropertyArray(wsdlDescription);
    }
    
    public void setWsdlDescription(Object w) {
        propertyValues[wsdlDescription] = w;
    }

    public Object getWsdlInterface() {
        return getFromPropertyArray(wsdlInterface);
    }
    
    public void setWsdlInterface(Object w) {
        propertyValues[wsdlInterface] = w;
    }

    public Object getWsdlOperation() {
        return getFromPropertyArray(wsdlOperation);
    }
    
    public void setWsdlOperation(Object w) {
        propertyValues[wsdlOperation] = w;
    }

    public Object getWsdlPort() {
        return getFromPropertyArray(wsdlPort);
    }
    
    public void setWsdlPort(Object w) {
        propertyValues[wsdlPort] = w;
    }

    public Object getWsdlService() {
        return getFromPropertyArray(wsdlService);
    }
    
    public void setWsdlService(Object w) {
        propertyValues[wsdlService] = w;
    }

    public Object getRequestUrl() {
        return getFromPropertyArray(requestUrl);
    }
    
    public void setRequestUrl(Object r) {
        propertyValues[requestUrl] = r;
    }

    public Object getRequestUri() {
        return getFromPropertyArray(requestUri);
    }
    
    public void setRequestUri(Object r) {
        propertyValues[requestUri] = r;
    }
    
    public Object getPathInfo() {
        return getFromPropertyArray(pathInfo);
    }
    
    public void setPathInfo(Object p) {
       propertyValues[pathInfo] = p;
    }
    
    public Object getBasePath() {
        return getFromPropertyArray(basePath);
    }
    
    public boolean containsBasePath() {
        return propertyValues[basePath] != NOT_FOUND;
    }
    
    public void setBasePath(Object b) {
        propertyValues[basePath] = b;
    }

    public Object getFixedParamOrder() {
        return getFromPropertyArray(fixedParamOrder);
    }
    
    public void setFixedParamOrder(Object f) {
        propertyValues[fixedParamOrder] = f;
    }

    public Object getInInterceptors() {
        return getFromPropertyArray(inInterceptors);
    }
    
    public void setInInterceptors(Object i) {
        propertyValues[inInterceptors] = i;
    }

    public Object getOutInterceptors() {
        return getFromPropertyArray(outInterceptors);
    }
    
    public void setOutInterceptors(Object o) {
        propertyValues[outInterceptors] = o;
    }

    public Object getResponseCode() {
        return getFromPropertyArray(responseCode);
    }
    
    public void setResponseCode(Object r) {
        propertyValues[responseCode] = r;
    }

    public Object getEncoding() {
        return getFromPropertyArray(encoding);
    }
    
    public void setEncoding(Object e) {
        propertyValues[encoding] = e;
    }

    public Object getHttpContext() {
        return getFromPropertyArray(httpContext);
    }
    
    public void setHttpContext(Object h) {
        propertyValues[httpContext] = h;
    }

    public Object getHttpConfig() {
        return getFromPropertyArray(httpConfig);
    }
    
    public void setHttpConfig(Object h) {
        propertyValues[httpConfig] = h;
    }

    public Object getHttpContextMatchStrategy() {
        return getFromPropertyArray(httpContextMatchStrategy);
    }
    
    public void setHttpContextMatchStrategy(Object h) {
        propertyValues[httpContextMatchStrategy] = h;
    }

    public Object getHttpBasePath() {
        return getFromPropertyArray(httpBasePath);
    }
    
    public void setHttpBasePath(Object h) {
        propertyValues[httpBasePath] = h;
    }

    public Object getAsyncPostDispatch() {
        return getFromPropertyArray(asyncPostDispatch);
    }
    
    public void setAsyncPostDispatch(Object a) {
        propertyValues[asyncPostDispatch] = a;
    }
    
    public Object getSecurityContext() {
        return getFromPropertyArray(securityContext);
    }
    
    public void setSecurityContext(Object s) {
        propertyValues[securityContext] = s;
    }

    @SuppressWarnings("rawtypes")
    public Collection getInterceptorProviders() {
        return (Collection) getFromPropertyArray(interceptorProviders);
    }
    
    @SuppressWarnings("rawtypes")
    public void setInterceptorProviders(Collection i) {
        propertyValues[interceptorProviders] = i;
    }

    public Object getTemplateParameters() {
        return getFromPropertyArray(templateParameters);
    }
    
    public void setTemplateParameters(Object t) {
        propertyValues[templateParameters] = t;
    }

    public void removeContentType() {
        propertyValues[contentType] = NOT_FOUND;
    }
    public void removeHttpResponse() {
        propertyValues[httpResponse] = NOT_FOUND;
    }
    public void removeHttpRequest() {
        propertyValues[httpRequest] = NOT_FOUND;
    }
    
    private Object getFromPropertyArray(int index) {
        Object value = propertyValues[index];
        return value == NOT_FOUND ? null : value;
    }
    
    @Override
    public int size() {
        int size = super.size();
        for (Object o : propertyValues) {
            if (o != NOT_FOUND) {
                size++;
            }
        }
        return size;
    }
    
    @Override
    public void clear() {
        super.clear();
        for (int i = 0; i < TOTAL; i++) {
            propertyValues[i] = NOT_FOUND;
        }
    }
    
    @Override
    public boolean isEmpty() {
        if (!super.isEmpty()) {
            return false;
        }
        
        for (Object o : propertyValues) {
            if (o != NOT_FOUND) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean containsValue(Object value) {
        if (super.containsValue(value)) {
            return true;
        }
        
        for (Object o : propertyValues) {
            if (o.equals(value)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public Object getOrDefault(Object key, Object d) {
        return getOrDefault((String) key, d);
    }
    
    public Object getOrDefault(String key, Object d) {
        Object v = super.getOrDefault(key, NOT_FOUND);
        if (v != NOT_FOUND) {
            return v;
        }
        for (int i = 0; i < TOTAL; i++) {
            if (propertyNames[i] == key) {
                if (propertyValues[i] != NOT_FOUND) {
                    return propertyValues[i];
                } else {
                    return d;
                }
            }
        }
        return d;
    }
    
    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        super.forEach(action);
        for (int i = 0; i < TOTAL; i++) {
            if (propertyValues[i] != NOT_FOUND) {
                action.accept(propertyNames[i], propertyValues[i]);
            }
        }
    }
    @Override
    public void replaceAll(BiFunction<? super String, ? super Object, ? extends Object> function) {
        super.replaceAll(function);
        for (int i = 0; i < TOTAL; i++) {
            if (propertyValues[i] != NOT_FOUND) {
                propertyValues[i] = function.apply(propertyNames[i], propertyValues[i]);
            }
        }
    }
    @Override
    public Object replace(String key, Object value) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            if (propertyValues[index] != NOT_FOUND) {
                Object ret = propertyValues[index];
                propertyValues[index] = value;
                return ret;
            } else {
                return null;
            }
        }

        return super.replace(key, value);
    }
    @Override
    public boolean replace(String key, Object oldValue, Object newValue) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            if (propertyValues[index] == oldValue) {
                propertyValues[index] = newValue;
                return true;
            } else {
                return false;
            }
        }
        return super.replace(key, oldValue, newValue);
    }
    @Override
    public Object putIfAbsent(String key, Object value) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            if (propertyValues[index] == NOT_FOUND) {
                propertyValues[index] = value;
                return null;
            } else {
                return propertyValues[index];
            }
        }
        return super.putIfAbsent(key, value);
    }
    @Override
    public boolean remove(Object key, Object value) {
        Integer index = KEYMAP.getOrDefault(key, KEY_NOT_FOUND);
        if (index != KEY_NOT_FOUND) {
            if (propertyValues[index] == value) {
                propertyValues[index] = NOT_FOUND;
                return true;
            } else {
                return false;
            }
        }
        return super.remove(key, value);
    }
    @Override
    public Object compute(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Object computeIfAbsent(String key, Function<? super String, ? extends Object> mappingFunction) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Object computeIfPresent(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
        throw new UnsupportedOperationException();
    }
    @Override
    public Object merge(String key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        throw new UnsupportedOperationException();
    }
    //Liberty code change end
}
