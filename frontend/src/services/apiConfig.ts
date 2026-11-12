import {Configuration, UsersApi} from "../generated/api";
import {AppConfig} from "../config";

// Generated clients (from openapi/openapi.yaml) wired to the backend through the /api proxy.
// accessToken is a callback so every request picks up the latest (silently renewed) token.
export const createApis = (config: AppConfig, getAccessToken: () => string | undefined) => {
    const configuration = new Configuration({
        basePath: config.apiBasePath,
        accessToken: async () => getAccessToken() ?? '',
    });

    return {
        usersApi: new UsersApi(configuration),
    };
}

export type Apis = ReturnType<typeof createApis>;
