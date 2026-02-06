# CI/CD Pipeline Architecture

## Pull Request Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                         PULL REQUEST CREATED                         │
│                     (to main or develop branch)                      │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │   Trigger CI Pipeline   │
                    │      (ci-pr.yml)        │
                    └────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
                    ▼                         ▼
        ┌──────────────────────┐   ┌──────────────────────┐
        │  build-and-test      │   │   code-quality       │
        │  (Matrix: 2 services)│   │                      │
        │  ├─ Consumer Service │   │  Maven Verify        │
        │  └─ Producer Service │   │  Both Services       │
        └──────────┬───────────┘   └──────────┬───────────┘
                   │                           │
                   └───────────┬───────────────┘
                               │
                ┌──────────────┴──────────────┐
                │                             │
                ▼                             ▼
    ┌──────────────────────┐      ┌──────────────────────┐
    │ docker-build-test    │      │  integration-test    │
    │ (Matrix: 2 services) │      │                      │
    │ ├─ Consumer Image    │      │  Start Kafka         │
    │ └─ Producer Image    │      │  Run Tests           │
    └──────────┬───────────┘      └──────────┬───────────┘
               │                              │
               └──────────────┬───────────────┘
                              │
                              ▼
                    ┌──────────────────┐
                    │   pr-summary     │
                    │  ✅ All Pass     │
                    │  ❌ Any Fail     │
                    └──────────────────┘
```

## Main Branch Deployment Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                      MERGE TO MAIN BRANCH                            │
│                  (Pull Request Approved & Merged)                    │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │   Trigger CD Pipeline   │
                    │      (cd-main.yml)      │
                    └────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │        BUILD           │
                    │  (Matrix: 2 services)  │
                    │  ├─ Consumer: mvn      │
                    │  │   clean verify      │
                    │  └─ Producer: mvn      │
                    │      clean verify      │
                    └──────────┬─────────────┘
                               │
                               ▼
                    ┌────────────────────────┐
                    │  docker-build-push     │
                    │  (Matrix: 2 services)  │
                    │                        │
                    │  Build Multi-Platform  │
                    │  ├─ linux/amd64       │
                    │  └─ linux/arm64       │
                    │                        │
                    │  Push to GHCR         │
                    │  ├─ latest tag        │
                    │  ├─ main-<sha>        │
                    │  └─ main tag          │
                    └──────────┬─────────────┘
                               │
                               ▼
                    ┌────────────────────────┐
                    │       DEPLOY           │
                    │                        │
                    │  Production Env        │
                    │  ├─ Pull Images       │
                    │  ├─ Update Services   │
                    │  └─ Health Checks     │
                    └──────────┬─────────────┘
                               │
                               ▼
                    ┌────────────────────────┐
                    │       NOTIFY           │
                    │                        │
                    │  ✅ Success → Slack    │
                    │  ❌ Failure → Alert    │
                    └────────────────────────┘
```

## Dependabot Weekly Cycle

```
┌─────────────────────────────────────────────────────────────────────┐
│                          WEEKLY SCHEDULE                             │
└─────────────────────────────────────────────────────────────────────┘

    Monday 09:00              Tuesday 09:00           Wednesday 09:00
         │                         │                        │
         ▼                         ▼                        ▼
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  Maven Updates  │      │  Docker Updates │      │ Actions Updates │
│                 │      │                 │      │                 │
│  Consumer POM   │      │  Consumer Image │      │  Workflow Files │
│  Producer POM   │      │  Producer Image │      │                 │
│                 │      │                 │      │                 │
│  Dependencies:  │      │  Base Images:   │      │  Actions:       │
│  ├─ Spring      │      │  ├─ eclipse-    │      │  ├─ checkout@v4 │
│  ├─ Kafka       │      │  │  temurin     │      │  ├─ setup-java  │
│  └─ Testing     │      │  └─ alpine      │      │  └─ build-push  │
└────────┬────────┘      └────────┬────────┘      └────────┬────────┘
         │                        │                         │
         ▼                        ▼                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    CREATE PULL REQUESTS                              │
│                                                                      │
│  ├─ Labeled by ecosystem (dependencies, docker, github-actions)    │
│  ├─ Grouped by related packages                                    │
│  ├─ Commit message: chore(deps|docker|actions): ...                │
│  └─ Assigned to reviewers                                          │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │   CI Pipeline Runs     │
                    │   (Automatic Testing)  │
                    └──────────┬─────────────┘
                               │
                               ▼
                    ┌────────────────────────┐
                    │   Manual Review        │
                    │   ├─ Check changelog  │
                    │   ├─ Review tests     │
                    │   └─ Approve/Reject   │
                    └──────────┬─────────────┘
                               │
                               ▼
                    ┌────────────────────────┐
                    │   Merge to Main        │
                    │   (Triggers CD)        │
                    └────────────────────────┘
```

## Overall Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                       DEVELOPMENT WORKFLOW                          │
└────────────────────────────────────────────────────────────────────┘

Developer                  GitHub                    Infrastructure
    │                        │                             │
    │  1. Create Branch      │                             │
    │─────────────────────>  │                             │
    │                        │                             │
    │  2. Commit & Push      │                             │
    │─────────────────────>  │                             │
    │                        │                             │
    │  3. Create PR          │                             │
    │─────────────────────>  │                             │
    │                        │                             │
    │                        │  4. Run CI Tests            │
    │                        │─────────────────────────┐   │
    │                        │                         │   │
    │                        │  <──────────────────────┘   │
    │                        │     (Build, Test, QA)       │
    │                        │                             │
    │  5. Review & Approve   │                             │
    │─────────────────────>  │                             │
    │                        │                             │
    │  6. Merge to Main      │                             │
    │─────────────────────>  │                             │
    │                        │                             │
    │                        │  7. Build & Push Images     │
    │                        │─────────────────────────>   │
    │                        │     (GHCR)                  │
    │                        │                             │
    │                        │  8. Deploy                  │
    │                        │─────────────────────────>   │
    │                        │     (Production)            │
    │                        │                             │
    │                        │  9. Health Check            │
    │                        │  <──────────────────────────│
    │                        │     (✅ Success)            │
    │                        │                             │
    │  10. Notify Success    │                             │
    │  <─────────────────────│                             │
    │                        │                             │
```

## GitHub Container Registry Structure

```
ghcr.io/<organization>/<repository>/
    │
    ├── consumer-service/
    │   ├── latest                  (main branch latest)
    │   ├── main                    (main branch)
    │   ├── main-abc1234            (specific commit)
    │   ├── main-def5678            (specific commit)
    │   └── ...
    │
    └── producer-service/
        ├── latest                  (main branch latest)
        ├── main                    (main branch)
        ├── main-abc1234            (specific commit)
        ├── main-def5678            (specific commit)
        └── ...

Each image:
  - Multi-platform: linux/amd64, linux/arm64
  - Cached layers for fast rebuilds
  - Automatic cleanup of old tags
```

## Cache Strategy

```
┌────────────────────────────────────────────────────────────────────┐
│                         GITHUB ACTIONS CACHE                        │
└────────────────────────────────────────────────────────────────────┘

Maven Cache (Per Service)
┌─────────────────────────┐
│  ~/.m2/repository       │
│                         │
│  Cached by:             │
│  ├─ pom.xml hash        │
│  └─ Java version        │
│                         │
│  Speeds up:             │
│  └─ Dependency download │
└─────────────────────────┘

Docker Build Cache
┌─────────────────────────┐
│  Build layers           │
│                         │
│  Cache from: type=gha   │
│  Cache to: type=gha     │
│                         │
│  Speeds up:             │
│  ├─ Base image pull     │
│  ├─ Layer builds        │
│  └─ Multi-stage builds  │
└─────────────────────────┘

Benefits:
  • CI builds: 10 min → 3 min
  • Docker builds: 8 min → 2 min
  • Total time savings: ~60%
```

## Testing Strategy

```
Unit Tests (Each Service)
    ├─ Service layer tests
    ├─ Controller tests
    └─ Configuration tests
        │
        ▼
Integration Tests
    ├─ Kafka producer/consumer
    ├─ Message serialization
    └─ End-to-end flow
        │
        ▼
Docker Build Tests
    ├─ Image builds successfully
    ├─ Correct base image
    └─ All dependencies present
        │
        ▼
Health Checks (Production)
    ├─ Service health endpoints
    ├─ Kafka connectivity
    └─ Actuator metrics
```

## Monitoring Points

```
┌────────────────────────────────────────────────────────────────────┐
│                          OBSERVABILITY                              │
└────────────────────────────────────────────────────────────────────┘

GitHub Actions
    ├─ Workflow run status
    ├─ Job duration metrics
    ├─ Test pass/fail rates
    └─ Artifact storage usage

Docker Images
    ├─ Image size trends
    ├─ Pull counts
    ├─ Vulnerability scans
    └─ Tag lifecycle

Services (Actuator)
    ├─ /actuator/health
    ├─ /actuator/metrics
    ├─ /actuator/prometheus
    └─ /actuator/info

Kafka
    ├─ Message throughput
    ├─ Consumer lag
    ├─ Error rates
    └─ Topic partition health
```
