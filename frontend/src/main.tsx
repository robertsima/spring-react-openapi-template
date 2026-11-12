import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import App from './App'
import './index.css'
import {BrowserRouter} from "react-router-dom";
import {ThemeProvider} from "@mui/material/styles";
import {defaultTheme} from "./themes/theme1";
import {loadConfig} from "./config";
import {AppContextProvider} from "./services/AppContext";
import {AuthenticationProvider} from "./services/Authentication/AuthenticationProvider";
import {logger} from "./utils/logger";

const root = createRoot(document.getElementById('root')!);

window.addEventListener('error', e => logger.error('Uncaught error', e.error));
window.addEventListener('unhandledrejection', e => logger.error('Unhandled promise rejection', e.reason));

loadConfig()
    .then(config => root.render(
        <StrictMode>
            <BrowserRouter>
                <ThemeProvider theme={defaultTheme}>
                    <AppContextProvider config={config}>
                        <AuthenticationProvider>
                            <App/>
                        </AuthenticationProvider>
                    </AppContextProvider>
                </ThemeProvider>
            </BrowserRouter>
        </StrictMode>
    ))
    .catch(err => {
        logger.error('Failed to start', err);
        root.render(<pre>Failed to start: {String(err)}</pre>);
    });
