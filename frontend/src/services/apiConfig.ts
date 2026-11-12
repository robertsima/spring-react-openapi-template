import {Configuration, Middleware, SessionApi, UsersApi} from "../generated/api";
import {AppConfig} from "../config";

const SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS', 'TRACE'];

const readCookie = (name: string): string | undefined =>
    document.cookie.split('; ').find(c => c.startsWith(`${name}=`))?.split('=')[1];

/**
 * CSRF: Windows SSO credentials are sent by the browser automatically, so the backend keeps CSRF protection on
 * (Spring Security csrf().spa()). It sets an XSRF-TOKEN cookie; we echo it back as X-XSRF-TOKEN on unsafe calls.
 */
export const csrfMiddleware: Middleware = {
    pre: async (context) => {
        const method = (context.init.method ?? 'GET').toUpperCase();
        const token = readCookie('XSRF-TOKEN');
        if (!SAFE_METHODS.includes(method) && token) {
            const headers = new Headers(context.init.headers);
            headers.set('X-XSRF-TOKEN', decodeURIComponent(token));
            return {url: context.url, init: {...context.init, headers}};
        }
    },
};

// Generated clients (from openapi/openapi.yaml) wired to the backend through the /api proxy.
// No token handling: the browser performs the Negotiate (Kerberos) handshake itself.
export const createApis = (config: AppConfig) => {
    const configuration = new Configuration({
        basePath: config.apiBasePath,
        credentials: 'same-origin',
        middleware: [csrfMiddleware],
    });

    return {
        sessionApi: new SessionApi(configuration),
        usersApi: new UsersApi(configuration),
    };
}

export type Apis = ReturnType<typeof createApis>;
