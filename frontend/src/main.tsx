import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import App from './App'
import './index.css'
import {BrowserRouter} from "react-router-dom";
import {ThemeProvider} from "@mui/material/styles";
import {AuthProvider} from "react-oidc-context";
import {WebStorageStateStore} from "oidc-client-ts";
import {defaultTheme} from "./themes/theme1";
import {loadConfig} from "./config";
import {AppContextProvider} from "./services/AppContext";

const root = createRoot(document.getElementById('root')!);

loadConfig()
    .then(config => root.render(
        <StrictMode>
            <BrowserRouter>
                <ThemeProvider theme={defaultTheme}>
                    {/* OIDC Authorization Code + PKCE against Keycloak (public client, no secret in the browser) */}
                    <AuthProvider
                        authority={config.oidc.authority}
                        client_id={config.oidc.clientId}
                        redirect_uri={window.location.origin}
                        post_logout_redirect_uri={window.location.origin}
                        userStore={new WebStorageStateStore({store: window.sessionStorage})}
                        automaticSilentRenew
                        // strip ?code=&state= from the URL after login
                        onSigninCallback={() => window.history.replaceState({}, document.title, window.location.pathname)}
                    >
                        <AppContextProvider config={config}>
                            <App/>
                        </AppContextProvider>
                    </AuthProvider>
                </ThemeProvider>
            </BrowserRouter>
        </StrictMode>
    ))
    .catch(err => {
        console.error(err);
        root.render(<pre>Failed to start: {String(err)}</pre>);
    });
