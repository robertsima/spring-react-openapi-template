import {FC, ReactNode} from 'react';
import {useAuth} from "react-oidc-context";
import {doesUserHaveAnyOneOfTheseRoles} from "../../utils/auth/auth";
import {UserRole} from "../../constants/roles";
import {NavigateTo} from "../navigation/NavigateTo";
import {ROUTE_UNAUTHORISED} from "../../constants/routes";

export type ProtectedRouteProps = {
    children: ReactNode;
    permittedRoles: UserRole[],
    unauthorisedRoute?: string
};

// Role-based route guard. UI-only convenience - the backend still enforces roles on every call.
export const ProtectedRoute: FC<ProtectedRouteProps> = ({children, permittedRoles, unauthorisedRoute}) => {
    const auth = useAuth();
    const isAuthorised = doesUserHaveAnyOneOfTheseRoles(auth.user, permittedRoles);

    return <>
        {isAuthorised && children}
        {auth.user && !isAuthorised && <NavigateTo to={unauthorisedRoute ?? ROUTE_UNAUTHORISED}/>}
    </>
};
