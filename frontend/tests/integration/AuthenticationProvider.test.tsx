import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {http, HttpResponse} from 'msw';
import {AppContextProvider} from '../../src/services/AppContext';
import {AuthenticationProvider} from '../../src/services/Authentication/AuthenticationProvider';
import {RequireAuth} from '../../src/components/routes/RequireAuth';
import {server} from './msw-setup';
import {API} from './handlers';

const config = {apiBasePath: API, websocketPath: '/websockets'};

const renderApp = () => render(
    <AppContextProvider config={config}>
        <AuthenticationProvider>
            <RequireAuth><p>secret content</p></RequireAuth>
        </AuthenticationProvider>
    </AppContextProvider>
);

// Placeholder integration tests: provider + generated client + mocked backend.
describe('Windows SSO bootstrap', () => {
    it('renders the app once /me succeeds', async () => {
        renderApp();
        expect(await screen.findByText('secret content')).toBeInTheDocument();
    });

    it('explains what to do when SSO fails', async () => {
        server.use(http.get(`${API}/me`, () => new HttpResponse(null, {status: 401})));
        renderApp();
        expect(await screen.findByText(/Windows sign-in failed \(401\)/)).toBeInTheDocument();
    });
});
