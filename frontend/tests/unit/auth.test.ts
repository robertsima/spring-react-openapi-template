import {describe, expect, it} from 'vitest';
import {doesUserHaveAnyOneOfTheseRoles, getDisplayName} from '../../src/utils/auth/auth';
import {ROLE_ADMIN, ROLE_USER} from '../../src/constants/roles';

// Placeholder unit tests: pure functions/components, no network.
describe('auth utils', () => {
    const admin = {username: 'MYDOMAIN\\jdoe', roles: [ROLE_USER, ROLE_ADMIN]};

    it('checks roles from /me', () => {
        expect(doesUserHaveAnyOneOfTheseRoles(admin, [ROLE_ADMIN])).toBe(true);
        expect(doesUserHaveAnyOneOfTheseRoles({...admin, roles: [ROLE_USER]}, [ROLE_ADMIN])).toBe(false);
        expect(doesUserHaveAnyOneOfTheseRoles(undefined, [ROLE_USER])).toBe(false);
    });

    it('strips the domain from Windows and Kerberos names', () => {
        expect(getDisplayName(admin)).toBe('jdoe');
        expect(getDisplayName({username: 'jdoe@MYDOMAIN.COM', roles: []})).toBe('jdoe');
    });
});
