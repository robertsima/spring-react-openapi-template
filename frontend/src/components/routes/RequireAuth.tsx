import {FC, PropsWithChildren} from "react";
import {Alert, AlertTitle, Button, Grid2 as Grid} from "@mui/material";
import {useAuth} from "../../services/Authentication/AuthenticationProvider";
import {LoadingScreen} from "../../screens/LoadingScreen/LoadingScreen";

/** Gate for the whole app: waits for GET /me (Windows SSO) and explains what to do if it fails. */
export const RequireAuth: FC<PropsWithChildren> = ({children}) => {
    const auth = useAuth();

    if (auth.loading) {
        return <LoadingScreen message="Signing you in with Windows..."/>;
    }
    if (!auth.user) {
        return <Grid container justifyContent="center" sx={{mt: 16, px: 2}}>
            <Alert severity="error" sx={{maxWidth: 640}}
                   action={<Button color="inherit" onClick={auth.refresh}>Retry</Button>}>
                <AlertTitle>Windows sign-in failed{auth.errorStatus ? ` (${auth.errorStatus})` : ''}</AlertTitle>
                Make sure you are on a domain-joined machine and signed in with your domain account, and that this
                site is in your browser's Local Intranet zone (Edge/Chrome: <code>AuthServerAllowlist</code> policy,
                Firefox: <code>network.negotiate-auth.trusted-uris</code>).
            </Alert>
        </Grid>;
    }
    return <>{children}</>;
}
