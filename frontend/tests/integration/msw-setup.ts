// Integration tests run the real generated API client against a mocked network (MSW),
// so they exercise the contract-generated code end to end without a backend.
import {afterAll, afterEach, beforeAll} from 'vitest';
import {setupServer} from 'msw/node';
import {handlers} from './handlers';

export const server = setupServer(...handlers);

beforeAll(() => server.listen({onUnhandledRequest: 'error'}));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
