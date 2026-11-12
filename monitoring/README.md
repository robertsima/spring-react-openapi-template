# Monitoring (placeholder)

| Source               | Endpoint                                            | How                                     |
| -------------------- | --------------------------------------------------- | --------------------------------------- |
| Backend (Spring)     | `spring-app-pod:8081/actuator/prometheus`           | Actuator + Micrometer Prometheus registry |
| Backend health       | `:8081/actuator/health/{liveness,readiness}`        | used by the pod probes                  |
| Frontend (nginx)     | `frontend-pod:9113/metrics`                         | `nginx-prometheus-exporter` sidecar reading `stub_status` |

The management port (8081) and nginx's `stub_status` listener are internal only. Neither is routed through
the public proxy.

## Run locally (same `app-net` network as the app pods)

```bash
podman run -d --name prometheus --network app-net -p 9090:9090 \
  -v ./monitoring/prometheus:/etc/prometheus:Z docker.io/prom/prometheus:latest

podman run -d --name grafana --network app-net -p 3001:3000 \
  -v ./monitoring/grafana/provisioning:/etc/grafana/provisioning:Z docker.io/grafana/grafana:latest
```

Prometheus runs at http://localhost:9090 (Status → Targets) and Grafana at http://localhost:3001 (admin/admin).

## To do
- [ ] Alertmanager + routing for `prometheus/rules/alerts.yml`
- [ ] Dashboards in `grafana/dashboards/`
- [ ] Custom business metrics: inject `MeterRegistry` and register counters/timers
- [ ] Frontend RUM / web-vitals if needed
