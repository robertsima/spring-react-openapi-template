import {createContext, FC, PropsWithChildren, useContext, useMemo} from "react";
import {AppConfig} from "../config";
import {Apis, createApis} from "./apiConfig";

type AppContextValue = { config: AppConfig, apis: Apis };

const AppContext = createContext<AppContextValue | undefined>(undefined);

export const AppContextProvider: FC<PropsWithChildren<{ config: AppConfig, apis?: Apis }>> = ({config, apis, children}) => {
    // apis can be injected (tests/Storybook); otherwise the generated clients are created once
    const value = useMemo(() => ({config, apis: apis ?? createApis(config)}), [config, apis]);
    return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}

export const useAppContext = (): AppContextValue => {
    const ctx = useContext(AppContext);
    if (!ctx) {
        throw new Error("useAppContext must be used inside AppContextProvider");
    }
    return ctx;
}
