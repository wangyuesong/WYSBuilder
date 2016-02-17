package wys.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wys.services.AuthenticationService;

/**
 * @Project: wysbuilder
 * @Title: RestAuthenticationFilter.java
 * @Package com.javapapers.webservices.rest.jersey
 * @Description: TODO
 * @author YuesongWang
 * @date Feb 17, 2016 12:10:39 AM
 * @version V1.0
 */
public class RestAuthenticationFilter implements Filter {
    public static final String AUTHENTICATION_HEADER = "Authorization";

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter) throws IOException,
            ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authCredentials = httpServletRequest
                .getHeader(AUTHENTICATION_HEADER);

        AuthenticationService service = new AuthenticationService();
        boolean authenticationStatus = service.doAuth(authCredentials);
        if (authenticationStatus) {
            filter.doFilter(request, response);
        } else {
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse
                        .setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }

}
