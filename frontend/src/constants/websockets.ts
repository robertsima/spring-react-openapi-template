const WS_TOPIC_PREFIX = '/topic';

export const WS_TOPIC_MY_EVENTS = `${WS_TOPIC_PREFIX}/my_events`;

// react-stomp-hooks uses SockJS when the URL is http(s), so build an absolute URL on the current origin
export const websocketUrl = (websocketPath: string) => `${window.location.origin}${websocketPath}`;
