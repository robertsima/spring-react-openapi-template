import {http, HttpResponse} from 'msw';
import type {CurrentUser, UserDTO} from '../../src/generated/api';

// Default happy-path responses, typed with the models generated from openapi/openapi.yaml.
// Override per test with server.use(...).
export const API = 'http://localhost/api';

export const handlers = [
    http.get(`${API}/me`, () => HttpResponse.json<CurrentUser>({
        username: 'MYDOMAIN\\jdoe',
        roles: ['ROLE_USER', 'ROLE_ADMIN'],
    })),
    http.get(`${API}/users`, ({request}) => {
        const name = new URL(request.url).searchParams.get('name');
        return HttpResponse.json<UserDTO[]>(name === 'Alice Johnson' ? [{name}] : []);
    }),
];
