/**
 * Minimal structured logger for the browser. Emits one JSON object per entry with the same ECS field names
 * as the backend and nginx logs, so client logs can be shipped to the same index later (placeholder: add a
 * transport in `emit`, e.g. navigator.sendBeacon to a log collector).
 */
type Level = 'debug' | 'info' | 'warn' | 'error';
type Fields = Record<string, unknown>;

const SERVICE = 'your_repo_name-frontend';

const emit = (level: Level, message: string, fields: Fields = {}) => {
    const entry = {
        '@timestamp': new Date().toISOString(),
        'log.level': level,
        'service.name': SERVICE,
        'event.dataset': 'browser',
        'url.path': window.location.pathname,
        message,
        ...fields,
    };
    // eslint-disable-next-line no-console
    (level === 'debug' ? console.debug : console[level])(JSON.stringify(entry));
};

export const logger = {
    debug: (message: string, fields?: Fields) => emit('debug', message, fields),
    info: (message: string, fields?: Fields) => emit('info', message, fields),
    warn: (message: string, fields?: Fields) => emit('warn', message, fields),
    error: (message: string, error?: unknown, fields?: Fields) => emit('error', message, {
        ...fields,
        'error.message': error instanceof Error ? error.message : error === undefined ? undefined : String(error),
        'error.stack_trace': error instanceof Error ? error.stack : undefined,
    }),
};
