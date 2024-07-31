package server;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @source https://stackoverflow.com/a/23590606/8524395
 * @author wf
 * 
 */
public class RemoteAddrFilter implements Filter {

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);
        final String remote_addr = request.getRemoteAddr();
        requestWrapper.addHeader("remote_addr", remote_addr);
        chain.doFilter(requestWrapper, response); // Goes to default servlet.
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    // https://stackoverflow.com/questions/2811769/adding-an-http-header-to-the-request-in-a-servlet-filter
    // http://sandeepmore.com/blog/2010/06/12/modifying-http-headers-using-java/
    // http://bijubnair.blogspot.de/2008/12/adding-header-information-to-existing.html
    /**
     * allow adding additional header entries to a request
     * 
     * @author wf
     * 
     */
    public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {
        /**
         * construct a wrapper for this request
         * 
         * @param request
         */
        public HeaderMapRequestWrapper(final HttpServletRequest request) {
            super(request);
        }

        private final Map<String, String> headerMap = new HashMap<String, String>();

        /**
         * add a header with given name and value
         * 
         * @param name
         * @param value
         */
        public void addHeader(final String name, final String value) {
            this.headerMap.put(name, value);
        }

        @Override
        public String getHeader(final String name) {
            String headerValue = super.getHeader(name);
            if (this.headerMap.containsKey(name)) {
                headerValue = this.headerMap.get(name);
            }
            return headerValue;
        }

        /**
         * get the Header names
         */
        @Override
        public Enumeration<String> getHeaderNames() {
            final List<String> names = Collections.list(super.getHeaderNames());
            for (final String name : this.headerMap.keySet()) {
                names.add(name);
            }
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(final String name) {
            final List<String> values = Collections.list(super.getHeaders(name));
            if (this.headerMap.containsKey(name)) {
                values.add(this.headerMap.get(name));
            }
            return Collections.enumeration(values);
        }

    }

}
