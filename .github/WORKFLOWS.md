# GitHub Actions CI/CD Documentation

This document describes the CI/CD pipelines configured for the Kafka microservices project.

## Overview

The project includes three main workflow files:

1. **CI - Pull Request** (`ci-pr.yml`) - Validation for pull requests
2. **CD - Main** (`cd-main.yml`) - Deployment pipeline for main branch
3. **Dependabot** (`dependabot.yml`) - Automated dependency updates

## Workflow Details

### 1. CI - Pull Request (`ci-pr.yml`)

**Triggers:**
- Pull requests to `main` or `develop` branches
- Changes to Java files, Maven POMs, service directories, or workflows

**Jobs:**

#### `build-and-test`
- **Purpose**: Build and test both services in parallel
- **Strategy**: Matrix build for `consumer-service` and `producer-service`
- **Steps**:
  1. Checkout code
  2. Setup JDK 21 with Maven cache
  3. Build service
  4. Run tests
  5. Package JAR
  6. Upload artifact (retained for 7 days)

#### `code-quality`
- **Purpose**: Run Maven verify for code quality checks
- **Steps**:
  1. Checkout code
  2. Setup JDK 21
  3. Run `mvn verify` for both services

#### `docker-build-test`
- **Purpose**: Verify Docker images can be built
- **Dependencies**: Requires `build-and-test` to complete
- **Strategy**: Matrix build for both services
- **Steps**:
  1. Setup Docker Buildx
  2. Build Docker image (no push)
  3. Use GitHub Actions cache for layers

#### `integration-test`
- **Purpose**: Run integration tests with actual Kafka
- **Dependencies**: Requires `build-and-test` to complete
- **Steps**:
  1. Start Kafka and Zookeeper via Docker Compose
  2. Wait for Kafka to be ready (60s timeout)
  3. Run integration tests
  4. Clean up containers

#### `pr-summary`
- **Purpose**: Provide final status of all checks
- **Dependencies**: Runs after all other jobs
- **Condition**: Always runs (even on failure)
- **Output**: Success or failure summary

**Expected Duration**: 5-8 minutes

**Cache Strategy**: Maven dependencies and Docker layers are cached

---

### 2. CD - Main Branch (`cd-main.yml`)

**Triggers:**
- Push to `main` branch
- Changes to Java files, Maven POMs, services, docker-compose, or workflows

**Environment Variables:**
```yaml
REGISTRY: ghcr.io
IMAGE_NAME: ${{ github.repository }}
```

**Jobs:**

#### `build`
- **Purpose**: Build and thoroughly test both services
- **Strategy**: Matrix build for both services
- **Steps**:
  1. Checkout code
  2. Setup JDK 21
  3. Run `mvn clean verify` (includes tests)
  4. Upload artifact (retained for 30 days)

#### `docker-build-push`
- **Purpose**: Build and publish Docker images
- **Dependencies**: Requires `build` to complete
- **Permissions**: `contents: read`, `packages: write`
- **Strategy**: Matrix build for both services
- **Steps**:
  1. Checkout code
  2. Setup Docker Buildx
  3. Login to GitHub Container Registry
  4. Extract metadata (tags and labels)
  5. Build and push multi-platform images
     - Platforms: `linux/amd64`, `linux/arm64`
  6. Use GitHub Actions cache

**Image Tags:**
- `latest` (for main branch)
- `main-<sha>` (specific commit)
- `main` (branch reference)

**Image Location:**
```
ghcr.io/<org>/<repo>/consumer-service:latest
ghcr.io/<org>/<repo>/producer-service:latest
```

#### `deploy`
- **Purpose**: Deploy to production environment
- **Dependencies**: Requires `docker-build-push` to complete
- **Environment**: `production`
- **Steps**:
  1. Checkout code
  2. Deploy to production (placeholder - customize for your infrastructure)
  3. Run health checks

**Note**: This job contains placeholder commands. Customize for your deployment method:
- SSH to production server
- Kubernetes deployment
- Cloud provider CLI (AWS ECS, Azure Container Apps, etc.)
- Docker Swarm
- etc.

#### `notify`
- **Purpose**: Send deployment notifications
- **Dependencies**: Runs after all jobs
- **Condition**: Always runs
- **Steps**:
  1. Check deployment status
  2. Send notification (Slack/Discord/Email - optional)

**Expected Duration**: 8-15 minutes

---

### 3. Dependabot (`dependabot.yml`)

**Purpose**: Automated dependency updates

**Update Schedule:**

| Ecosystem | Directory | Day | Time |
|-----------|-----------|-----|------|
| Maven (consumer) | `/consumer-service` | Monday | 09:00 |
| Maven (producer) | `/producer-service` | Monday | 09:00 |
| Docker (consumer) | `/consumer-service` | Tuesday | 09:00 |
| Docker (producer) | `/producer-service` | Tuesday | 09:00 |
| GitHub Actions | `/` | Wednesday | 09:00 |

**Configuration:**
- Open PRs limit: 10 for Maven, 5 for Docker/Actions
- Automatic labeling by service and ecosystem
- Commit prefix: `chore(deps)`, `chore(docker)`, `chore(actions)`
- Reviewers: `your-team` (configure this)

**Dependency Groups:**
- **Spring**: All `org.springframework*` packages (minor + patch)
- **Kafka**: Kafka and Spring Kafka packages
- **Testing**: JUnit, Mockito, AssertJ

**Ignored Updates:**
- Spring Boot major versions (requires manual review)

---

## Setup Instructions

### 1. Repository Settings

#### Enable GitHub Actions
1. Go to repository Settings → Actions → General
2. Ensure "Allow all actions and reusable workflows" is selected

#### Configure Secrets
Required secrets (some are automatic):
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions ✓

Optional secrets for notifications:
- `SLACK_WEBHOOK` - For Slack notifications
- `DISCORD_WEBHOOK` - For Discord notifications

#### Create Production Environment
1. Go to Settings → Environments
2. Click "New environment"
3. Name: `production`
4. Configure protection rules:
   - Required reviewers (optional)
   - Wait timer (optional)
   - Deployment branches: `main` only

### 2. Enable Dependabot

Dependabot configuration is already in `.github/dependabot.yml`.

To enable:
1. Go to Settings → Code security and analysis
2. Enable "Dependabot alerts"
3. Enable "Dependabot security updates"
4. Enable "Dependabot version updates"

Update reviewers in `dependabot.yml`:
```yaml
reviewers:
  - "your-team-name"  # Replace with your team or username
```

### 3. GitHub Container Registry Permissions

To publish Docker images:
1. Go to repository Settings → Actions → General
2. Under "Workflow permissions"
3. Select "Read and write permissions"
4. Check "Allow GitHub Actions to create and approve pull requests"

### 4. First Time Setup

#### Test CI/CD
1. Create a test branch: `git checkout -b test/ci-cd`
2. Make a small change
3. Push and create a PR
4. Verify all CI checks pass
5. Merge to main
6. Verify CD pipeline runs

#### Configure Deployment
Edit `.github/workflows/cd-main.yml` in the `deploy` job:

**Example for SSH deployment:**
```yaml
- name: Deploy to production
  run: |
    # Install SSH key
    mkdir -p ~/.ssh
    echo "${{ secrets.SSH_PRIVATE_KEY }}" > ~/.ssh/id_rsa
    chmod 600 ~/.ssh/id_rsa
    
    # Deploy
    ssh -o StrictHostKeyChecking=no user@server.com << 'EOF'
      cd /app/kafka-app
      docker-compose pull
      docker-compose up -d
      docker-compose ps
    EOF
```

**Example for Kubernetes:**
```yaml
- name: Deploy to Kubernetes
  run: |
    kubectl set image deployment/consumer-service \
      consumer-service=ghcr.io/${{ github.repository }}/consumer-service:${{ github.sha }}
    kubectl set image deployment/producer-service \
      producer-service=ghcr.io/${{ github.repository }}/producer-service:${{ github.sha }}
    kubectl rollout status deployment/consumer-service
    kubectl rollout status deployment/producer-service
```

---

## Monitoring Workflows

### View Workflow Runs
1. Go to Actions tab in GitHub repository
2. Select workflow from left sidebar
3. View individual run details

### Workflow Status Badges

Add to README.md:
```markdown
![CI](https://github.com/<org>/<repo>/workflows/CI%20-%20Pull%20Request/badge.svg)
![CD](https://github.com/<org>/<repo>/workflows/CD%20-%20Deploy%20to%20Production/badge.svg)
```

### Notifications

#### Slack Integration (Optional)
Add to workflow:
```yaml
- name: Slack notification
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    text: 'Deployment to production ${{ job.status }}'
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
  if: always()
```

#### Email Notifications
GitHub sends email notifications by default for:
- Workflow failures
- First workflow success after failure

Configure in Settings → Notifications

---

## Troubleshooting

### Build Failures

**Maven dependency issues:**
```bash
# Clear Maven cache
rm -rf ~/.m2/repository
```

**Docker build issues:**
```bash
# Clear Docker build cache
docker builder prune -a
```

### Permission Denied for GitHub Container Registry

1. Verify workflow permissions in repository settings
2. Check if packages are set to public or private
3. Ensure `GITHUB_TOKEN` has package write permissions

### Integration Tests Failing

1. Increase Kafka startup wait time in workflow
2. Check Docker Compose compatibility
3. Verify Kafka connection settings

### Dependabot PRs Not Created

1. Check Dependabot logs in Insights → Dependency graph → Dependabot
2. Verify `dependabot.yml` syntax
3. Ensure dependencies can actually be updated

---

## Best Practices

### Branch Protection Rules

Configure for `main` branch:
1. Require pull request reviews (1-2 reviewers)
2. Require status checks to pass:
   - `build-and-test`
   - `code-quality`
   - `docker-build-test`
   - `integration-test`
3. Require conversation resolution
4. Do not allow force pushes

### Workflow Optimization

1. **Cache Dependencies**: Already configured for Maven and Docker
2. **Matrix Builds**: Parallelize builds for both services
3. **Conditional Jobs**: Skip unnecessary jobs based on changed files
4. **Artifact Retention**: 7 days for PRs, 30 days for main branch

### Security

1. **Never commit secrets** - Use GitHub Secrets
2. **Limit token permissions** - Use minimum required permissions
3. **Pin action versions** - Use specific versions (e.g., `@v4` not `@latest`)
4. **Review Dependabot PRs** - Don't auto-merge without review

---

## Workflow Costs

GitHub Actions includes:
- **Free tier**: 2,000 minutes/month for private repos
- **Public repos**: Unlimited minutes

**Estimated usage:**
- PR workflow: ~6 minutes
- Main workflow: ~12 minutes
- Total per PR + merge: ~18 minutes

**Monthly estimate** (10 PRs):
- 180 minutes (9% of free tier)

---

## Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub Container Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
