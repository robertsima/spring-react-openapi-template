import {CurrentUser} from "../../generated/api";
import {UserRole} from "../../constants/roles";

/**
 * Returns true if the specified user has any one of the specified roles.
 * Roles come from GET /me, already mapped from AD groups by the backend (app.security.role-mappings).
 */
export const doesUserHaveAnyOneOfTheseRoles = (user: CurrentUser | undefined, roles: UserRole[]): boolean =>
    roles.some(role => user?.roles.includes(role) ?? false);

/** "MYDOMAIN\jdoe" or "jdoe@MYDOMAIN.COM" -> "jdoe" */
export const getDisplayName = (user?: CurrentUser): string =>
    (user?.username ?? '').replace(/^.*\\/, '').replace(/@.*$/, '');
