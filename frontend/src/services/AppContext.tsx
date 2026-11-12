import {createContext, FC, PropsWithChildren, useContext, useMemo, useRef} from "react";
import {useAuth} from "react-oidc-context";
import {AppConfig} from "../config";
import {Apis, createApis} from "./apiConfig";

type AppContextValue = { config: AppConfig, apis: Apis };

const AppContext = createContext<AppContextValue | undefined>(undefined);

export const AppContextProvider: FC<PropsWithChildren<{ config: AppConfig }>> = ({config, children}) => {
    const auth = useAuth();
    // keep a ref so the API clients are created once but always read the current token
    const tokenRef = useRef<string | undefined>();
    tokenRef.current = auth.user?.access_token;

    const value = useMemo(() => ({
        config,
        apis: createApis(config, () => tokenRef.current),
    }), [config]);

    return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export const useAppContext = (): AppContextValue => {
    const ctx = useContext(AppContext);
    if (!ctx) {
        throw new Error("useAppContext must be used inside AppContextProvider");
    }
    return ctx;
}
