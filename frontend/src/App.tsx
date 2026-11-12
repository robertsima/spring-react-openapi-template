import HomeScreen from "./screens/HomeScreen/HomeScreen";
import {ROUTE_ADMIN, ROUTE_HOME, ROUTE_UNAUTHORISED} from "./constants/routes";
import {ProtectedRoute} from "./components/routes/ProtectedRoute";
import AdminScreen from "./screens/AdminScreen/AdminScreen";
import {ROLE_ADMIN} from "./constants/roles";
import UnauthorisedScreen from "./screens/UnauthorisedScreen/UnauthorisedScreen";
import {Route, Routes} from "react-router";
import {StompSessionProvider} from "react-stomp-hooks";
import {useAuth} from "react-oidc-context";
import {RequireAuth} from "./components/routes/RequireAuth";
import {useAppContext} from "./services/AppContext";
import {websocketUrl} from "./constants/websockets";
import "./App.scss";

function App() {
    const auth = useAuth();
    const {config} = useAppContext();

    return <RequireAuth>
        {/* The backend validates this bearer token on STOMP CONNECT (backend/.../websocket/WebSocketConfig.java) */}
        <StompSessionProvider
            url={websocketUrl(config.websocketPath)}
            connectHeaders={{Authorization: `Bearer ${auth.user?.access_token ?? ''}`}}
        >
            <Routes>
                <Route path={ROUTE_HOME} element={<HomeScreen/>}/>

                {/*Admin Role*/}
                <Route path={ROUTE_ADMIN} element={<ProtectedRoute permittedRoles={[ROLE_ADMIN]}>
                    <AdminScreen/>
                </ProtectedRoute>}/>

                <Route element={<UnauthorisedScreen/>} path={ROUTE_UNAUTHORISED}/>
            </Routes>
        </StompSessionProvider>
    </RequireAuth>
}

export default App;
