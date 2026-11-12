// Runtime configuration, fetched from /config.json before the app renders.
// - dev: served from public/config.json
// - container: rendered by nginx from env vars (see frontend/nginx/default.conf.template)
// One image can therefore be promoted across environments without a rebuild.
export type AppConfig = {
    apiBasePath: string,
    websocketPath: string,
    oidc: {
        authority: string,
        clientId: string,
    }
}

export const loadConfig = async (): Promise<AppConfig> => {
    const response = await fetch('/config.json', {cache: 'no-store'});
    if (!response.ok) {
        throw new Error(`Unable to load /config.json (${response.status})`);
    }
    return response.json();
}
