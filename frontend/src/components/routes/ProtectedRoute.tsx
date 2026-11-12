import {FC, ReactNode} from 'react';
import {doesUserHaveAnyOneOfTheseRoles} from "../../utils/auth/auth";
import {UserRole} from "../../constants/roles";
import {NavigateTo} from "../navigation/NavigateTo";
import {ROUTE_UNAUTHORISED} from "../../constants/routes";
import {useAuth} from "../../services/Authentication/AuthenticationProvider";

export type ProtectedRouteProps = {
    children: ReactNode;
    permittedRoles: UserRole[],
    unauthorisedRoute?: string
};

// Role-based route guard. UI-only convenience - the backend still enforces roles on every call.
export const ProtectedRoute: FC<ProtectedRouteProps> = ({children, permittedRoles, unauthorisedRoute}) => {
    const {user} = useAuth();
    const isAuthorised = doesUserHaveAnyOneOfTheseRoles(user, permittedRoles);

    return <>
        {isAuthorised && children}
        {user && !isAuthorised && <NavigateTo to={unauthorisedRoute ?? ROUTE_UNAUTHORISED}/>}
    </>
};
