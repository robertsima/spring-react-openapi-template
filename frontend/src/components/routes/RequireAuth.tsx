import {FC, PropsWithChildren, useEffect} from "react";
import {hasAuthParams, useAuth} from "react-oidc-context";
import {LoadingScreen} from "../../screens/LoadingScreen/LoadingScreen";

/**
 * Gate for the whole app: sends unauthenticated users to the Keycloak login page
 * and shows the loading screen while the OIDC redirect/callback is in flight.
 */
export const RequireAuth: FC<PropsWithChildren> = ({children}) => {
    const auth = useAuth();

    useEffect(() => {
        if (!hasAuthParams() && !auth.isAuthenticated && !auth.activeNavigator && !auth.isLoading && !auth.error) {
            void auth.signinRedirect();
        }
    }, [auth]);

    if (auth.error) {
        return <pre>Authentication error: {auth.error.message}</pre>;
    }
    if (!auth.isAuthenticated) {
        return <LoadingScreen/>;
    }
    return <>{children}</>;
}
