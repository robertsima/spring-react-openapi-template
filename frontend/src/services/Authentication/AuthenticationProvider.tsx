import {createContext, FC, PropsWithChildren, useCallback, useContext, useEffect, useState} from "react";
import {CurrentUser, ResponseError} from "../../generated/api";
import {useAppContext} from "../AppContext";
import {logger} from "../../utils/logger";

export type AuthState = {
    loading: boolean,
    user?: CurrentUser,
    /** HTTP status when sign-in failed, e.g. 401 = the browser didn't complete Windows SSO */
    errorStatus?: number,
    refresh: () => void,
}

const AuthContext = createContext<AuthState>({loading: true, refresh: () => {}});

/**
 * Windows SSO: there is no login form or token. The first API call gets "401 WWW-Authenticate: Negotiate",
 * the browser answers with the user's Kerberos ticket and retries automatically (if the site is in the
 * intranet zone / auth-server allowlist). We just ask the backend who we are.
 */
export const AuthenticationProvider: FC<PropsWithChildren> = ({children}) => {
    const {apis} = useAppContext();
    const [state, setState] = useState<Omit<AuthState, 'refresh'>>({loading: true});

    const refresh = useCallback(() => {
        setState({loading: true});
        apis.sessionApi.getCurrentUser()
            .then(user => {
                logger.info('Signed in', {'user.name': user.username});
                setState({loading: false, user});
            })
            .catch(async (err: unknown) => {
                const status = err instanceof ResponseError ? err.response.status : undefined;
                logger.warn('Windows sign-in failed', {'http.response.status_code': status});
                setState({loading: false, errorStatus: status ?? 0});
            });
    }, [apis]);

    useEffect(refresh, [refresh]);

    return <AuthContext.Provider value={{...state, refresh}}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
