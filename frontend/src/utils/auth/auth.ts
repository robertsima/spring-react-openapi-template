import {User} from "oidc-client-ts";
import {UserRole} from "../../constants/roles";

const decodeJwtPayload = (token: string): Record<string, unknown> => {
    try {
        const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(payload));
    } catch {
        return {};
    }
}

/**
 * Keycloak realm roles from the access token, normalised the same way the backend does
 * (backend/.../config/KeycloakRoleConverter.java): "admin" -> "ROLE_ADMIN".
 */
export const getRoles = (user?: User | null): string[] => {
    if (!user?.access_token) {
        return [];
    }
    const realmAccess = decodeJwtPayload(user.access_token).realm_access as { roles?: string[] } | undefined;
    return (realmAccess?.roles ?? []).map(role => `ROLE_${role.toUpperCase()}`);
}

/**
 * Returns true if the specified user has any one of the specified roles
 */
export const doesUserHaveAnyOneOfTheseRoles = (user: User | null | undefined, roles: UserRole[]): boolean => {
    const userRoles = getRoles(user);
    return roles.some(role => userRoles.includes(role));
}

export const getDisplayName = (user?: User | null): string =>
    (user?.profile.preferred_username ?? user?.profile.name ?? user?.profile.sub ?? '') as string;
